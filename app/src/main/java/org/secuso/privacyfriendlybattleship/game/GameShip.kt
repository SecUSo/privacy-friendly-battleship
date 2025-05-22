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

/**
 * This class represents a ship of the battleships game. Each ship
 * consists of two or more cells and can be moved using the methods
 * provided in this class
 *
 * @author Alexander Müller, Ali Kalsen
 */
class GameShip : Parcelable {
    var size: Int
        private set
    lateinit var shipsCells: Array<GameCell>
        private set
    var orientation: Direction
        private set
    private var grid: GameGrid? = null
    private var shipSet: GameShipSet? = null

    private var startCellCol: Int
    private var startCellRow: Int

    constructor(
        grid: GameGrid,
        shipSet: GameShipSet?,
        shipStart: GameCell,
        shipSize: Int,
        shipOrientation: Direction
    ) {
        require(
            argumentsValid(
                shipStart,
                shipSize,
                shipOrientation,
                grid.size
            )
        ) { "The ship exceeds the limits of the game field" }

        this.size = shipSize
        this.orientation = shipOrientation
        this.grid = grid
        this.shipSet = shipSet
        this.startCellCol = shipStart.col
        this.startCellRow = shipStart.row

        //initialize shipsCells with cells of the ship
        initializeShipsCells()
    }

    private fun initializeShipsCells() {
        val shipCellList = mutableListOf<GameCell>()

        when (this.orientation) {
            Direction.NORTH -> {
                for (i in 0 until this.size) {
                    shipCellList.add(grid!!.getCell(this.startCellCol, this.startCellRow + i))
                }
            }
            Direction.SOUTH -> {
                for (i in 0 until this.size) {
                    shipCellList.add(grid!!.getCell(this.startCellCol, this.startCellRow - i))
                }
            }
            Direction.EAST -> {
                for (i in 0 until this.size) {
                    shipCellList.add(grid!!.getCell(this.startCellCol - i, this.startCellRow))
                }
            }
            Direction.WEST -> {
                for (i in 0 until this.size) {
                    shipCellList.add(grid!!.getCell(this.startCellCol + i, this.startCellRow))
                }
            }
        }
        shipsCells = shipCellList.toTypedArray()

        for (i in shipsCells.indices) {
            shipsCells[i].isShip = true
        }
    }

    val firstCell: GameCell
        get() = shipsCells[0]

    val lastCell: GameCell
        get() = shipsCells[size - 1]

    val isDestroyed: Boolean
        get() {
            for (i in shipsCells.indices) {
                if (!shipsCells[i].isHit) {
                    return false
                }
            }
            return true
        }

    fun containsCell(cell: GameCell): Boolean {
        for (i in shipsCells.indices) {
            if (cell == shipsCells[i]) return true
        }
        return false
    }

    /**
     * Returns true if the ships have at least one cell in between, false if they are adjacent or
     * overlapping. Diagonal cells are considered adjacent.
     * @param other ship to compare to
     * @return true if the given ship is not in contact
     */
    fun keepsDistanceTo(other: GameShip): Boolean {
        for (i in 0 until this.size) {
            val otherCells = other.shipsCells
            for (j in 0 until other.size) {
                if (shipsCells[i].isNextTo(otherCells[j])) return false
            }
        }
        return true
    }

    /**
     * Marks all cells of the ship as water except the ones with a ship-collision. Call this method
     * before deleting the Object.
     */
    fun close() {
        for (cell in this.shipsCells) {
            if (shipSet!!.shipsOnCell(cell) == 1) cell.isShip = false
        }
    }

    fun moveShip(direction: Direction) {
        var col = -1
        var row = -1
        when (direction) {
            Direction.NORTH -> {
                col = startCellCol
                row = startCellRow - 1
            }

            Direction.EAST -> {
                col = startCellCol + 1
                row = startCellRow
            }

            Direction.SOUTH -> {
                col = startCellCol
                row = startCellRow + 1
            }

            Direction.WEST -> {
                col = startCellCol - 1
                row = startCellRow
            }
        }

        if (col < 0 || col >= grid!!.size || row < 0 || row >= grid!!.size) {
            return
        }

        if (!argumentsValid(
                grid!!.getCell(col, row),
                this.size,
                this.orientation,
                grid!!.size
            )
        ) {
            return
        }

        this.close()
        this.startCellCol = col
        this.startCellRow = row
        this.initializeShipsCells()
    }

