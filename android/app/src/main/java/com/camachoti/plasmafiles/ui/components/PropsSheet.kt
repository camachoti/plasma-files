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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.camachoti.plasmafiles.data.model.FileItem
import com.camachoti.plasmafiles.data.model.FileKind
import com.camachoti.plasmafiles.data.model.formatDate
import com.camachoti.plasmafiles.data.model.formatSize
import com.camachoti.plasmafiles.ui.theme.PlasmaTheme

@Composable
fun PropsBottomSheet(
    item: FileItem,
    onClose: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    theme: PlasmaTheme,
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize().clickable { onClose() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(theme.panel)
                    .clickable { /* consume clicks */ }
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
            ) {
                // Handle bar
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp, bottom = 14.dp)
                        .size(36.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(theme.line)
                )

                // File header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    FileIcon(item, 46.dp, theme)
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.text)
                        val sub = if (item.kind == FileKind.FOLDER)
                            "Folder · ${item.childCount} items"
                        else
                            "${item.extension.uppercase()} · ${item.size.formatSize()}"
                        Text(sub, fontSize = 12.sp, color = theme.sub)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, Modifier.size(16.dp), tint = theme.text)
                    }
                }

                // Quick actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for ((icon, label, warn, action) in listOf(
                        PropsAction(Icons.Default.Share,    "Share",  false, onShare),
                        PropsAction(Icons.Default.ContentCopy, "Copy", false, {}),
                        PropsAction(Icons.Default.Edit,     "Rename", false, onRename),
                        PropsAction(Icons.Default.Star,     "Star",   false, {}),
                        PropsAction(Icons.Default.Delete,   "Delete", true,  onDelete),
                    )) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.sunken)
                                .clickable { action() }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(icon, null, Modifier.size(16.dp), tint = if (warn) theme.warn else theme.text)
                            Text(label, fontSize = 11.sp, color = if (warn) theme.warn else theme.text, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Metadata table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.sunken),
                ) {
                    val rows = listOf(
                        "Type"        to if (item.kind == FileKind.FOLDER) "File folder" else "${item.extension.uppercase()} document",
                        "Location"    to item.file.parent.orEmpty(),
                        "Size"        to item.size.formatSize().ifEmpty { "—" },
                        "Modified"    to item.lastModified.formatDate(),
                        "Permissions" to "Read · Write",
                    )
                    rows.forEachIndexed { i, (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(key, modifier = Modifier.width(100.dp), fontSize = 12.5.sp, color = theme.sub, fontWeight = FontWeight.Medium)
                            Text(
                                value,
                                modifier = Modifier.weight(1f),
                                fontSize = 12.5.sp, color = theme.text,
                                fontFamily = if (key == "Location") FontFamily.Monospace else FontFamily.Default,
                            )
                        }
                        if (i < rows.lastIndex) Divider(color = theme.softLine, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

private data class PropsAction(
    val icon: ImageVector,
    val label: String,
    val warn: Boolean,
    val action: () -> Unit,
)
