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

package org.secuso.privacyfriendlybattleship.game;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents the set of ships of a player for the battleships
 * game. It is initialized with the amount of ships of each possible ship
 * size and can place the ships randomly on the game grid.
 *
 * @author Alexander Müller, Ali Kalsen
 */

public class GameShipSet implements Parcelable{
    private GameShip[][] ships;
    private GameShip[] size2Ships;
    private GameShip[] size3Ships;
    private GameShip[] size4Ships;
    private GameShip[] size5Ships;
    private int totalShipCount;
    private GameGrid grid;

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

    public GameShipSet(GameGrid grid, int shipsSize2, int shipsSize3, int shipsSize4, int shipsSize5) {
        this.grid = grid;
        this.size2Ships = new GameShip[shipsSize2];
        this.size3Ships = new GameShip[shipsSize3];
        this.size4Ships = new GameShip[shipsSize4];
        this.size5Ships = new GameShip[shipsSize5];
        this.ships = new GameShip[][] {this.size2Ships, this.size3Ships, this.size4Ships, this.size5Ships};
        this.totalShipCount = shipsSize2 + shipsSize3 + shipsSize4 + shipsSize5;
    }

    public int getTotalShipCount() {
        return totalShipCount;
    }

    /**
     * Returns true if all ships of this set are destroyed and therefore the corresponding player has lost.
     * @return True if all ships are destroyed, false if not
     */
    public boolean allShipsDestroyed() {
        for (GameShip[] shipsSizeN : this.ships) {
            for (GameShip ship : shipsSizeN) {
                if ( ship == null ) continue;
                if ( !ship.isDestroyed() ) return false;
            }
        }
        return true;
    }

    /**
     * Places an ship on the grid. The ship starts at the given row and column and expands to the
     * back.
     * @param startCol The column of the ships front cell
     * @param startRow The row of the ships front cell
     * @param size The size of the ship
     * @param direction The direction the ship is facing
     */
    public void placeShip(int startCol, int startRow, int size, Direction direction) {
        if (size < 2 || size > 5) throw new IllegalArgumentException("Illegal ship-size.");

        //get free slot for ship
        int shipIndex;
        for (shipIndex = 0; shipIndex < this.ships[size - 2].length; shipIndex++)
            if (this.ships[size - 2][shipIndex] == null) break;
        if (shipIndex == this.ships[size - 2].length)
            throw new IllegalArgumentException("All ships of this size already placed.");

        this.ships[size - 2][shipIndex] = new GameShip(this.grid, this, this.grid.getCell(startCol, startRow), size, direction);
    }

    /**
     * Places all ships randomly on the grid. The resulting placement will be legit according to the
     * rules of the game. Ships that have already been placed will be overwritten. This method may
     * not terminate if the amount of ships is chosen to be higher than with normal game rules.
     */
    public void placeShipsRandomly() {
        for (int i = this.ships.length - 1; i >= 0; i--) {
            for (int j = 0; j < this.ships[i].length; j++) {
                if (this.ships[i][j] != null) {
                    this.ships[i][j].close();
                }
                this.ships[i][j] = this.getRandomShip(i + 2);
                while ( !this.placementLegit() ) {
                    this.ships[i][j].close();
                    this.ships[i][j] = this.getRandomShip(i + 2);
                }
            }
        }
    }

    private GameShip getRandomShip(int size) {GameCell cell;
        Direction orientation;
        do {
            cell = grid.getRandomCell();
            orientation = Direction.getRandomDirection();
        } while ( !GameShip.argumentsValid(cell, size, orientation, grid.getSize() ) );

        return new GameShip( grid, this, cell, size, orientation );
    }

    public boolean allShipsPlaced() {
        for (GameShip[] shipsSizeN : this.ships) {
            for (GameShip ship : shipsSizeN) {
                if ( ship == null ) return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all ships are placed correctly according to the rules, false if not.
     * @return True if ship-placement is legit, false if not
     */
    public boolean placementLegit() {
        for (int i = 0; i < this.ships.length; i++) {
            for (int j = 0; j < this.ships[i].length; j++) {
                if (this.ships[i][j] == null)
                    continue;

                for (GameCell cell : this.ships[i][j].getShipsCells() ){
                    if ( this.shipsOnCell(cell) > 1 ) return false;
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
        return true;
    }

    /**
     * Returns the number of ships on the given cell.
     * @param cell The cell to count ships on
     * @return Amount if ships ob the given cell
     */
    public int shipsOnCell(GameCell cell) {
        int count = 0;
        for (GameShip[] shipsSizeN : this.ships) {
            for (GameShip ship : shipsSizeN) {
                if ( ship == null )
                    continue;
                if ( ship.containsCell(cell) )
                    count++;
            }
        }
        return count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        for (int i = 0; i < this.ships.length; i++) {
            out.writeTypedArray(this.ships[i], 0);
        }
    }

    public static final Parcelable.Creator<GameShipSet> CREATOR = new Parcelable.Creator<GameShipSet>() {
        public GameShipSet createFromParcel(Parcel in) {
            return new GameShipSet(in);
        }

        public GameShipSet[] newArray(int size) {
            return new GameShipSet[size];
        }
    };

    public GameShipSet(Parcel in) {
        this.size2Ships = in.createTypedArray(GameShip.CREATOR);
        this.size3Ships = in.createTypedArray(GameShip.CREATOR);
        this.size4Ships = in.createTypedArray(GameShip.CREATOR);
        this.size5Ships = in.createTypedArray(GameShip.CREATOR);
        this.ships = new GameShip[][] { size2Ships, size3Ships, size4Ships, size5Ships };
        this.totalShipCount = size2Ships.length + size3Ships.length + size4Ships.length + size5Ships.length;
        //recreateShipSet has to be called for this.grid and ships to be recovered.
    }

    void recreateShipSet(GameGrid grid) {
        this.grid = grid;

        for (GameShip[] shipsSizeN : this.ships) {
            for (GameShip ship : shipsSizeN) {
                if(ship != null)
                        ship.recreateShip(this.grid, this);
            }
        }
    }

    /**
     * Finds the ship, which contains the cell.
     * @param gameCell: The cell which is assigned to at most one ship.
     * @return The ship containing gameCell.
     */
    public GameShip findShipContainingCell(GameCell gameCell){
        GameShip shiptoFind = null;
        if(gameCell.isShip()){
            for(GameShip[] shipSizeN : this.ships){
                for(GameShip ship : shipSizeN){
                    if(ship.containsCell(gameCell)){
                        return ship;
                    }
                }
            }
        }
        return null;
    }

    public int getNumberOfShipsSize2(){
        return size2Ships.length;
    }

    public int getNumberOfShipsSize3(){
        return size3Ships.length;
    }

    public int getNumberOfShipsSize4(){
        return size4Ships.length;
    }

    public int getNumberOfShipsSize5(){
        return size5Ships.length;
    }
}
