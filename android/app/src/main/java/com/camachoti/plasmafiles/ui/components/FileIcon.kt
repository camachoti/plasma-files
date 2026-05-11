package com.camachoti.plasmafiles.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camachoti.plasmafiles.data.model.FileItem
import com.camachoti.plasmafiles.data.model.FileKind
import com.camachoti.plasmafiles.ui.theme.PlasmaColors
import com.camachoti.plasmafiles.ui.theme.PlasmaTheme

fun kindColor(kind: FileKind): Color = when (kind) {
    FileKind.PDF     -> PlasmaColors.KindPdf
    FileKind.DOC     -> PlasmaColors.KindDoc
    FileKind.SHEET   -> PlasmaColors.KindSheet
    FileKind.SLIDE   -> PlasmaColors.KindSlide
    FileKind.TEXT    -> PlasmaColors.KindText
    FileKind.IMAGE   -> PlasmaColors.KindImage
    FileKind.VIDEO   -> PlasmaColors.KindVideo
    FileKind.AUDIO   -> PlasmaColors.KindAudio
    FileKind.ARCHIVE -> PlasmaColors.KindArchive
    FileKind.APP     -> PlasmaColors.KindApp
    else             -> Color.Gray
}

@Composable
fun FileIcon(item: FileItem, size: Dp = 28.dp, theme: PlasmaTheme) {
    when (item.kind) {
        FileKind.FOLDER, FileKind.VOLUME -> FolderIcon(size = size, isDark = theme.isDark)
        else -> FileTileIcon(
            size  = size,
            color = kindColor(item.kind),
            label = item.extension.uppercase().take(4),
            theme = theme,
        )
    }
}

@Composable
fun FolderIcon(size: Dp = 28.dp, isDark: Boolean = false) {
    val front = if (isDark) Color(0xFFD6A04A) else Color(0xFFE8B962)
    val back  = if (isDark) Color(0xFFA07A32) else Color(0xFFCAA052)
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        // Body
        val body = Path().apply {
            addRoundRect(RoundRect(
                Rect(Offset(w * 2/32f, h * 4/28f), Size(w * 28/32f, h * 22/28f)),
                CornerRadius(w * 2/32f)
            ))
        }
        drawPath(body, back)
        // Tab
        val tab = Path().apply {
            moveTo(w * 2/32f, h * 7/28f)
            lineTo(w * 2/32f, h * 4/28f)
            arcTo(Rect(Offset(w * 2/32f, h * 4/28f), Size(w * 2/32f, h * 2/28f)), 180f, -90f, false)
            lineTo(w * 9/32f, h * 2/28f)
            lineTo(w * 12/32f, h * 5/28f)
            lineTo(w * 30/32f, h * 5/28f)
            lineTo(w * 30/32f, h * 7/28f)
            close()
        }
        drawPath(tab, back)
        // Front panel
        val front2 = Path().apply {
            addRoundRect(RoundRect(
                Rect(Offset(w * 2/32f, h * 10/28f), Size(w * 28/32f, h * 16/28f)),
                CornerRadius(w * 2/32f)
            ))
        }
        drawPath(front2, front)
    }
}

@Composable
fun FileTileIcon(size: Dp = 28.dp, color: Color, label: String = "", theme: PlasmaTheme) {
    Box(Modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width; val h = this.size.height
            val r = CornerRadius(w * 1.5f / 28f)
            // Page body
            val page = Path().apply {
                moveTo(w * 5/28f, h * 3/28f)
                lineTo(w * 18/28f, h * 3/28f)
                lineTo(w * 23/28f, h * 8/28f)
                lineTo(w * 23/28f, h * 26/28f)
                arcTo(Rect(Offset(w * 21/28f, h * 24/28f), Size(w * 2/28f, h * 2/28f)), 0f, 90f, false)
                lineTo(w * 7/28f, h * 26/28f)
                arcTo(Rect(Offset(w * 5/28f, h * 24/28f), Size(w * 2/28f, h * 2/28f)), 90f, 90f, false)
                lineTo(w * 5/28f, h * 5/28f)
                arcTo(Rect(Offset(w * 5/28f, h * 3/28f), Size(w * 2/28f, h * 2/28f)), 180f, 90f, false)
                close()
            }
            drawPath(page, theme.panel)
            drawPath(page, theme.line, style = Stroke(width = w / 28f))
            // Dog-ear
            val fold = Path().apply {
                moveTo(w * 18/28f, h * 3/28f)
                lineTo(w * 23/28f, h * 8/28f)
                lineTo(w * 18/28f, h * 8/28f)
                close()
            }
            drawPath(fold, theme.sunken)
            drawPath(fold, theme.line, style = Stroke(width = w / 28f))
            // Color band
            drawRoundRect(
                color      = color,
                topLeft    = Offset(w * 4/28f, h * 17/28f),
                size       = Size(w * 20/28f, h * 8/28f),
                cornerRadius = CornerRadius(w * 1.4f / 28f),
            )
        }
        if (label.isNotEmpty()) {
            Text(
                text       = label,
                modifier   = Modifier.align(Alignment.BottomCenter),
                fontSize   = (size.value * 0.22f).sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )
        }
    }
}
