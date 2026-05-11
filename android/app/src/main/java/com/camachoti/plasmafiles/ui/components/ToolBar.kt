package com.camachoti.plasmafiles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camachoti.plasmafiles.ui.theme.PlasmaTheme

data class RibbonItem(
    val icon: ImageVector,
    val label: String,
    val enabled: Boolean = true,
    val warn: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun Ribbon(items: List<RibbonItem>, dividers: List<Int> = emptyList(), theme: PlasmaTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.panel)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items.forEachIndexed { i, item ->
            if (i in dividers) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(theme.line)
                        .padding(horizontal = 4.dp)
                )
            }
            RibbonButton(item, theme)
        }
    }
}

@Composable
private fun RibbonButton(item: RibbonItem, theme: PlasmaTheme) {
    val color = when {
        !item.enabled -> theme.mute
        item.warn     -> theme.warn
        else          -> theme.text
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .run { if (item.enabled) clickableNoRipple(item.onClick) else this }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .widthIn(min = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(item.icon, null, Modifier.size(18.dp), tint = color)
        Text(item.label, fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
fun SelectionBar(
    count: Int,
    onCancel: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit,
    theme: PlasmaTheme,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.accent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, null, Modifier.size(18.dp), tint = Color.White)
        }
        Text(
            "$count selected",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
        for ((icon, action) in listOf(
            Icons.Default.ContentCopy to onCopy,
            Icons.Default.ContentCut to onCut,
            Icons.Default.Share to onShare,
            Icons.Default.Delete to onDelete,
            Icons.Default.MoreVert to onMore,
        )) {
            IconButton(onClick = action, modifier = Modifier.size(36.dp)) {
                Icon(icon, null, Modifier.size(18.dp), tint = Color.White)
            }
        }
    }
}

// Workaround — Modifier.clickable without ripple for ribbon buttons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = source, indication = null, onClick = onClick)
}
