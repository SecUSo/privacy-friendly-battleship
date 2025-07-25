package org.secuso.privacyfriendlybattleship.game

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import kotlin.math.floor

/**
 * Created by Alexander Müller on 16.12.2016.
 */
class GameController : Parcelable {
    var attemptsPlayerOne: Int
        private set
    var attemptsPlayerTwo: Int
        private set
    private var timePlayerOne: BattleshipsTimer
    private var timePlayerTwo: BattleshipsTimer

    var gridFirstPlayer: GameGrid
        private set
    var gridSecondPlayer: GameGrid
        private set
    var gridSize: Int
        private set
    var mode: GameMode
        private set
    var currentPlayer: Boolean //false if first players turn, true if second players turn
        private set
    var opponentAI: GameAI? = null
        private set
    lateinit var shipCount: IntArray
        private set

    constructor(gameMode: GameMode, gridSize: Int, shipCount: IntArray) {
        this.gridSize = gridSize
        this.mode = gameMode
        this.currentPlayer = false
        this.shipCount = shipCount

        this.gridFirstPlayer = GameGrid(gridSize, this.shipCount)
        this.gridSecondPlayer = GameGrid(gridSize, this.shipCount)

        if (this.mode == GameMode.VS_AI_EASY || this.mode == GameMode.VS_AI_HARD) {
            this.opponentAI = GameAI(this.gridSize, this.mode, this)
        } else if (this.mode == GameMode.VS_PLAYER) {
            this.opponentAI = null
        }
        this.timePlayerOne = BattleshipsTimer()
        this.timePlayerTwo = BattleshipsTimer()
        this.attemptsPlayerOne = 0
        this.attemptsPlayerTwo = 0
    }

    /**
     * This constructor is called in the MainActivity
     * @param gridSize: The size of the game board
     * @param mode: The game mode
     */
    constructor(gridSize: Int, mode: GameMode) {
        require(mode != GameMode.CUSTOM) { "Provide ship-count for custom game-mode." }
        require(!(gridSize != 5 && gridSize != 10)) { "Provide ship-count for custom game-size." }
        this.gridSize = gridSize
        this.currentPlayer = false
        this.mode = mode

        when (gridSize) {
            5 -> {
                this.shipCount = SHIPCOUNTFIVE
                this.gridFirstPlayer = GameGrid(gridSize, SHIPCOUNTFIVE)
                this.gridSecondPlayer = GameGrid(gridSize, SHIPCOUNTFIVE)
            }

            else -> {
                this.shipCount = SHIPCOUNTTEN
                this.gridFirstPlayer = GameGrid(gridSize, SHIPCOUNTTEN)
                this.gridSecondPlayer = GameGrid(gridSize, SHIPCOUNTTEN)
            }
        }

        if (this.mode == GameMode.VS_AI_EASY || this.mode == GameMode.VS_AI_HARD) {
            this.opponentAI = GameAI(this.gridSize, this.mode, this)
        } else if (this.mode == GameMode.VS_PLAYER) {
            this.opponentAI = null
        }
        this.timePlayerOne = BattleshipsTimer()
        this.timePlayerTwo = BattleshipsTimer()
        this.attemptsPlayerOne = 0
        this.attemptsPlayerTwo = 0
    }

    /**
     * Places all ships for both players randomly, resulting in a legit placement to start the game.
     */
    fun placeAllShips() {
        gridFirstPlayer.shipSet.placeShipsRandomly()
        gridSecondPlayer.shipSet.placeShipsRandomly()
    }

    /**
     * Performs the move for the current player.
     * @param player Current player. False for player one, true for player two.
     * @param col Column that shall be attacked.
     * @param row Row that shall be attacked.
     * @return True if move was a hit, false if not.
     */
    fun makeMove(player: Boolean, col: Int, row: Int): Boolean {
        require(this.currentPlayer == player) { "It is the other players turn." }

        val cellUnderAttack = gridUnderAttack().getCell(col, row)
        require(!cellUnderAttack.isHit) { "This cell has already been attacked" }

        //mark cell hit
        cellUnderAttack.isHit = true
        increaseAttempts()

        //return if move was a hit
        return cellUnderAttack.isShip
    }

