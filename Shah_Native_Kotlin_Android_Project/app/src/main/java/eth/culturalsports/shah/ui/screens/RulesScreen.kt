package eth.culturalsports.shah.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eth.culturalsports.shah.R
import eth.culturalsports.shah.model.AppScreen
import eth.culturalsports.shah.model.BoardTopology
import eth.culturalsports.shah.ui.components.ShahBoardLogoWithGlowingPebble
import eth.culturalsports.shah.ui.theme.LocalShahPalette
import eth.culturalsports.shah.viewmodel.ShahViewModel

@Composable
fun RulesScreen(viewModel: ShahViewModel) {
    val palette = LocalShahPalette.current
    val rules by viewModel.rulesList.collectAsState()
    val selectedCategory by viewModel.selectedRuleCategory.collectAsState()
    val scrollState = rememberScrollState()
    val filterScroll = rememberScrollState()
    val expandedMap = remember { mutableStateMapOf<String, Boolean>("rule-1" to true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(palette.bgCard)
                .border(1.5.dp, palette.gold.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ShahBoardLogoWithGlowingPebble(size = 54.dp, animateMoveShadow = false)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewModel.tr(R.string.shah_rules_main_title),
                    color = palette.gold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.tr(R.string.shah_rules_main_desc),
                    color = palette.textBody,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Category Filter Bar
        val filters = listOf(
            "all" to viewModel.tr(R.string.shah_filter_all),
            "board" to viewModel.tr(R.string.shah_filter_board),
            "play" to viewModel.tr(R.string.shah_filter_play),
            "referee" to viewModel.tr(R.string.shah_filter_referee)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for ((catKey, label) in filters) {
                val active = selectedCategory == catKey
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (active) palette.gold else palette.bgCard)
                        .border(1.5.dp, if (active) palette.gold else palette.borderGlass, RoundedCornerShape(99.dp))
                        .clickable { viewModel.setRuleCategoryFilter(catKey) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (active) Color(0xFF090B14) else palette.textMuted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Visual Board & Pebbles Specification Card (40x40 cm, 5 cm spacing, 32 pebbles)
        if (selectedCategory == "all" || selectedCategory == "board") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.bgCard)
                    .border(1.5.dp, palette.borderGlass, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShahBoardLogoWithGlowingPebble(size = 32.dp, animateMoveShadow = false)
                    Text(
                        text = viewModel.tr(R.string.shah_visual_card_title),
                        color = palette.gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = viewModel.tr(R.string.shah_visual_card_desc),
                    color = palette.textBody,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Native Compose 40x40cm Board Spec Diagram
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                        .aspectRatio(1.1f)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E2230))
                        .border(2.dp, palette.gold, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    BoardSpecificationDiagramCanvas(viewModel = viewModel)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Equipment Specification Table
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_spec_th1),
                    col2 = viewModel.tr(R.string.shah_spec_th2),
                    col3 = viewModel.tr(R.string.shah_spec_th3),
                    isHeader = true
                )
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_spec_r1_c1),
                    col2 = viewModel.tr(R.string.shah_spec_r1_c2),
                    col3 = viewModel.tr(R.string.shah_spec_r1_c3)
                )
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_spec_r2_c1),
                    col2 = viewModel.tr(R.string.shah_spec_r2_c2),
                    col3 = viewModel.tr(R.string.shah_spec_r2_c3)
                )
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_spec_r3_c1),
                    col2 = viewModel.tr(R.string.shah_spec_r3_c2),
                    col3 = viewModel.tr(R.string.shah_spec_r3_c3)
                )
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_spec_r4_c1),
                    col2 = viewModel.tr(R.string.shah_spec_r4_c2),
                    col3 = viewModel.tr(R.string.shah_spec_r4_c3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. Expandable Rule Articles (Articles 1 to 9)
        val filteredRules = rules.filter { selectedCategory == "all" || it.category == selectedCategory }
        for (article in filteredRules) {
            val isExpanded = if (selectedCategory != "all") true else (expandedMap[article.id] ?: false)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.bgCard)
                    .border(1.5.dp, palette.borderGlass, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedMap[article.id] = !isExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.title,
                        color = palette.gold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        color = palette.gold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                if (isExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(palette.borderGlass)
                    )
                    Text(
                        text = article.content,
                        color = palette.textBody,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 5. Referee Score Sheet Card
        if (selectedCategory == "all" || selectedCategory == "referee") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.bgCard)
                    .border(2.dp, palette.gold, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = viewModel.tr(R.string.shah_ref_card_title),
                    color = palette.gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_ref_th_round),
                    col2 = viewModel.tr(R.string.shah_ref_th_winner),
                    col3 = viewModel.tr(R.string.shah_ref_th_pts),
                    isHeader = true
                )
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_ref_td_r1),
                    col2 = viewModel.tr(R.string.shah_ref_td_p1_win),
                    col3 = "2 - 0 (${viewModel.tr(R.string.shah_ref_td30s)})"
                )
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_ref_td_r2),
                    col2 = "—",
                    col3 = "— (${viewModel.tr(R.string.shah_ref_td30s)})"
                )
                SpecRowItem(
                    col1 = viewModel.tr(R.string.shah_ref_td_r3),
                    col2 = "—",
                    col3 = "— (${viewModel.tr(R.string.shah_ref_td30s)})"
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 6. Action Buttons to Quiz or Home
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.gold)
                    .clickable { viewModel.navigateTo(AppScreen.QUIZ) }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.tr(R.string.shah_btn_go_quiz),
                    color = Color(0xFF090B14),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.bgCard)
                    .border(1.5.dp, palette.borderGlass, RoundedCornerShape(14.dp))
                    .clickable { viewModel.navigateTo(AppScreen.HOME) }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.tr(R.string.shah_btn_go_home),
                    color = palette.textMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BoardSpecificationDiagramCanvas(viewModel: ShahViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${viewModel.tr(R.string.shah_svg_title)} (${viewModel.tr(R.string.shah_svg40cm)} | ${viewModel.tr(R.string.shah_svg5cm)})",
            color = Color(0xFF38BDF8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .padding(4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val s = minOf(size.width, size.height)
                fun pt(v: Float): Float = s * (v / 100f)
                val strokeW = (s * 0.016f).coerceAtLeast(2f)

                drawRect(Color(0xFF22C55E), Offset(pt(8f), pt(8f)), Size(pt(84f), pt(84f)))
                drawRect(Color.Black, Offset(pt(8f), pt(8f)), Size(pt(84f), pt(84f)), style = Stroke(strokeW))

                drawRect(Color(0xFFEAB308), Offset(pt(21f), pt(21f)), Size(pt(58f), pt(58f)))
                drawRect(Color.Black, Offset(pt(21f), pt(21f)), Size(pt(58f), pt(58f)), style = Stroke(strokeW))

                drawRect(Color(0xFFEF4444), Offset(pt(34f), pt(34f)), Size(pt(32f), pt(32f)))
                drawRect(Color.Black, Offset(pt(34f), pt(34f)), Size(pt(32f), pt(32f)), style = Stroke(strokeW))

                drawLine(Color.Black, Offset(pt(50f), pt(8f)), Offset(pt(50f), pt(34f)), strokeW)
                drawLine(Color.Black, Offset(pt(50f), pt(66f)), Offset(pt(50f), pt(92f)), strokeW)
                drawLine(Color.Black, Offset(pt(8f), pt(50f)), Offset(pt(34f), pt(50f)), strokeW)
                drawLine(Color.Black, Offset(pt(66f), pt(50f)), Offset(pt(92f), pt(50f)), strokeW)

                for (c in BoardTopology.nodeCoords) {
                    drawCircle(Color.Black, radius = pt(2.6f), center = Offset(pt(c[0]), pt(c[1])))
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFD97706)).border(1.dp, Color.White, CircleShape))
                Text(text = "P1 (12+4)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFA855F7)).border(1.dp, Color.White, CircleShape))
                Text(text = "P2 (12+4)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SpecRowItem(
    col1: String,
    col2: String,
    col3: String,
    isHeader: Boolean = false
) {
    val palette = LocalShahPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) palette.gold.copy(alpha = 0.16f) else Color.Transparent)
            .border(0.5.dp, palette.borderGlass)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = col1,
            color = if (isHeader) palette.gold else palette.textMain,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = col2,
            color = if (isHeader) palette.gold else palette.textBody,
            fontSize = 12.sp,
            fontWeight = if (isHeader) FontWeight.Black else FontWeight.SemiBold,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = col3,
            color = if (isHeader) palette.gold else palette.textBody,
            fontSize = 12.sp,
            fontWeight = if (isHeader) FontWeight.Black else FontWeight.Normal,
            modifier = Modifier.weight(1.6f)
        )
    }
}
