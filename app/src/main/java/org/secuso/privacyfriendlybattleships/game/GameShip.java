package org.secuso.privacyfriendlybattleships.game;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameShip {
    private int size;
    private GameCell[] shipsCells;
    private Direction orientation;
    private GameGrid grid;
    private GameShipSet shipSet;

    public GameShip(GameGrid grid, GameShipSet shipSet, GameCell shipStart, int shipSize, Direction shipOrientation) {
        if (    (this.orientation == Direction.NORTH) && ( (shipStart.getRow() + (shipSize - 1) ) >= grid.getSize() ) ||
                (this.orientation == Direction.SOUTH) && ( (shipStart.getRow() - (shipSize - 1) ) < 0 ) ||
                (this.orientation == Direction.EAST) && ( (shipStart.getCol() - (shipSize - 1) ) < 0 ) ||
                (this.orientation == Direction.WEST) && ( (shipStart.getCol() + (shipSize - 1) ) >= grid.getSize())) {
            throw new IllegalArgumentException("The ship exceeds the limits of the game field");
        }

        this.size = shipSize;
        this.orientation = shipOrientation;
        this.shipsCells = new GameCell[this.size];
        this.grid = grid;
        this.shipSet = shipSet;


        //initialize shipsCells with cells of the ship
        if (this.orientation == Direction.NORTH) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(shipStart.getCol(), shipStart.getRow() + i);
            }
        } else if (this.orientation == Direction.SOUTH) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(shipStart.getCol(), shipStart.getRow() - i);
            }
        } else if (this.orientation == Direction.EAST) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(shipStart.getCol() - i, shipStart.getRow());
            }
        } else if (this.orientation == Direction.WEST) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(shipStart.getCol() + i, shipStart.getRow());
            }
        }

        for (int i = 0; i < this.shipsCells.length; i++) {
            this.shipsCells[i].setShip(true);
        }
    }

    public int getSize() {
        return size;
    }

    public GameCell[] getShipsCells() {
        return shipsCells;
    }

    public boolean isDestroyed() {
        for (int i = 0; i < shipsCells.length; i++) {
            if ( !shipsCells[i].isHit() ) {
                return false;
            }
        }
        return true;
    }

    public boolean containsCell(GameCell cell) {
        for (int i = 0; i < this.shipsCells.length; i++) {
            if ( cell.equals(this.shipsCells[i]) ) return true;
        }
        return false;
    }

    /**
     * Returns true if the ships have at least one cell in between, false if they are adjacent or
     * overlapping. Diagonal cells are considered adjacent.
     * @param other ship to compare to
     * @return true if the given ship is not in contact
     */
    public boolean keepsDistanceTo(GameShip other) {
        for (int i = 0; i < this.size; i++) {
            GameCell[] otherCells = other.getShipsCells();
            for (int j = 0; j < other.getSize(); j++) {
                if ( this.shipsCells[i].isNextTo(otherCells[j]) )
                    return false;
            }
        }
        return true;
    }

    /**
     * Marks all cells of the ship as water except the ones with a ship-collision. Call this method
     * before deleting the Object.
     */
    public void close() {
        for (int i = 0; i < this.size; i++) {
            if ( this.shipSet.shipsOnCell(this.shipsCells[i]) == 1) this.shipsCells[i].setShip(false);
        }
    }
}
