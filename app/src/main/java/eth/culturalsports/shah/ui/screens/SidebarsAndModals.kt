package eth.culturalsports.shah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eth.culturalsports.shah.R
import eth.culturalsports.shah.model.AiDifficulty
import eth.culturalsports.shah.model.GameMode
import eth.culturalsports.shah.model.SidebarTab
import eth.culturalsports.shah.ui.components.FederationLogoBadge
import eth.culturalsports.shah.ui.components.ShahBoardLogoWithGlowingPebble
import eth.culturalsports.shah.ui.components.SocialVectorIcon
import eth.culturalsports.shah.ui.theme.LocalShahPalette
import eth.culturalsports.shah.viewmodel.ShahViewModel

@Composable
fun LeftSidebarDrawer(viewModel: ShahViewModel) {
    val palette = LocalShahPalette.current
    val context = LocalContext.current
    val activeTab by viewModel.leftDrawerTab.collectAsState()
    val scrollState = rememberScrollState()

    var issueSubject by remember { mutableStateOf("") }
    var issueBody by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 390.dp)
            .fillMaxWidth(0.90f)
            .background(palette.bgCardElevated)
            .border(1.5.dp, palette.borderGlass)
    ) {
        // 1. Drawer Header with Federation Logo & Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.gold.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                FederationLogoBadge(size = 38.dp)
                Text(
                    text = viewModel.tr(R.string.shah_fed_header_title),
                    color = palette.gold,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.bgGlass)
                    .border(1.dp, palette.borderGlass, RoundedCornerShape(10.dp))
                    .clickable { viewModel.closeDrawers() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✕", color = palette.textMain, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }

        // 2. Functional Share Button inside Sidebar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(palette.gold.copy(alpha = 0.22f), palette.emerald.copy(alpha = 0.22f))
                    )
                )
                .border(1.5.dp, palette.gold, RoundedCornerShape(13.dp))
                .clickable { viewModel.shareApplication(context) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SocialVectorIcon(kind = "share", size = 20.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = viewModel.tr(R.string.shah_share_app_btn),
                color = palette.gold,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black
            )
        }

        // 3. Sidebar Tabs (About Us, Contact Us, Privacy Policy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.bgGlass)
                .border(0.5.dp, palette.borderGlass)
        ) {
            val tabs = listOf(
                SidebarTab.ABOUT to viewModel.tr(R.string.shah_tab_about),
                SidebarTab.CONTACT to viewModel.tr(R.string.shah_tab_contact),
                SidebarTab.PRIVACY to viewModel.tr(R.string.shah_tab_privacy)
            )
            for ((tab, title) in tabs) {
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) palette.gold.copy(alpha = 0.14f) else Color.Transparent)
                        .clickable { viewModel.selectLeftDrawerTab(tab) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) palette.gold else palette.textMuted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 4. Sidebar Body Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (activeTab) {
                SidebarTab.ABOUT -> {
                    // Main Federation Identity Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.bgCard)
                            .border(2.dp, palette.gold, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FederationLogoBadge(size = 86.dp)
                            ShahBoardLogoWithGlowingPebble(size = 78.dp, animateMoveShadow = true)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = viewModel.tr(R.string.shah_fed_header_title),
                            color = palette.gold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = viewModel.tr(R.string.shah_about_sub),
                            color = palette.sky,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = viewModel.tr(R.string.shah_about_intro),
                            color = palette.textBody,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // ራዕይ (Vision)
                    InfoSectionCard(
                        title = viewModel.tr(R.string.shah_vision_title),
                        body = viewModel.tr(R.string.shah_vision_desc),
                        accentColor = palette.gold
                    )

                    // ተልዕኮ (Mission)
                    InfoSectionCard(
                        title = viewModel.tr(R.string.shah_mission_title),
                        body = viewModel.tr(R.string.shah_mission_desc),
                        accentColor = palette.emerald
                    )

                    // እሴቶች (Core Values)
                    InfoSectionCard(
                        title = viewModel.tr(R.string.shah_values_title),
                        body = viewModel.tr(R.string.shah_values_desc),
                        accentColor = palette.sky
                    )
                }

                SidebarTab.CONTACT -> {
                    // 1. የማህበራዊ ሚዲያ ገፆቻችን (Email, Telegram, Facebook ONLY)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.bgCard)
                            .border(1.5.dp, palette.gold.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = viewModel.tr(R.string.shah_social_section_title),
                            color = palette.gold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )

                        SocialLinkRow(
                            iconKind = "email",
                            label = "Email - eth.culturalsportsfed.1977@gmail.com",
                            onClick = {
                                viewModel.openExternalLink(context, "mailto:eth.culturalsportsfed.1977@gmail.com")
                            }
                        )

                        SocialLinkRow(
                            iconKind = "telegram",
                            label = "Telegram - @EthiopianculturalsportFDE",
                            onClick = {
                                viewModel.openExternalLink(context, "https://t.me/EthiopianculturalsportFDE")
                            }
                        )

                        SocialLinkRow(
                            iconKind = "facebook",
                            label = "Facebook - @ECSF",
                            onClick = {
                                viewModel.openExternalLink(context, "https://www.facebook.com/profile.php?id=61558619311072")
                            }
                        )
                    }

                    // 2. አድራሻ (Address)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.bgCard)
                            .border(1.5.dp, palette.emerald.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = viewModel.tr(R.string.shah_address_title),
                            color = palette.emerald,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = viewModel.tr(R.string.shah_address_desc),
                            color = palette.textBody,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 21.sp
                        )
                    }

                    // 3. Direct Issue / Feedback Email Sender to eth.culturalsportsfed.1977@gmail.com
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.bgCard)
                            .border(1.5.dp, palette.sky.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SocialVectorIcon(kind = "email", size = 22.dp)
                            Text(
                                text = viewModel.tr(R.string.shah_report_issue_title),
                                color = palette.sky,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = viewModel.tr(R.string.shah_report_issue_desc),
                            color = palette.textBody,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        OutlinedTextField(
                            value = issueSubject,
                            onValueChange = { issueSubject = it },
                            placeholder = {
                                Text(
                                    text = viewModel.tr(R.string.shah_report_issue_subject_hint),
                                    color = palette.textMuted,
                                    fontSize = 12.5.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.gold,
                                unfocusedBorderColor = palette.borderGlass,
                                focusedTextColor = palette.textMain,
                                unfocusedTextColor = palette.textMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = issueBody,
                            onValueChange = { issueBody = it },
                            placeholder = {
                                Text(
                                    text = viewModel.tr(R.string.shah_report_issue_body_hint),
                                    color = palette.textMuted,
                                    fontSize = 12.5.sp
                                )
                            },
                            minLines = 3,
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.gold,
                                unfocusedBorderColor = palette.borderGlass,
                                focusedTextColor = palette.textMain,
                                unfocusedTextColor = palette.textMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.gold)
                                .clickable {
                                    viewModel.sendIssueEmailToFederation(context, issueSubject, issueBody)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = viewModel.tr(R.string.shah_report_issue_send_btn),
                                color = Color(0xFF090B14),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                SidebarTab.PRIVACY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.bgCard)
                            .border(1.5.dp, palette.sky.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = viewModel.tr(R.string.shah_privacy_title),
                            color = palette.gold,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = viewModel.tr(R.string.shah_privacy_eff_date),
                            color = palette.textMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = viewModel.tr(R.string.shah_priv_h1),
                            color = palette.sky,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = viewModel.tr(R.string.shah_priv_p1),
                            color = palette.textBody,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = viewModel.tr(R.string.shah_priv_h2),
                            color = palette.sky,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = viewModel.tr(R.string.shah_priv_p2).replace("<b>", "").replace("</b>", ""),
                            color = palette.textBody,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = viewModel.tr(R.string.shah_priv_h3),
                            color = palette.sky,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = viewModel.tr(R.string.shah_priv_p3),
                            color = palette.textBody,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = viewModel.tr(R.string.shah_priv_h4),
                            color = palette.sky,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${viewModel.tr(R.string.shah_priv_p4)} eth.culturalsportsfed.1977@gmail.com",
                            color = palette.gold,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    body: String,
    accentColor: Color
) {
    val palette = LocalShahPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.bgCard)
            .border(1.5.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(15.dp)
    ) {
        Text(
            text = title,
            color = accentColor,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            color = palette.textBody,
            fontSize = 13.8.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SocialLinkRow(
    iconKind: String,
    label: String,
    onClick: () -> Unit
) {
    val palette = LocalShahPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.bgGlass)
            .border(1.dp, palette.borderGlass, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SocialVectorIcon(kind = iconKind, size = 22.dp)
        Text(
            text = label,
            color = palette.textMain,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun RightSettingsDrawer(viewModel: ShahViewModel) {
    val palette = LocalShahPalette.current
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()
    val fontPct = 100 + (settings.fontScaleStep * 9)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 380.dp)
            .fillMaxWidth(0.88f)
            .background(palette.bgCardElevated)
            .border(1.5.dp, palette.borderGlass)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.gold.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.tr(R.string.shah_settings_header),
                color = palette.gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.bgGlass)
                    .border(1.dp, palette.borderGlass, RoundedCornerShape(10.dp))
                    .clickable { viewModel.closeDrawers() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✕", color = palette.textMain, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Language Switcher
            SettingsCardBox {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = viewModel.tr(R.string.shah_set_lang_title), color = palette.textMain, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(text = viewModel.tr(R.string.shah_set_lang_sub), color = palette.textMuted, fontSize = 12.sp)
                    }
                    PillActionBtn(
                        text = viewModel.tr(R.string.shah_set_lang_btn),
                        active = true,
                        onClick = { viewModel.openLanguageModal() }
                    )
                }
            }

            // 2. Theme Mode
            SettingsCardBox {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = viewModel.tr(R.string.shah_set_theme_title), color = palette.textMain, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(text = viewModel.tr(R.string.shah_set_theme_sub), color = palette.textMuted, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PillActionBtn(text = "🌙", active = settings.isDarkTheme, onClick = { viewModel.setThemeMode(true) })
                        PillActionBtn(text = "☀️", active = !settings.isDarkTheme, onClick = { viewModel.setThemeMode(false) })
                    }
                }
            }

            // 3. Font Scale
            SettingsCardBox {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${viewModel.tr(R.string.shah_set_font_title)} ($fontPct%)", color = palette.textMain, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(text = viewModel.tr(R.string.shah_set_font_sub), color = palette.textMuted, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PillActionBtn(text = "➖", active = false, onClick = { viewModel.changeFontSize(-1) })
                        PillActionBtn(text = "🔄", active = false, onClick = { viewModel.resetFontSize() })
                        PillActionBtn(text = "➕", active = false, onClick = { viewModel.changeFontSize(1) })
                    }
                }
            }

            // 4. Automatic & Unblock Rule Help
            SettingsCardBox {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = viewModel.tr(R.string.shah_set_rules_help_title), color = palette.textMain, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(text = viewModel.tr(R.string.shah_set_rules_help_sub), color = palette.textMuted, fontSize = 12.sp)
                    }
                    PillActionBtn(text = "❓", active = true, onClick = { viewModel.openHelpModal() })
                }
            }

            // 5. Sound & Vibration
            SettingsCardBox {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = viewModel.tr(R.string.shah_set_sound_title), color = palette.textMain, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(text = viewModel.tr(R.string.shah_set_sound_sub), color = palette.textMuted, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PillActionBtn(
                            text = if (settings.soundEnabled) "🔊" else "🔇",
                            active = settings.soundEnabled,
                            onClick = { viewModel.toggleSound() }
                        )
                        PillActionBtn(
                            text = "📳",
                            active = settings.vibrationEnabled,
                            onClick = { viewModel.toggleVibration() }
                        )
                    }
                }
            }

            // 6. Station Numbers (1-24)
            SettingsCardBox {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = viewModel.tr(R.string.shah_set_num_title), color = palette.textMain, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(text = viewModel.tr(R.string.shah_set_num_sub), color = palette.textMuted, fontSize = 12.sp)
                    }
                    PillActionBtn(
                        text = "#️⃣",
                        active = settings.showStationNumbers,
                        onClick = { viewModel.toggleStationNumbers() }
                    )
                }
            }

            // 7. AI Difficulty Level (Easy, Medium, Hard, Master)
            SettingsCardBox {
                Text(
                    text = viewModel.tr(R.string.shah_set_ai_level_title),
                    color = palette.textMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                DifficultySelectorRow(
                    selected = settings.aiDifficulty,
                    onSelect = { viewModel.setAiDifficulty(it) },
                    viewModel = viewModel
                )
            }

            // 8. Lifetime Match Statistics
            SettingsCardBox {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = viewModel.tr(R.string.shah_set_stats_title), color = palette.textMain, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    PillActionBtn(text = "🗑️", active = false, onClick = { viewModel.resetLifetimeStats() })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBoxItem(modifier = Modifier.weight(1f), label = viewModel.tr(R.string.shah_stat_matches_lbl), value = settings.matchesPlayed.toString(), color = palette.gold)
                    StatBoxItem(modifier = Modifier.weight(1f), label = viewModel.tr(R.string.shah_stat_wins_lbl), value = settings.p1Wins.toString(), color = palette.emerald)
                    StatBoxItem(modifier = Modifier.weight(1f), label = viewModel.tr(R.string.shah_stat_loss_lbl), value = settings.p2Wins.toString(), color = palette.purple)
                }
            }

            // 9. Share App Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.gold.copy(alpha = 0.16f))
                    .border(1.5.dp, palette.gold, RoundedCornerShape(14.dp))
                    .clickable { viewModel.shareApplication(context) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialVectorIcon(kind = "share", size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.tr(R.string.shah_share_app_btn),
                    color = palette.gold,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun SettingsCardBox(content: @Composable () -> Unit) {
    val palette = LocalShahPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.bgGlass)
            .border(1.5.dp, palette.borderGlass, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun PillActionBtn(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalShahPalette.current
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) palette.gold.copy(alpha = 0.22f) else palette.bgCard)
            .border(1.5.dp, if (active) palette.gold else palette.borderGlass, RoundedCornerShape(11.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (active) palette.gold else palette.textMain,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun StatBoxItem(
    modifier: Modifier,
    label: String,
    value: String,
    color: Color
) {
    val palette = LocalShahPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.bgCard)
            .border(1.dp, palette.borderGlass, RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = palette.textMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun DifficultySelectorRow(
    selected: AiDifficulty,
    onSelect: (AiDifficulty) -> Unit,
    viewModel: ShahViewModel
) {
    val palette = LocalShahPalette.current
    val levels = listOf(
        AiDifficulty.EASY to viewModel.tr(R.string.shah_diff_easy),
        AiDifficulty.MEDIUM to viewModel.tr(R.string.shah_diff_medium),
        AiDifficulty.HARD to viewModel.tr(R.string.shah_diff_hard),
        AiDifficulty.MASTER to viewModel.tr(R.string.shah_diff_master)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for ((lvl, label) in levels) {
            val isSel = selected == lvl
            val isMaster = lvl == AiDifficulty.MASTER
            val borderCol = when {
                isSel && isMaster -> palette.gold
                isSel -> palette.emerald
                else -> palette.borderGlass
            }
            val bgCol = when {
                isSel && isMaster -> palette.gold.copy(alpha = 0.24f)
                isSel -> palette.emerald.copy(alpha = 0.22f)
                else -> palette.bgGlass
            }
            val txtCol = when {
                isSel && isMaster -> palette.gold
                isSel -> palette.emerald
                else -> palette.textMuted
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgCol)
                    .border(1.5.dp, borderCol, RoundedCornerShape(10.dp))
                    .clickable { onSelect(lvl) }
                    .padding(vertical = 9.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = txtCol,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ShahModalsOverlay(viewModel: ShahViewModel) {
    val palette = LocalShahPalette.current
    val settings by viewModel.settings.collectAsState()
    val showLang by viewModel.showLanguageModal.collectAsState()
    val showStarter by viewModel.showStarterModal.collectAsState()
    val pendingMode by viewModel.pendingGameMode.collectAsState()
    val showHelp by viewModel.showHelpModal.collectAsState()
    val winnerModal by viewModel.winnerModal.collectAsState()

    // 1. Language Selection Modal
    if (showLang) {
        ModalBackdropBox(onDismiss = { viewModel.closeLanguageModal() }) {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FederationLogoBadge(size = 60.dp)
                    ShahBoardLogoWithGlowingPebble(size = 60.dp, animateMoveShadow = false)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = viewModel.tr(R.string.shah_lang_modal_title),
                    color = palette.gold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = viewModel.tr(R.string.shah_lang_modal_sub),
                    color = palette.textBody,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val langs = viewModel.supportedLanguages
                    for (lang in langs) {
                        val isSelected = settings.languageCode == lang.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (isSelected) palette.gold.copy(alpha = 0.22f) else palette.bgGlass)
                                .border(
                                    1.8.dp,
                                    if (isSelected) palette.gold else palette.borderGlass,
                                    RoundedCornerShape(13.dp)
                                )
                                .clickable { viewModel.selectLanguage(lang.code) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = lang.flagEmoji, fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lang.nativeName,
                                    color = palette.textMain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = lang.regionName,
                                    color = palette.textMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (isSelected) {
                                Text(text = "✅", fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.gold)
                        .clickable { viewModel.closeLanguageModal() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.tr(R.string.shah_lang_continue_btn),
                        color = Color(0xFF090B14),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }

    // 2. Starter & Difficulty Selection Modal
    if (showStarter) {
        ModalBackdropBox(onDismiss = { viewModel.closeStarterModal() }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShahBoardLogoWithGlowingPebble(size = 62.dp, animateMoveShadow = true)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = viewModel.tr(R.string.shah_starter_modal_title),
                    color = palette.gold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = viewModel.tr(R.string.shah_starter_modal_desc),
                    color = palette.textBody,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center
                )

                if (pendingMode == GameMode.AI) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = viewModel.tr(R.string.shah_ai_level_label),
                        color = palette.gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DifficultySelectorRow(
                        selected = settings.aiDifficulty,
                        onSelect = { viewModel.setAiDifficulty(it) },
                        viewModel = viewModel
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.gold)
                            .clickable { viewModel.startMatchWithStarter("p1") }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.tr(R.string.shah_btn_start_p1),
                            color = Color(0xFF090B14),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.bgGlass)
                            .border(1.5.dp, palette.borderGlass, RoundedCornerShape(14.dp))
                            .clickable { viewModel.startMatchWithStarter("p2") }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (pendingMode == GameMode.AI) viewModel.tr(R.string.shah_btn_start_ai) else viewModel.tr(R.string.shah_btn_start_p2),
                            color = palette.textMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.bgGlass)
                            .border(1.5.dp, palette.gold, RoundedCornerShape(14.dp))
                            .clickable { viewModel.startMatchWithStarter("random") }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.tr(R.string.shah_btn_start_random),
                            color = palette.gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    // 3. Winner / Round / Quiz Certificate Modal (Uses ShahBoardLogoWithGlowingPebble instead of Crown!)
    winnerModal?.let { modal ->
        ModalBackdropBox(onDismiss = { viewModel.closeWinnerModal() }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShahBoardLogoWithGlowingPebble(size = 68.dp, animateMoveShadow = true)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = modal.title,
                    color = palette.gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = modal.message,
                    color = palette.textBody,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.gold)
                            .clickable { viewModel.onWinnerModalPrimaryAction() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = modal.primaryButtonText,
                            color = Color(0xFF090B14),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.bgGlass)
                            .border(1.5.dp, palette.borderGlass, RoundedCornerShape(14.dp))
                            .clickable { viewModel.closeWinnerModal() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.tr(R.string.shah_btn_view_board),
                            color = palette.textMain,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    // 4. Automatic & 2-Step Unblock Rule Help Modal
    if (showHelp) {
        ModalBackdropBox(onDismiss = { viewModel.closeHelpModal() }) {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShahBoardLogoWithGlowingPebble(size = 56.dp, animateMoveShadow = false)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = viewModel.tr(R.string.shah_help_modal_title),
                    color = palette.gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = viewModel.tr(R.string.shah_help_modal_body).replace("<b>", "").replace("</b>", ""),
                    color = palette.textBody,
                    fontSize = 13.8.sp,
                    lineHeight = 21.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.gold)
                        .clickable { viewModel.closeHelpModal() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.tr(R.string.shah_help_close_btn),
                        color = Color(0xFF090B14),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ModalBackdropBox(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val palette = LocalShahPalette.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xC8000000))
            .clickable { onDismiss() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 470.dp)
                .heightIn(max = 640.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(palette.bgCardElevated)
                .border(2.5.dp, palette.gold, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            content()
        }
    }
}
