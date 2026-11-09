package eth.culturalsports.shah.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eth.culturalsports.shah.R
import eth.culturalsports.shah.ai.ShahMasterAiEngine
import eth.culturalsports.shah.data.ShahRepository
import eth.culturalsports.shah.model.AiDifficulty
import eth.culturalsports.shah.model.AppScreen
import eth.culturalsports.shah.model.BoardTopology
import eth.culturalsports.shah.model.GameMode
import eth.culturalsports.shah.model.GamePhase
import eth.culturalsports.shah.model.GameSnapshot
import eth.culturalsports.shah.model.GameState
import eth.culturalsports.shah.model.LanguageOption
import eth.culturalsports.shah.model.Player
import eth.culturalsports.shah.model.QuizQuestion
import eth.culturalsports.shah.model.QuizState
import eth.culturalsports.shah.model.RuleArticle
import eth.culturalsports.shah.model.SidebarTab
import eth.culturalsports.shah.model.UserSettings
import eth.culturalsports.shah.model.WinnerModalData
import eth.culturalsports.shah.sound.ShahSoundAndHaptics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ShahViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShahRepository(application.applicationContext)
    private val soundEngine = ShahSoundAndHaptics(application.applicationContext)
    private val aiEngine = ShahMasterAiEngine()

    private val _settings = MutableStateFlow(repository.loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val screenBackStack = ArrayDeque<AppScreen>().apply { add(AppScreen.HOME) }

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _rulesList = MutableStateFlow<List<RuleArticle>>(emptyList())
    val rulesList: StateFlow<List<RuleArticle>> = _rulesList.asStateFlow()

    private val _selectedRuleCategory = MutableStateFlow("all")
    val selectedRuleCategory: StateFlow<String> = _selectedRuleCategory.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _showLanguageModal = MutableStateFlow(false)
    val showLanguageModal: StateFlow<Boolean> = _showLanguageModal.asStateFlow()

    private val _showStarterModal = MutableStateFlow(false)
    val showStarterModal: StateFlow<Boolean> = _showStarterModal.asStateFlow()

    private val _pendingGameMode = MutableStateFlow(GameMode.AI)
    val pendingGameMode: StateFlow<GameMode> = _pendingGameMode.asStateFlow()

    private val _showHelpModal = MutableStateFlow(false)
    val showHelpModal: StateFlow<Boolean> = _showHelpModal.asStateFlow()

    private val _winnerModal = MutableStateFlow<WinnerModalData?>(null)
    val winnerModal: StateFlow<WinnerModalData?> = _winnerModal.asStateFlow()

    private val _leftDrawerOpen = MutableStateFlow(false)
    val leftDrawerOpen: StateFlow<Boolean> = _leftDrawerOpen.asStateFlow()

    private val _leftDrawerTab = MutableStateFlow(SidebarTab.ABOUT)
    val leftDrawerTab: StateFlow<SidebarTab> = _leftDrawerTab.asStateFlow()

    private val _rightDrawerOpen = MutableStateFlow(false)
    val rightDrawerOpen: StateFlow<Boolean> = _rightDrawerOpen.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val undoStack = ArrayDeque<GameSnapshot>()
    private val redoStack = ArrayDeque<GameSnapshot>()

    private var turnTimerJob: Job? = null
    private var aiJob: Job? = null
    private var toastJob: Job? = null

    val supportedLanguages: List<LanguageOption> = repository.supportedLanguages

    init {
        reloadLocalizedContent(_settings.value.languageCode)
        // Fast < 2 seconds splash transition (1.35s) -> Home Screen + Language Modal
        viewModelScope.launch {
            delay(1350L)
            _currentScreen.value = AppScreen.HOME
            _showLanguageModal.value = true
        }
    }

    fun tr(resId: Int, vars: Map<String, String> = emptyMap()): String {
        return repository.getString(_settings.value.languageCode, resId, vars)
    }

    fun getOptionLetters(): List<String> {
        return repository.getOptionLetters(_settings.value.languageCode)
    }

    fun getPlayerTitle(player: Player, mode: GameMode = _gameState.value.mode): String {
        return if (player == Player.P1) {
            tr(R.string.shah_p1_name)
        } else {
            if (mode == GameMode.AI) tr(R.string.shah_p2_name_ai) else tr(R.string.shah_p2_name_human)
        }
    }

    private fun reloadLocalizedContent(langCode: String) {
        _rulesList.value = repository.getRules(langCode)
        _quizQuestions.value = repository.getQuizQuestions(langCode)
        refreshBannerForCurrentPhase()
    }

    fun selectLanguage(langCode: String) {
        val updated = _settings.value.copy(languageCode = langCode)
        _settings.value = updated
        repository.saveSettings(updated)
        reloadLocalizedContent(langCode)
        playSound("place")
        _showLanguageModal.value = false
    }

    fun openLanguageModal() {
        closeDrawers()
        _showLanguageModal.value = true
    }

    fun closeLanguageModal() {
        _showLanguageModal.value = false
    }

    fun setThemeMode(isDark: Boolean) {
        val updated = _settings.value.copy(isDarkTheme = isDark)
        _settings.value = updated
        repository.saveSettings(updated)
        showToast(if (isDark) tr(R.string.shah_toast_dark_on) else tr(R.string.shah_toast_light_on))
    }

    fun changeFontSize(delta: Int) {
        val nextStep = (_settings.value.fontScaleStep + delta).coerceIn(-2, 4)
        val updated = _settings.value.copy(fontScaleStep = nextStep)
        _settings.value = updated
        repository.saveSettings(updated)
    }

    fun resetFontSize() {
        val updated = _settings.value.copy(fontScaleStep = 0)
        _settings.value = updated
        repository.saveSettings(updated)
        showToast(tr(R.string.shah_toast_font_reset))
    }

    fun toggleSound() {
        val next = !_settings.value.soundEnabled
        val updated = _settings.value.copy(soundEnabled = next)
        _settings.value = updated
        repository.saveSettings(updated)
        showToast(if (next) tr(R.string.shah_toast_sound_on) else tr(R.string.shah_toast_sound_off))
    }

    fun toggleVibration() {
        val next = !_settings.value.vibrationEnabled
        val updated = _settings.value.copy(vibrationEnabled = next)
        _settings.value = updated
        repository.saveSettings(updated)
        if (next) playSound("place")
        showToast(if (next) tr(R.string.shah_toast_vib_on) else tr(R.string.shah_toast_vib_off))
    }

    fun toggleStationNumbers() {
        val next = !_settings.value.showStationNumbers
        val updated = _settings.value.copy(showStationNumbers = next)
        _settings.value = updated
        repository.saveSettings(updated)
        showToast(if (next) tr(R.string.shah_toast_num_on) else tr(R.string.shah_toast_num_off))
    }

    fun setAiDifficulty(difficulty: AiDifficulty) {
        val updated = _settings.value.copy(aiDifficulty = difficulty)
        _settings.value = updated
        repository.saveSettings(updated)
    }

    fun resetLifetimeStats() {
        val updated = _settings.value.copy(matchesPlayed = 0, p1Wins = 0, p2Wins = 0)
        _settings.value = updated
        repository.saveSettings(updated)
        showToast(tr(R.string.shah_toast_stats_reset))
    }

    fun toggleLeftDrawer(tab: SidebarTab? = null) {
        _rightDrawerOpen.value = false
        if (tab != null) {
            _leftDrawerTab.value = tab
            _leftDrawerOpen.value = true
        } else {
            _leftDrawerOpen.value = !_leftDrawerOpen.value
        }
    }

    fun selectLeftDrawerTab(tab: SidebarTab) {
        _leftDrawerTab.value = tab
    }

    fun toggleRightDrawer() {
        _leftDrawerOpen.value = false
        _rightDrawerOpen.value = !_rightDrawerOpen.value
    }

    fun closeDrawers() {
        _leftDrawerOpen.value = false
        _rightDrawerOpen.value = false
    }

    fun openHelpModal() {
        closeDrawers()
        _showHelpModal.value = true
    }

    fun closeHelpModal() {
        _showHelpModal.value = false
    }

    fun navigateTo(screen: AppScreen) {
        closeDrawers()
        if (_currentScreen.value != screen) {
            screenBackStack.addLast(screen)
            _currentScreen.value = screen
        }
        if (screen != AppScreen.GAME) {
            turnTimerJob?.cancel()
        } else if (_gameState.value.phase != GamePhase.GAMEOVER && !_gameState.value.isPaused) {
            startTurnTimer(resetSeconds = false)
        }
    }

    fun navigateBack(): Boolean {
        if (_leftDrawerOpen.value || _rightDrawerOpen.value) {
            closeDrawers()
            return true
        }
        if (screenBackStack.size > 1) {
            screenBackStack.removeLast()
            val prev = screenBackStack.last()
            _currentScreen.value = prev
            if (prev != AppScreen.GAME) {
                turnTimerJob?.cancel()
            }
            return true
        }
        if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            turnTimerJob?.cancel()
            return true
        }
        return false
    }

    fun openStarterModal(mode: GameMode) {
        _pendingGameMode.value = mode
        _showStarterModal.value = true
    }

    fun closeStarterModal() {
        _showStarterModal.value = false
    }

    fun startMatchWithStarter(choice: String) {
        _showStarterModal.value = false
        val mode = _pendingGameMode.value
        val starter: Player = when (choice) {
            "p1" -> Player.P1
            "p2" -> Player.P2
            else -> {
                val chosen = if (Random.nextBoolean()) Player.P1 else Player.P2
                showToast(tr(R.string.shah_toast_random_lot, mapOf("starter" to getPlayerTitle(chosen, mode))))
                chosen
            }
        }

        undoStack.clear()
        redoStack.clear()
        aiJob?.cancel()

        val currentRound = _gameState.value.matchRound
        val s1 = _gameState.value.matchScoreP1
        val s2 = _gameState.value.matchScoreP2

        _gameState.value = GameState(
            mode = mode,
            board = List(24) { null },
            p1Inventory = 12,
            p2Inventory = 12,
            p1OnBoard = 0,
            p2OnBoard = 0,
            p1Eaten = 0,
            p2Eaten = 0,
            p1Fouls = 0,
            p2Fouls = 0,
            isPaused = false,
            turn = starter,
            firstTurn = starter,
            phase = GamePhase.PLACEMENT,
            selectedStation = null,
            lastMovedFrom = null,
            lastMovedTo = null,
            millFirst = null,
            firstPicker = null,
            unblockStep = 0,
            highlightedMill1 = null,
            highlightedMill2 = null,
            moveHistory = emptyList(),
            bannerMessage = tr(R.string.shah_banner_place_start, mapOf("player" to getPlayerTitle(starter, mode))),
            bannerIsWarning = false,
            turnSecondsLeft = 30,
            matchRound = currentRound,
            matchScoreP1 = s1,
            matchScoreP2 = s2,
            canUndo = false,
            canRedo = false
        )

        navigateTo(AppScreen.GAME)
        startTurnTimer(resetSeconds = true)
        triggerAiIfNeeded()
    }

    fun togglePauseGame() {
        val st = _gameState.value
        if (st.phase == GamePhase.GAMEOVER) return
        val nextPaused = !st.isPaused
        if (nextPaused) {
            turnTimerJob?.cancel()
            aiJob?.cancel()
            _gameState.update {
                it.copy(
                    isPaused = true,
                    bannerMessage = tr(R.string.shah_banner_paused),
                    bannerIsWarning = true
                )
            }
            showToast(tr(R.string.shah_toast_paused))
        } else {
            _gameState.update { it.copy(isPaused = false) }
            refreshBannerForCurrentPhase()
            showToast(tr(R.string.shah_toast_resumed))
            startTurnTimer(resetSeconds = false)
            triggerAiIfNeeded()
        }
    }

    private fun startTurnTimer(resetSeconds: Boolean) {
        turnTimerJob?.cancel()
        val st = _gameState.value
        if (st.phase == GamePhase.GAMEOVER) return
        if (resetSeconds) {
            _gameState.update { it.copy(turnSecondsLeft = 30) }
        }
        if (_gameState.value.isPaused) return

        turnTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val curr = _gameState.value
                if (curr.phase == GamePhase.GAMEOVER || curr.isPaused) break
                val nextSec = curr.turnSecondsLeft - 1
                _gameState.update { it.copy(turnSecondsLeft = nextSec.coerceAtLeast(0)) }

                // 5-second countdown Warning Siren (5s, 4s, 3s, 2s, 1s)
                if (nextSec in 1..5) {
                    playSound("siren")
                }

                if (nextSec <= 0) {
                    handleTurnTimeout()
                    break
                }
            }
        }
    }

    /**
     * Enforces Shah 30-second Timer & Foul/Card Rules (Article 4.21 & 6.1–6.2):
     * - Does NOT snatch or auto-move the player's pebble!
     * - Records 1 foul against the active player, restarts a fresh 30-second timer, and waits for the same player to play.
     * - 4th foul = Yellow Card (🟨) warning.
     * - 5th foul = Red Card (🟥) immediate round disqualification.
     */
    private fun handleTurnTimeout() {
        val st = _gameState.value
        if (st.phase == GamePhase.GAMEOVER || st.isPaused) return

        val offender = st.turn
        val opponent = offender.opponent()
        val newFouls = if (offender == Player.P1) st.p1Fouls + 1 else st.p2Fouls + 1

        _gameState.update {
            if (offender == Player.P1) it.copy(p1Fouls = newFouls)
            else it.copy(p2Fouls = newFouls)
        }

        val offenderTitle = getPlayerTitle(offender)
        if (newFouls >= 5) {
            playSound("redcard")
            showToast(tr(R.string.shah_toast_red_card, mapOf("player" to offenderTitle)))
            declareRoundWinner(
                winner = opponent,
                reasonText = tr(
                    R.string.shah_win_by_red_card,
                    mapOf(
                        "offender" to offenderTitle,
                        "winner" to getPlayerTitle(opponent)
                    )
                )
            )
            return
        }

        if (newFouls == 4) {
            playSound("yellowcard")
            showToast(tr(R.string.shah_toast_yellow_card, mapOf("player" to offenderTitle)))
            _gameState.update {
                it.copy(
                    bannerMessage = tr(R.string.shah_banner_yellow_card, mapOf("player" to offenderTitle)),
                    bannerIsWarning = true
                )
            }
        } else {
            playSound("foul")
            showToast(
                tr(
                    R.string.shah_toast_timeout_foul,
                    mapOf("player" to offenderTitle, "fouls" to newFouls.toString())
                )
            )
            _gameState.update {
                it.copy(
                    bannerMessage = tr(
                        R.string.shah_banner_timeout_foul,
                        mapOf("player" to offenderTitle, "fouls" to newFouls.toString())
                    ),
                    bannerIsWarning = true
                )
            }
        }

        // Start a new 30-second countdown for the same player
        startTurnTimer(resetSeconds = true)
        triggerAiIfNeeded()
    }

    fun onStationTapped(stationIndex: Int) {
        val st = _gameState.value
        if (st.phase == GamePhase.GAMEOVER) return
        if (st.isPaused) {
            playSound("error")
            showToast(tr(R.string.shah_toast_paused))
            return
        }
        if (st.mode == GameMode.AI && st.turn == Player.P2) return
        processMove(stationIndex)
    }

    private fun processMove(i: Int) {
        val st = _gameState.value
        if (st.phase == GamePhase.GAMEOVER || st.isPaused) return

        val player = st.turn
        val opponent = player.opponent()
        val preMoveSnapshot = st.toSnapshot()
        val boardArr = st.board.toTypedArray()

        when (st.phase) {
            GamePhase.PLACEMENT -> {
                if (boardArr[i] != null) {
                    playSound("error")
                    showToast(tr(R.string.shah_err_station_occupied))
                    return
                }
                boardArr[i] = player
                val nextP1Inv = if (player == Player.P1) st.p1Inventory - 1 else st.p1Inventory
                val nextP2Inv = if (player == Player.P2) st.p2Inventory - 1 else st.p2Inventory
                val nextP1Board = if (player == Player.P1) st.p1OnBoard + 1 else st.p1OnBoard
                val nextP2Board = if (player == Player.P2) st.p2OnBoard + 1 else st.p2OnBoard

                playSound("place")
                val formedMills = BoardTopology.getMillsAt(i, player, boardArr)
                var nextMillFirst = st.millFirst
                val m1 = formedMills.firstOrNull()
                if (m1 != null && nextMillFirst == null) {
                    nextMillFirst = player
                    showToast(tr(R.string.shah_toast_first_mill, mapOf("player" to getPlayerTitle(player))))
                }

                if (nextP1Inv == 0 && nextP2Inv == 0) {
                    val starterAfterPlace = nextMillFirst ?: st.firstTurn.opponent()
                    commitStateAfterMove(
                        preMoveSnapshot = preMoveSnapshot,
                        newState = st.copy(
                            board = boardArr.toList(),
                            p1Inventory = 0,
                            p2Inventory = 0,
                            p1OnBoard = nextP1Board,
                            p2OnBoard = nextP2Board,
                            millFirst = nextMillFirst,
                            firstPicker = starterAfterPlace,
                            turn = starterAfterPlace,
                            phase = GamePhase.INITIAL_REMOVE_1,
                            highlightedMill1 = m1,
                            highlightedMill2 = null,
                            bannerMessage = tr(
                                R.string.shah_banner_initial_remove1,
                                mapOf("player" to getPlayerTitle(starterAfterPlace))
                            ),
                            bannerIsWarning = true
                        )
                    )
                } else {
                    commitStateAfterMove(
                        preMoveSnapshot = preMoveSnapshot,
                        newState = st.copy(
                            board = boardArr.toList(),
                            p1Inventory = nextP1Inv,
                            p2Inventory = nextP2Inv,
                            p1OnBoard = nextP1Board,
                            p2OnBoard = nextP2Board,
                            millFirst = nextMillFirst,
                            turn = opponent,
                            highlightedMill1 = m1,
                            highlightedMill2 = null,
                            bannerMessage = tr(
                                R.string.shah_banner_place_turn,
                                mapOf("player" to getPlayerTitle(opponent))
                            ),
                            bannerIsWarning = false
                        )
                    )
                }
            }

            GamePhase.INITIAL_REMOVE_1, GamePhase.INITIAL_REMOVE_2 -> {
                if (boardArr[i] != opponent) {
                    playSound("error")
                    showToast(tr(R.string.shah_err_pick_opponent))
                    return
                }
                boardArr[i] = null
                val nextP1Board = if (opponent == Player.P1) st.p1OnBoard - 1 else st.p1OnBoard
                val nextP2Board = if (opponent == Player.P2) st.p2OnBoard - 1 else st.p2OnBoard
                val nextP1Eaten = if (opponent == Player.P2) st.p1Eaten + 1 else st.p1Eaten
                val nextP2Eaten = if (opponent == Player.P1) st.p2Eaten + 1 else st.p2Eaten

                playSound("eat")

                if (st.phase == GamePhase.INITIAL_REMOVE_1) {
                    commitStateAfterMove(
                        preMoveSnapshot = preMoveSnapshot,
                        newState = st.copy(
                            board = boardArr.toList(),
                            p1OnBoard = nextP1Board,
                            p2OnBoard = nextP2Board,
                            p1Eaten = nextP1Eaten,
                            p2Eaten = nextP2Eaten,
                            phase = GamePhase.INITIAL_REMOVE_2,
                            turn = opponent,
                            highlightedMill1 = null,
                            highlightedMill2 = null,
                            bannerMessage = tr(
                                R.string.shah_banner_initial_remove2,
                                mapOf("player" to getPlayerTitle(opponent))
                            ),
                            bannerIsWarning = true
                        )
                    )
                } else {
                    val firstMover = st.firstPicker ?: Player.P1
                    val interimState = st.copy(
                        board = boardArr.toList(),
                        p1OnBoard = nextP1Board,
                        p2OnBoard = nextP2Board,
                        p1Eaten = nextP1Eaten,
                        p2Eaten = nextP2Eaten,
                        phase = GamePhase.PLAYING,
                        turn = firstMover,
                        unblockStep = 0,
                        highlightedMill1 = null,
                        highlightedMill2 = null
                    )
                    commitStateAfterMove(preMoveSnapshot, evaluateStartOfSlidingTurn(interimState))
                }
            }

            GamePhase.PLAYING -> {
                if (boardArr[i] == player) {
                    playSound("place")
                    _gameState.update { it.copy(selectedStation = i) }
                    return
                }
                val fromIdx = st.selectedStation
                if (boardArr[i] == null && fromIdx != null) {
                    if (!BoardTopology.adjacency[fromIdx].contains(i)) {
                        playSound("error")
                        showToast(tr(R.string.shah_err_orthogonal_only))
                        return
                    }
                    boardArr[i] = player
                    boardArr[fromIdx] = null
                    val nextHistory = (st.moveHistory + "${player.name}:$fromIdx->$i").takeLast(16)

                    val autoWin = BoardTopology.evaluateAutomaticWin(i, player, boardArr)
                    if (autoWin.isAutoWin) {
                        playSound("win")
                        val reasonStr = if (autoWin.isDoubleMill) {
                            tr(R.string.shah_auto_reason_double)
                        } else {
                            tr(
                                R.string.shah_auto_reason_shuttle,
                                mapOf(
                                    "a" to BoardTopology.stationLabels[autoWin.shuttleStationA],
                                    "b" to BoardTopology.stationLabels[autoWin.shuttleStationB]
                                )
                            )
                        }
                        _gameState.update {
                            it.copy(
                                board = boardArr.toList(),
                                selectedStation = null,
                                lastMovedFrom = fromIdx,
                                lastMovedTo = i,
                                highlightedMill1 = autoWin.mill1,
                                highlightedMill2 = autoWin.mill2,
                                moveHistory = nextHistory
                            )
                        }
                        declareRoundWinner(
                            winner = player,
                            reasonText = tr(
                                R.string.shah_win_auto_msg,
                                mapOf(
                                    "player" to getPlayerTitle(player),
                                    "from" to BoardTopology.stationLabels[fromIdx],
                                    "to" to BoardTopology.stationLabels[i],
                                    "reason" to reasonStr
                                )
                            )
                        )
                        return
                    }

                    val formedMills = BoardTopology.getMillsAt(i, player, boardArr)
                    if (formedMills.isNotEmpty()) {
                        playSound("mill")
                        commitStateAfterMove(
                            preMoveSnapshot = preMoveSnapshot,
                            newState = st.copy(
                                board = boardArr.toList(),
                                selectedStation = null,
                                lastMovedFrom = fromIdx,
                                lastMovedTo = i,
                                phase = GamePhase.EATING,
                                highlightedMill1 = formedMills[0],
                                highlightedMill2 = null,
                                moveHistory = nextHistory,
                                bannerMessage = tr(
                                    R.string.shah_banner_mill_eat,
                                    mapOf("player" to getPlayerTitle(player))
                                ),
                                bannerIsWarning = true
                            )
                        )
                    } else {
                        playSound("move")
                        val interimState = st.copy(
                            board = boardArr.toList(),
                            selectedStation = null,
                            lastMovedFrom = fromIdx,
                            lastMovedTo = i,
                            turn = opponent,
                            unblockStep = 0,
                            highlightedMill1 = null,
                            highlightedMill2 = null,
                            moveHistory = nextHistory
                        )
                        commitStateAfterMove(preMoveSnapshot, evaluateStartOfSlidingTurn(interimState))
                    }
                }
            }

            GamePhase.EATING -> {
                if (boardArr[i] != opponent) {
                    playSound("error")
                    showToast(tr(R.string.shah_err_select_opponent_eat))
                    return
                }
                boardArr[i] = null
                val nextP1Board = if (opponent == Player.P1) st.p1OnBoard - 1 else st.p1OnBoard
                val nextP2Board = if (opponent == Player.P2) st.p2OnBoard - 1 else st.p2OnBoard
                val nextP1Eaten = if (opponent == Player.P2) st.p1Eaten + 1 else st.p1Eaten
                val nextP2Eaten = if (opponent == Player.P1) st.p2Eaten + 1 else st.p2Eaten
                val oppRem = if (opponent == Player.P1) nextP1Board else nextP2Board

                playSound("eat")
                if (oppRem < 3) {
                    _gameState.update {
                        it.copy(
                            board = boardArr.toList(),
                            p1OnBoard = nextP1Board,
                            p2OnBoard = nextP2Board,
                            p1Eaten = nextP1Eaten,
                            p2Eaten = nextP2Eaten,
                            highlightedMill1 = null,
                            highlightedMill2 = null
                        )
                    }
                    playSound("win")
                    declareRoundWinner(
                        winner = player,
                        reasonText = tr(
                            R.string.shah_win_less_than3,
                            mapOf(
                                "opp" to getPlayerTitle(opponent),
                                "rem" to oppRem.toString(),
                                "player" to getPlayerTitle(player)
                            )
                        )
                    )
                    return
                }

                val interimState = st.copy(
                    board = boardArr.toList(),
                    p1OnBoard = nextP1Board,
                    p2OnBoard = nextP2Board,
                    p1Eaten = nextP1Eaten,
                    p2Eaten = nextP2Eaten,
                    phase = GamePhase.PLAYING,
                    turn = opponent,
                    unblockStep = 0,
                    highlightedMill1 = null,
                    highlightedMill2 = null
                )
                commitStateAfterMove(preMoveSnapshot, evaluateStartOfSlidingTurn(interimState))
            }

            GamePhase.FORCED_UNBLOCK -> {
                if (boardArr[i] != opponent) {
                    playSound("error")
                    showToast(tr(R.string.shah_err_forced_unblock_pick))
                    return
                }
                boardArr[i] = null
                val nextP1Board = if (opponent == Player.P1) st.p1OnBoard - 1 else st.p1OnBoard
                val nextP2Board = if (opponent == Player.P2) st.p2OnBoard - 1 else st.p2OnBoard
                val nextP1Eaten = if (opponent == Player.P2) st.p1Eaten + 1 else st.p1Eaten
                val nextP2Eaten = if (opponent == Player.P1) st.p2Eaten + 1 else st.p2Eaten
                val oppRem = if (opponent == Player.P1) nextP1Board else nextP2Board

                playSound("eat")
                if (oppRem < 3) {
                    _gameState.update {
                        it.copy(
                            board = boardArr.toList(),
                            p1OnBoard = nextP1Board,
                            p2OnBoard = nextP2Board,
                            p1Eaten = nextP1Eaten,
                            p2Eaten = nextP2Eaten
                        )
                    }
                    playSound("win")
                    declareRoundWinner(
                        winner = player,
                        reasonText = tr(
                            R.string.shah_win_unblock_less_than3,
                            mapOf(
                                "opp" to getPlayerTitle(opponent),
                                "player" to getPlayerTitle(player)
                            )
                        )
                    )
                    return
                }

                if (BoardTopology.hasValidMoves(opponent, boardArr)) {
                    commitStateAfterMove(
                        preMoveSnapshot = preMoveSnapshot,
                        newState = st.copy(
                            board = boardArr.toList(),
                            p1OnBoard = nextP1Board,
                            p2OnBoard = nextP2Board,
                            p1Eaten = nextP1Eaten,
                            p2Eaten = nextP2Eaten,
                            phase = GamePhase.PLAYING,
                            turn = opponent,
                            unblockStep = 0,
                            bannerMessage = tr(
                                R.string.shah_banner_path_opened,
                                mapOf("opp" to getPlayerTitle(opponent))
                            ),
                            bannerIsWarning = false
                        )
                    )
                } else {
                    if (st.unblockStep < 2) {
                        showToast(tr(R.string.shah_toast_still_blocked2))
                        commitStateAfterMove(
                            preMoveSnapshot = preMoveSnapshot,
                            newState = st.copy(
                                board = boardArr.toList(),
                                p1OnBoard = nextP1Board,
                                p2OnBoard = nextP2Board,
                                p1Eaten = nextP1Eaten,
                                p2Eaten = nextP2Eaten,
                                phase = GamePhase.FORCED_UNBLOCK,
                                turn = player,
                                unblockStep = 2,
                                bannerMessage = tr(
                                    R.string.shah_banner_still_blocked2,
                                    mapOf(
                                        "opp" to getPlayerTitle(opponent),
                                        "player" to getPlayerTitle(player)
                                    )
                                ),
                                bannerIsWarning = true
                            )
                        )
                    } else {
                        _gameState.update {
                            it.copy(
                                board = boardArr.toList(),
                                p1OnBoard = nextP1Board,
                                p2OnBoard = nextP2Board,
                                p1Eaten = nextP1Eaten,
                                p2Eaten = nextP2Eaten
                            )
                        }
                        playSound("win")
                        declareRoundWinner(
                            winner = player,
                            reasonText = tr(
                                R.string.shah_win_blocked_after2,
                                mapOf(
                                    "opp" to getPlayerTitle(opponent),
                                    "player" to getPlayerTitle(player)
                                )
                            )
                        )
                    }
                }
            }

            GamePhase.GAMEOVER -> Unit
        }
    }

    private fun evaluateStartOfSlidingTurn(state: GameState): GameState {
        val activePlayer = state.turn
        val blocker = activePlayer.opponent()
        val blockerEaten = if (blocker == Player.P1) state.p1Eaten else state.p2Eaten
        val boardArr = state.board.toTypedArray()

        if (!BoardTopology.hasValidMoves(activePlayer, boardArr)) {
            if (blockerEaten >= 2) {
                playSound("win")
                declareRoundWinner(
                    winner = blocker,
                    reasonText = tr(
                        R.string.shah_win_by_blockade,
                        mapOf(
                            "blocker" to getPlayerTitle(blocker, state.mode),
                            "active" to getPlayerTitle(activePlayer, state.mode)
                        )
                    )
                )
                return state.copy(phase = GamePhase.GAMEOVER)
            } else {
                return state.copy(
                    phase = GamePhase.FORCED_UNBLOCK,
                    turn = blocker,
                    unblockStep = 1,
                    bannerMessage = tr(
                        R.string.shah_banner_forced_unblock1,
                        mapOf(
                            "active" to getPlayerTitle(activePlayer, state.mode),
                            "blocker" to getPlayerTitle(blocker, state.mode)
                        )
                    ),
                    bannerIsWarning = true
                )
            }
        }
        return state.copy(
            unblockStep = 0,
            bannerMessage = tr(
                R.string.shah_banner_normal_turn,
                mapOf("player" to getPlayerTitle(activePlayer, state.mode))
            ),
            bannerIsWarning = false
        )
    }

    private fun commitStateAfterMove(preMoveSnapshot: GameSnapshot, newState: GameState) {
        if (newState.phase == GamePhase.GAMEOVER) return
        undoStack.addLast(preMoveSnapshot)
        redoStack.clear()
        _gameState.value = newState.copy(
            canUndo = undoStack.isNotEmpty(),
            canRedo = false
        )
        startTurnTimer(resetSeconds = true)
        triggerAiIfNeeded()
    }

    private fun triggerAiIfNeeded() {
        aiJob?.cancel()
        val st = _gameState.value
        if (st.phase == GamePhase.GAMEOVER || st.isPaused) return
        if (st.mode == GameMode.AI && st.turn == Player.P2) {
            aiJob = viewModelScope.launch {
                delay(320L)
                val curr = _gameState.value
                if (curr.phase == GamePhase.GAMEOVER || curr.isPaused || curr.turn != Player.P2) return@launch
                val diff = _settings.value.aiDifficulty

                when (curr.phase) {
                    GamePhase.PLACEMENT -> {
                        val spot = withContext(Dispatchers.Default) {
                            aiEngine.findBestPlacement(curr, Player.P2, diff)
                        }
                        if (spot != null && !_gameState.value.isPaused) {
                            processMove(spot)
                        }
                    }

                    GamePhase.INITIAL_REMOVE_1,
                    GamePhase.INITIAL_REMOVE_2,
                    GamePhase.EATING,
                    GamePhase.FORCED_UNBLOCK -> {
                        val target = withContext(Dispatchers.Default) {
                            aiEngine.findBestPieceToEat(curr, Player.P1, diff)
                        }
                        if (target != null && !_gameState.value.isPaused) {
                            processMove(target)
                        }
                    }

                    GamePhase.PLAYING -> {
                        val aiMove = withContext(Dispatchers.Default) {
                            aiEngine.findBestMove(curr, Player.P2, diff)
                        }
                        if (aiMove != null && !_gameState.value.isPaused) {
                            _gameState.update { it.copy(selectedStation = aiMove.from) }
                            processMove(aiMove.to)
                        }
                    }

                    GamePhase.GAMEOVER -> Unit
                }
            }
        }
    }

    private fun declareRoundWinner(winner: Player, reasonText: String) {
        turnTimerJob?.cancel()
        aiJob?.cancel()

        val st = _gameState.value
        val nextScoreP1 = if (winner == Player.P1) st.matchScoreP1 + 2 else st.matchScoreP1
        val nextScoreP2 = if (winner == Player.P2) st.matchScoreP2 + 2 else st.matchScoreP2

        val updatedSettings = _settings.value.copy(
            matchesPlayed = _settings.value.matchesPlayed + 1,
            p1Wins = if (winner == Player.P1) _settings.value.p1Wins + 1 else _settings.value.p1Wins,
            p2Wins = if (winner == Player.P2) _settings.value.p2Wins + 1 else _settings.value.p2Wins
        )
        _settings.value = updatedSettings
        repository.saveSettings(updatedSettings)

        var modalTitle = tr(R.string.shah_round_complete_title, mapOf("round" to st.matchRound.toString()))
        var fullMsg = reasonText + "

" + tr(
            R.string.shah_match_score_summary,
            mapOf(
                "p1" to getPlayerTitle(Player.P1, st.mode),
                "s1" to nextScoreP1.toString(),
                "p2" to getPlayerTitle(Player.P2, st.mode),
                "s2" to nextScoreP2.toString()
            )
        )

        val winnerScore = if (winner == Player.P1) nextScoreP1 else nextScoreP2
        val primaryBtnText: String
        val nextRound: Int
        val resetP1: Int
        val resetP2: Int

        if (winnerScore >= 4 || st.matchRound >= 3) {
            modalTitle = tr(R.string.shah_champ_title, mapOf("champ" to getPlayerTitle(winner, st.mode)))
            fullMsg += "

" + tr(R.string.shah_champ_msg)
            primaryBtnText = tr(R.string.shah_btn_new_match)
            nextRound = 1
            resetP1 = 0
            resetP2 = 0
        } else {
            nextRound = st.matchRound + 1
            resetP1 = nextScoreP1
            resetP2 = nextScoreP2
            primaryBtnText = tr(R.string.shah_btn_next_round, mapOf("round" to nextRound.toString()))
        }

        _gameState.update {
            it.copy(
                phase = GamePhase.GAMEOVER,
                isPaused = false,
                matchRound = nextRound,
                matchScoreP1 = resetP1,
                matchScoreP2 = resetP2
            )
        }

        _winnerModal.value = WinnerModalData(
            title = modalTitle,
            message = fullMsg,
            primaryButtonText = primaryBtnText,
            isQuizCertificate = false
        )
    }

    fun closeWinnerModal() {
        _winnerModal.value = null
    }

    fun onWinnerModalPrimaryAction() {
        val currentModal = _winnerModal.value ?: return
        _winnerModal.value = null
        if (currentModal.isQuizCertificate) {
            restartQuiz()
        } else {
            openStarterModal(_gameState.value.mode)
        }
    }

    fun undoMove() {
        if (undoStack.isEmpty()) return
        aiJob?.cancel()
        val currentSnap = _gameState.value.toSnapshot()
        redoStack.addLast(currentSnap)
        var targetSnap = undoStack.removeLast()

        if (_gameState.value.mode == GameMode.AI && targetSnap.turn == Player.P2 && undoStack.isNotEmpty()) {
            redoStack.addLast(targetSnap)
            targetSnap = undoStack.removeLast()
        }

        restoreSnapshot(targetSnap)
        showToast(tr(R.string.shah_btn_undo))
    }

    fun redoMove() {
        if (redoStack.isEmpty()) return
        aiJob?.cancel()
        val currentSnap = _gameState.value.toSnapshot()
        undoStack.addLast(currentSnap)
        var targetSnap = redoStack.removeLast()

        if (_gameState.value.mode == GameMode.AI && targetSnap.turn == Player.P2 && redoStack.isNotEmpty()) {
            undoStack.addLast(targetSnap)
            targetSnap = redoStack.removeLast()
        }

        restoreSnapshot(targetSnap)
        showToast(tr(R.string.shah_btn_redo))
    }

    private fun restoreSnapshot(snap: GameSnapshot) {
        _gameState.update {
            it.copy(
                board = snap.board,
                p1Inventory = snap.p1Inventory,
                p2Inventory = snap.p2Inventory,
                p1OnBoard = snap.p1OnBoard,
                p2OnBoard = snap.p2OnBoard,
                p1Eaten = snap.p1Eaten,
                p2Eaten = snap.p2Eaten,
                p1Fouls = snap.p1Fouls,
                p2Fouls = snap.p2Fouls,
                isPaused = false,
                turn = snap.turn,
                firstTurn = snap.firstTurn,
                phase = snap.phase,
                selectedStation = snap.selectedStation,
                lastMovedFrom = snap.lastMovedFrom,
                lastMovedTo = snap.lastMovedTo,
                millFirst = snap.millFirst,
                firstPicker = snap.firstPicker,
                unblockStep = snap.unblockStep,
                highlightedMill1 = null,
                highlightedMill2 = null,
                moveHistory = snap.moveHistory,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        refreshBannerForCurrentPhase()
        startTurnTimer(resetSeconds = true)
    }

    private fun refreshBannerForCurrentPhase() {
        val st = _gameState.value
        if (st.isPaused) {
            _gameState.update {
                it.copy(bannerMessage = tr(R.string.shah_banner_paused), bannerIsWarning = true)
            }
            return
        }
        val pTitle = getPlayerTitle(st.turn, st.mode)
        val oppTitle = getPlayerTitle(st.turn.opponent(), st.mode)
        val (msg, isWarn) = when (st.phase) {
            GamePhase.PLACEMENT -> {
                if (st.p1Inventory == 12 && st.p2Inventory == 12) {
                    tr(R.string.shah_banner_place_start, mapOf("player" to pTitle)) to false
                } else {
                    tr(R.string.shah_banner_place_turn, mapOf("player" to pTitle)) to false
                }
            }
            GamePhase.INITIAL_REMOVE_1 -> tr(R.string.shah_banner_initial_remove1, mapOf("player" to pTitle)) to true
            GamePhase.INITIAL_REMOVE_2 -> tr(R.string.shah_banner_initial_remove2, mapOf("player" to pTitle)) to true
            GamePhase.EATING -> tr(R.string.shah_banner_mill_eat, mapOf("player" to pTitle)) to true
            GamePhase.FORCED_UNBLOCK -> {
                if (st.unblockStep >= 2) {
                    tr(R.string.shah_banner_still_blocked2, mapOf("opp" to oppTitle, "player" to pTitle)) to true
                } else {
                    tr(R.string.shah_banner_forced_unblock1, mapOf("active" to oppTitle, "blocker" to pTitle)) to true
                }
            }
            GamePhase.PLAYING -> tr(R.string.shah_banner_normal_turn, mapOf("player" to pTitle)) to false
            GamePhase.GAMEOVER -> st.bannerMessage to false
        }
        _gameState.update { it.copy(bannerMessage = msg, bannerIsWarning = isWarn) }
    }

    // Rules Filter
    fun setRuleCategoryFilter(category: String) {
        _selectedRuleCategory.value = category
    }

    // 20-Question Knowledge Quiz Engine
    fun selectQuizAnswer(optionIdx: Int) {
        val qState = _quizState.value
        val idx = qState.currentIndex
        if (qState.selectedAnswers[idx] != null) return

        val question = _quizQuestions.value.getOrNull(idx) ?: return
        val updatedAnswers = qState.selectedAnswers.toMutableList()
        updatedAnswers[idx] = optionIdx
        val isCorrect = (optionIdx == question.correctAnswer)
        val nextScore = if (isCorrect) qState.score + 1 else qState.score

        if (isCorrect) playSound("mill") else playSound("error")

        _quizState.value = qState.copy(
            selectedAnswers = updatedAnswers,
            score = nextScore
        )
    }

    fun nextQuizQuestion() {
        val qState = _quizState.value
        if (qState.currentIndex < 19) {
            _quizState.value = qState.copy(currentIndex = qState.currentIndex + 1)
        } else {
            showQuizCertificateModal()
        }
    }

    fun prevQuizQuestion() {
        val qState = _quizState.value
        if (qState.currentIndex > 0) {
            _quizState.value = qState.copy(currentIndex = qState.currentIndex - 1)
        }
    }

    fun restartQuiz() {
        _quizState.value = QuizState()
        showToast(tr(R.string.shah_toast_quiz_restarted))
    }

    private fun showQuizCertificateModal() {
        val sc = _quizState.value.score
        val pct = (sc * 100) / 20
        val rankMsg = when {
            sc >= 18 -> tr(R.string.shah_cert_rank_high)
            sc >= 14 -> tr(R.string.shah_cert_rank_mid)
            else -> tr(R.string.shah_cert_rank_low)
        }
        playSound("win")
        _winnerModal.value = WinnerModalData(
            title = tr(R.string.shah_cert_title),
            message = tr(R.string.shah_cert_score_line, mapOf("sc" to sc.toString(), "pct" to pct.toString())) + "

" + rankMsg,
            primaryButtonText = tr(R.string.shah_btn_retry_quiz),
            isQuizCertificate = true
        )
    }

    // Native Intents: Share App, Direct Problem/Issue Email to Federation, & Social Media Links
    fun shareApplication(context: Context) {
        try {
            val shareText = tr(R.string.shah_share_app_message) +
                "
https://play.google.com/store/apps/details?id=${context.packageName}"
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, tr(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, tr(R.string.shah_share_app_btn)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Throwable) {
            // Safely ignore if no share target exists
        }
    }

    fun sendIssueEmailToFederation(context: Context, subjectInput: String, bodyInput: String) {
        if (bodyInput.isBlank()) {
            playSound("error")
            showToast(tr(R.string.shah_report_issue_empty_err))
            return
        }
        try {
            val recipient = "eth.culturalsportsfed.1977@gmail.com"
            val finalSubject = if (subjectInput.isBlank()) {
                "ሻህ (Shah App v1.0.0) - የተጠቃሚ መልዕክት / Issue Report"
            } else {
                "ሻህ (Shah App v1.0.0) - $subjectInput"
            }
            val deviceFooter = "

---
App: ሻህ (eth.culturalsports.shah v1.0.0)
Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
            val mailUri = Uri.parse(
                "mailto:$recipient?subject=" + Uri.encode(finalSubject) +
                    "&body=" + Uri.encode(bodyInput.trim() + deviceFooter)
            )
            val emailIntent = Intent(Intent.ACTION_SENDTO, mailUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(emailIntent)
        } catch (_: Throwable) {
            // Fallback to generic ACTION_SEND if no dedicated mailto handler is installed
            try {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("eth.culturalsportsfed.1977@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, subjectInput.ifBlank { "ሻህ (Shah App)" })
                    putExtra(Intent.EXTRA_TEXT, bodyInput)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(fallback, "Email").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: Throwable) {
                // Ignore
            }
        }
    }

    fun openExternalLink(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
            // Ignore if browser unavailable
        }
    }

    private fun playSound(kind: String) {
        val s = _settings.value
        soundEngine.playEffect(kind, s.soundEnabled, s.vibrationEnabled)
    }

    fun showToast(msg: String) {
        toastJob?.cancel()
        _toastMessage.value = msg
        toastJob = viewModelScope.launch {
            delay(2700L)
            _toastMessage.value = null
        }
    }
}
