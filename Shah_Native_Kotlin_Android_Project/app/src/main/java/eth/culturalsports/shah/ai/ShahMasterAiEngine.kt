package eth.culturalsports.shah.ai

import eth.culturalsports.shah.model.AiDifficulty
import eth.culturalsports.shah.model.BoardTopology
import eth.culturalsports.shah.model.GamePhase
import eth.culturalsports.shah.model.GameState
import eth.culturalsports.shah.model.Player
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class AiMove(val from: Int, val to: Int, val priority: Int = 0)

/**
 * 1000x Upgraded Native Kotlin Master AI Engine for Shah Cultural Sport Game.
 * Features:
 * - Iterative Deepening up to 5-6 Plies (Easy, Medium, Hard, Master)
 * - Zobrist Hashing & Transposition Table for lightning-fast tree pruning
 * - Placement Clustering & Future Sliding Synergy analysis
 * - Opponent Suffocation (Blockade preparation) & Anti-Blockade Defense
 * - Strategic Capture selection that destroys opponent's key mill/shuttle pieces
 * - Instant Shuttle 3-in-a-Row (Automatic Win) & Double Mill execution
 * - Draw / Repetition awareness when defending vs pressing for a win
 */
class ShahMasterAiEngine {

    private val zobristTable: Array<LongArray> = Array(24) { LongArray(3) }
    private val zobristTurn: Long
    private val transpositionTable = HashMap<Long, TtEntry>(16384)

    private var searchStartTimeMs: Long = 0L
    private var timeBudgetMs: Long = 300L

    private data class TtEntry(
        val depth: Int,
        val score: Int,
        val flag: Int // 0 = EXACT, 1 = LOWERBOUND, 2 = UPPERBOUND
    )

    init {
        val rng = Random(19702026L)
        for (i in 0 until 24) {
            for (p in 0 until 3) {
                zobristTable[i][p] = rng.nextLong()
            }
        }
        zobristTurn = rng.nextLong()
    }

    private fun computeBoardHash(board: Array<Player?>, isMaxTurn: Boolean): Long {
        var h = if (isMaxTurn) zobristTurn else 0L
        for (i in 0 until 24) {
            val p = board[i]
            if (p == Player.P1) h = h xor zobristTable[i][1]
            else if (p == Player.P2) h = h xor zobristTable[i][2]
        }
        return h
    }

    private fun isTimeUp(): Boolean {
        return (System.currentTimeMillis() - searchStartTimeMs) >= timeBudgetMs
    }

    fun generateLegalMoves(player: Player, board: Array<Player?>): List<AiMove> {
        val moves = ArrayList<AiMove>(24)
        for (i in 0 until 24) {
            if (board[i] == player) {
                for (to in BoardTopology.adjacency[i]) {
                    if (board[to] == null) {
                        moves.add(AiMove(from = i, to = to))
                    }
                }
            }
        }
        return moves
    }

