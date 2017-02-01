package org.secuso.privacyfriendlybattleships.game;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameShip implements Parcelable{
    private int size;
    private GameCell[] shipsCells;
    private Direction orientation;
    private GameGrid grid;
    private GameShipSet shipSet;

    private int startCellCol;
    private int startCellRow;

    public GameShip(GameGrid grid, GameShipSet shipSet, GameCell shipStart, int shipSize, Direction shipOrientation) {
        if (    (this.orientation == Direction.NORTH) && ( (shipStart.getRow() + (shipSize - 1) ) >= grid.getSize() ) ||
                (this.orientation == Direction.SOUTH) && ( (shipStart.getRow() - (shipSize - 1) ) < 0 ) ||
                (this.orientation == Direction.EAST) && ( (shipStart.getCol() - (shipSize - 1) ) < 0 ) ||
                (this.orientation == Direction.WEST) && ( (shipStart.getCol() + (shipSize - 1) ) >= grid.getSize())) {
            throw new IllegalArgumentException("The ship exceeds the limits of the game field");
        }

        this.size = shipSize;
        this.orientation = shipOrientation;
        this.grid = grid;
        this.shipSet = shipSet;
        this.startCellCol = shipStart.getCol();
        this.startCellRow = shipStart.getRow();

        //initialize shipsCells with cells of the ship
       initializeShipsCells();
    }

    private void initializeShipsCells() {
        this.shipsCells = new GameCell[this.size];

        if (this.orientation == Direction.NORTH) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(this.startCellCol, this.startCellRow + i);
            }
        } else if (this.orientation == Direction.SOUTH) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(this.startCellCol, this.startCellRow - i);
            }
        } else if (this.orientation == Direction.EAST) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(this.startCellCol - i, this.startCellRow);
            }
        } else if (this.orientation == Direction.WEST) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(this.startCellCol + i, this.startCellRow);
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
        for(GameCell cell : this.shipsCells){
            if ( this.shipSet.shipsOnCell(cell) == 1) cell.setShip(false);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.size);
        out.writeString(this.orientation.name());
        out.writeInt(this.shipsCells[0].getCol());
        out.writeInt(this.shipsCells[0].getRow());
    }

    public static final Parcelable.Creator<GameShip> CREATOR = new Parcelable.Creator<GameShip>() {
        public GameShip createFromParcel(Parcel in) {
            return new GameShip(in);
        }

        public GameShip[] newArray(int size) {
            return new GameShip[size];
        }
    };

    private GameShip(Parcel in) {
        this.size = in.readInt();
        this.orientation = Direction.valueOf(in.readString());
        this.startCellCol = in.readInt();
        this.startCellRow = in.readInt();
        //recreateShip has to be called for the ship to be fully recovered.
    }

    void recreateShip(GameGrid grid, GameShipSet set) {
        this.grid = grid;
        this.shipSet = set;

        initializeShipsCells();
    }
}
