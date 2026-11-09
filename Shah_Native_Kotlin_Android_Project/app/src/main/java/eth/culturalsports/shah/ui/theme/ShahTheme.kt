package eth.culturalsports.shah.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

data class ShahPalette(
    val isDark: Boolean,
    val bgMain: Color,
    val bgCard: Color,
    val bgCardElevated: Color,
    val bgGlass: Color,
    val borderGlass: Color,
    val headerBg: Color,
    val gold: Color,
    val crimson: Color,
    val emerald: Color,
    val sky: Color,
    val purple: Color,
    val textMain: Color,
    val textBody: Color,
    val textMuted: Color,
    val boardOuterFrame: Color,
    val boardGreen: Color,
    val boardYellow: Color,
    val boardRed: Color
)

val DarkShahPalette = ShahPalette(
    isDark = true,
    bgMain = Color(0xFF090B14),
    bgCard = Color(0xFF14192E),
    bgCardElevated = Color(0xFF1C2340),
    bgGlass = Color(0x14FFFFFF),
    borderGlass = Color(0x29FFFFFF),
    headerBg = Color(0xF20B0E1A),
    gold = Color(0xFFFCA311),
    crimson = Color(0xFFE94560),
    emerald = Color(0xFF10B981),
    sky = Color(0xFF38BDF8),
    purple = Color(0xFFA855F7),
    textMain = Color(0xFFF8FAFC),
    textBody = Color(0xFFE2E8F0),
    textMuted = Color(0xFF94A3B8),
    boardOuterFrame = Color(0xFF1676B8),
    boardGreen = Color(0xFF17B84B),
    boardYellow = Color(0xFFFFEA00),
    boardRed = Color(0xFFEF1A2D)
)

val LightShahPalette = ShahPalette(
    isDark = false,
    bgMain = Color(0xFFF1F5F9),
    bgCard = Color(0xFFFFFFFF),
    bgCardElevated = Color(0xFFF8FAFC),
    bgGlass = Color(0x0F0F172A),
    borderGlass = Color(0x260F172A),
    headerBg = Color(0xF5FFFFFF),
    gold = Color(0xFFD97706),
    crimson = Color(0xFFDC2626),
    emerald = Color(0xFF059669),
    sky = Color(0xFF0284C7),
    purple = Color(0xFF7E22CE),
    textMain = Color(0xFF0F172A),
    textBody = Color(0xFF1E293B),
    textMuted = Color(0xFF475569),
    boardOuterFrame = Color(0xFF1676B8),
    boardGreen = Color(0xFF17B84B),
    boardYellow = Color(0xFFFFEA00),
    boardRed = Color(0xFFEF1A2D)
)

val LocalShahPalette = compositionLocalOf { DarkShahPalette }

@Composable
fun ShahTheme(
    isDarkTheme: Boolean,
    fontScaleStep: Int,
    isRtl: Boolean,
    content: @Composable () -> Unit
) {
    val palette = if (isDarkTheme) DarkShahPalette else LightShahPalette
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = palette.gold,
            secondary = palette.emerald,
            tertiary = palette.sky,
            background = palette.bgMain,
            surface = palette.bgCard,
            onPrimary = Color(0xFF090B14),
            onBackground = palette.textMain,
            onSurface = palette.textMain
        )
    } else {
        lightColorScheme(
            primary = palette.gold,
            secondary = palette.emerald,
            tertiary = palette.sky,
            background = palette.bgMain,
            surface = palette.bgCard,
            onPrimary = Color.White,
            onBackground = palette.textMain,
            onSurface = palette.textMain
        )
    }

    val currentDensity = LocalDensity.current
    val scaledFontMultiplier = (1f + (fontScaleStep * 0.09f)).coerceIn(0.82f, 1.36f)
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * scaledFontMultiplier
    )
    val layoutDir = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalShahPalette provides palette,
        LocalDensity provides customDensity,
        LocalLayoutDirection provides layoutDir
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
