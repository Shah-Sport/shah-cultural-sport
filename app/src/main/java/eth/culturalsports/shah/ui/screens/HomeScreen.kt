package eth.culturalsports.shah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import eth.culturalsports.shah.model.AppScreen
import eth.culturalsports.shah.model.GameMode
import eth.culturalsports.shah.ui.components.FederationLogoBadge
import eth.culturalsports.shah.ui.components.ShahBoardLogoWithGlowingPebble
import eth.culturalsports.shah.ui.theme.LocalShahPalette
import eth.culturalsports.shah.viewmodel.ShahViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: ShahViewModel) {
    val palette = LocalShahPalette.current
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        val isWideTablet = maxWidth >= 620.dp
        val logoSize = if (maxWidth < 360.dp) 70.dp else 86.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Banner Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0x4D166534),
                                Color(0x29FCA311),
                                Color(0x40DC2626)
                            )
                        )
                    )
                    .border(2.dp, palette.gold.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FederationLogoBadge(size = logoSize)
                    ShahBoardLogoWithGlowingPebble(size = logoSize, animateMoveShadow = true)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = viewModel.tr(R.string.shah_hero_title),
                    color = palette.gold,
                    fontSize = if (isWideTablet) 24.sp else 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = viewModel.tr(R.string.shah_hero_desc),
                    color = palette.textBody,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroPill(text = viewModel.tr(R.string.shah_hero_pill1))
                    HeroPill(text = viewModel.tr(R.string.shah_hero_pill2))
                    HeroPill(text = viewModel.tr(R.string.shah_hero_pill3))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Mode Cards Grid (2x2 on Tablets, Vertical Stack on Phones)
            val cards = listOf(
                ModeCardItem(
                    badge = viewModel.tr(R.string.shah_card1_badge),
                    title = viewModel.tr(R.string.shah_card1_title),
                    desc = viewModel.tr(R.string.shah_card1_desc),
                    showFedLogo = true,
                    onClick = { viewModel.openStarterModal(GameMode.AI) }
                ),
                ModeCardItem(
                    badge = viewModel.tr(R.string.shah_card2_badge),
                    title = viewModel.tr(R.string.shah_card2_title),
                    desc = viewModel.tr(R.string.shah_card2_desc),
                    showFedLogo = true,
                    onClick = { viewModel.openStarterModal(GameMode.PVP) }
                ),
                ModeCardItem(
                    badge = viewModel.tr(R.string.shah_card3_badge),
                    title = viewModel.tr(R.string.shah_card3_title),
                    desc = viewModel.tr(R.string.shah_card3_desc),
                    showFedLogo = false,
                    onClick = { viewModel.navigateTo(AppScreen.RULES) }
                ),
                ModeCardItem(
                    badge = viewModel.tr(R.string.shah_card4_badge),
                    title = viewModel.tr(R.string.shah_card4_title),
                    desc = viewModel.tr(R.string.shah_card4_desc),
                    showFedLogo = true,
                    onClick = { viewModel.navigateTo(AppScreen.QUIZ) }
                )
            )

            if (isWideTablet) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) { ModeCardView(cards[0]) }
                        Box(modifier = Modifier.weight(1f)) { ModeCardView(cards[1]) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) { ModeCardView(cards[2]) }
                        Box(modifier = Modifier.weight(1f)) { ModeCardView(cards[3]) }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (card in cards) {
                        ModeCardView(card)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class ModeCardItem(
    val badge: String,
    val title: String,
    val desc: String,
    val showFedLogo: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun HeroPill(text: String) {
    val palette = LocalShahPalette.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(palette.gold.copy(alpha = 0.15f))
            .border(1.dp, palette.gold.copy(alpha = 0.45f), RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = palette.gold,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ModeCardView(item: ModeCardItem) {
    val palette = LocalShahPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.bgCard)
            .border(1.5.dp, palette.borderGlass, RoundedCornerShape(20.dp))
            .clickable { item.onClick() }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShahBoardLogoWithGlowingPebble(size = 46.dp, animateMoveShadow = false)
                if (item.showFedLogo) {
                    FederationLogoBadge(size = 44.dp)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(palette.gold.copy(alpha = 0.18f))
                    .border(1.dp, palette.gold.copy(alpha = 0.5f), RoundedCornerShape(99.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.badge,
                    color = palette.gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.title,
            color = palette.textMain,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.desc,
            color = palette.textMuted,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 19.sp
        )
    }
}
