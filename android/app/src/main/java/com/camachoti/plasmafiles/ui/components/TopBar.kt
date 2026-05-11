package com.camachoti.plasmafiles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camachoti.plasmafiles.ui.theme.PlasmaTheme
import com.camachoti.plasmafiles.ui.viewmodel.Tab

@Composable
fun StatusBar(theme: PlasmaTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("9:30", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = theme.text)
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(18.dp)
                .background(Color(0xFF2E2E2E), CircleShape)
        )
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Default.Wifi, null, Modifier.size(14.dp), tint = theme.text)
            Icon(Icons.Default.SignalCellularAlt, null, Modifier.size(14.dp), tint = theme.text)
            Text("72%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.text)
            Icon(Icons.Default.BatteryFull, null, Modifier.size(18.dp), tint = theme.text)
        }
    }
}

@Composable
fun TabStrip(
    tabs: List<Tab>,
    activeTabId: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onNewTab: () -> Unit,
    titleFor: (String) -> String,
    theme: PlasmaTheme,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(theme.bg)
            .padding(start = 8.dp, top = 6.dp, end = 4.dp)
            .height(34.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        tabs.forEach { tab ->
            val active = tab.id == activeTabId
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(if (active) theme.panel else Color.Transparent)
                    .clickable { onSelectTab(tab.id) }
                    .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 7.dp)
                    .widthIn(min = 90.dp, max = 160.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FolderIcon(size = 14.dp, isDark = theme.isDark)
                Text(
                    text = titleFor(tab.path),
                    fontSize = 12.5.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (active) theme.text else theme.sub,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onCloseTab(tab.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(11.dp), tint = theme.sub)
                }
            }
        }
        IconButton(onClick = onNewTab, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Add, null, Modifier.size(18.dp), tint = theme.sub)
        }
    }
}

@Composable
fun PathBar(
    path: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    canGoUp: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onUp: () -> Unit,
    onSearch: () -> Unit,
    onMenu: () -> Unit,
    onNavigateTo: (String) -> Unit,
    theme: PlasmaTheme,
) {
    val segments = buildSegments(path)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.panel)
    ) {
        // Row 1: nav buttons + title + utilities
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavButton(Icons.Default.ArrowBack, onBack, canGoBack, theme)
            NavButton(Icons.Default.ArrowForward, onForward, canGoForward, theme)
            NavButton(Icons.Default.ArrowUpward, onUp, canGoUp, theme)
            Spacer(Modifier.weight(1f))
            Text(
                text = segments.lastOrNull()?.label ?: "",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = theme.sub,
            )
            Spacer(Modifier.weight(1f))
            NavButton(Icons.Default.Search, onSearch, true, theme)
            NavButton(Icons.Default.Menu, onMenu, true, theme)
        }
        // Row 2: breadcrumb
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
                .height(30.dp)
                .background(theme.sunken, RoundedCornerShape(7.dp))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            segments.forEachIndexed { i, seg ->
                Text(
                    text = seg.label,
                    fontSize = 13.sp,
                    fontWeight = if (i == segments.lastIndex) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (i == segments.lastIndex) theme.text else theme.sub,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .clickable { onNavigateTo(seg.path) }
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                )
                if (i < segments.lastIndex) {
                    Icon(Icons.Default.ChevronRight, null, Modifier.size(12.dp), tint = theme.mute)
                }
            }
        }
    }
}

@Composable
private fun NavButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, enabled: Boolean, theme: PlasmaTheme) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(36.dp)) {
        Icon(icon, null, Modifier.size(18.dp), tint = if (enabled) theme.text else theme.mute)
    }
}

data class PathSegment(val label: String, val path: String)

fun buildSegments(path: String): List<PathSegment> {
    if (path.isEmpty()) return listOf(PathSegment("Home", "/"))
    val parts = path.split("/").filter { it.isNotEmpty() }
    return listOf(PathSegment("Home", "/")) + parts.mapIndexed { i, part ->
        val displayName = when (part) {
            "storage", "0" -> "Internal storage"
            "sdcard"        -> "SD card"
            else            -> part
        }
        PathSegment(displayName, "/" + parts.take(i + 1).joinToString("/"))
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit, theme: PlasmaTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.panel)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.Search, null, Modifier.size(16.dp), tint = theme.sub)
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search this folder…", color = theme.mute, fontSize = 14.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = theme.text,
                unfocusedTextColor = theme.text,
            ),
            singleLine = true,
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = theme.sub)
            }
        }
    }
}
