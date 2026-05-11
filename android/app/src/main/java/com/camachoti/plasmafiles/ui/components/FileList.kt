package com.camachoti.plasmafiles.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camachoti.plasmafiles.data.model.FileItem
import com.camachoti.plasmafiles.data.model.FileKind
import com.camachoti.plasmafiles.data.model.formatDate
import com.camachoti.plasmafiles.data.model.formatSize
import com.camachoti.plasmafiles.ui.theme.PlasmaTheme
import com.camachoti.plasmafiles.ui.viewmodel.Density
import com.camachoti.plasmafiles.ui.viewmodel.SortDir
import com.camachoti.plasmafiles.ui.viewmodel.SortKey

@Composable
fun ColumnHeader(
    sortKey: SortKey,
    sortDir: SortDir,
    onSort: (SortKey) -> Unit,
    density: Density,
    selectedCount: Int,
    totalCount: Int,
    onToggleAll: () -> Unit,
    theme: PlasmaTheme,
) {
    val h = when (density) { Density.COMPACT -> 26.dp; Density.COZY -> 28.dp; else -> 32.dp }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(h)
            .background(theme.bg)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CheckBox(
            checked = selectedCount == totalCount && totalCount > 0,
            indeterminate = selectedCount > 0 && selectedCount < totalCount,
            onToggle = onToggleAll,
            theme = theme,
        )
        Spacer(Modifier.width(28.dp))
        SortHeaderCell("Name", SortKey.NAME, sortKey, sortDir, onSort, theme, Modifier.weight(1f))
        SortHeaderCell("Date modified", SortKey.MODIFIED, sortKey, sortDir, onSort, theme, Modifier.width(110.dp))
        SortHeaderCell("Size", SortKey.SIZE, sortKey, sortDir, onSort, theme, Modifier.width(60.dp), rightAlign = true)
    }
}

