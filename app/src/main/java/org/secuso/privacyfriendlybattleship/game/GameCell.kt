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
    var col: Int //Column of the Cell
        private set
    var row: Int //Row of the Cell
        private set
    @JvmField
    var isShip: Boolean = false //false if this cell contains water, true if it contains a ship
    var isHit: Boolean = false //false if this cell was not hit yet, true if it was
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

    val resourceId: Int
        get() {
            if (!this.isShip) {
                return 0
            }

            val ship =
                grid!!.shipSet.findShipContainingCell(this)
            when (ship!!.orientation) {
                Direction.NORTH -> {
                    if (this == ship.firstCell) {
                        //return North-start
                        return R.drawable.ship_front_up
                    }
                    if (this == ship.lastCell) {
                        //return North-end
                        return R.drawable.ship_back_up
                    }
                    return R.drawable.ship_middle_up
                }

                Direction.EAST -> {
                    if (this == ship.firstCell) {
                        //return East-start
                        return R.drawable.ship_front_right
                    }
                    if (this == ship.lastCell) {
                        //return East-end
                        return R.drawable.ship_back_right
                    }
                    return R.drawable.ship_middle_right
                }

                Direction.SOUTH -> {
                    if (this == ship.firstCell) {
                        //return South-start
                        return R.drawable.ship_front_down
                    }
                    if (this == ship.lastCell) {
                        //return South-end
                        return R.drawable.ship_back_down
                    }
                    return R.drawable.ship_middle_down
                }

                Direction.WEST -> {
                    if (this == ship.firstCell) {
                        //return West-start
                        return R.drawable.ship_front_left
                    }
                    if (this == ship.lastCell) {
                        //return West-end
                        return R.drawable.ship_back_left
                    }
                    return R.drawable.ship_middle_left
                }
            }
            return R.drawable.ic_info_black_24dp
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
        val shipHit = BooleanArray(2)
        parcel.readBooleanArray(shipHit)
        this.isShip = shipHit[0]
        this.isHit = shipHit[1]
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