    /**
     * Deep evaluation of Placement Board:
     * - Rule 4.7 first 3-in-a-row priority
     * - Double-threat forks & opponent fork prevention
     * - Clustering & adjacency synergy (keeping own pebbles close & connected for sliding phase)
     * - Pre-built Shuttle Automatic Win structures
     * - Early suffocation/blockade of opponent pebbles
     */
    fun evaluatePlacementBoard(board: Array<Player?>, me: Player, opp: Player): Int {
        var score = 0
        var myTwoThreats = 0
        var oppTwoThreats = 0
        val myCompletionCount = IntArray(24)
        val oppCompletionCount = IntArray(24)

        for (m in BoardTopology.mills) {
            var myCnt = 0
            var oppCnt = 0
            var emptyIdx = -1
            for (k in 0..2) {
                val v = board[m[k]]
                if (v == me) myCnt++
                else if (v == opp) oppCnt++
                else emptyIdx = m[k]
            }

            if (myCnt == 3) {
                score += 1350
                // Check if any piece in this mill can shuttle to an adjacent open station for an Automatic Win
                for (k in 0..2) {
                    val stIdx = m[k]
                    for (nIdx in BoardTopology.adjacency[stIdx]) {
                        if (board[nIdx] == null) {
                            board[stIdx] = null
                            board[nIdx] = me
                            if (BoardTopology.checkMill(nIdx, me, board)) {
                                score += 820 // Pre-built Shuttle Automatic Win!
                            }
                            board[nIdx] = null
                            board[stIdx] = me
                        }
                    }
                }
            } else if (oppCnt == 3) {
                score -= 1450
            } else if (myCnt == 2 && oppCnt == 0 && emptyIdx != -1) {
                myTwoThreats++
                myCompletionCount[emptyIdx]++
                score += 310
                // Check if we already have a 3rd friendly pebble adjacent to emptyIdx ready to slide in!
                for (nIdx in BoardTopology.adjacency[emptyIdx]) {
                    if (board[nIdx] == me && nIdx != m[0] && nIdx != m[1] && nIdx != m[2]) {
                        score += 290
                    }
                }
            } else if (oppCnt == 2 && myCnt == 0 && emptyIdx != -1) {
                oppTwoThreats++
                oppCompletionCount[emptyIdx]++
                score -= 360
                for (nIdx in BoardTopology.adjacency[emptyIdx]) {
                    if (board[nIdx] == opp && nIdx != m[0] && nIdx != m[1] && nIdx != m[2]) {
                        score -= 320
                    }
                }
            } else if (myCnt == 2 && oppCnt == 1) {
                score -= 45
            } else if (oppCnt == 2 && myCnt == 1) {
                score += 195 // Successfully blocked opponent 3-in-a-row
            } else if (myCnt == 1 && oppCnt == 0) {
                score += 38
            } else if (oppCnt == 1 && myCnt == 0) {
                score -= 38
            }
        }

        if (myTwoThreats >= 2) score += myTwoThreats * 620
        if (oppTwoThreats >= 2) score -= oppTwoThreats * 740

        for (i in 0 until 24) {
            if (myCompletionCount[i] >= 2) score += 1150
            if (oppCompletionCount[i] >= 2) score -= 1280
        }

        // Hub control, Own Pebble Clustering Synergy, and Early Opponent Suffocation
        for (i in 0 until 24) {
            val occ = board[i]
            if (occ == me) {
                if (BoardTopology.hubStations4Way.contains(i)) score += 88
                else if (BoardTopology.junctionStations3Way.contains(i)) score += 52
                else score += 20

                var friendlyNeighbors = 0
                var openNeighbors = 0
                for (adj in BoardTopology.adjacency[i]) {
                    if (board[adj] == me) friendlyNeighbors++
                    else if (board[adj] == null) openNeighbors++
                }
                // Clustering bonus: connected pebbles with open sliding lanes
                if (friendlyNeighbors >= 1 && openNeighbors >= 1) {
                    score += friendlyNeighbors * 36 + openNeighbors * 22
                } else if (openNeighbors == 0) {
                    score -= 70 // Avoid self-blocking during placement
                }
            } else if (occ == opp) {
                if (BoardTopology.hubStations4Way.contains(i)) score -= 88
                else if (BoardTopology.junctionStations3Way.contains(i)) score -= 52

                var blockedByMe = 0
                var openNeighbors = 0
                for (adj in BoardTopology.adjacency[i]) {
                    if (board[adj] == me) blockedByMe++
                    else if (board[adj] == null) openNeighbors++
                }
                if (openNeighbors == 0) score += 165 // Opponent pebble completely suffocated!
                else score += blockedByMe * 34
            }
        }

        return score
    }