@Composable
private fun SortHeaderCell(
    label: String,
    key: SortKey,
    current: SortKey,
    dir: SortDir,
    onSort: (SortKey) -> Unit,
    theme: PlasmaTheme,
    modifier: Modifier,
    rightAlign: Boolean = false,
) {
    Row(
        modifier = modifier.clickableNoRipple { onSort(key) },
        horizontalArrangement = if (rightAlign) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.sub,
            letterSpacing = 0.4.sp,
        )
        if (current == key) {
            Icon(
                if (dir == SortDir.ASC) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, Modifier.size(12.dp), tint = theme.sub,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileDetailsView(
    items: List<FileItem>,
    selectedItems: Set<String>,
    anySelected: Boolean,
    density: Density,
    sortKey: SortKey,
    sortDir: SortDir,
    onSort: (SortKey) -> Unit,
    onOpen: (FileItem) -> Unit,
    onToggleSelect: (FileItem) -> Unit,
    onToggleAll: () -> Unit,
    theme: PlasmaTheme,
    emptyLabel: String = "This folder is empty",
) {
    LazyColumn(Modifier.fillMaxSize()) {
        stickyHeader {
            ColumnHeader(
                sortKey = sortKey, sortDir = sortDir, onSort = onSort,
                density = density,
                selectedCount = selectedItems.size, totalCount = items.size,
                onToggleAll = onToggleAll, theme = theme,
            )
            Divider(color = theme.line, thickness = 1.dp)
        }
        if (items.isEmpty()) {
            item { EmptyState(theme, emptyLabel) }
        } else {
            items(items, key = { it.file.absolutePath }) { item ->
                ListRow(
                    item = item,
                    selected = item.name in selectedItems,
                    anySelected = anySelected,
                    density = density,
                    theme = theme,
                    onOpen = { onOpen(item) },
                    onToggle = { onToggleSelect(item) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListRow(
    item: FileItem,
    selected: Boolean,
    anySelected: Boolean,
    density: Density,
    theme: PlasmaTheme,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    val h = when (density) { Density.COMPACT -> 36.dp; Density.COZY -> 44.dp; else -> 52.dp }
    val iconSz = when (density) { Density.COMPACT -> 22.dp; Density.COZY -> 26.dp; else -> 30.dp }
    val fs = if (density == Density.COMPACT) 13.sp else 14.sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(h)
            .background(if (selected) theme.selected else Color.Transparent)
            .run {
                if (selected) border(start = 2.5.dp, color = theme.accent)
                else border(start = 2.5.dp, color = Color.Transparent)
            }
            .combinedClickable(
                onClick = { if (anySelected) onToggle() else onOpen() },
                onLongClick = { onToggle() },
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.width(22.dp), contentAlignment = Alignment.Center) {
            if (anySelected) CheckBox(selected, false, onToggle, theme)
        }
        Box(Modifier.width(28.dp), contentAlignment = Alignment.Center) {
            FileIcon(item, iconSz, theme)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(item.name, fontSize = fs, fontWeight = FontWeight.Medium, color = theme.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (density != Density.COMPACT) {
                val sub = when (item.kind) {
                    FileKind.FOLDER -> "${item.childCount} items"
                    else -> if (item.extension.isNotEmpty()) item.extension.uppercase() + " file" else ""
                }
                Text(sub, fontSize = 11.5.sp, color = theme.sub)
            }
        }
        Text(
            item.lastModified.formatDate(),
            modifier = Modifier.width(110.dp),
            fontSize = 12.sp, color = theme.sub, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Text(
            if (item.kind == FileKind.FOLDER) "—" else item.size.formatSize(),
            modifier = Modifier.width(60.dp),
            fontSize = 12.sp, color = theme.sub,
            fontFamily = FontFamily.Monospace,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
    Divider(color = theme.softLine, thickness = 1.dp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTilesView(
    items: List<FileItem>,
    selectedItems: Set<String>,
    anySelected: Boolean,
    onOpen: (FileItem) -> Unit,
    onToggleSelect: (FileItem) -> Unit,
    theme: PlasmaTheme,
    emptyLabel: String = "This folder is empty",
) {
    if (items.isEmpty()) {
        EmptyState(theme, emptyLabel)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.file.absolutePath }) { item ->
            GridTile(
                item = item,
                selected = item.name in selectedItems,
                anySelected = anySelected,
                theme = theme,
                onOpen = { onOpen(item) },
                onToggle = { onToggleSelect(item) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridTile(
    item: FileItem,
    selected: Boolean,
    anySelected: Boolean,
    theme: PlasmaTheme,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) theme.selected else theme.panel)
            .border(1.dp, if (selected) theme.accent else theme.softLine, RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = { if (anySelected) onToggle() else onOpen() },
                onLongClick = { onToggle() },
            )
            .padding(vertical = 10.dp, horizontal = 6.dp),
    ) {
        if (anySelected) {
            CheckBox(selected, false, onToggle, theme, Modifier.align(Alignment.TopStart).size(16.dp))
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FileIcon(item, 42.dp, theme)
            Text(
                item.name,
                fontSize = 12.sp, fontWeight = FontWeight.Medium, color = theme.text,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                if (item.kind == FileKind.FOLDER) "${item.childCount} items" else item.size.formatSize(),
                fontSize = 10.5.sp, color = theme.sub,
            )
        }
    }
}

@Composable
fun CheckBox(
    checked: Boolean,
    indeterminate: Boolean,
    onToggle: () -> Unit,
    theme: PlasmaTheme,
    modifier: Modifier = Modifier.size(18.dp),
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (checked) theme.accent else Color.Transparent)
            .border(1.5.dp, if (checked) theme.accent else theme.mute, RoundedCornerShape(4.dp))
            .clickableNoRipple(onToggle),
        contentAlignment = Alignment.Center,
    ) {
        when {
            checked && !indeterminate -> Icon(Icons.Default.Check, null, Modifier.size(10.dp), tint = Color.White)
            indeterminate -> Box(Modifier.width(8.dp).height(2.dp).background(Color.White))
        }
    }
}

@Composable
fun EmptyState(theme: PlasmaTheme, label: String = "This folder is empty") {
    Column(
        modifier = Modifier.fillMaxWidth().padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FolderIcon(size = 56.dp, isDark = theme.isDark)
        Text(label, fontSize = 13.sp, color = theme.sub)
    }
}

@Composable
fun StatusFooter(count: Int, selectedCount: Int, freeText: String, theme: PlasmaTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(theme.panel)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$count items" + if (selectedCount > 0) " · $selectedCount selected" else "",
            fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = theme.sub,
        )
        Spacer(Modifier.weight(1f))
        Text(freeText, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = theme.sub)
    }
}

private fun Modifier.border(start: androidx.compose.ui.unit.Dp, color: Color): Modifier =
    this.padding(start = start).background(color)
