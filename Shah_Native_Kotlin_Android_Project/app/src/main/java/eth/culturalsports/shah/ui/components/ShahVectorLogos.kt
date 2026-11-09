package eth.culturalsports.shah.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eth.culturalsports.shah.R
import eth.culturalsports.shah.model.BoardTopology

/**
 * 100% Copyright-Free Native Kotlin Compose Shah Board Logo:
 * - Royal Blue Outer Frame (#1676B8)
 * - Outer Green Square (#17B84B), Middle Yellow Square (#FFEA00), Inner Red Square (#EF1A2D)
 * - 8 Vertical & 8 Horizontal Black Lines + 24 Black Stations
 * - Center Radiant Golden Pebble with animated 'Shadow Effect Move' trail!
 */
@Composable
fun ShahBoardLogoWithGlowingPebble(
    size: Dp,
    modifier: Modifier = Modifier,
    animateMoveShadow: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "shahLogoGlow")
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pebblePulse"
    )
    val scaleFactor = if (animateMoveShadow) pulse else 1f

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val s = minOf(w, h)
        fun pt(v: Float): Float = s * (v / 100f)

        val lineStroke = (s * 0.024f).coerceAtLeast(1.5f)

        // 1. Royal Blue Outer Board Frame
        drawRoundRect(
            color = Color(0xFF1676B8),
            topLeft = Offset(pt(2f), pt(2f)),
            size = Size(pt(96f), pt(96f)),
            cornerRadius = CornerRadius(pt(14f), pt(14f))
        )
        drawRoundRect(
            color = Color(0xFFFCA311),
            topLeft = Offset(pt(2f), pt(2f)),
            size = Size(pt(96f), pt(96f)),
            cornerRadius = CornerRadius(pt(14f), pt(14f)),
            style = Stroke(width = (s * 0.02f).coerceAtLeast(1.2f))
        )

        // 2. Outer Green Square (8..92)
        drawRect(
            color = Color(0xFF17B84B),
            topLeft = Offset(pt(8f), pt(8f)),
            size = Size(pt(84f), pt(84f))
        )
        drawRect(
            color = Color(0xFF0D0D0D),
            topLeft = Offset(pt(8f), pt(8f)),
            size = Size(pt(84f), pt(84f)),
            style = Stroke(width = lineStroke)
        )

        // 3. Middle Yellow Square (21..79)
        drawRect(
            color = Color(0xFFFFEA00),
            topLeft = Offset(pt(21f), pt(21f)),
            size = Size(pt(58f), pt(58f))
        )
        drawRect(
            color = Color(0xFF0D0D0D),
            topLeft = Offset(pt(21f), pt(21f)),
            size = Size(pt(58f), pt(58f)),
            style = Stroke(width = lineStroke)
        )

        // 4. Inner Red Square (34..66)
        drawRect(
            color = Color(0xFFEF1A2D),
            topLeft = Offset(pt(34f), pt(34f)),
            size = Size(pt(32f), pt(32f))
        )
        drawRect(
            color = Color(0xFF0D0D0D),
            topLeft = Offset(pt(34f), pt(34f)),
            size = Size(pt(32f), pt(32f)),
            style = Stroke(width = lineStroke)
        )

        // 5. Orthogonal Black Connecting Lines
        drawLine(Color(0xFF0D0D0D), Offset(pt(50f), pt(8f)), Offset(pt(50f), pt(34f)), strokeWidth = lineStroke)
        drawLine(Color(0xFF0D0D0D), Offset(pt(50f), pt(66f)), Offset(pt(50f), pt(92f)), strokeWidth = lineStroke)
        drawLine(Color(0xFF0D0D0D), Offset(pt(8f), pt(50f)), Offset(pt(34f), pt(50f)), strokeWidth = lineStroke)
        drawLine(Color(0xFF0D0D0D), Offset(pt(66f), pt(50f)), Offset(pt(92f), pt(50f)), strokeWidth = lineStroke)

        // 6. 24 Black Stations
        val nodeRadius = (s * 0.032f).coerceAtLeast(1.8f)
        for (coord in BoardTopology.nodeCoords) {
            val nx = pt(coord[0])
            val ny = pt(coord[1])
            drawCircle(
                color = Color(0x88000000),
                radius = nodeRadius,
                center = Offset(nx + s * 0.006f, ny + s * 0.008f)
            )
            drawCircle(
                color = Color(0xFF121418),
                radius = nodeRadius,
                center = Offset(nx, ny)
            )
        }

        // 7. Dynamic 'Shadow Effect Move' Trail leading into the center (50, 50)
        val mainPebbleR = (s * 0.088f).coerceAtLeast(4f)
        val trailSteps = listOf(
            Triple(50f, 37f, 0.22f),
            Triple(50f, 41f, 0.36f),
            Triple(50f, 45f, 0.54f),
            Triple(50f, 48f, 0.72f)
        )
        for ((tx, ty, alphaFactor) in trailSteps) {
            drawCircle(
                color = Color(0xFFFFBE0B).copy(alpha = alphaFactor * 0.65f),
                radius = mainPebbleR * (0.62f + alphaFactor * 0.32f),
                center = Offset(pt(tx), pt(ty))
            )
        }

        // 8. Center Radiant Glowing Pebble at (50, 50)
        val centerPt = Offset(pt(50f), pt(50f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xBBFFD60A),
                    Color(0x44FCA311),
                    Color.Transparent
                ),
                center = centerPt,
                radius = mainPebbleR * 1.95f * scaleFactor
            ),
            radius = mainPebbleR * 1.95f * scaleFactor,
            center = centerPt
        )
        // Drop shadow
        drawCircle(
            color = Color(0xBB000000),
            radius = mainPebbleR,
            center = Offset(centerPt.x + s * 0.016f, centerPt.y + s * 0.02f)
        )
        // Pebble body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
                center = Offset(centerPt.x - mainPebbleR * 0.25f, centerPt.y - mainPebbleR * 0.25f),
                radius = mainPebbleR * 1.3f
            ),
            radius = mainPebbleR,
            center = centerPt
        )
        drawCircle(
            color = Color.White,
            radius = mainPebbleR,
            center = centerPt,
            style = Stroke(width = (s * 0.014f).coerceAtLeast(1f))
        )
        // Specular 3D shine highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = mainPebbleR * 0.28f,
            center = Offset(centerPt.x - mainPebbleR * 0.28f, centerPt.y - mainPebbleR * 0.28f)
        )
    }
}