    fun switchPlayers() {
        //prepare for next turn
        this.currentPlayer = !this.currentPlayer
    }

    /**
     * Returns the grid attacked by the current player. Note the difference between the methods
     * gridUnderAttack() and getCurrentGrid(). The former is mainly used in the GameActvity and GameAI.
     * @return The grid attacked
     */
    fun gridUnderAttack(): GameGrid {
        return if (this.currentPlayer) gridFirstPlayer else gridSecondPlayer
    }

    val currentGrid: GameGrid
        /**
         * Returns the grid of the current player. Used in the GameGridAdapter and the PlaceShipActivity.
         * @return grid of current player
         */
        get() {
            if (!this.currentPlayer) {
                return gridFirstPlayer
            }
            return gridSecondPlayer
        }

    fun isShipCountLegit(shipCount: IntArray): Boolean {
        // The current bound for the numer of cells covered by the ships is set to the half of the
        // total amount of grid cells, such that the probability of randomly hitting a ship is at most 1/2.
        val bound = floor((gridSize * gridSize * 2 / 5).toDouble()).toInt()
        val coveredGridCells =
            2 * shipCount[0] + 3 * shipCount[1] + 4 * shipCount[2] + 5 * shipCount[3]
        return coveredGridCells <= bound
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(this.gridSize)
        out.writeString(mode.name)
        out.writeBooleanArray(booleanArrayOf(this.currentPlayer))
        out.writeTypedArray(arrayOf(this.gridFirstPlayer, this.gridSecondPlayer), 0)
        out.writeTypedArray(arrayOf(this.opponentAI), 0)
    }

    private constructor(parcel: Parcel) {
        this.gridSize = parcel.readInt()
        this.mode = GameMode.valueOf(parcel.readString()!!)
        this.currentPlayer = parcel.createBooleanArray()!![0]
        val grids = parcel.createTypedArray(GameGrid.CREATOR)
        this.gridFirstPlayer = grids!![0]
        this.gridSecondPlayer = grids[1]

        this.opponentAI = parcel.createTypedArray(GameAI.CREATOR)!![0]
        if (this.opponentAI != null) {
            opponentAI!!.setController(this)
        }
        this.timePlayerOne = BattleshipsTimer()
        this.timePlayerTwo = BattleshipsTimer()
        this.attemptsPlayerOne = 0
        this.attemptsPlayerTwo = 0
    }

    fun increaseAttempts() {
        if (currentPlayer) {
            this.attemptsPlayerTwo += 1
        } else {
            this.attemptsPlayerOne += 1
        }
    }

    fun startTimer() {
        if (currentPlayer) {
            timePlayerTwo.start()
        } else {
            timePlayerOne.start()
        }
    }

    fun stopTimer() {
        timePlayerOne.stop()
        timePlayerTwo.stop()
    }

    val time: Int
        get() = if (mode == GameMode.VS_AI_EASY || mode == GameMode.VS_AI_HARD) {
            timePlayerOne.time
        } else {
            if (currentPlayer) timePlayerTwo.time else timePlayerOne.time
        }

    fun timeToString(time: Int): String {
        val seconds = time % 60
        val minutes = ((time - seconds) / 60) % 60
        val s = if ((seconds < 10)) "0$seconds" else seconds.toString()
        val m = if ((minutes < 10)) "0$minutes" else minutes.toString()
        return "$m:$s"
    }

    fun attemptsToString(attempts: Int): String {
        return if ((attempts < 10)) "0$attempts" else attempts.toString()
    }

    companion object {
        // Amount of ships for standard grid sizes.
        private val SHIPCOUNTFIVE = intArrayOf(2, 1, 0, 0)
        private val SHIPCOUNTTEN = intArrayOf(1, 2, 1, 1)

        @JvmField
        val CREATOR: Creator<GameController> = object : Creator<GameController> {
            override fun createFromParcel(parcel: Parcel): GameController {
                return GameController(parcel)
            }

            override fun newArray(size: Int): Array<GameController?> {
                return arrayOfNulls(size)
            }
        }
    }
}
