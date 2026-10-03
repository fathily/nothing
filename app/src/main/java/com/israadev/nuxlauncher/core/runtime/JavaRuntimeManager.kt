package com.israadev.nuxlauncher.core.runtime

import android.content.Context
import android.os.Build
import android.system.Os
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object JavaRuntimeManager {

    fun getDeviceArch(): String {
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        return when {
            abi.contains("arm64") -> "arm64"
            abi.contains("armeabi") || abi.contains("arm") -> "arm"
            abi.contains("x86_64") -> "x86_64"
            abi.contains("x86") -> "x86"
            else -> "arm64"
        }
    }

    fun getRuntimesDir(context: Context): File {
        val dir = File(context.filesDir, "runtimes")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getRuntimeHome(context: Context, runtimeName: String): File {
        return File(getRuntimesDir(context), runtimeName)
    }

    fun isRuntimeInstalled(context: Context, runtimeName: String): Boolean {
        val home = getRuntimeHome(context, runtimeName)
        if (!home.exists() || !home.isDirectory) return false

        val javaBin = File(home, "bin/java")
        if (!javaBin.exists()) return false
        if (!javaBin.canExecute()) javaBin.setExecutable(true, false)

        val jliFile = if (File(home, "lib/jli/libjli.so").exists()) File(home, "lib/jli/libjli.so") else File(home, "lib/libjli.so")
        if (!jliFile.exists()) return false

        val jvmFile = File(home, "lib/server/libjvm.so")
        val clientJvmFile = File(home, "lib/client/libjvm.so")
        if (!jvmFile.exists() && !clientJvmFile.exists()) return false

        val modulesFile = File(home, "lib/modules")
        val rtJar = File(home, "lib/rt.jar")
        if (!modulesFile.exists() && !rtJar.exists()) return false

        return true
    }

    /**
     * Resolves recommended OpenJDK runtime based on Mojang manifest metadata or Minecraft version prefix.
     * Matches NUX Launcher Windows logic identically.
     */
    fun getRecommendedRuntime(mcVersion: String, javaMajorVersion: Int? = null): String {
        if (javaMajorVersion != null) {
            return when {
                javaMajorVersion >= 25 -> "jre-25"
                javaMajorVersion >= 21 -> "jre-21"
                javaMajorVersion >= 17 -> "jre-17"
                else -> "jre-8"
            }
        }

        return when {
            mcVersion.startsWith("26.") -> "jre-25"
            mcVersion.startsWith("1.21") || mcVersion.startsWith("1.20.5") || mcVersion.startsWith("1.20.6") -> "jre-21"
            mcVersion.startsWith("1.20") || mcVersion.startsWith("1.19") || mcVersion.startsWith("1.18") || mcVersion.startsWith("1.17") -> "jre-17"
            else -> "jre-8"
        }
    }

    /**
     * Resolves the runtime for an instance using this precedence:
     * instance override -> global Settings runtime -> Minecraft recommendation.
     * "auto" means let NUX choose the recommended Java for the Minecraft version.
     */
    fun resolveRuntimeName(
        instanceRuntime: String?,
        globalRuntime: String?,
        mcVersion: String,
        javaMajorVersion: Int? = null
    ): String {
        val instance = instanceRuntime?.trim()?.lowercase().orEmpty()
        if (instance.isNotBlank() && instance != "auto") return instanceRuntime!!.trim()

        val global = globalRuntime?.trim()?.lowercase().orEmpty()
        if (global.isNotBlank() && global != "auto") return globalRuntime!!.trim()

        return getRecommendedRuntime(mcVersion, javaMajorVersion)
    }

    fun getRuntimeDisplayName(runtimeName: String): String {
        return when (runtimeName) {
            "jre-8" -> "Java 8 (Auto)"
            "jre-17" -> "Java 17 (Auto)"
            "jre-21" -> "Java 21 (Auto)"
            "jre-25" -> "Java 25 (Auto)"
            "temurin-8" -> "Java 8 Android ARM64"
            "temurin-17" -> "Java 17 Android ARM64"
            "temurin-21" -> "Java 21 Android ARM64"
            "temurin-25" -> "Java 25 Android ARM64"
            else -> runtimeName.uppercase()
        }
    }

    fun getJavaExecutable(context: Context, runtimeName: String): File {
        return File(getRuntimeHome(context, runtimeName), "bin/java")
    }

    fun hasRuntimeAssets(context: Context, runtimeName: String): Boolean {
        return runCatching {
            val files = context.assets.list("runtimes/$runtimeName") ?: return@runCatching false
            files.contains("universal.tar.xz")
        }.getOrDefault(false)
    }

    suspend fun extractRuntime(
        context: Context,
        runtimeName: String,
        onProgress: (String) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val assetPath = "runtimes/$runtimeName"

            // Never create a fake/incomplete runtime directory when the APK does not
            // actually contain the requested runtime. This is especially important for
            // optional Android ARM64 runtimes.
            if (!hasRuntimeAssets(context, runtimeName)) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Runtime $runtimeName belum tersedia di APK ini"
                    )
                )
            }

            val destDir = getRuntimeHome(context, runtimeName)
            if (isRuntimeInstalled(context, runtimeName)) {
                return@withContext Result.success(destDir)
            }

            if (destDir.exists()) {
                destDir.deleteRecursively()
            }
            destDir.mkdirs()

            val arch = getDeviceArch()

            // 1. Unpack the common/full runtime payload.
            onProgress("Mengekstrak Java Runtime ($runtimeName)...")
            context.assets.open("$assetPath/universal.tar.xz").use { input ->
                unpackTarXz(input, destDir)
            }

            // 2. Optional architecture overlay. The Android ARM64 builds produced by
            // FCL already contain the complete ARM64 JRE, so this overlay is optional.
            val archAsset = "$assetPath/bin-$arch.tar.xz"
            if (runCatching { context.assets.open(archAsset).close(); true }.getOrDefault(false)) {
                onProgress("Menerapkan komponen Java $arch...")
                context.assets.open(archAsset).use { input ->
                    unpackTarXz(input, destDir)
                }
            }

            // 3. Mark executables/readable files.
            val binDir = File(destDir, "bin")
            if (binDir.exists()) {
                binDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        file.setExecutable(true, false)
                        file.setReadable(true, false)
                    }
                }
            }

            val libDir = File(destDir, "lib")
            if (libDir.exists()) {
                libDir.walkTopDown().filter { it.isFile && it.extension == "so" }.forEach { so ->
                    so.setExecutable(true, false)
                    so.setReadable(true, false)
                }
            }

            val javaBin = File(destDir, "bin/java")
            val hasJava = javaBin.exists() && javaBin.isFile
            if (hasJava) {
                javaBin.setExecutable(true, false)
                javaBin.setReadable(true, false)
            }

            val hasJli = destDir.walkTopDown().any { it.isFile && it.name == "libjli.so" }
            val hasJvm = destDir.walkTopDown().any { it.isFile && it.name == "libjvm.so" }

            if (!hasJava || !hasJli || !hasJvm) {
                destDir.deleteRecursively()
                return@withContext Result.failure(
                    IllegalStateException(
                        "Incomplete $runtimeName runtime: java=$hasJava, libjli=$hasJli, libjvm=$hasJvm"
                    )
                )
            }

            Result.success(destDir)
        } catch (e: Exception) {
            runCatching {
                val failedDir = getRuntimeHome(context, runtimeName)
                if (failedDir.exists() && !isRuntimeInstalled(context, runtimeName)) {
                    failedDir.deleteRecursively()
                }
            }
            Result.failure(e)
        }
    }

    private fun unpackTarXz(inputStream: InputStream, destDir: File) {
        TarArchiveInputStream(XZCompressorInputStream(inputStream)).use { tarIn ->
            val buffer = ByteArray(32768)
            var entry = tarIn.nextEntry
            while (entry != null) {
                val targetFile = File(destDir, entry.name)

                if (entry.isSymbolicLink) {
                    try {
                        targetFile.delete()
                        targetFile.parentFile?.mkdirs()
                        Os.symlink(entry.linkName, targetFile.absolutePath)
                    } catch (e: Throwable) {
                        throw IllegalStateException("Failed to create JRE symlink ${entry.name} -> ${entry.linkName}", e)
                    }
                } else if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    targetFile.parentFile?.mkdirs()
                    FileOutputStream(targetFile).use { out ->
                        var len: Int
                        while (tarIn.read(buffer).also { len = it } != -1) {
                            out.write(buffer, 0, len)
                        }
                    }
                }
                entry = tarIn.nextEntry
            }
        }
    }
}
