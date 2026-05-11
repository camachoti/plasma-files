package com.camachoti.plasmafiles.ui.theme

import androidx.compose.ui.graphics.Color

enum class AccentColor { TEAL, AMBER, VIOLET, ROSE }
enum class AppThemeMode { LIGHT, DARK }

data class PlasmaTheme(
    val bg: Color,
    val panel: Color,
    val sunken: Color,
    val line: Color,
    val softLine: Color,
    val text: Color,
    val sub: Color,
    val mute: Color,
    val warn: Color,
    val accent: Color,
    val accentSoft: Color,
    val selected: Color,
    val hover: Color,
    val isDark: Boolean,
)

fun buildTheme(mode: AppThemeMode, accent: AccentColor): PlasmaTheme {
    val isDark = mode == AppThemeMode.DARK
    val (accentColor, accentSoft, selected) = when (accent) {
        AccentColor.TEAL -> Triple(
            if (isDark) PlasmaColors.Teal.Dark    else PlasmaColors.Teal.Light,
            if (isDark) PlasmaColors.Teal.SoftDark else PlasmaColors.Teal.SoftLight,
            if (isDark) PlasmaColors.Teal.SelDark  else PlasmaColors.Teal.SelLight,
        )
        AccentColor.AMBER -> Triple(
            if (isDark) PlasmaColors.Amber.Dark    else PlasmaColors.Amber.Light,
            if (isDark) PlasmaColors.Amber.SoftDark else PlasmaColors.Amber.SoftLight,
            if (isDark) PlasmaColors.Amber.SelDark  else PlasmaColors.Amber.SelLight,
        )
        AccentColor.VIOLET -> Triple(
            if (isDark) PlasmaColors.Violet.Dark    else PlasmaColors.Violet.Light,
            if (isDark) PlasmaColors.Violet.SoftDark else PlasmaColors.Violet.SoftLight,
            if (isDark) PlasmaColors.Violet.SelDark  else PlasmaColors.Violet.SelLight,
        )
        AccentColor.ROSE -> Triple(
            if (isDark) PlasmaColors.Rose.Dark    else PlasmaColors.Rose.Light,
            if (isDark) PlasmaColors.Rose.SoftDark else PlasmaColors.Rose.SoftLight,
            if (isDark) PlasmaColors.Rose.SelDark  else PlasmaColors.Rose.SelLight,
        )
    }
    return PlasmaTheme(
        bg         = if (isDark) PlasmaColors.DarkBg       else PlasmaColors.LightBg,
        panel      = if (isDark) PlasmaColors.DarkPanel     else PlasmaColors.LightPanel,
        sunken     = if (isDark) PlasmaColors.DarkSunken    else PlasmaColors.LightSunken,
        line       = if (isDark) PlasmaColors.DarkLine      else PlasmaColors.LightLine,
        softLine   = if (isDark) PlasmaColors.DarkSoftLine  else PlasmaColors.LightSoftLine,
        text       = if (isDark) PlasmaColors.DarkText      else PlasmaColors.LightText,
        sub        = if (isDark) PlasmaColors.DarkSub       else PlasmaColors.LightSub,
        mute       = if (isDark) PlasmaColors.DarkMute      else PlasmaColors.LightMute,
        warn       = if (isDark) PlasmaColors.DarkWarn      else PlasmaColors.LightWarn,
        accent     = accentColor,
        accentSoft = accentSoft,
        selected   = selected,
        hover      = if (isDark) PlasmaColors.DarkHover     else PlasmaColors.LightHover,
        isDark     = isDark,
    )
}
