package eth.culturalsports.shah.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eth.culturalsports.shah.R
import eth.culturalsports.shah.ui.components.FederationLogoBadge
import eth.culturalsports.shah.ui.components.ShahBoardLogoWithGlowingPebble
import eth.culturalsports.shah.viewmodel.ShahViewModel

@Composable
fun SplashScreen(viewModel: ShahViewModel) {
    val transition = rememberInfiniteTransition(label = "splashFloat")
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF161B33), Color(0xFF070911))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.offset(y = offsetY.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FederationLogoBadge(size = 104.dp)
                ShahBoardLogoWithGlowingPebble(size = 104.dp, animateMoveShadow = true)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = viewModel.tr(R.string.shah_splash_title),
                color = Color(0xFFFCA311),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = viewModel.tr(R.string.shah_fed_header_title),
                color = Color(0xFFCBD5E1),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(210.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = Color(0xFFFCA311),
                trackColor = Color(0x26FFFFFF)
            )
        }
    }
}