    /**
     * Finds the optimal station during the Placement Phase using 2 to 5-6 ply lookahead.
     */
    fun findBestPlacement(state: GameState, player: Player, difficulty: AiDifficulty): Int? {
        val opp = player.opponent()
        val board = state.board.toTypedArray()
        val empties = ArrayList<Int>(24)
        for (i in 0 until 24) {
            if (board[i] == null) empties.add(i)
        }
        if (empties.isEmpty()) return null

        // 1. Immediate 3-in-a-row formation (Secures Rule 4.7 millFirst priority!)
        var bestImmediateMill = -1
        var bestImmediateMillScore = Int.MIN_VALUE
        for (pos in empties) {
            board[pos] = player
            val millsFormed = BoardTopology.getMillsAt(pos, player, board)
            if (millsFormed.isNotEmpty()) {
                val sc = evaluatePlacementBoard(board, player, opp) +
                    (millsFormed.size * 2200) +
                    (if (state.millFirst == null) 4200 else 0)
                if (sc > bestImmediateMillScore) {
                    bestImmediateMillScore = sc
                    bestImmediateMill = pos
                }
            }
            board[pos] = null
        }
        if (bestImmediateMill != -1) return bestImmediateMill

        // 2. Immediate block of Opponent's 3-in-a-row threat
        var bestBlockPos = -1
        var bestBlockScore = Int.MIN_VALUE
        for (pos in empties) {
            board[pos] = opp
            val oppMills = BoardTopology.getMillsAt(pos, opp, board)
            board[pos] = null
            if (oppMills.isNotEmpty()) {
                board[pos] = player
                val sc = evaluatePlacementBoard(board, player, opp) + (oppMills.size * 2000)
                board[pos] = null
                if (sc > bestBlockScore) {
                    bestBlockScore = sc
                    bestBlockPos = pos
                }
            }
        }
        if (bestBlockPos != -1) return bestBlockPos

        // 3. Score & order candidate stations for multi-ply Alpha-Beta placement search
        val scoredCandidates = ArrayList<Pair<Int, Int>>(empties.size)
        for (pos in empties) {
            board[pos] = player
            val myEval = evaluatePlacementBoard(board, player, opp)
            board[pos] = opp
            val oppForkDanger = evaluatePlacementBoard(board, opp, player)
            board[pos] = null
            val combined = myEval + (oppForkDanger * 3) / 4
            scoredCandidates.add(pos to combined)
        }
        scoredCandidates.sortByDescending { it.second }

        val topLimit = when (difficulty) {
            AiDifficulty.MASTER -> min(12, scoredCandidates.size)
            AiDifficulty.HARD -> min(9, scoredCandidates.size)
            AiDifficulty.MEDIUM -> min(7, scoredCandidates.size)
            AiDifficulty.EASY -> min(5, scoredCandidates.size)
        }

        val maxDepth = min(difficulty.maxPlacementDepth, empties.size)
        searchStartTimeMs = System.currentTimeMillis()
        timeBudgetMs = difficulty.timeBudgetMs

        var bestStation = scoredCandidates[0].first
        var bestVal = Int.MIN_VALUE
        var alpha = -1_000_000
        val beta = 1_000_000

        for (i in 0 until topLimit) {
            val pos = scoredCandidates[i].first
            board[pos] = player
            val v = placementAlphaBeta(board, maxDepth - 1, alpha, beta, false, player, opp)
            board[pos] = null
            if (v > bestVal) {
                bestVal = v
                bestStation = pos
            }
            if (v > alpha) alpha = v
        }
        return bestStation
    }

    private fun placementAlphaBeta(
        board: Array<Player?>,
        depth: Int,
        alphaInit: Int,
        betaInit: Int,
        isMaximizing: Boolean,
        me: Player,
        opp: Player
    ): Int {
        if (depth <= 0 || isTimeUp()) {
            return evaluatePlacementBoard(board, me, opp)
        }
        val empties = ArrayList<Int>(24)
        for (i in 0 until 24) {
            if (board[i] == null) empties.add(i)
        }
        if (empties.isEmpty()) {
            return evaluatePlacementBoard(board, me, opp)
        }
        empties.sortByDescending { BoardTopology.adjacency[it].size }
        val limit = min(8, empties.size)

        var alpha = alphaInit
        var beta = betaInit

        if (isMaximizing) {
            var maxEval = -1_000_000
            for (i in 0 until limit) {
                val pos = empties[i]
                board[pos] = me
                val ev = if (BoardTopology.checkMill(pos, me, board)) {
                    evaluatePlacementBoard(board, me, opp) + 1600 * depth
                } else {
                    placementAlphaBeta(board, depth - 1, alpha, beta, false, me, opp)
                }
                board[pos] = null
                if (ev > maxEval) maxEval = ev
                if (ev > alpha) alpha = ev
                if (beta <= alpha) break
            }
            return maxEval
        } else {
            var minEval = 1_000_000
            for (i in 0 until limit) {
                val pos = empties[i]
                board[pos] = opp
                val ev = if (BoardTopology.checkMill(pos, opp, board)) {
                    evaluatePlacementBoard(board, me, opp) - 1600 * depth
                } else {
                    placementAlphaBeta(board, depth - 1, alpha, beta, true, me, opp)
                }
                board[pos] = null
                if (ev < minEval) minEval = ev
                if (ev < beta) beta = ev
                if (beta <= alpha) break
            }
            return minEval
        }
    }

