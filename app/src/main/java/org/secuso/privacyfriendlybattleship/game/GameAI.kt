/*
    Copyright 2017 Alexander Müller, Ali Kalsen

    This file is part of Privacy Friendly Battleships.

    Privacy Friendly Battleships is free software: you can redistribute
    it and/or modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Privacy Friendly Battleships is distributed in the hope that it will
    be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see http://www.gnu.org/licenses/.
 */
/**
 * This class represents the AI for the battleships game. The AI can be
 * initialized in one of two difficulty levels and and will make its
 * moves accordingly.
 *
 * @author Alexander Müller, Ali Kalsen
 */
package org.secuso.privacyfriendlybattleship.game

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.Random

/**
 * Created by Alexander Müller on 16.12.2016.
 */
class GameAI : Parcelable {
    private enum class Cell(var `val`: Int) {
        UNKNOWN(0), WATER(1), SHIP(2)
    }

    private var gridUnderAttack: Array<IntArray?> //represents the opponents grid; 0: unknown, 1: ship, 2: water
    var isAIWinner: Boolean = false
        private set
    private var gridSize: Int
    private var mode: GameMode
    private var controller: GameController? = null
    private var ranGen: Random
    private val shipCandidates = mutableListOf<IntArray>()


    constructor(gridSize: Int, mode: GameMode, controller: GameController?) {
        require(mode != GameMode.VS_PLAYER) { "No AI possible in player vs player matches." }
        this.gridSize = gridSize
        this.gridUnderAttack = Array(this.gridSize) { IntArray(this.gridSize) }
        this.mode = mode
        this.controller = controller

        //initialize local grid
        for (i in 0 until this.gridSize * this.gridSize) {
            gridUnderAttack[i / this.gridSize]!![i % this.gridSize] = Cell.UNKNOWN.`val`
        }

        //initialize random number generator
        this.ranGen = Random()
        this.isAIWinner = false
    }

    fun makeMove() {
        if (this.mode == GameMode.VS_AI_EASY) {
            makeRandomMove()
            controller!!.switchPlayers()
        } else if (this.mode == GameMode.VS_AI_HARD) {
            makeSmartMove()
            controller!!.switchPlayers()
        }
    }

    private fun makeRandomMove(): Boolean {
        var col: Int
        var row: Int

        //get random coordinate to attack
        do {
            col = ranGen.nextInt(this.gridSize)
            row = ranGen.nextInt(this.gridSize)
        } while (gridUnderAttack[col]!![row] != Cell.UNKNOWN.`val`)

        //attack opponent and update local grid
        val isHit = controller!!.makeMove(true, col, row)

        if (isHit) {
            gridUnderAttack[col]!![row] = Cell.SHIP.`val`
            // Check if the AI has won set hasAIWon to true in that case.
            if (controller!!.gridUnderAttack().shipSet.allShipsDestroyed()) {
                this.isAIWinner = true
                return false
            }
        } else {
            gridUnderAttack[col]!![row] = Cell.WATER.`val`
        }

        return isHit
    }

    private fun makeSmartMove(): Boolean {
        return if (shipCandidates.isEmpty()) {
            makeSearchingMove()
        } else {
            makeCandidateMove()
        }
    }

    private fun makeSearchingMove(): Boolean {
        var col: Int
        var row: Int

        //get random coordinate to attack; choose no adjacent coordinates;
        do {
            col = ranGen.nextInt(this.gridSize)
            row = ranGen.nextInt(this.gridSize)
        } while (gridUnderAttack[col]!![row] != Cell.UNKNOWN.`val` || (col + row) % 2 != 1)

        //attack opponent and update local grid
        val isHit = controller!!.makeMove(true, col, row)

        if (isHit) {
            gridUnderAttack[col]!![row] = Cell.SHIP.`val`

            //add adjacent cells to candidates
            if (isValidTarget(col - 1, row)) shipCandidates.add(intArrayOf(col - 1, row))
            if (isValidTarget(col + 1, row)) shipCandidates.add(intArrayOf(col + 1, row))
            if (isValidTarget(col, row - 1)) shipCandidates.add(intArrayOf(col, row - 1))
            if (isValidTarget(col, row + 1)) shipCandidates.add(intArrayOf(col, row + 1))

            // Check if the AI has won
            if (controller!!.gridUnderAttack().shipSet.allShipsDestroyed()) {
                this.isAIWinner = true
                return false
            }
        } else {
            gridUnderAttack[col]!![row] = Cell.WATER.`val`
        }

        return isHit
    }

    private fun makeCandidateMove(): Boolean {
        val index = ranGen.nextInt(shipCandidates.size)
        val col = shipCandidates[index][0]
        val row = shipCandidates[index][1]
        shipCandidates.removeAt(index)

        //attack opponent and update local grid
        if (!isValidTarget(col, row)) {
            return true
        }
        val isHit = controller!!.makeMove(true, col, row)

        if (isHit) {
            gridUnderAttack[col]!![row] = Cell.SHIP.`val`

            //add adjacent cells to candidates
            if (isValidTarget(col - 1, row)) shipCandidates.add(intArrayOf(col - 1, row))
            if (isValidTarget(col + 1, row)) shipCandidates.add(intArrayOf(col + 1, row))
            if (isValidTarget(col, row - 1)) shipCandidates.add(intArrayOf(col, row - 1))
            if (isValidTarget(col, row + 1)) shipCandidates.add(intArrayOf(col, row + 1))

            // Check if the AI has won set hasAIWon to true in that case.
            if (controller!!.gridUnderAttack().shipSet.allShipsDestroyed()) {
                this.isAIWinner = true
                return false
            }
        } else {
            gridUnderAttack[col]!![row] = Cell.WATER.`val`
        }

        return isHit
    }

    private fun isValidTarget(col: Int, row: Int): Boolean {
        if (col < 0 || col >= this.gridSize || row < 0 || row >= this.gridSize) return false
        if (gridUnderAttack[col]!![row] != Cell.UNKNOWN.`val`) return false
        return true
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(this.gridSize)
        out.writeString(mode.name)
        for (i in 0 until this.gridSize) {
            out.writeIntArray(gridUnderAttack[i])
        }
    }

    private constructor(parcel: Parcel) {
        this.gridSize = parcel.readInt()
        this.mode = GameMode.valueOf(parcel.readString()!!)
        this.gridUnderAttack = Array(this.gridSize) { IntArray(this.gridSize) }
        for (i in 0 until this.gridSize) {
            gridUnderAttack[i] = parcel.createIntArray()
        }

        this.ranGen = Random()
    }

    fun setController(controller: GameController?) {
        this.controller = controller
    }

    companion object {
        @JvmField
        val CREATOR: Creator<GameAI> = object : Creator<GameAI> {
            override fun createFromParcel(parcel: Parcel): GameAI {
                return GameAI(parcel)
            }

            override fun newArray(size: Int): Array<GameAI?> {
                return arrayOfNulls(size)
            }
        }
    }
}