package com.camachoti.plasmafiles.data.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import com.camachoti.plasmafiles.data.model.FileItem
import com.camachoti.plasmafiles.data.model.FileKind
import com.camachoti.plasmafiles.data.model.StorageVolume
import com.camachoti.plasmafiles.data.model.toFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

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
}
