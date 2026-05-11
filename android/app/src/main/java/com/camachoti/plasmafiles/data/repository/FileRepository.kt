package com.camachoti.plasmafiles.data.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import com.camachoti.plasmafiles.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class FileRepository(private val context: Context) {

    suspend fun listFiles(path: String): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()
        dir.listFiles()
            ?.filter { !it.name.startsWith(".") }
            ?.map { it.toFileItem() }
            ?: emptyList()
    }

    fun getStorageVolumes(): List<StorageVolume> {
        val manager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return manager.storageVolumes.mapNotNull { vol ->
            val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                vol.directory?.absolutePath
            } else {
                Environment.getExternalStorageDirectory().absolutePath
            } ?: return@mapNotNull null

            val stat = try { StatFs(path) } catch (e: Exception) { return@mapNotNull null }
            val total = stat.blockCountLong * stat.blockSizeLong
            val free  = stat.availableBlocksLong * stat.blockSizeLong

            StorageVolume(
                name = if (vol.isRemovable) "SD card" else "Internal storage",
                path = path,
                totalBytes = total,
                usedBytes = total - free,
                isRemovable = vol.isRemovable,
            )
        }
    }

    suspend fun deleteFiles(files: List<File>): Boolean = withContext(Dispatchers.IO) {
        files.all { it.deleteRecursively() }
    }

    suspend fun copyFile(source: File, destDir: File): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val dest = File(destDir, source.name)
            source.copyRecursively(dest, overwrite = true)
        }.isSuccess
    }

    suspend fun moveFile(source: File, destDir: File): Boolean = withContext(Dispatchers.IO) {
        val dest = File(destDir, source.name)
        source.renameTo(dest)
    }

    suspend fun createFolder(parentPath: String, name: String): Boolean = withContext(Dispatchers.IO) {
        File(parentPath, name).mkdirs()
    }

    suspend fun renameFile(file: File, newName: String): Boolean = withContext(Dispatchers.IO) {
        File(file.parent, newName).let { file.renameTo(it) }
    }

    // ── Storage Analysis ──────────────────────────────────────────────────────

    suspend fun analyzeStorage(rootPath: String): AnalysisResult = withContext(Dispatchers.IO) {
        val allFiles   = mutableListOf<File>()
        val allFolders = mutableListOf<File>()
        walkRecursive(File(rootPath), allFiles, allFolders)

        val fileItems = allFiles.map { it.toFileItem() }

        val largeFiles = fileItems
            .filter { it.size > 10L * 1024 * 1024 }
            .sortedByDescending { it.size }
            .take(50)

        val bySize = fileItems.groupBy { it.size }.filter { it.value.size > 1 && it.key > 0 }
        val duplicateGroups = bySize.values.mapNotNull { group ->
            val byHash = group.groupBy { md5(it.file) }.filter { it.value.size > 1 && it.key.isNotEmpty() }
            byHash.values.map { DuplicateGroup(group.first().size, it) }.takeIf { it.isNotEmpty() }
        }.flatten().sortedByDescending { it.size }

        val emptyFolders = allFolders
            .filter { it.listFiles()?.isEmpty() == true }
            .map { it.toFileItem() }

        val typeStats = fileItems
            .groupBy { it.name.substringAfterLast('.', "").lowercase().ifEmpty { "—" } }
            .map { (ext, items) -> FileTypeStats(ext, items.size, items.sumOf { it.size }) }
            .sortedByDescending { it.totalSize }
            .take(30)

        AnalysisResult(
            duplicateGroups = duplicateGroups,
            largeFiles      = largeFiles,
            emptyFolders    = emptyFolders,
            fileTypeStats   = typeStats,
            totalScanned    = allFiles.size,
            totalSize       = fileItems.sumOf { it.size },
        )
    }

    private fun walkRecursive(dir: File, files: MutableList<File>, folders: MutableList<File>) {
        val children = try { dir.listFiles() } catch (e: Exception) { null } ?: return
        for (child in children) {
            if (child.name.startsWith(".")) continue
            if (child.isDirectory) {
                folders.add(child)
                walkRecursive(child, files, folders)
            } else {
                files.add(child)
            }
        }
    }

    private fun md5(file: File): String = try {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().buffered(65536).use { stream ->
            val buf = ByteArray(65536)
            var n: Int
            while (stream.read(buf).also { n = it } != -1) md.update(buf, 0, n)
        }
        md.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) { "" }
}
