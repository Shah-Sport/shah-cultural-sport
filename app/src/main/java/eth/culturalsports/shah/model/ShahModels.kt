package eth.culturalsports.shah.model

enum class Player {
    P1,
    P2;

    fun opponent(): Player = if (this == P1) P2 else P1
}

enum class GameMode {
    AI,
    PVP
}

enum class AiDifficulty(val code: String, val maxPlacementDepth: Int, val maxMoveDepth: Int, val timeBudgetMs: Long) {
    EASY("easy", 2, 2, 90L),
    MEDIUM("medium", 3, 3, 140L),
    HARD("hard", 4, 4, 210L),
    MASTER("master", 5, 6, 320L);

    companion object {
        fun fromCode(code: String): AiDifficulty {
            return entries.firstOrNull { it.code == code } ?: MASTER
        }
    }
}

enum class GamePhase {
    PLACEMENT,
    INITIAL_REMOVE_1,
    INITIAL_REMOVE_2,
    PLAYING,
    EATING,
    FORCED_UNBLOCK,
    GAMEOVER
}

enum class AppScreen {
    SPLASH,
    HOME,
    GAME,
    RULES,
    QUIZ
}

enum class SidebarTab {
    ABOUT,
    CONTACT,
    PRIVACY
}

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val regionName: String,
    val flagEmoji: String,
    val isRtl: Boolean = false
)

data class AutomaticWinResult(
    val isAutoWin: Boolean,
    val isDoubleMill: Boolean = false,
    val mill1: IntArray? = null,
    val mill2: IntArray? = null,
    val shuttleStationA: Int = -1,
    val shuttleStationB: Int = -1
)

object BoardTopology {
    // 24 official station coordinates in [0f..100f] normalized board space
    val nodeCoords: Array<FloatArray> = arrayOf(
        // Outer Green Square (0..7)
        floatArrayOf(6f, 6f),   floatArrayOf(50f, 6f),  floatArrayOf(94f, 6f),  floatArrayOf(94f, 50f),
        floatArrayOf(94f, 94f), floatArrayOf(50f, 94f), floatArrayOf(6f, 94f),  floatArrayOf(6f, 50f),
        // Middle Yellow Square (8..15)
        floatArrayOf(20f, 20f), floatArrayOf(50f, 20f), floatArrayOf(80f, 20f), floatArrayOf(80f, 50f),
        floatArrayOf(80f, 80f), floatArrayOf(50f, 80f), floatArrayOf(20f, 80f), floatArrayOf(20f, 50f),
        // Inner Red Square (16..23)
        floatArrayOf(34f, 34f), floatArrayOf(50f, 34f), floatArrayOf(66f, 34f), floatArrayOf(66f, 50f),
        floatArrayOf(66f, 66f), floatArrayOf(50f, 66f), floatArrayOf(34f, 66f), floatArrayOf(34f, 50f)
    )

    // Official station labels (1..24)
    val stationLabels: Array<String> = arrayOf(
        "6",  "24", "8",  "13", "7",  "1",  "5",  "4",
        "22", "23", "20", "18", "19", "2",  "14", "9",
        "16", "21", "15", "17", "12", "3",  "11", "10"
    )

    // Orthogonal adjacency list for all 24 stations (Jumping & diagonal moves strictly prohibited by Rule 4.15)
    val adjacency: Array<IntArray> = arrayOf(
        intArrayOf(1, 7),         // 0
        intArrayOf(0, 2, 9),      // 1
        intArrayOf(1, 3),         // 2
        intArrayOf(2, 4, 11),     // 3
        intArrayOf(3, 5),         // 4
        intArrayOf(4, 6, 13),     // 5
        intArrayOf(5, 7),         // 6
        intArrayOf(0, 6, 15),     // 7
        intArrayOf(9, 15),        // 8
        intArrayOf(1, 8, 10, 17), // 9  (4-way hub)
        intArrayOf(9, 11),       // 10
        intArrayOf(3, 10, 12, 19),// 11 (4-way hub)
        intArrayOf(11, 13),       // 12
        intArrayOf(5, 12, 14, 21),// 13 (4-way hub)
        intArrayOf(13, 15),       // 14
        intArrayOf(7, 8, 14, 23), // 15 (4-way hub)
        intArrayOf(17, 23),       // 16
        intArrayOf(9, 16, 18),    // 17
        intArrayOf(17, 19),       // 18
        intArrayOf(11, 18, 20),   // 19
        intArrayOf(19, 21),       // 20
        intArrayOf(13, 20, 22),   // 21
        intArrayOf(21, 23),       // 22
        intArrayOf(15, 16, 22)    // 23
    )

    // 16 possible 3-in-a-row lines (Mills / Shah)
    val mills: Array<IntArray> = arrayOf(
        intArrayOf(0, 1, 2),    intArrayOf(2, 3, 4),    intArrayOf(4, 5, 6),    intArrayOf(6, 7, 0),
        intArrayOf(8, 9, 10),   intArrayOf(10, 11, 12), intArrayOf(12, 13, 14), intArrayOf(14, 15, 8),
        intArrayOf(16, 17, 18), intArrayOf(18, 19, 20), intArrayOf(20, 21, 22), intArrayOf(22, 23, 16),
        intArrayOf(1, 9, 17),   intArrayOf(3, 11, 19),  intArrayOf(5, 13, 21),  intArrayOf(7, 15, 23)
    )

    val hubStations4Way: IntArray = intArrayOf(9, 11, 13, 15)
    val junctionStations3Way: IntArray = intArrayOf(1, 3, 5, 7, 17, 19, 21, 23)

