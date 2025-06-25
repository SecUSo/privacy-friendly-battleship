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
package org.secuso.privacyfriendlybattleship.game

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.Random

/**
 * This class represents a players grid for the battleships game. It
 * provides access to its cells or its set of ships and is assigned to
 * one of the two players (or the AI) of the game.
 *
 * @author Alexander Müller, Ali Kalsen
 */
class GameGrid : Parcelable {
    private var cellGrid: Array<Array<GameCell?>?>
    var size: Int
        private set
    var shipSet: GameShipSet
        private set

    constructor(size: Int, shipCount: IntArray) {
        this.size = size
        this.cellGrid = Array(this.size) { arrayOfNulls(this.size) }
        this.shipSet = GameShipSet(
            this,
            shipCount[0], shipCount[1], shipCount[2], shipCount[3]
        )

        for (i in 0 until size) {
            for (j in 0 until size) {
                cellGrid[i]!![j] = GameCell(i, j, this)
            }
        }
    }

    /**
     * Returns the cell at the given row and column. Rows and columns start with 0.
     * @param col Column of the cell to be returned
     * @param row Row of the cell to be returned
     * @return The cell at the given row and column
     */
    fun getCell(col: Int, row: Int): GameCell {
        require(!(col >= size || row >= size || col < 0 || row < 0)) { "Column or row exceeds the limits of the grid." }
        return cellGrid[col]!![row]!!
    }

    val randomCell: GameCell
        get() {
            val ranGen = Random()
            return this.getCell(
                ranGen.nextInt(this.size),
                ranGen.nextInt(this.size)
            )
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(this.size)
        for (i in 0 until this.size) {
            out.writeTypedArray(cellGrid[i], 0)
        }

        out.writeTypedArray(arrayOf(this.shipSet), 0)
    }

    private constructor(parcel: Parcel) {
        this.size = parcel.readInt()
        this.cellGrid = Array(this.size) { arrayOfNulls(this.size) }
        for (i in 0 until this.size) {
            cellGrid[i] = parcel.createTypedArray(GameCell.CREATOR)
        }
        for (i in 0 until this.size) {
            for (j in 0 until this.size) {
                cellGrid[i]!![j]!!.grid = this
            }
        }

        this.shipSet = parcel.createTypedArray(GameShipSet.CREATOR)!![0]
        shipSet.recreateShipSet(this)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<GameGrid> = object : Creator<GameGrid> {
            override fun createFromParcel(parcel: Parcel): GameGrid {
                return GameGrid(parcel)
            }

            override fun newArray(size: Int): Array<GameGrid?> {
                return arrayOfNulls(size)
            }
        }
    }
}
