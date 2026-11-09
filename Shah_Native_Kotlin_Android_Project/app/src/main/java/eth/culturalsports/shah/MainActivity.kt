package eth.culturalsports.shah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eth.culturalsports.shah.model.AppScreen
import eth.culturalsports.shah.ui.components.ShahBoardLogoWithGlowingPebble
import eth.culturalsports.shah.ui.screens.GameScreen
import eth.culturalsports.shah.ui.screens.HomeScreen
import eth.culturalsports.shah.ui.screens.LeftSidebarDrawer
import eth.culturalsports.shah.ui.screens.QuizScreen
import eth.culturalsports.shah.ui.screens.RightSettingsDrawer
import eth.culturalsports.shah.ui.screens.RulesScreen
import eth.culturalsports.shah.ui.screens.ShahModalsOverlay
import eth.culturalsports.shah.ui.screens.SplashScreen
import eth.culturalsports.shah.ui.theme.LocalShahPalette
import eth.culturalsports.shah.ui.theme.ShahTheme
import eth.culturalsports.shah.viewmodel.ShahViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ShahViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!viewModel.navigateBack()) {
                        finish()
                    }
                }
            }
        )

        setContent {
            val settings by viewModel.settings.collectAsState()
            val isRtl = settings.languageCode == "ar"

            ShahTheme(
                isDarkTheme = settings.isDarkTheme,
                fontScaleStep = settings.fontScaleStep,
                isRtl = isRtl
            ) {
                ShahRootScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ShahRootScaffold(viewModel: ShahViewModel) {
    val palette = LocalShahPalette.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val leftOpen by viewModel.leftDrawerOpen.collectAsState()
    val rightOpen by viewModel.rightDrawerOpen.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    if (currentScreen == AppScreen.SPLASH) {
        SplashScreen(viewModel = viewModel)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bgMain)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top App Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(palette.headerBg)
                    .border(0.5.dp, palette.borderGlass)
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    HeaderIconBtn(text = "☰", onClick = { viewModel.toggleLeftDrawer() })
                    if (currentScreen != AppScreen.HOME) {
                        HeaderIconBtn(text = "⬅️", onClick = { viewModel.navigateBack() })
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(AppScreen.HOME) }
                            .padding(horizontal = 4.dp)
                    ) {
                        ShahBoardLogoWithGlowingPebble(size = 34.dp, animateMoveShadow = false)
                        Text(
                            text = viewModel.tr(R.string.shah_brand_title),
                            color = palette.gold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(palette.crimson, Color(0xFFFF6B6B))
                                    )
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.gold.copy(alpha = 0.16f))
                            .border(1.5.dp, palette.gold, RoundedCornerShape(10.dp))
                            .clickable { viewModel.openLanguageModal() }
                            .padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(text = "🌐", fontSize = 13.sp)
                        Text(
                            text = viewModel.tr(R.string.shah_lang_name),
                            color = palette.gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }

                    HeaderIconBtn(
                        text = if (settings.soundEnabled) "🔊" else "🔇",
                        onClick = { viewModel.toggleSound() }
                    )
                    HeaderIconBtn(
                        text = "⚙️",
                        onClick = { viewModel.toggleRightDrawer() }
                    )
                }
            }

            // 2. Active Screen Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentScreen) {
                    AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                    AppScreen.GAME -> GameScreen(viewModel = viewModel)
                    AppScreen.RULES -> RulesScreen(viewModel = viewModel)
                    AppScreen.QUIZ -> QuizScreen(viewModel = viewModel)
                    AppScreen.SPLASH -> Unit
                }
            }

            // 3. Bottom Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(palette.headerBg)
                    .border(0.5.dp, palette.borderGlass),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    Triple(AppScreen.HOME, "🏠", viewModel.tr(R.string.shah_nav_home)),
                    Triple(AppScreen.GAME, "BOARD_LOGO", viewModel.tr(R.string.shah_nav_game)),
                    Triple(AppScreen.RULES, "📜", viewModel.tr(R.string.shah_nav_rules)),
                    Triple(AppScreen.QUIZ, "🎓", viewModel.tr(R.string.shah_nav_quiz))
                )
                for ((screen, iconTag, label) in navItems) {
                    val isActive = currentScreen == screen
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { viewModel.navigateTo(screen) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (iconTag == "BOARD_LOGO") {
                            ShahBoardLogoWithGlowingPebble(size = 22.dp, animateMoveShadow = false)
                        } else {
                            Text(text = iconTag, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            color = if (isActive) palette.gold else palette.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Drawer Backdrop
        AnimatedVisibility(
            visible = leftOpen || rightOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAD000000))
                    .clickable { viewModel.closeDrawers() }
            )
        }

        // 5. Left Drawer (About Us, Contact Us, Privacy Policy, Share App)
        AnimatedVisibility(
            visible = leftOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            LeftSidebarDrawer(viewModel = viewModel)
        }

        // 6. Right Drawer (Settings & Tools)
        AnimatedVisibility(
            visible = rightOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            RightSettingsDrawer(viewModel = viewModel)
        }

        // 7. Modals Overlay
        ShahModalsOverlay(viewModel = viewModel)

        // 8. Toast Notification Pill
        toastMsg?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 78.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xF50F172A))
                    .border(2.dp, palette.gold, RoundedCornerShape(99.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HeaderIconBtn(
    text: String,
    onClick: () -> Unit
) {
    val palette = LocalShahPalette.current
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.bgGlass)
            .border(1.5.dp, palette.borderGlass, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = palette.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
