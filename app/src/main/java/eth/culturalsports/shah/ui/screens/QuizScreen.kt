package eth.culturalsports.shah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eth.culturalsports.shah.R
import eth.culturalsports.shah.model.AppScreen
import eth.culturalsports.shah.ui.components.ShahBoardLogoWithGlowingPebble
import eth.culturalsports.shah.ui.theme.LocalShahPalette
import eth.culturalsports.shah.viewmodel.ShahViewModel

@Composable
fun QuizScreen(viewModel: ShahViewModel) {
    val palette = LocalShahPalette.current
    val questions by viewModel.quizQuestions.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    val scrollState = rememberScrollState()

    val currentQuestion = questions.getOrNull(quizState.currentIndex)
    val optLetters = viewModel.getOptionLetters()
    val userPicked = quizState.selectedAnswers.getOrNull(quizState.currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
        ) {
            // 1. Quiz Progress Header Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.bgCard)
                    .border(1.5.dp, palette.borderGlass, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        ShahBoardLogoWithGlowingPebble(size = 26.dp, animateMoveShadow = false)
                        Text(
                            text = viewModel.tr(R.string.shah_quiz_header_title),
                            color = palette.gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "${viewModel.tr(R.string.shah_quiz_q_word)} ${quizState.currentIndex + 1}/20 | ${viewModel.tr(R.string.shah_quiz_score_word)} ${quizState.score}",
                        color = palette.emerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (quizState.currentIndex + 1) / 20f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = palette.gold,
                    trackColor = palette.borderGlass
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Question & 4 Options Card
            if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.bgCard)
                        .border(1.5.dp, palette.borderGlass, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(palette.sky.copy(alpha = 0.16f))
                            .padding(horizontal = 11.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentQuestion.category,
                            color = palette.sky,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentQuestion.question,
                        color = palette.textMain,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (optIdx in currentQuestion.options.indices) {
                            val optText = currentQuestion.options[optIdx]
                            val letter = optLetters.getOrElse(optIdx) { ('A' + optIdx).toString() }
                            val isCorrectOpt = optIdx == currentQuestion.correctAnswer
                            val isWrongPicked = userPicked == optIdx && !isCorrectOpt

                            val bgCol = when {
                                userPicked != null && isCorrectOpt -> palette.emerald.copy(alpha = 0.22f)
                                isWrongPicked -> palette.crimson.copy(alpha = 0.22f)
                                else -> palette.bgGlass
                            }
                            val borderCol = when {
                                userPicked != null && isCorrectOpt -> palette.emerald
                                isWrongPicked -> palette.crimson
                                else -> palette.borderGlass
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bgCol)
                                    .border(2.dp, borderCol, RoundedCornerShape(14.dp))
                                    .clickable(enabled = userPicked == null) {
                                        viewModel.selectQuizAnswer(optIdx)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.borderGlass),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter,
                                        color = palette.gold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optText,
                                    color = palette.textMain,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Explanation Box
                    if (userPicked != null) {
                        val isCorrect = userPicked == currentQuestion.correctAnswer
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isCorrect) palette.emerald.copy(alpha = 0.16f)
                                    else palette.crimson.copy(alpha = 0.16f)
                                )
                                .border(
                                    1.5.dp,
                                    if (isCorrect) palette.emerald else palette.crimson,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Text(
                                text = if (isCorrect) viewModel.tr(R.string.shah_quiz_correct) else viewModel.tr(R.string.shah_quiz_wrong),
                                color = if (isCorrect) palette.emerald else palette.crimson,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuestion.explanation,
                                color = palette.textBody,
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Quiz Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.bgCard)
                        .border(1.5.dp, palette.borderGlass, RoundedCornerShape(12.dp))
                        .clickable(enabled = quizState.currentIndex > 0) { viewModel.prevQuizQuestion() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.tr(R.string.shah_quiz_prev_btn),
                        color = if (quizState.currentIndex > 0) palette.textMain else palette.textMuted.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.crimson)
                        .clickable { viewModel.nextQuizQuestion() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (quizState.currentIndex == 19) viewModel.tr(R.string.shah_quiz_see_result_btn) else viewModel.tr(R.string.shah_quiz_next_btn),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.bgCard)
                        .border(1.5.dp, palette.borderGlass, RoundedCornerShape(12.dp))
                        .clickable { viewModel.restartQuiz() }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.tr(R.string.shah_quiz_restart_btn),
                        color = palette.textMain,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.crimson.copy(alpha = 0.14f))
                        .border(1.5.dp, palette.crimson.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(AppScreen.HOME) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.tr(R.string.shah_btn_exit_home),
                        color = palette.crimson,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