    /**
     * Deep evaluation of Sliding Movement Board:
     * - 1-step Shuttle Automatic Win & Double Mill detection
     * - 1-step sliding Mill detection
     * - Mobility differential, Blockade attack, and Anti-Blockade defense
     * - Pebble clustering & line control
     */
    fun evaluatePlayingBoard(
        board: Array<Player?>,
        me: Player,
        opp: Player,
        myEaten: Int,
        oppEaten: Int
    ): Int {
        var myPieces = 0
        var oppPieces = 0
        var myMobility = 0
        var oppMobility = 0
        var myTrapped = 0
        var oppTrapped = 0
        var myAutoThreats = 0
        var oppAutoThreats = 0
        var myMillMoves = 0
        var oppMillMoves = 0

        for (i in 0 until 24) {
            val occ = board[i]
            if (occ == me) {
                myPieces++
                var localMoves = 0
                for (to in BoardTopology.adjacency[i]) {
                    if (board[to] == null) {
                        localMoves++
                        myMobility++
                        board[to] = me
                        board[i] = null
                        if (BoardTopology.evaluateAutomaticWin(to, me, board).isAutoWin) {
                            myAutoThreats++
                        } else if (BoardTopology.checkMill(to, me, board)) {
                            myMillMoves++
                        }
                        board[i] = me
                        board[to] = null
                    }
                }
                if (localMoves == 0) myTrapped++
            } else if (occ == opp) {
                oppPieces++
                var localMoves = 0
                for (to in BoardTopology.adjacency[i]) {
                    if (board[to] == null) {
                        localMoves++
                        oppMobility++
                        board[to] = opp
                        board[i] = null
                        if (BoardTopology.evaluateAutomaticWin(to, opp, board).isAutoWin) {
                            oppAutoThreats++
                        } else if (BoardTopology.checkMill(to, opp, board)) {
                            oppMillMoves++
                        }
                        board[i] = opp
                        board[to] = null
                    }
                }
                if (localMoves == 0) oppTrapped++
            }
        }

        if (oppPieces < 3) return 92000
        if (myPieces < 3) return -92000
        if (oppMobility == 0 && myEaten >= 2) return 89000
        if (myMobility == 0 && oppEaten >= 2) return -89000

        var score = (myPieces - oppPieces) * 580
        score += (myEaten - oppEaten) * 210
        if (myEaten >= 2) score += 320
        if (oppEaten >= 2) score -= 320

        // Automatic Win (Shuttle / Double Mill) threats
        score += myAutoThreats * 2900
        score -= oppAutoThreats * 3400

        // Sliding 3-in-a-row threats
        score += myMillMoves * 740
        score -= oppMillMoves * 860

        // Mobility, Blockade attack & Anti-Blockade self-defense
        score += (myMobility * 48) - (oppMobility * 66)
        score += (oppTrapped - myTrapped) * 125
        if (oppMobility <= 2 && myEaten >= 2) score += (3 - oppMobility) * 720
        if (myMobility <= 2 && oppEaten >= 2) score -= (3 - myMobility) * 820 // Anti-Blockade defense!

        for (m in BoardTopology.mills) {
            var mc = 0
            var oc = 0
            for (k in 0..2) {
                if (board[m[k]] == me) mc++
                else if (board[m[k]] == opp) oc++
            }
            if (mc == 3) score += 240
            else if (oc == 3) score -= 240
            else if (mc == 2 && oc == 0) score += 130
            else if (oc == 2 && mc == 0) score -= 145
        }

        return score
    }

    private fun chooseBestCaptureSim(
        board: Array<Player?>,
        me: Player,
        opp: Player,
        myEaten: Int,
        oppEaten: Int
    ): Int {
        var bestIdx = -1
        var bestSc = Int.MIN_VALUE
        for (i in 0 until 24) {
            if (board[i] == opp) {
                board[i] = null
                val sc = evaluatePlayingBoard(board, me, opp, myEaten + 1, oppEaten)
                board[i] = opp
                if (sc > bestSc) {
                    bestSc = sc
                    bestIdx = i
                }
            }
        }
        return bestIdx
    }

