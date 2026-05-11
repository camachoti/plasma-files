package com.camachoti.plasmafiles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.camachoti.plasmafiles.data.model.StorageVolume
import com.camachoti.plasmafiles.data.model.formatSize
import com.camachoti.plasmafiles.ui.theme.PlasmaColors
import com.camachoti.plasmafiles.ui.theme.PlasmaTheme

data class DrawerLocation(
    val name: String,
    val path: String,
    val kind: LocationKind,
    val meta: String? = null,
)

enum class LocationKind { HOME, RECENT, STARRED, DOWNLOAD, FOLDER, VOLUME }

@Composable
fun SideDrawer(
    open: Boolean,
    currentPath: String,
    volumes: List<StorageVolume>,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    theme: PlasmaTheme,
) {
    if (!open) return

    Box(Modifier.fillMaxSize()) {
        // Scrim
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { onClose() }
        )
        // Drawer panel
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(268.dp)
                .background(theme.panel)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Folder, null, Modifier.size(18.dp), tint = Color.White)
                }
                Column {
                    Text("Plasma Files", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = theme.text)
                    val usedVol = volumes.firstOrNull { !it.isRemovable }
                    if (usedVol != null) {
                        Text(
                            "${usedVol.usedBytes.formatSize()} used of ${usedVol.totalBytes.formatSize()}",
                            fontSize = 11.sp, color = theme.sub,
                        )
                    }
                }
            }

            // Storage bar
            val internalVol = volumes.firstOrNull { !it.isRemovable }
            if (internalVol != null) {
                StorageBar(internalVol, theme)
            }

            Divider(color = theme.line)

            // Quick access
            DrawerGroup(
                label = "Quick access",
                items = listOf(
                    DrawerLocation("Home",      "/",    LocationKind.HOME),
                    DrawerLocation("Downloads", getDownloadsPath(), LocationKind.DOWNLOAD),
                ),
                currentPath = currentPath,
                onNavigate = { onNavigate(it); onClose() },
                theme = theme,
            )

            // This phone
            DrawerGroup(
                label = "This phone",
                items = volumes.filter { !it.isRemovable }.map { vol ->
                    listOf(
                        DrawerLocation("Documents", vol.path + "/Documents", LocationKind.FOLDER),
                        DrawerLocation("Pictures",  vol.path + "/Pictures",  LocationKind.FOLDER),
                        DrawerLocation("Music",     vol.path + "/Music",     LocationKind.FOLDER),
                        DrawerLocation("Movies",    vol.path + "/Movies",    LocationKind.FOLDER),
                    )
                }.flatten(),
                currentPath = currentPath,
                onNavigate = { onNavigate(it); onClose() },
                theme = theme,
            )

            // Devices
            DrawerGroup(
                label = "Devices",
                items = volumes.map { vol ->
                    DrawerLocation(
                        name = vol.name,
                        path = vol.path,
                        kind = LocationKind.VOLUME,
                        meta = "${vol.usedBytes.formatSize()} / ${vol.totalBytes.formatSize()}",
                    )
                },
                currentPath = currentPath,
                onNavigate = { onNavigate(it); onClose() },
                theme = theme,
            )

            Divider(color = theme.line, modifier = Modifier.padding(horizontal = 12.dp))

            for (label in listOf("Trash", "Cloud accounts", "Settings")) {
                Text(
                    label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClose() }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    fontSize = 13.sp, color = theme.text,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerGroup(
    label: String,
    items: List<DrawerLocation>,
    currentPath: String,
    onNavigate: (String) -> Unit,
    theme: PlasmaTheme,
) {
    Text(
        label.uppercase(),
        modifier = Modifier.padding(start = 18.dp, top = 14.dp, bottom = 4.dp),
        fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
        color = theme.mute, letterSpacing = 0.6.sp,
    )
    items.forEach { loc ->
        val active = currentPath == loc.path
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (active) theme.accentSoft else Color.Transparent)
                .clickable { onNavigate(loc.path) }
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val iconTint = if (active) theme.accent else theme.sub
            Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                when (loc.kind) {
                    LocationKind.VOLUME   -> Icon(Icons.Default.Storage, null, Modifier.size(18.dp), tint = iconTint)
                    LocationKind.STARRED  -> Icon(Icons.Default.Star,    null, Modifier.size(18.dp), tint = iconTint)
                    LocationKind.RECENT   -> Icon(Icons.Default.History,  null, Modifier.size(18.dp), tint = iconTint)
                    LocationKind.DOWNLOAD -> Icon(Icons.Default.Download, null, Modifier.size(18.dp), tint = iconTint)
                    else -> FolderIcon(size = 20.dp, isDark = theme.isDark)
                }
            }
            Text(
                loc.name,
                modifier = Modifier.weight(1f),
                fontSize = 13.5.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                color = if (active) theme.accent else theme.text,
                maxLines = 1,
            )
            if (loc.meta != null) {
                Text(loc.meta, fontSize = 11.sp, color = theme.sub, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun StorageBar(vol: StorageVolume, theme: PlasmaTheme) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(theme.sunken)
        ) {
            val fraction = (vol.usedBytes.toFloat() / vol.totalBytes.coerceAtLeast(1)).coerceIn(0f, 1f)
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(theme.accent)
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for ((color, label) in listOf(
                PlasmaColors.KindImage to "Photos",
                PlasmaColors.KindVideo to "Video",
                PlasmaColors.KindAudio to "Audio",
                PlasmaColors.KindDoc   to "Docs",
            )) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
                    Text(label, fontSize = 10.5.sp, color = theme.sub)
                }
            }
        }
    }
}

private fun getDownloadsPath(): String =
    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).absolutePath
