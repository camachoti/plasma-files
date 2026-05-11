package com.camachoti.plasmafiles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camachoti.plasmafiles.data.model.*
import com.camachoti.plasmafiles.ui.theme.PlasmaTheme

@Composable
fun StorageAnalyzerScreen(
    scanPath: String,
    result: AnalysisResult?,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onDeleteFiles: (List<FileItem>) -> Unit,
    onNavigateTo: (String) -> Unit,
    onBack: () -> Unit,
    theme: PlasmaTheme,
) {
    var activeTab by remember { mutableStateOf(AnalyzerTab.LARGE_FILES) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.panel)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, null, Modifier.size(20.dp), tint = theme.text)
            }
            Column(Modifier.weight(1f)) {
                Text("Storage Analyzer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.text)
                Text(scanPath, fontSize = 11.sp, color = theme.mute, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(
                onClick = onStartScan,
                enabled = !isScanning,
                colors  = ButtonDefaults.buttonColors(containerColor = theme.accent),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                if (isScanning) {
                    CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Scanning…", fontSize = 13.sp, color = Color.White)
                } else {
                    Text(if (result == null) "Scan" else "Re-scan", fontSize = 13.sp, color = Color.White)
                }
            }
        }

        // Summary strip
        result?.let { r ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.accent.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SummaryChip("${r.totalScanned} files", theme)
                SummaryChip(r.totalSize.formatSize(), theme)
                SummaryChip("${r.duplicateGroups.size} dup groups", theme)
                SummaryChip("${r.largeFiles.size} large", theme)
            }
        }

        // Tab strip
        AnalyzerTabStrip(activeTab) { activeTab = it }
        HorizontalDivider(color = theme.line, thickness = 1.dp)

        // Content
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (result != null) {
                Column(Modifier.fillMaxSize()) {
                    if (isScanning) {
                        LinearProgressIndicator(
                            modifier   = Modifier.fillMaxWidth(),
                            color      = theme.accent,
                            trackColor = theme.line,
                        )
                    }
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when (activeTab) {
                            AnalyzerTab.LARGE_FILES   -> LargeFilesTab(result.largeFiles, onDeleteFiles, onNavigateTo, theme)
                            AnalyzerTab.DUPLICATES    -> DuplicatesTab(result.duplicateGroups, onDeleteFiles, theme)
                            AnalyzerTab.EMPTY_FOLDERS -> EmptyFoldersTab(result.emptyFolders, onDeleteFiles, onNavigateTo, theme)
                            AnalyzerTab.FILE_TYPES    -> FileTypesTab(result.fileTypeStats, result.totalSize, theme)
                        }
                    }
                }
            } else if (isScanning) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(color = theme.accent)
                        Text("Scanning storage…", fontSize = 14.sp, color = theme.mute)
                    }
                }
            } else {
                EmptyAnalyzerState(theme, onStartScan)
            }
        }
    }
}

@Composable
private fun AnalyzerTabStrip(active: AnalyzerTab, onSelect: (AnalyzerTab) -> Unit) {
    val tabs = listOf(
        AnalyzerTab.LARGE_FILES   to "Large Files",
        AnalyzerTab.DUPLICATES    to "Duplicates",
        AnalyzerTab.EMPTY_FOLDERS to "Empty Folders",
        AnalyzerTab.FILE_TYPES    to "File Types",
    )
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == active }.coerceAtLeast(0),
        containerColor   = Color.Transparent,
        edgePadding      = 12.dp,
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = active == tab,
                onClick  = { onSelect(tab) },
                text     = { Text(label, fontSize = 13.sp) },
            )
        }
    }
}

@Composable
private fun SummaryChip(text: String, theme: PlasmaTheme) {
    Text(text, fontSize = 12.sp, color = theme.accent, fontWeight = FontWeight.Medium)
}

@Composable
private fun EmptyAnalyzerState(theme: PlasmaTheme, onScan: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Default.Search, null, Modifier.size(56.dp), tint = theme.mute)
            Text("Analyze your storage", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.text)
            Text("Find duplicates, large files, and more", fontSize = 13.sp, color = theme.mute)
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onScan,
                colors  = ButtonDefaults.buttonColors(containerColor = theme.accent),
            ) { Text("Start scan", color = Color.White) }
        }
    }
}

@Composable
private fun LargeFilesTab(
    files: List<FileItem>,
    onDelete: (List<FileItem>) -> Unit,
    onNavigateTo: (String) -> Unit,
    theme: PlasmaTheme,
) {
    if (files.isEmpty()) { EmptyTabMessage("No files larger than 10 MB found", theme); return }
    var selected by remember { mutableStateOf(setOf<String>()) }
    Column(Modifier.fillMaxSize()) {
        if (selected.isNotEmpty()) {
            BulkActionBar(
                count    = selected.size,
                theme    = theme,
                onDelete = { onDelete(files.filter { it.file.absolutePath in selected }); selected = emptySet() },
                onClear  = { selected = emptySet() },
            )
        }
        LazyColumn(Modifier.weight(1f)) {
            items(files, key = { it.file.absolutePath }) { item ->
                val path = item.file.absolutePath
                AnalyzerFileRow(
                    name       = item.name,
                    subtitle   = item.file.parent ?: "",
                    isSelected = path in selected,
                    badge      = item.size.formatSize(),
                    badgeWarn  = true,
                    onToggle   = { selected = if (path in selected) selected - path else selected + path },
                    onNavigate = { onNavigateTo(item.file.parent ?: "") },
                    theme      = theme,
                )
            }
        }
    }
}