    /**
     * Strategic Piece Capture & Forced Unblock selector (`findBestPieceToEat`):
     * - Identifies which opponent pebble removal unlocks an immediate Shuttle Automatic Win for AI
     * - Identifies and eliminates the opponent's most dangerous mill/shuttle builder pebble
     * - In Rule 4.20 Forced Unblock, selects the pebble that keeps the opponent STILL BLOCKED (or trapped)
     */
    fun findBestPieceToEat(state: GameState, opp: Player, difficulty: AiDifficulty): Int? {
        val me = opp.opponent()
        val board = state.board.toTypedArray()
        val oppIndices = ArrayList<Int>(12)
        for (i in 0 until 24) {
            if (board[i] == opp) oppIndices.add(i)
        }
        if (oppIndices.isEmpty()) return null

        val myEaten = if (me == Player.P1) state.p1Eaten else state.p2Eaten
        val oppEaten = if (opp == Player.P1) state.p1Eaten else state.p2Eaten

        var oppAutoBefore = 0
        var oppMillBefore = 0
        for (mv in generateLegalMoves(opp, board)) {
            board[mv.to] = opp
            board[mv.from] = null
            if (BoardTopology.evaluateAutomaticWin(mv.to, opp, board).isAutoWin) oppAutoBefore++
            else if (BoardTopology.checkMill(mv.to, opp, board)) oppMillBefore++
            board[mv.from] = opp
            board[mv.to] = null
        }

        searchStartTimeMs = System.currentTimeMillis()
        timeBudgetMs = difficulty.timeBudgetMs
        transpositionTable.clear()

        var bestTarget = oppIndices[0]
        var highestScore = Int.MIN_VALUE

        for (idx in oppIndices) {
            val simBoard = board.copyOf()
            simBoard[idx] = null
            var score = 0

            // 1. Check if this opponent piece was part of a 2-in-a-row threat or 3-in-a-row mill
            for (m in BoardTopology.mills) {
                if (m[0] == idx || m[1] == idx || m[2] == idx) {
                    var oppCount = 0
                    var emptyCount = 0
                    for (k in 0..2) {
                        if (board[m[k]] == opp) oppCount++
                        else if (board[m[k]] == null) emptyCount++
                    }
                    if (oppCount == 3) score += 1800
                    else if (oppCount == 2 && emptyCount == 1) score += 2400 // Crucial mill-forming piece!
                }
            }

            // 2. Rule 4.20 Forced Unblock logic: keep opponent blocked if possible!
            if (state.phase == GamePhase.FORCED_UNBLOCK) {
                val oppMovesAfter = generateLegalMoves(opp, simBoard)
                if (oppMovesAfter.isEmpty()) {
                    score += 135000
                } else {
                    score -= oppMovesAfter.size * 1400
                }
            }

            // 3. Instant Blockade Win check (when AI has >= 2 eaten pieces)
            if (!BoardTopology.hasValidMoves(opp, simBoard) && (myEaten + 1) >= 2) {
                score += 120000
            }

            // 4. Does removing `idx` unlock an immediate 1-step Shuttle Automatic Win or Mill for AI?
            for (mv in generateLegalMoves(me, simBoard)) {
                simBoard[mv.to] = me
                simBoard[mv.from] = null
                if (BoardTopology.evaluateAutomaticWin(mv.to, me, simBoard).isAutoWin) {
                    score += 48000
                } else if (BoardTopology.checkMill(mv.to, me, simBoard)) {
                    score += 4600
                }
                simBoard[mv.from] = me
                simBoard[mv.to] = null
            }

            // 5. Does removing `idx` destroy opponent's Shuttle Automatic Win or sliding Mill threat?
            var oppAutoAfter = 0
            var oppMillAfter = 0
            for (mv in generateLegalMoves(opp, simBoard)) {
                simBoard[mv.to] = opp
                simBoard[mv.from] = null
                if (BoardTopology.evaluateAutomaticWin(mv.to, opp, simBoard).isAutoWin) oppAutoAfter++
                else if (BoardTopology.checkMill(mv.to, opp, simBoard)) oppMillAfter++
                simBoard[mv.from] = opp
                simBoard[mv.to] = null
            }
            if (oppAutoBefore > oppAutoAfter) {
                score += (oppAutoBefore - oppAutoAfter) * 42000
            }
            if (oppMillBefore > oppMillAfter) {
                score += (oppMillBefore - oppMillAfter) * 7200
            }

            // 6. Multi-ply lookahead on the resulting board
            score += evaluatePlayingBoard(simBoard, me, opp, myEaten + 1, oppEaten)
            val lookDepth = when (difficulty) {
                AiDifficulty.MASTER -> 4
                AiDifficulty.HARD -> 3
                AiDifficulty.MEDIUM -> 2
                AiDifficulty.EASY -> 1
            }

            if (state.phase == GamePhase.INITIAL_REMOVE_1) {
                val oppVictim = chooseBestCaptureSim(simBoard, opp, me, oppEaten, myEaten + 1)
                if (oppVictim != -1) {
                    simBoard[oppVictim] = null
                    val firstMover = state.firstPicker ?: me
                    score += (minimaxAlphaBeta(
                        simBoard,
                        lookDepth - 1,
                        -1_000_000,
                        1_000_000,
                        firstMover == me,
                        me,
                        opp,
                        myEaten + 1,
                        oppEaten + 1
                    ) * 7) / 10
                }
            } else {
                val nextMover = if (state.phase == GamePhase.INITIAL_REMOVE_2) (state.firstPicker ?: me) else opp
                score += (minimaxAlphaBeta(
                    simBoard,
                    lookDepth,
                    -1_000_000,
                    1_000_000,
                    nextMover == me,
                    me,
                    opp,
                    myEaten + 1,
                    oppEaten
                ) * 3) / 4
            }

            if (score > highestScore) {
                highestScore = score
                bestTarget = idx
            }
        }
        return bestTarget
    }