    fun getMillsAt(station: Int, player: Player, board: Array<Player?>): List<IntArray> {
        val found = ArrayList<IntArray>(2)
        for (m in mills) {
            if ((m[0] == station || m[1] == station || m[2] == station) &&
                board[m[0]] == player && board[m[1]] == player && board[m[2]] == player
            ) {
                found.add(m)
            }
        }
        return found
    }

    fun checkMill(station: Int, player: Player, board: Array<Player?>): Boolean {
        for (m in mills) {
            if ((m[0] == station || m[1] == station || m[2] == station) &&
                board[m[0]] == player && board[m[1]] == player && board[m[2]] == player
            ) {
                return true
            }
        }
        return false
    }

    fun hasValidMoves(player: Player, board: Array<Player?>): Boolean {
        for (i in 0 until 24) {
            if (board[i] == player) {
                for (adj in adjacency[i]) {
                    if (board[adj] == null) return true
                }
            }
        }
        return false
    }

    fun evaluateAutomaticWin(toIdx: Int, player: Player, board: Array<Player?>): AutomaticWinResult {
        val currentMills = getMillsAt(toIdx, player, board)
        if (currentMills.isEmpty()) {
            return AutomaticWinResult(isAutoWin = false)
        }
        if (currentMills.size >= 2) {
            return AutomaticWinResult(
                isAutoWin = true,
                isDoubleMill = true,
                mill1 = currentMills[0],
                mill2 = currentMills[1]
            )
        }
        val adjList = adjacency[toIdx]
        for (adjIdx in adjList) {
            if (board[adjIdx] == null) {
                val prevTo = board[toIdx]
                board[toIdx] = null
                board[adjIdx] = player
                val shuttleMills = getMillsAt(adjIdx, player, board)
                board[adjIdx] = null
                board[toIdx] = prevTo
                if (shuttleMills.isNotEmpty()) {
                    return AutomaticWinResult(
                        isAutoWin = true,
                        isDoubleMill = false,
                        mill1 = currentMills[0],
                        mill2 = shuttleMills[0],
                        shuttleStationA = toIdx,
                        shuttleStationB = adjIdx
                    )
                }
            }
        }
        return AutomaticWinResult(isAutoWin = false)
    }
}

data class GameSnapshot(
    val board: List<Player?>,
    val p1Inventory: Int,
    val p2Inventory: Int,
    val p1OnBoard: Int,
    val p2OnBoard: Int,
    val p1Eaten: Int,
    val p2Eaten: Int,
    val p1Fouls: Int,
    val p2Fouls: Int,
    val turn: Player,
    val firstTurn: Player,
    val phase: GamePhase,
    val selectedStation: Int?,
    val lastMovedFrom: Int?,
    val lastMovedTo: Int?,
    val millFirst: Player?,
    val firstPicker: Player?,
    val unblockStep: Int,
    val moveHistory: List<String>
)

data class GameState(
    val mode: GameMode = GameMode.AI,
    val board: List<Player?> = List(24) { null },
    val p1Inventory: Int = 12,
    val p2Inventory: Int = 12,
    val p1OnBoard: Int = 0,
    val p2OnBoard: Int = 0,
    val p1Eaten: Int = 0,
    val p2Eaten: Int = 0,
    val p1Fouls: Int = 0,
    val p2Fouls: Int = 0,
    val isPaused: Boolean = false,
    val turn: Player = Player.P1,
    val firstTurn: Player = Player.P1,
    val phase: GamePhase = GamePhase.PLACEMENT,
    val selectedStation: Int? = null,
    val lastMovedFrom: Int? = null,
    val lastMovedTo: Int? = null,
    val millFirst: Player? = null,
    val firstPicker: Player? = null,
    val unblockStep: Int = 0,
    val highlightedMill1: IntArray? = null,
    val highlightedMill2: IntArray? = null,
    val moveHistory: List<String> = emptyList(),
    val bannerMessage: String = "",
    val bannerIsWarning: Boolean = false,
    val turnSecondsLeft: Int = 30,
    val matchRound: Int = 1,
    val matchScoreP1: Int = 0,
    val matchScoreP2: Int = 0,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
    fun toSnapshot(): GameSnapshot = GameSnapshot(
        board = board.toList(),
        p1Inventory = p1Inventory,
        p2Inventory = p2Inventory,
        p1OnBoard = p1OnBoard,
        p2OnBoard = p2OnBoard,
        p1Eaten = p1Eaten,
        p2Eaten = p2Eaten,
        p1Fouls = p1Fouls,
        p2Fouls = p2Fouls,
        turn = turn,
        firstTurn = firstTurn,
        phase = phase,
        selectedStation = selectedStation,
        lastMovedFrom = lastMovedFrom,
        lastMovedTo = lastMovedTo,
        millFirst = millFirst,
        firstPicker = firstPicker,
        unblockStep = unblockStep,
        moveHistory = moveHistory.toList()
    )
}

data class RuleArticle(
    val id: String,
    val category: String,
    val title: String,
    val content: String
)

data class QuizQuestion(
    val index: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val category: String,
    val explanation: String
)

data class QuizState(
    val currentIndex: Int = 0,
    val selectedAnswers: List<Int?> = List(20) { null },
    val score: Int = 0
)

data class UserSettings(
    val languageCode: String = "am",
    val isDarkTheme: Boolean = true,
    val fontScaleStep: Int = 0,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showStationNumbers: Boolean = true,
    val aiDifficulty: AiDifficulty = AiDifficulty.MASTER,
    val matchesPlayed: Int = 0,
    val p1Wins: Int = 0,
    val p2Wins: Int = 0
)

data class WinnerModalData(
    val title: String,
    val message: String,
    val primaryButtonText: String,
    val isQuizCertificate: Boolean = false
)
