package com.camachoti.plasmafiles.data.model

data class DuplicateGroup(
    val size: Long,
    val files: List<FileItem>,
)

data class FileTypeStats(
    val extension: String,
    val count: Int,
    val totalSize: Long,
)

data class AnalysisResult(
    val duplicateGroups: List<DuplicateGroup> = emptyList(),
    val largeFiles: List<FileItem> = emptyList(),
    val emptyFolders: List<FileItem> = emptyList(),
    val fileTypeStats: List<FileTypeStats> = emptyList(),
    val totalScanned: Int = 0,
    val totalSize: Long = 0L,
)

enum class AnalyzerTab { DUPLICATES, LARGE_FILES, EMPTY_FOLDERS, FILE_TYPES }