    /**
     * Finds the best sliding move using Iterative Deepening + Zobrist Transposition Table up to 5-6 plies.
     */
    fun findBestMove(state: GameState, player: Player, difficulty: AiDifficulty): AiMove? {
        val opp = player.opponent()
        val board = state.board.toTypedArray()
        val allMoves = generateLegalMoves(player, board)
        if (allMoves.isEmpty()) return null

        val myEaten = if (player == Player.P1) state.p1Eaten else state.p2Eaten
        val oppEaten = if (opp == Player.P1) state.p1Eaten else state.p2Eaten

        // 1. Immediate Automatic Win (Shuttle 3-in-a-row or Double Mill) check at depth 1
        for (mv in allMoves) {
            board[mv.to] = player
            board[mv.from] = null
            val isAuto = BoardTopology.evaluateAutomaticWin(mv.to, player, board).isAutoWin
            board[mv.from] = player
            board[mv.to] = null
            if (isAuto) return mv
        }

        // 2. Immediate Blockade Win check at depth 1
        if (myEaten >= 2) {
            for (mv in allMoves) {
                board[mv.to] = player
                board[mv.from] = null
                val oppHasMoves = BoardTopology.hasValidMoves(opp, board)
                board[mv.from] = player
                board[mv.to] = null
                if (!oppHasMoves) return mv
            }
        }

        searchStartTimeMs = System.currentTimeMillis()
        timeBudgetMs = difficulty.timeBudgetMs
        transpositionTable.clear()

        // Initial static ordering of root candidates
        val rootCandidates = ArrayList<AiMove>(allMoves.size)
        for (mv in allMoves) {
            board[mv.to] = player
            board[mv.from] = null
            var qSc = evaluatePlayingBoard(board, player, opp, myEaten, oppEaten)
            if (BoardTopology.checkMill(mv.to, player, board)) qSc += 2500
            board[mv.from] = player
            board[mv.to] = null
            rootCandidates.add(AiMove(mv.from, mv.to, qSc))
        }
        rootCandidates.sortByDescending { it.priority }

        var bestMoveOverall = rootCandidates[0]
        val recentHistory = state.moveHistory.takeLast(6)
        val staticEvalNow = evaluatePlayingBoard(board, player, opp, myEaten, oppEaten)

        // Iterative Deepening from depth 2 up to difficulty.maxMoveDepth (up to 6 plies in MASTER)
        for (currentDepth in 2..difficulty.maxMoveDepth) {
            if (isTimeUp() && currentDepth > 2) break

            var bestMoveThisDepth = rootCandidates[0]
            var bestScoreThisDepth = -1_000_000
            var alpha = -1_000_000
            val beta = 1_000_000

            for (mv in rootCandidates) {
                if (isTimeUp() && currentDepth > 2) break

                board[mv.to] = player
                board[mv.from] = null

                val moveScore: Int = if (BoardTopology.checkMill(mv.to, player, board)) {
                    val capIdx = chooseBestCaptureSim(board, player, opp, myEaten, oppEaten)
                    if (capIdx != -1) {
                        board[capIdx] = null
                        var oppRem = 0
                        for (k in 0 until 24) if (board[k] == opp) oppRem++
                        val sc = if (oppRem < 3) {
                            95000 + currentDepth * 500
                        } else {
                            minimaxAlphaBeta(
                                board,
                                currentDepth - 1,
                                alpha,
                                beta,
                                false,
                                player,
                                opp,
                                myEaten + 1,
                                oppEaten
                            )
                        }
                        board[capIdx] = opp
                        sc
                    } else {
                        95000
                    }
                } else {
                    minimaxAlphaBeta(
                        board,
                        currentDepth - 1,
                        alpha,
                        beta,
                        false,
                        player,
                        opp,
                        myEaten,
                        oppEaten
                    )
                }

                board[mv.from] = player
                board[mv.to] = null

                var adjustedScore = moveScore
                val moveKey = "${player.name}:${mv.from}->${mv.to}"
                if (recentHistory.contains(moveKey) && adjustedScore < 50000) {
                    // Draw / Repetition awareness:
                    // If AI is ahead or equal, penalize repetition so AI presses for a win;
                    // if AI is behind in a dangerous position, allow fortress repetition to hold the draw!
                    if (staticEvalNow >= -150) {
                        adjustedScore -= 240
                    } else {
                        adjustedScore += 90
                    }
                }

                if (adjustedScore > bestScoreThisDepth) {
                    bestScoreThisDepth = adjustedScore
                    bestMoveThisDepth = AiMove(mv.from, mv.to, adjustedScore)
                }
                if (adjustedScore > alpha) alpha = adjustedScore
            }

            if (!isTimeUp() || currentDepth == 2) {
                bestMoveOverall = bestMoveThisDepth
            }
        }

        return bestMoveOverall
    }

