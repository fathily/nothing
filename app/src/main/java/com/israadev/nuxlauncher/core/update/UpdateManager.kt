package com.israadev.nuxlauncher.core.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.google.gson.JsonParser
import com.israadev.nuxlauncher.core.network.NuxConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class AndroidUpdateInfo(
    val code: Int,
    val version: String,
    val localVersion: String,
    val localCode: Long,
    val createdAt: String,
    val downloadUrl: String,
    val fileName: String,
    val fileSize: Long,
    val changelog: String,
    val isUpdateAvailable: Boolean
)

object UpdateManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(context: Context): Result<AndroidUpdateInfo> = withContext(Dispatchers.IO) {
        val endpoints = buildList {
            add(NuxConfig.UPDATE_ENDPOINT)
            if (NuxConfig.isConfigured && NuxConfig.UPDATE_ENDPOINT != NuxConfig.OFFICIAL_UPDATE_ENDPOINT) {
                add(NuxConfig.OFFICIAL_UPDATE_ENDPOINT)
            }
        }.filter { it.isNotBlank() }.distinct()

        if (endpoints.isEmpty()) {
            return@withContext Result.failure(Exception("Server pembaruan NUX belum tersedia."))
        }

        var lastError: Exception? = null

        for (endpoint in endpoints) {
            try {
                val request = Request.Builder()
                    .url(endpoint)
                    .header("User-Agent", "NuxLauncher-Android/1.0.5")
                    .header("Accept", "application/json")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        lastError = Exception("Server HTTP " + response.code)
                        return@use
                    }

                    val root = JsonParser.parseString(response.body.string()).asJsonObject
                    val isGithubRelease = root.has("tag_name") && root.has("assets")

                    val remoteVersion = if (isGithubRelease) {
                        root.get("tag_name")?.asString?.trim()?.removePrefix("v") ?: "1.0.0"
                    } else {
                        root.get("version")?.takeUnless { it.isJsonNull }?.asString?.trim()?.removePrefix("v")
                            ?: "1.0.0"
                    }

                    val remoteCode = if (!isGithubRelease) {
                        root.get("code")?.takeUnless { it.isJsonNull }?.asInt ?: 1
                    } else {
                        0
                    }

                    val createdAt = if (isGithubRelease) {
                        root.get("published_at")?.asString ?: ""
                    } else {
                        root.get("created_at")?.takeUnless { it.isJsonNull }?.asString ?: ""
                    }

                    var downloadUrl = NuxConfig.getDownloadUrl("uploads/installers/NuxLauncher.apk")
                    var fileName = "NuxLauncher-Android.apk"
                    var fileSize = 0L

                    if (isGithubRelease) {
                        val assets = root.getAsJsonArray("assets")
                        for (element in assets) {
                            val asset = element.asJsonObject
                            val name = asset.get("name")?.asString ?: continue
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                downloadUrl = asset.get("browser_download_url")?.asString ?: downloadUrl
                                fileName = name
                                fileSize = asset.get("size")?.asLong ?: 0L
                                break
                            }
                        }
                    } else {
                        val files = root.get("files")
                        if (files != null && files.isJsonArray && files.asJsonArray.size() > 0) {
                            val file = files.asJsonArray[0].asJsonObject
                            file.get("uri")?.takeUnless { it.isJsonNull }?.asString?.let {
                                downloadUrl = NuxConfig.getDownloadUrl(it)
                            }
                            file.get("file_name")?.takeUnless { it.isJsonNull }?.asString?.let {
                                fileName = it
                            }
                            file.get("size")?.takeUnless { it.isJsonNull }?.asLong?.let {
                                fileSize = it
                            }
                        }
                    }

                    var changelog = if (isGithubRelease) {
                        root.get("body")?.asString ?: ""
                    } else {
                        ""
                    }

                    if (changelog.isBlank()) {
                        root.get("default_body")?.takeIf { it.isJsonObject }?.asJsonObject?.get("markdown")
                            ?.takeUnless { it.isJsonNull }?.asString?.let { changelog = it }

                        if (changelog.isBlank()) {
                            root.get("bodies")?.takeIf { it.isJsonArray }?.asJsonArray?.let { bodies ->
                                if (bodies.size() > 0) {
                                    bodies[0].asJsonObject.get("markdown")
                                        ?.takeUnless { it.isJsonNull }?.asString?.let { changelog = it }
                                }
                            }
                        }
                    }

                    if (changelog.isBlank()) {
                        changelog = "• Pembaruan NUX Launcher tersedia.\n• Peningkatan performa dan stabilitas."
                    }

                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }

                    val localCode = PackageInfoCompat.getLongVersionCode(packageInfo)
                    val localVersion = (packageInfo.versionName ?: "1.0.5").trim().removePrefix("v")

                    val newerCode = remoteCode > 0 && remoteCode > localCode
                    val newerVersion = compareVersions(remoteVersion, localVersion) > 0

                    return@withContext Result.success(
                        AndroidUpdateInfo(
                            code = remoteCode,
                            version = remoteVersion,
                            localVersion = localVersion,
                            localCode = localCode,
                            createdAt = createdAt,
                            downloadUrl = downloadUrl,
                            fileName = fileName,
                            fileSize = fileSize,
                            changelog = changelog,
                            isUpdateAvailable = newerCode || newerVersion
                        )
                    )
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Gagal memeriksa pembaruan NUX."))
    }

    private fun compareVersions(first: String, second: String): Int {
        val a = first.removePrefix("v").split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val b = second.removePrefix("v").split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val max = maxOf(a.size, b.size)

        for (i in 0 until max) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    fun openDownloadUrl(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
