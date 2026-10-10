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
    val isUpdateAvailable: Boolean,
    val forkVersion: String = "",
    val forkReady: Boolean = false
)

object UpdateManager {
    const val FORK_REPOSITORY_URL = "https://github.com/fathily/nothing"
    const val FORK_RELEASES_URL = "https://github.com/fathily/nothing/releases/latest"
    private const val FORK_RELEASE_ENDPOINT = "https://api.github.com/repos/fathily/nothing/releases/latest"

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(context: Context): Result<AndroidUpdateInfo> = withContext(Dispatchers.IO) {
        // This is a forked launcher: only its own GitHub Releases can determine
        // whether an APK update exists. The upstream NUX server may be ahead and
        // must never trigger an update prompt for an APK that the fork has not built.
        try {
            val request = Request.Builder()
                .url(FORK_RELEASE_ENDPOINT)
                .header("User-Agent", "NUX-Launcher-Fork")
                .header("Accept", "application/vnd.github+json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Gagal memeriksa GitHub Releases (HTTP ${response.code}). Coba lagi nanti.")
                    )
                }

                val root = JsonParser.parseString(response.body.string()).asJsonObject
                val remoteVersion = root.get("tag_name")?.takeUnless { it.isJsonNull }?.asString
                    ?.trim()?.removePrefix("v")
                    ?.takeIf { it.isNotBlank() }
                    ?: return@withContext Result.failure(Exception("Tag versi GitHub tidak ditemukan."))

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
                val localVersion = (packageInfo.versionName ?: "1.0.0").trim().removePrefix("v")

                var downloadUrl = FORK_RELEASES_URL
                var fileName = "APK dari GitHub Releases"
                var fileSize = 0L
                val assets = root.get("assets")
                if (assets != null && assets.isJsonArray) {
                    for (element in assets.asJsonArray) {
                        if (!element.isJsonObject) continue
                        val asset = element.asJsonObject
                        val name = asset.get("name")?.takeUnless { it.isJsonNull }?.asString ?: continue
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.get("browser_download_url")
                                ?.takeUnless { it.isJsonNull }?.asString ?: FORK_RELEASES_URL
                            fileName = name
                            fileSize = asset.get("size")?.takeUnless { it.isJsonNull }?.asLong ?: 0L
                            break
                        }
                    }
                }

                var changelog = root.get("body")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
                if (changelog.isBlank()) {
                    changelog = "• Pembaruan NUX Launcher fork tersedia di GitHub Releases."
                }

                val updateAvailable = compareVersions(remoteVersion, localVersion) > 0
                Result.success(
                    AndroidUpdateInfo(
                        code = 0,
                        version = remoteVersion,
                        localVersion = localVersion,
                        localCode = localCode,
                        createdAt = root.get("published_at")?.takeUnless { it.isJsonNull }?.asString.orEmpty(),
                        downloadUrl = downloadUrl,
                        fileName = fileName,
                        fileSize = fileSize,
                        changelog = changelog,
                        isUpdateAvailable = updateAvailable,
                        forkVersion = remoteVersion,
                        forkReady = true
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memeriksa versi fork di GitHub: ${e.localizedMessage ?: "koneksi bermasalah"}", e))
        }
    }

    private fun fetchForkReleaseVersion(): String {
        return try {
            val request = Request.Builder()
                .url(FORK_RELEASE_ENDPOINT)
                .header("User-Agent", "NUX-Launcher-Fork")
                .header("Accept", "application/vnd.github+json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return ""
                val root = JsonParser.parseString(response.body.string()).asJsonObject
                root.get("tag_name")?.takeUnless { it.isJsonNull }?.asString
                    ?.trim()?.removePrefix("v") ?: ""
            }
        } catch (_: Exception) {
            ""
        }
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