@Composable
private fun DuplicatesTab(
    groups: List<DuplicateGroup>,
    onDelete: (List<FileItem>) -> Unit,
    theme: PlasmaTheme,
) {
    if (groups.isEmpty()) { EmptyTabMessage("No duplicate files found", theme); return }
    var selected by remember { mutableStateOf(setOf<String>()) }
    val allFiles = remember(groups) { groups.flatMap { it.files } }
    val wastedBytes = remember(groups) { groups.sumOf { g -> g.size * (g.files.size - 1) } }
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${groups.size} groups · ${allFiles.size} files", fontSize = 13.sp, color = theme.mute)
            Text("${wastedBytes.formatSize()} wasted", fontSize = 13.sp, color = theme.warn, fontWeight = FontWeight.SemiBold)
        }
        if (selected.isNotEmpty()) {
            BulkActionBar(
                count    = selected.size,
                theme    = theme,
                onDelete = { onDelete(allFiles.filter { it.file.absolutePath in selected }); selected = emptySet() },
                onClear  = { selected = emptySet() },
            )
        }
        LazyColumn(Modifier.weight(1f)) {
            groups.forEach { group ->
                item {
                    Text(
                        "${group.files.size} copies · ${group.size.formatSize()} each",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.panel)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize   = 11.sp,
                        color      = theme.mute,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(group.files, key = { it.file.absolutePath }) { item ->
                    val path = item.file.absolutePath
                    AnalyzerFileRow(
                        name       = item.name,
                        subtitle   = item.file.parent ?: "",
                        isSelected = path in selected,
                        badge      = null,
                        badgeWarn  = false,
                        onToggle   = { selected = if (path in selected) selected - path else selected + path },
                        onNavigate = null,
                        theme      = theme,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFoldersTab(
    folders: List<FileItem>,
    onDelete: (List<FileItem>) -> Unit,
    onNavigateTo: (String) -> Unit,
    theme: PlasmaTheme,
) {
    if (folders.isEmpty()) { EmptyTabMessage("No empty folders found", theme); return }
    var selected by remember { mutableStateOf(setOf<String>()) }
    Column(Modifier.fillMaxSize()) {
        if (selected.isNotEmpty()) {
            BulkActionBar(
                count    = selected.size,
                theme    = theme,
                onDelete = { onDelete(folders.filter { it.file.absolutePath in selected }); selected = emptySet() },
                onClear  = { selected = emptySet() },
            )
        }
        LazyColumn(Modifier.weight(1f)) {
            items(folders, key = { it.file.absolutePath }) { item ->
                val path = item.file.absolutePath
                AnalyzerFileRow(
                    name       = item.name,
                    subtitle   = item.file.parent ?: "",
                    isSelected = path in selected,
                    badge      = "empty",
                    badgeWarn  = false,
                    onToggle   = { selected = if (path in selected) selected - path else selected + path },
                    onNavigate = { onNavigateTo(item.file.absolutePath) },
                    theme      = theme,
                )
            }
        }
    }
}

@Composable
private fun FileTypesTab(stats: List<FileTypeStats>, totalSize: Long, theme: PlasmaTheme) {
    if (stats.isEmpty()) { EmptyTabMessage("No files found", theme); return }
    LazyColumn(Modifier.fillMaxSize()) {
        items(stats) { stat ->
            val ratio = if (totalSize > 0) stat.totalSize.toFloat() / totalSize else 0f
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        if (stat.extension == "—") stat.extension else ".${stat.extension}",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = theme.text,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${stat.count} files", fontSize = 12.sp, color = theme.mute)
                        Text(stat.totalSize.formatSize(), fontSize = 12.sp, color = theme.accent, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress   = { ratio },
                    modifier   = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color      = theme.accent,
                    trackColor = theme.line,
                )
            }
            HorizontalDivider(color = theme.line.copy(alpha = 0.5f), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun AnalyzerFileRow(
    name: String,
    subtitle: String,
    isSelected: Boolean,
    badge: String?,
    badgeWarn: Boolean,
    onToggle: () -> Unit,
    onNavigate: (() -> Unit)?,
    theme: PlasmaTheme,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) theme.accent.copy(alpha = 0.10f) else Color.Transparent)
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() }, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = theme.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 11.sp, color = theme.mute, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (badge != null) {
            val badgeColor = if (badgeWarn) theme.warn else theme.mute
            Text(
                badge,
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize   = 11.sp,
                color      = badgeColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (onNavigate != null) {
            IconButton(onClick = onNavigate, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp), tint = theme.mute)
            }
        }
    }
    HorizontalDivider(color = theme.line.copy(alpha = 0.5f), thickness = 0.5.dp)
}

@Composable
private fun BulkActionBar(
    count: Int,
    theme: PlasmaTheme,
    onDelete: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.accent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, null, Modifier.size(18.dp), tint = Color.White)
        }
        Text(
            "$count selected",
            modifier   = Modifier.weight(1f),
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color.White,
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = Color.White)
        }
    }
}

@Composable
private fun EmptyTabMessage(message: String, theme: PlasmaTheme) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, fontSize = 14.sp, color = theme.mute)
    }
}