/**
 * Responsive Ethiopian Cultural Sports Federation Logo Badge:
 * Uses the high-resolution enhanced federation_logo_hd drawable inside a crisp circular golden frame.
 */
@Composable
fun FederationLogoBadge(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.federation_logo_hd),
            contentDescription = "የኢትዮጵያ ባህል ስፖርቶች ፌዴሬሽን ሎጎ",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

/**
 * 100% Copyright-Free Vector Icons for Email, Telegram, Facebook, and Share.
 */
@Composable
fun SocialVectorIcon(
    kind: String,
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        when (kind) {
            "email" -> {
                drawRoundRect(
                    color = Color(0xFFEA4335),
                    topLeft = Offset(0f, h * 0.12f),
                    size = Size(w, h * 0.76f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
                )
                val flap = Path().apply {
                    moveTo(w * 0.08f, h * 0.20f)
                    lineTo(w * 0.50f, h * 0.54f)
                    lineTo(w * 0.92f, h * 0.20f)
                }
                drawPath(
                    path = flap,
                    color = Color.White,
                    style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
                )
            }

            "telegram" -> {
                drawCircle(
                    color = Color(0xFF26A5E4),
                    radius = w * 0.48f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                val plane = Path().apply {
                    moveTo(w * 0.22f, h * 0.51f)
                    lineTo(w * 0.75f, h * 0.28f)
                    lineTo(w * 0.64f, h * 0.76f)
                    lineTo(w * 0.46f, h * 0.59f)
                    close()
                }
                drawPath(path = plane, color = Color.White)
            }

            "facebook" -> {
                drawCircle(
                    color = Color(0xFF1877F2),
                    radius = w * 0.48f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                // Clean 'f' letterform
                val fPath = Path().apply {
                    moveTo(w * 0.56f, h * 0.82f)
                    lineTo(w * 0.56f, h * 0.52f)
                    lineTo(w * 0.68f, h * 0.52f)
                    lineTo(w * 0.70f, h * 0.39f)
                    lineTo(w * 0.56f, h * 0.39f)
                    lineTo(w * 0.56f, h * 0.31f)
                    cubicTo(w * 0.56f, h * 0.26f, w * 0.59f, h * 0.24f, w * 0.65f, h * 0.24f)
                    lineTo(w * 0.71f, h * 0.24f)
                    lineTo(w * 0.71f, h * 0.13f)
                    lineTo(w * 0.61f, h * 0.13f)
                    cubicTo(w * 0.47f, h * 0.13f, w * 0.42f, h * 0.20f, w * 0.42f, h * 0.31f)
                    lineTo(w * 0.42f, h * 0.39f)
                    lineTo(w * 0.32f, h * 0.39f)
                    lineTo(w * 0.32f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.82f)
                    close()
                }
                drawPath(path = fPath, color = Color.White)
            }

            "share" -> {
                val c1 = Offset(w * 0.24f, h * 0.50f)
                val c2 = Offset(w * 0.74f, h * 0.22f)
                val c3 = Offset(w * 0.74f, h * 0.78f)
                drawLine(Color(0xFFFCA311), c1, c2, strokeWidth = w * 0.10f, cap = StrokeCap.Round)
                drawLine(Color(0xFFFCA311), c1, c3, strokeWidth = w * 0.10f, cap = StrokeCap.Round)
                drawCircle(Color(0xFF10B981), radius = w * 0.15f, center = c1)
                drawCircle(Color(0xFFFCA311), radius = w * 0.15f, center = c2)
                drawCircle(Color(0xFF38BDF8), radius = w * 0.15f, center = c3)
            }
        }
    }
}
