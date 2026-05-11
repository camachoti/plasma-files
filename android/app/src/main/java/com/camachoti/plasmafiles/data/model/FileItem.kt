package com.camachoti.plasmafiles.data.model

import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

enum class FileKind {
    FOLDER, VOLUME,
    PDF, DOC, SHEET, SLIDE,
    TEXT, IMAGE, VIDEO, AUDIO,
    ARCHIVE, APP, UNKNOWN
}

data class FileItem(
    val name: String,
    val file: File,
    val kind: FileKind,
    val extension: String = "",
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val childCount: Int = -1,
)

data class StorageVolume(
    val name: String,
    val path: String,
    val totalBytes: Long,
    val usedBytes: Long,
    val isRemovable: Boolean,
)

fun File.toFileItem(): FileItem {
    val ext = extension.lowercase(Locale.getDefault())
    val kind = when {
        isDirectory -> FileKind.FOLDER
        ext == "pdf" -> FileKind.PDF
        ext in listOf("doc", "docx") -> FileKind.DOC
        ext in listOf("xls", "xlsx", "csv") -> FileKind.SHEET
        ext in listOf("ppt", "pptx") -> FileKind.SLIDE
        ext in listOf("txt", "md", "log", "json", "xml") -> FileKind.TEXT
        ext in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "svg") -> FileKind.IMAGE
        ext in listOf("mp4", "mkv", "avi", "mov", "wmv", "webm") -> FileKind.VIDEO
        ext in listOf("mp3", "aac", "ogg", "wav", "flac", "m4a") -> FileKind.AUDIO
        ext in listOf("zip", "rar", "7z", "tar", "gz", "bz2") -> FileKind.ARCHIVE
        ext in listOf("apk", "aab") -> FileKind.APP
        else -> FileKind.UNKNOWN
    }
    val childCount = if (isDirectory) (listFiles()?.size ?: 0) else -1
    return FileItem(
        name = name,
        file = this,
        kind = kind,
        extension = ext,
        size = if (isFile) length() else 0L,
        lastModified = lastModified(),
        childCount = childCount,
    )
}

fun Long.formatSize(): String {
    if (this <= 0L) return ""
    val digitGroups = (log10(toDouble()) / log10(1024.0)).toInt().coerceIn(0, 4)
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    return DecimalFormat("#,##0.#").format(this / 1024.0.pow(digitGroups)) + " " + units[digitGroups]
}

fun Long.formatDate(): String {
    val sdf = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}
