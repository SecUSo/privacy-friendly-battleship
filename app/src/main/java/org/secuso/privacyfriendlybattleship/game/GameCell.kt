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
    along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.secuso.privacyfriendlybattleship.game

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import org.secuso.privacyfriendlybattleship.R
import kotlin.math.abs
import kotlin.math.max

/**
 * This class represents a cell of an battleships grid. A grid of size N
 * consists of N*N cells, each of which can be either water or part of a
 * ship.
 *
 * @author Alexander Müller, Ali Kalsen
 */
class GameCell : Parcelable {
    /** Column of the Cell */
    var col: Int
        private set
    /** Row of the Cell */
    var row: Int
        private set
    /** false if this cell contains water, true if it contains a ship */
    @JvmField
    var isShip: Boolean = false
    /** false if this cell was not hit yet, true if it was */
    var isHit: Boolean = false
    var grid: GameGrid?

    constructor(col: Int, row: Int, grid: GameGrid?) {
        this.col = col
        this.row = row
        this.grid = grid
    }

    /**
     * Returns true if the cells are adjacent to each other or have the same coordinates and false
     * if they have at least one cell in between. Cells diagonal to each other are considered
     * adjacent.
     * @param other Cell to compare to
     * @return True if the the given cell is adjacent
     */
    fun isNextTo(other: GameCell): Boolean {
        val distance = max(
            abs((this.col - other.col).toDouble()),
            abs((this.row - other.row).toDouble())
        ).toInt()
        return distance <= 1
    }

    fun getImage(): Bitmap {
        val ship = grid!!.shipSet.findShipContainingCell(this)
        return if (null != ship) {
            GameResources.getShipBitmap(ship.first.orientation, ship.first.size, ship.second)
        } else {
            GameResources.DUMMY_BITMAP
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(this.col)
        out.writeInt(this.row)
        out.writeBooleanArray(booleanArrayOf(this.isShip, this.isHit))
    }

    private constructor(parcel: Parcel) {
        this.col = parcel.readInt()
        this.row = parcel.readInt()
        val booleanFlags = BooleanArray(2)
        parcel.readBooleanArray(booleanFlags)
        this.isShip = booleanFlags[0]
        this.isHit = booleanFlags[1]
        this.grid = null
    }

    companion object {
        @JvmField
        val CREATOR: Creator<GameCell> = object : Creator<GameCell> {
            override fun createFromParcel(parcel: Parcel): GameCell {
                return GameCell(parcel)
            }

            override fun newArray(size: Int): Array<GameCell?> {
                return arrayOfNulls(size)
            }
        }
    }
}
