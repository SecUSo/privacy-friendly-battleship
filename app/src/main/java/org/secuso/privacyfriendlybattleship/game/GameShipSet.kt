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
 * This class represents the set of ships of a player for the battleships
 * game. It is initialized with the amount of ships of each possible ship
 * size and can place the ships randomly on the game grid.
 *
 * @author Alexander Müller, Ali Kalsen
 */
class GameShipSet : Parcelable {
    private var ships: Array<Array<GameShip?>>
    private var size2Ships: Array<GameShip?>
    private var size3Ships: Array<GameShip?>
    private var size4Ships: Array<GameShip?>
    private var size5Ships: Array<GameShip?>
    var totalShipCount: Int
        private set
    private var grid: GameGrid? = null

    /*
    public GameShipSet(GameGrid grid){
        this.grid = grid;
        this.size2Ships = new GameShip[1];
        this.size3Ships = new GameShip[2];
        this.size4Ships = new GameShip[1];
        this.size5Ships = new GameShip[1];
        this.ships = new GameShip[][] {this.size2Ships, this.size3Ships, this.size4Ships, this.size5Ships};
        this.totalShipCount = 5;
    }
    */
    constructor(
        grid: GameGrid?,
        shipsSize2: Int,
        shipsSize3: Int,
        shipsSize4: Int,
        shipsSize5: Int
    ) {
        this.grid = grid
        this.size2Ships = arrayOfNulls(shipsSize2)
        this.size3Ships = arrayOfNulls(shipsSize3)
        this.size4Ships = arrayOfNulls(shipsSize4)
        this.size5Ships = arrayOfNulls(shipsSize5)
        this.ships = arrayOf(this.size2Ships, this.size3Ships, this.size4Ships, this.size5Ships)
        this.totalShipCount = shipsSize2 + shipsSize3 + shipsSize4 + shipsSize5
    }

    /**
     * Returns true if all ships of this set are destroyed and therefore the corresponding player has lost.
     * @return True if all ships are destroyed, false if not
     */
    fun allShipsDestroyed(): Boolean {
        for (shipsSizeN in this.ships) {
            for (ship in shipsSizeN) {
                if (ship == null) continue
                if (!ship.isDestroyed) return false
            }
        }
        return true
    }

    /**
     * Places an ship on the grid. The ship starts at the given row and column and expands to the
     * back.
     * @param startCol The column of the ships front cell
     * @param startRow The row of the ships front cell
     * @param size The size of the ship
     * @param direction The direction the ship is facing
     */
    fun placeShip(startCol: Int, startRow: Int, size: Int, direction: Direction) {
        require(!(size < 2 || size > 5)) { "Illegal ship-size." }

        //get free slot for ship
        var shipIndex = 0
        while (shipIndex < ships[size - 2].size) {
            if (ships[size - 2][shipIndex] == null) break
            shipIndex++
        }
        require(shipIndex != ships[size - 2].size) { "All ships of this size already placed." }

        ships[size - 2][shipIndex] = GameShip(this.grid!!, this,
            grid!!.getCell(startCol, startRow), size, direction)
    }

    /**
     * Places all ships randomly on the grid. The resulting placement will be legit according to the
     * rules of the game. Ships that have already been placed will be overwritten. This method may
     * not terminate if the amount of ships is chosen to be higher than with normal game rules.
     */
    fun placeShipsRandomly() {
        for (i in ships.indices.reversed()) {
            for (j in ships[i].indices) {
                ships[i][j]?.close()
                ships[i][j] = this.getRandomShip(i + 2)
                while (!this.placementLegit()) {
                    ships[i][j]?.close()
                    ships[i][j] = this.getRandomShip(i + 2)
                }
            }
        }
    }

    private fun getRandomShip(size: Int): GameShip {
        var cell: GameCell?
        var orientation: Direction?
        do {
            cell = grid!!.randomCell
            orientation = Direction.getRandomDirection()
        } while (!GameShip.argumentsValid(cell, size, orientation, grid!!.size))

        return GameShip(grid!!, this, cell!!, size, orientation!!)
    }