    fun turnShipRight() {
        val middleCellIndex = this.size / 2
        var newOrientation = Direction.NORTH
        var newStartCol = startCellCol
        var newStartRow = startCellRow
        when (this.orientation) {
            Direction.NORTH -> {
                newOrientation = Direction.EAST
                newStartCol = this.startCellCol + middleCellIndex
                newStartRow = this.startCellRow + middleCellIndex
                if (newStartCol < this.size - 1) newStartCol = this.size - 1
                if (newStartCol > grid!!.size - 1) newStartCol =
                    grid!!.size - 1
            }

            Direction.EAST -> {
                newOrientation = Direction.SOUTH
                newStartCol = this.startCellCol - middleCellIndex
                newStartRow = this.startCellRow + middleCellIndex
                if (newStartRow < this.size - 1) newStartRow = this.size - 1
                if (newStartRow > grid!!.size - 1) newStartRow =
                    grid!!.size - 1
            }

            Direction.SOUTH -> {
                newOrientation = Direction.WEST
                newStartCol = this.startCellCol - middleCellIndex
                newStartRow = this.startCellRow - middleCellIndex
                if (newStartCol < 0) newStartCol = 0
                if (newStartCol + this.size - 1 > grid!!.size - 1) newStartCol =
                    grid!!.size - this.size
            }

            Direction.WEST -> {
                newOrientation = Direction.NORTH
                newStartCol = this.startCellCol + middleCellIndex
                newStartRow = this.startCellRow - middleCellIndex
                if (newStartRow < 0) newStartRow = 0
                if (newStartRow + this.size - 1 > grid!!.size - 1) newStartRow =
                    grid!!.size - this.size
            }
        }

        this.close()
        this.orientation = newOrientation
        this.startCellCol = newStartCol
        this.startCellRow = newStartRow
        this.initializeShipsCells()
    }

    fun turnShipLeft() {
        val middleCellIndex = this.size / 2
        var newOrientation = Direction.NORTH
        var newStartCol = startCellCol
        var newStartRow = startCellRow
        when (this.orientation) {
            Direction.NORTH -> {
                newOrientation = Direction.WEST
                newStartCol = this.startCellCol - middleCellIndex
                newStartRow = this.startCellRow + middleCellIndex
                if (newStartCol < 0) newStartCol = 0
                if (newStartCol + this.size - 1 > grid!!.size - 1) newStartCol =
                    grid!!.size - this.size
            }

            Direction.EAST -> {
                newOrientation = Direction.NORTH
                newStartCol = this.startCellCol - middleCellIndex
                newStartRow = this.startCellRow - middleCellIndex
                if (newStartRow < 0) newStartRow = 0
                if (newStartRow + this.size - 1 > grid!!.size - 1) newStartRow =
                    grid!!.size - this.size
            }

            Direction.SOUTH -> {
                newOrientation = Direction.EAST
                newStartCol = this.startCellCol + middleCellIndex
                newStartRow = this.startCellRow - middleCellIndex
                if (newStartCol < this.size - 1) newStartCol = this.size - 1
                if (newStartCol > grid!!.size - 1) newStartCol =
                    grid!!.size - 1
            }

            Direction.WEST -> {
                newOrientation = Direction.SOUTH
                newStartCol = this.startCellCol + middleCellIndex
                newStartRow = this.startCellRow + middleCellIndex
                if (newStartRow < this.size - 1) newStartRow = this.size - 1
                if (newStartRow > grid!!.size - 1) newStartRow =
                    grid!!.size - 1
            }
        }

        this.close()
        this.orientation = newOrientation
        this.startCellCol = newStartCol
        this.startCellRow = newStartRow
        this.initializeShipsCells()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(this.size)
        out.writeString(orientation.name)
        out.writeInt(shipsCells[0].col)
        out.writeInt(shipsCells[0].row)
    }

    private constructor(parcel: Parcel) {
        this.size = parcel.readInt()
        this.orientation = Direction.valueOf(
            parcel.readString()!!
        )
        this.startCellCol = parcel.readInt()
        this.startCellRow = parcel.readInt()
        //recreateShip has to be called for the ship to be fully recovered.
    }

    fun recreateShip(grid: GameGrid?, set: GameShipSet?) {
        this.grid = grid
        this.shipSet = set

        initializeShipsCells()
    }

    companion object {
        fun argumentsValid(
            shipStart: GameCell?,
            shipSize: Int,
            orientation: Direction?,
            gridSize: Int
        ): Boolean {
            if (shipStart == null || orientation == null) {
                return false
            }
            if ((orientation == Direction.NORTH) &&
                ((shipStart.row + (shipSize - 1)) >= gridSize) || (orientation == Direction.SOUTH) && ((shipStart.row - (shipSize - 1)) < 0) || (orientation == Direction.EAST) && ((shipStart.col - (shipSize - 1)) < 0) || (orientation == Direction.WEST) && ((shipStart.col + (shipSize - 1)) >= gridSize)
            ) {
                return false
            }
            return true
        }

        @JvmField
        val CREATOR: Creator<GameShip> = object : Creator<GameShip> {
            override fun createFromParcel(parcel: Parcel): GameShip {
                return GameShip(parcel)
            }

            override fun newArray(size: Int): Array<GameShip?> {
                return arrayOfNulls(size)
            }
        }
    }
}