    private fun minimaxAlphaBeta(
        board: Array<Player?>,
        depth: Int,
        alphaInit: Int,
        betaInit: Int,
        isMaximizing: Boolean,
        me: Player,
        opp: Player,
        myEaten: Int,
        oppEaten: Int
    ): Int {
        val boardHash = computeBoardHash(board, isMaximizing)
        val cached = transpositionTable[boardHash]
        if (cached != null && cached.depth >= depth) {
            when (cached.flag) {
                0 -> return cached.score
                1 -> if (cached.score >= betaInit) return cached.score
                2 -> if (cached.score <= alphaInit) return cached.score
            }
        }

        if (depth <= 0 || isTimeUp()) {
            return evaluatePlayingBoard(board, me, opp, myEaten, oppEaten)
        }

        val activePlayer = if (isMaximizing) me else opp
        val otherPlayer = if (isMaximizing) opp else me
        val activeEaten = if (isMaximizing) myEaten else oppEaten
        val otherEaten = if (isMaximizing) oppEaten else myEaten

        val rawMoves = generateLegalMoves(activePlayer, board)
        if (rawMoves.isEmpty()) {
            if (otherEaten >= 2) {
                return if (isMaximizing) (-88000 - depth * 500) else (88000 + depth * 500)
            }
            val unblockIdx = chooseBestCaptureSim(board, otherPlayer, activePlayer, otherEaten, activeEaten)
            if (unblockIdx == -1) {
                return if (isMaximizing) -88000 else 88000
            }
            board[unblockIdx] = null
            val stillBlocked = !BoardTopology.hasValidMoves(activePlayer, board)
            val valAfter = if (stillBlocked) {
                if (isMaximizing) (-84000 - depth * 400) else (84000 + depth * 400)
            } else {
                evaluatePlayingBoard(
                    board,
                    me,
                    opp,
                    if (isMaximizing) myEaten else myEaten + 1,
                    if (isMaximizing) oppEaten + 1 else oppEaten
                )
            }
            board[unblockIdx] = activePlayer
            return valAfter
        }

        val orderedMoves = ArrayList<AiMove>(rawMoves.size)
        for (mv in rawMoves) {
            board[mv.to] = activePlayer
            board[mv.from] = null
            var prio = BoardTopology.adjacency[mv.to].size * 8
            if (BoardTopology.evaluateAutomaticWin(mv.to, activePlayer, board).isAutoWin) {
                prio += 18000
            } else if (BoardTopology.checkMill(mv.to, activePlayer, board)) {
                prio += 3600
            }
            if (activeEaten >= 2 && !BoardTopology.hasValidMoves(otherPlayer, board)) {
                prio += 14000
            }
            board[mv.from] = activePlayer
            board[mv.to] = null
            orderedMoves.add(AiMove(mv.from, mv.to, prio))
        }
        orderedMoves.sortByDescending { it.priority }

        var alpha = alphaInit
        var beta = betaInit
        val bestValue: Int

        if (isMaximizing) {
            var maxEval = -1_000_000
            for (mv in orderedMoves) {
                board[mv.to] = me
                board[mv.from] = null

                val ev: Int = if (BoardTopology.evaluateAutomaticWin(mv.to, me, board).isAutoWin) {
                    100000 + depth * 1000
                } else if (myEaten >= 2 && !BoardTopology.hasValidMoves(opp, board)) {
                    90000 + depth * 900
                } else if (BoardTopology.checkMill(mv.to, me, board)) {
                    val capIdx = chooseBestCaptureSim(board, me, opp, myEaten, oppEaten)
                    if (capIdx != -1) {
                        board[capIdx] = null
                        var oppRem = 0
                        for (k in 0 until 24) if (board[k] == opp) oppRem++
                        val sub = if (oppRem < 3) {
                            94000 + depth * 1000
                        } else {
                            minimaxAlphaBeta(board, depth - 1, alpha, beta, false, me, opp, myEaten + 1, oppEaten)
                        }
                        board[capIdx] = opp
                        sub
                    } else {
                        94000 + depth * 1000
                    }
                } else {
                    minimaxAlphaBeta(board, depth - 1, alpha, beta, false, me, opp, myEaten, oppEaten)
                }

                board[mv.from] = me
                board[mv.to] = null

                if (ev > maxEval) maxEval = ev
                if (ev > alpha) alpha = ev
                if (beta <= alpha) break
            }
            bestValue = maxEval
        } else {
            var minEval = 1_000_000
            for (mv in orderedMoves) {
                board[mv.to] = opp
                board[mv.from] = null

                val ev: Int = if (BoardTopology.evaluateAutomaticWin(mv.to, opp, board).isAutoWin) {
                    -100000 - depth * 1000
                } else if (oppEaten >= 2 && !BoardTopology.hasValidMoves(me, board)) {
                    -90000 - depth * 900
                } else if (BoardTopology.checkMill(mv.to, opp, board)) {
                    val capIdx = chooseBestCaptureSim(board, opp, me, oppEaten, myEaten)
                    if (capIdx != -1) {
                        board[capIdx] = null
                        var myRem = 0
                        for (k in 0 until 24) if (board[k] == me) myRem++
                        val sub = if (myRem < 3) {
                            -94000 - depth * 1000
                        } else {
                            minimaxAlphaBeta(board, depth - 1, alpha, beta, true, me, opp, myEaten, oppEaten + 1)
                        }
                        board[capIdx] = me
                        sub
                    } else {
                        -94000 - depth * 1000
                    }
                } else {
                    minimaxAlphaBeta(board, depth - 1, alpha, beta, true, me, opp, myEaten, oppEaten)
                }

                board[mv.from] = opp
                board[mv.to] = null

                if (ev < minEval) minEval = ev
                if (ev < beta) beta = ev
                if (beta <= alpha) break
            }
            bestValue = minEval
        }

        if (transpositionTable.size < 16000) {
            val flag = when {
                bestValue <= alphaInit -> 2
                bestValue >= betaInit -> 1
                else -> 0
            }
            transpositionTable[boardHash] = TtEntry(depth, bestValue, flag)
        }
        return bestValue
    }
}