    fun allShipsPlaced(): Boolean {
        for (shipsSizeN in this.ships) {
            for (ship in shipsSizeN) {
                if (ship == null) return false
            }
        }
        return true
    }

    /**
     * Returns true if all ships are placed correctly according to the rules, false if not.
     * @return True if ship-placement is legit, false if not
     */
    fun placementLegit(): Boolean {
        for (i in ships.indices) {
            for (j in ships[i].indices) {
                val ship = ships[i][j] ?: continue

                for (cell in ship.shipsCells) {
                    if (this.shipsOnCell(cell) > 1) return false
                }
            }
        }

        /* //implementation for ship-distance of at least one cell
        for (int i = 0; i < this.ships.length; i++) {
            for (int j = 0; j < this.ships[i].length; j++) {
                if (this.ships[i][j] == null)
                    continue;

                for (int k = 0; k < this.ships.length; k++) {
                    for (int l = 0; l < this.ships[k].length; l++) {
                        if ( this.ships[k][l] == null || this.ships[k][l].equals(this.ships[i][j]) )
                            continue;

                        if ( !this.ships[i][j].keepsDistanceTo(this.ships[k][l]) )
                            return false;
                    }
                }
            }
        }
        */
        return true
    }

    /**
     * Returns the number of ships on the given cell.
     * @param cell The cell to count ships on
     * @return Amount if ships ob the given cell
     */
    fun shipsOnCell(cell: GameCell): Int {
        var count = 0
        for (shipsSizeN in this.ships) {
            for (ship in shipsSizeN) {
                if (ship == null) continue
                if (ship.containsCell(cell)) count++
            }
        }
        return count
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        for (i in ships.indices) {
            out.writeTypedArray(ships[i], 0)
        }
    }

    constructor(parcelIn: Parcel) {
        this.size2Ships = parcelIn.createTypedArray(GameShip.CREATOR)!!
        this.size3Ships = parcelIn.createTypedArray(GameShip.CREATOR)!!
        this.size4Ships = parcelIn.createTypedArray(GameShip.CREATOR)!!
        this.size5Ships = parcelIn.createTypedArray(GameShip.CREATOR)!!
        this.ships = arrayOf(size2Ships, size3Ships, size4Ships, size5Ships)
        this.totalShipCount =
            size2Ships.size + size3Ships.size + size4Ships.size + size5Ships.size
        //recreateShipSet has to be called for this.grid and ships to be recovered.
    }

    fun recreateShipSet(grid: GameGrid?) {
        this.grid = grid

        for (shipsSizeN in this.ships) {
            for (ship in shipsSizeN) {
                ship?.recreateShip(this.grid, this)
            }
        }
    }

    /**
     * Finds the ship, which contains the cell.
     * @param gameCell: The cell which is assigned to at most one ship.
     * @return The ship containing gameCell.
     */
    fun findShipContainingCell(gameCell: GameCell): GameShip? {
        val shiptoFind: GameShip? = null
        if (gameCell.isShip) {
            for (shipSizeN in this.ships) {
                for (ship in shipSizeN) {
                    if (null != ship && ship.containsCell(gameCell)) {
                        return ship
                    }
                }
            }
        }
        return null
    }

    val numberOfShipsSize2: Int
        get() = size2Ships.size

    val numberOfShipsSize3: Int
        get() = size3Ships.size

    val numberOfShipsSize4: Int
        get() = size4Ships.size

    val numberOfShipsSize5: Int
        get() = size5Ships.size

    companion object {
        @JvmField
        val CREATOR: Creator<GameShipSet> = object : Creator<GameShipSet> {
            override fun createFromParcel(parcel: Parcel): GameShipSet {
                return GameShipSet(parcel)
            }

            override fun newArray(size: Int): Array<GameShipSet?> {
                return arrayOfNulls(size)
            }
        }
    }
}
