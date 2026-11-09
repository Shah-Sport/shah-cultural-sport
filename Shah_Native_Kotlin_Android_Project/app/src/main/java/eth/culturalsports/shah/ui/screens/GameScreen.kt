package eth.culturalsports.shah.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import eth.culturalsports.shah.R
import eth.culturalsports.shah.model.AppScreen
import eth.culturalsports.shah.model.BoardTopology
import eth.culturalsports.shah.model.GameMode
import eth.culturalsports.shah.model.GamePhase
import eth.culturalsports.shah.model.GameState
import eth.culturalsports.shah.model.Player
import eth.culturalsports.shah.ui.components.ShahBoardLogoWithGlowingPebble
import eth.culturalsports.shah.ui.theme.LocalShahPalette
import eth.culturalsports.shah.viewmodel.ShahViewModel
import kotlin.math.hypot

@Composable
fun GameScreen(viewModel: ShahViewModel) {
    val palette = LocalShahPalette.current
    val state by viewModel.gameState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val boardMaxSide = min(maxWidth - 16.dp, (maxHeight * 0.52f).coerceAtLeast(250.dp))

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Match Round, Station Number Toggle & 30s Countdown Timer Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(palette.gold.copy(alpha = 0.16f))
                        .border(1.5.dp, palette.gold.copy(alpha = 0.5f), RoundedCornerShape(99.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ShahBoardLogoWithGlowingPebble(size = 20.dp, animateMoveShadow = false)
                    Text(
                        text = "${viewModel.tr(R.string.shah_round_word)} ${state.matchRound}/3 | ${viewModel.tr(R.string.shah_score_word)} ${state.matchScoreP1}-${state.matchScoreP2}",
                        color = palette.gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleStationNumbers() }
                ) {
                    Checkbox(
                        checked = settings.showStationNumbers,
                        onCheckedChange = { viewModel.toggleStationNumbers() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = palette.gold,
                            uncheckedColor = palette.textMuted
                        ),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = viewModel.tr(R.string.shah_show_nums),
                        color = palette.textMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val isWarningTime = !state.isPaused && state.turnSecondsLeft <= 5
                val timerBg = when {
                    state.isPaused -> palette.gold.copy(alpha = 0.22f)
                    isWarningTime -> palette.crimson.copy(alpha = 0.28f)
                    else -> palette.sky.copy(alpha = 0.16f)
                }
                val timerBorder = when {
                    state.isPaused -> palette.gold
                    isWarningTime -> palette.crimson
                    else -> palette.sky
                }
                val timerColor = when {
                    state.isPaused -> palette.gold
                    isWarningTime -> Color(0xFFFF4D6D)
                    else -> palette.sky
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(timerBg)
                        .border(1.5.dp, timerBorder, RoundedCornerShape(99.dp))
                        .clickable { viewModel.togglePauseGame() }
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isPaused) "⏸️ ${state.turnSecondsLeft}s" else "⏱️ ${state.turnSecondsLeft}s",
                        color = timerColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Player 1 & Player 2 Status Cards (Hand, Board, Captured, Fouls 0/5, Yellow/Red Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlayerStatusCard(
                    modifier = Modifier.weight(1f),
                    title = viewModel.getPlayerTitle(Player.P1, state.mode),
                    isTurn = state.turn == Player.P1,
                    turnBadge = if (state.turn == Player.P1) viewModel.tr(R.string.shah_your_turn) else viewModel.tr(R.string.shah_wait_turn),
                    dotColor = Color(0xFFF59E0B),
                    hand = state.p1Inventory,
                    board = state.p1OnBoard,
                    eaten = state.p1Eaten,
                    fouls = state.p1Fouls,
                    viewModel = viewModel
                )
                PlayerStatusCard(
                    modifier = Modifier.weight(1f),
                    title = viewModel.getPlayerTitle(Player.P2, state.mode),
                    isTurn = state.turn == Player.P2,
                    turnBadge = if (state.turn == Player.P2) {
                        if (state.mode == GameMode.AI) viewModel.tr(R.string.shah_ai_thinking) else viewModel.tr(R.string.shah_your_turn)
                    } else {
                        viewModel.tr(R.string.shah_wait_turn)
                    },
                    dotColor = Color(0xFF9333EA),
                    hand = state.p2Inventory,
                    board = state.p2OnBoard,
                    eaten = state.p2Eaten,
                    fouls = state.p2Fouls,
                    viewModel = viewModel
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Info Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (state.bannerIsWarning) palette.gold.copy(alpha = 0.16f)
                        else palette.emerald.copy(alpha = 0.15f)
                    )
                    .border(
                        1.5.dp,
                        if (state.bannerIsWarning) palette.gold.copy(alpha = 0.55f)
                        else palette.emerald.copy(alpha = 0.45f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.bannerMessage.ifBlank { viewModel.tr(R.string.shah_banner_ready) },
                    color = if (state.bannerIsWarning) palette.gold else palette.emerald,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. 100% Native Compose Interactive Shah Board
            Box(
                modifier = Modifier
                    .size(boardMaxSide)
                    .aspectRatio(1f)
                    .alpha(if (state.isPaused) 0.55f else 1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141824))
                    .border(3.dp, palette.gold, RoundedCornerShape(20.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                InteractiveShahBoardCanvas(
                    state = state,
                    showNumbers = settings.showStationNumbers,
                    onStationTap = { idx -> viewModel.onStationTapped(idx) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Game Control Buttons Bar (Pause/Play, Undo, Redo, New Round, Exit)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameControlButton(
                        modifier = Modifier.weight(1.1f),
                        text = if (state.isPaused) viewModel.tr(R.string.shah_btn_play) else viewModel.tr(R.string.shah_btn_pause),
                        isHighlighted = true,
                        isSuccess = state.isPaused,
                        enabled = state.phase != GamePhase.GAMEOVER,
                        onClick = { viewModel.togglePauseGame() }
                    )
                    GameControlButton(
                        modifier = Modifier.weight(1f),
                        text = viewModel.tr(R.string.shah_btn_undo),
                        enabled = state.canUndo,
                        onClick = { viewModel.undoMove() }
                    )
                    GameControlButton(
                        modifier = Modifier.weight(1f),
                        text = viewModel.tr(R.string.shah_btn_redo),
                        enabled = state.canRedo,
                        onClick = { viewModel.redoMove() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameControlButton(
                        modifier = Modifier.weight(1f),
                        text = viewModel.tr(R.string.shah_btn_new_round),
                        onClick = { viewModel.openStarterModal(state.mode) }
                    )
                    GameControlButton(
                        modifier = Modifier.weight(1f),
                        text = viewModel.tr(R.string.shah_btn_exit_home),
                        isDanger = true,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerStatusCard(
    modifier: Modifier,
    title: String,
    isTurn: Boolean,
    turnBadge: String,
    dotColor: Color,
    hand: Int,
    board: Int,
    eaten: Int,
    fouls: Int,
    viewModel: ShahViewModel
) {
    val palette = LocalShahPalette.current
    val cardEmoji = when {
        fouls >= 5 -> " 🟥"
        fouls == 4 -> " 🟨"
        else -> ""
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isTurn) palette.gold.copy(alpha = 0.11f) else palette.bgCard)
            .border(
                width = if (isTurn) 2.dp else 1.5.dp,
                color = if (isTurn) palette.gold else palette.borderGlass,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.5.dp, Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "$title$cardEmoji",
                    color = palette.textMain,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = turnBadge,
                color = if (isTurn) palette.gold else palette.textMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatMiniItem(label = viewModel.tr(R.string.shah_stat_hand), value = hand.toString())
            StatMiniItem(label = viewModel.tr(R.string.shah_stat_board), value = board.toString())
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatMiniItem(label = viewModel.tr(R.string.shah_stat_eaten), value = eaten.toString())
            StatMiniItem(
                label = viewModel.tr(R.string.shah_stat_fouls),
                value = "$fouls/5",
                valueColor = if (fouls >= 4) palette.crimson else palette.textMain
            )
        }
    }
}

@Composable
private fun StatMiniItem(
    label: String,
    value: String,
    valueColor: Color = LocalShahPalette.current.textMain
) {
    val palette = LocalShahPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label ",
            color = palette.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun GameControlButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    isHighlighted: Boolean = false,
    isSuccess: Boolean = false,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    val palette = LocalShahPalette.current
    val bgColor = when {
        !enabled -> palette.bgCard.copy(alpha = 0.4f)
        isSuccess -> palette.emerald.copy(alpha = 0.22f)
        isHighlighted -> palette.gold.copy(alpha = 0.16f)
        isDanger -> palette.crimson.copy(alpha = 0.15f)
        else -> palette.bgCard
    }
    val borderColor = when {
        !enabled -> palette.borderGlass.copy(alpha = 0.3f)
        isSuccess -> palette.emerald
        isHighlighted -> palette.gold
        isDanger -> palette.crimson.copy(alpha = 0.5f)
        else -> palette.borderGlass
    }
    val textColor = when {
        !enabled -> palette.textMuted.copy(alpha = 0.45f)
        isSuccess -> palette.emerald
        isHighlighted -> palette.gold
        isDanger -> palette.crimson
        else -> palette.textMain
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(11.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 6.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InteractiveShahBoardCanvas(
    state: GameState,
    showNumbers: Boolean,
    onStationTap: (Int) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "boardPulse")
    val pulseRadiusFactor by transition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(680, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "targetPulse"
    )

    val validTargets = if (state.phase == GamePhase.PLAYING && state.selectedStation != null) {
        val sel = state.selectedStation
        BoardTopology.adjacency[sel].filter { state.board[it] == null }.toSet()
    } else {
        emptySet()
    }

    val isRemovingOpponent = state.phase == GamePhase.INITIAL_REMOVE_1 ||
        state.phase == GamePhase.INITIAL_REMOVE_2 ||
        state.phase == GamePhase.EATING ||
        state.phase == GamePhase.FORCED_UNBLOCK
    val opponent = state.turn.opponent()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state.phase, state.turn, state.isPaused) {
                detectTapGestures { tapOffset ->
                    val s = minOf(size.width, size.height).toFloat()
                    var closestIdx = -1
                    var minDist = s * 0.095f
                    for (idx in 0 until 24) {
                        val c = BoardTopology.nodeCoords[idx]
                        val cx = s * (c[0] / 100f)
                        val cy = s * (c[1] / 100f)
                        val dist = hypot(tapOffset.x - cx, tapOffset.y - cy)
                        if (dist < minDist) {
                            minDist = dist
                            closestIdx = idx
                        }
                    }
                    if (closestIdx != -1) {
                        onStationTap(closestIdx)
                    }
                }
            }
    ) {
        val s = minOf(size.width, size.height)
        fun pt(v: Float): Float = s * (v / 100f)
        val lineW = (s * 0.016f).coerceAtLeast(2f)

        // 1. Outer Green Square (6..94)
        drawRoundRect(
            color = Color(0xFF1B6B36),
            topLeft = Offset(pt(6f), pt(6f)),
            size = Size(pt(88f), pt(88f)),
            cornerRadius = CornerRadius(pt(1.5f), pt(1.5f))
        )
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(pt(6f), pt(6f)),
            size = Size(pt(88f), pt(88f)),
            cornerRadius = CornerRadius(pt(1.5f), pt(1.5f)),
            style = Stroke(width = lineW)
        )

        // 2. Middle Yellow Square (20..80)
        drawRoundRect(
            color = Color(0xFFEAB308),
            topLeft = Offset(pt(20f), pt(20f)),
            size = Size(pt(60f), pt(60f)),
            cornerRadius = CornerRadius(pt(1.2f), pt(1.2f))
        )
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(pt(20f), pt(20f)),
            size = Size(pt(60f), pt(60f)),
            cornerRadius = CornerRadius(pt(1.2f), pt(1.2f)),
            style = Stroke(width = lineW)
        )

        // 3. Inner Red Square (34..66)
        drawRoundRect(
            color = Color(0xFFDC2626),
            topLeft = Offset(pt(34f), pt(34f)),
            size = Size(pt(32f), pt(32f)),
            cornerRadius = CornerRadius(pt(1f), pt(1f))
        )
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(pt(34f), pt(34f)),
            size = Size(pt(32f), pt(32f)),
            cornerRadius = CornerRadius(pt(1f), pt(1f)),
            style = Stroke(width = lineW)
        )

        // 4. Orthogonal Black Connecting Lines
        drawLine(Color.Black, Offset(pt(50f), pt(6f)), Offset(pt(50f), pt(34f)), strokeWidth = lineW)
        drawLine(Color.Black, Offset(pt(50f), pt(66f)), Offset(pt(50f), pt(94f)), strokeWidth = lineW)
        drawLine(Color.Black, Offset(pt(6f), pt(50f)), Offset(pt(34f), pt(50f)), strokeWidth = lineW)
        drawLine(Color.Black, Offset(pt(66f), pt(50f)), Offset(pt(94f), pt(50f)), strokeWidth = lineW)

        // 5. Highlighted Mill / Shuttle Lines
        state.highlightedMill1?.let { m ->
            val pA = BoardTopology.nodeCoords[m[0]]
            val pB = BoardTopology.nodeCoords[m[2]]
            drawLine(
                color = Color(0xFF00F5D4),
                start = Offset(pt(pA[0]), pt(pA[1])),
                end = Offset(pt(pB[0]), pt(pB[1])),
                strokeWidth = s * 0.022f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(s * 0.03f, s * 0.02f), 0f)
            )
        }
        state.highlightedMill2?.let { m ->
            val pC = BoardTopology.nodeCoords[m[0]]
            val pD = BoardTopology.nodeCoords[m[2]]
            drawLine(
                color = Color(0xFFFFBE0B),
                start = Offset(pt(pC[0]), pt(pC[1])),
                end = Offset(pt(pD[0]), pt(pD[1])),
                strokeWidth = s * 0.024f,
                cap = StrokeCap.Round
            )
        }

        // 6. Shadow Effect Move Trail for the most recently slid pebble
        val lastFrom = state.lastMovedFrom
        val lastTo = state.lastMovedTo
        if (lastFrom != null && lastTo != null && state.board[lastTo] != null) {
            val cFrom = BoardTopology.nodeCoords[lastFrom]
            val cTo = BoardTopology.nodeCoords[lastTo]
            val trailColor = if (state.board[lastTo] == Player.P1) Color(0xFFF59E0B) else Color(0xFFA855F7)
            for (step in 1..3) {
                val frac = step / 4f
                val tx = pt(cFrom[0] + (cTo[0] - cFrom[0]) * frac)
                val ty = pt(cFrom[1] + (cTo[1] - cFrom[1]) * frac)
                drawCircle(
                    color = trailColor.copy(alpha = 0.16f * step),
                    radius = pt(3.2f + step * 0.35f),
                    center = Offset(tx, ty)
                )
            }
        }

        // 7. 24 Stations & Player Pebbles
        val textPaintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = s * 0.033f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = s * 0.033f
            style = Paint.Style.STROKE
            strokeWidth = s * 0.009f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        for (idx in 0 until 24) {
            val c = BoardTopology.nodeCoords[idx]
            val cx = pt(c[0])
            val cy = pt(c[1])
            val isTarget = validTargets.contains(idx)

            // Station node
            drawCircle(
                color = if (isTarget) Color(0xFF10B981) else Color(0xFF0B0F19),
                radius = if (isTarget) pt(3.5f) * pulseRadiusFactor else pt(2.7f),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White,
                radius = if (isTarget) pt(3.5f) * pulseRadiusFactor else pt(2.7f),
                center = Offset(cx, cy),
                style = Stroke(width = pt(0.65f))
            )

            // Player Pebble on Station
            val occupant = state.board[idx]
            if (occupant != null) {
                val isSelected = state.selectedStation == idx
                val isRemovable = isRemovingOpponent && occupant == opponent
                val pebbleRadius = if (isRemovable) pt(4.9f) * (0.95f + (pulseRadiusFactor - 1f) * 0.4f) else pt(4.7f)

                // Drop shadow
                drawCircle(
                    color = Color(0xAA000000),
                    radius = pebbleRadius,
                    center = Offset(cx + pt(0.5f), cy + pt(0.7f))
                )

                // Selection / Removable outer aura
                if (isSelected) {
                    drawCircle(
                        color = Color(0x6600F5D4),
                        radius = pebbleRadius * 1.38f,
                        center = Offset(cx, cy)
                    )
                }

                // Pebble gradient fill
                val pebbleBrush = if (occupant == Player.P1) {
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
                        center = Offset(cx - pebbleRadius * 0.3f, cy - pebbleRadius * 0.3f),
                        radius = pebbleRadius * 1.35f
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFE9D5FF), Color(0xFF9333EA), Color(0xFF581C87)),
                        center = Offset(cx - pebbleRadius * 0.3f, cy - pebbleRadius * 0.3f),
                        radius = pebbleRadius * 1.35f
                    )
                }

                drawCircle(
                    brush = pebbleBrush,
                    radius = pebbleRadius,
                    center = Offset(cx, cy)
                )

                val borderCol = when {
                    isSelected -> Color(0xFF00F5D4)
                    isRemovable -> Color(0xFFFF0055)
                    else -> Color.White
                }
                drawCircle(
                    color = borderCol,
                    radius = pebbleRadius,
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = if (isSelected || isRemovable) pt(1.8f) else pt(1.1f),
                        pathEffect = if (isRemovable) {
                            PathEffect.dashPathEffect(floatArrayOf(pt(1.8f), pt(1.1f)), 0f)
                        } else null
                    )
                )
            }

            // Station label (1..24)
            if (showNumbers) {
                val label = BoardTopology.stationLabels[idx]
                val textY = cy - (textPaintFill.descent() + textPaintFill.ascent()) / 2f
                drawContext.canvas.nativeCanvas.drawText(label, cx, textY, textPaintStroke)
                drawContext.canvas.nativeCanvas.drawText(label, cx, textY, textPaintFill)
            }
        }
    }
}
