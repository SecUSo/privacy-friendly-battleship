package org.secuso.privacyfriendlybattleship.game;

import android.os.Parcel;
import android.os.Parcelable;

import org.secuso.privacyfriendlybattleship.R;

import static java.lang.Math.max;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameCell implements Parcelable{

    private int col;//Column of the Cell
    private int row;//Row of the Cell
    private boolean isShip = false;//false if this cell contains water, true if it contains a ship
    private boolean isHit = false;//false if this cell was not hit yet, true if it was
    private GameGrid grid;


    public GameCell(int col, int row, GameGrid grid) {
        this.col = col;
        this.row = row;
        this.grid = grid;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public GameGrid getGrid() {
        return grid;
    }

    public boolean isShip() {
        return isShip;
    }

    public boolean isHit() {
        return isHit;
    }

    public void setShip(boolean ship) {
        isShip = ship;
    }

    public void setHit(boolean hit) {
        isHit = hit;
    }

    /**
     * Returns true if the cells are adjacent to each other or have the same coordinates and false
     * if they have at least one cell in between. Cells diagonal to each other are considered
     * adjacent.
     * @param other Cell to compare to
     * @return True if the the given cell is adjacent
     */
    public boolean isNextTo(GameCell other) {
        int distance = max( Math.abs(this.col - other.getCol() ), Math.abs(this.row - other.getRow() ) );
        if (distance > 1)
            return false;
        return true;
    }

    public int getResourceId() {
        if (!this.isShip) {
            return 0;
        }

        GameShip ship = this.getGrid().getShipSet().findShipContainingCell(this);
        switch (ship.getOrientation()) {
            case NORTH:
                if (this.equals( ship.getFirstCell() )){
                    //return North-start
                    return R.drawable.ship_front_up;
                }
                if (this.equals( ship.getLastCell() )){
                    //return North-end
                    return R.drawable.ship_back_up;
                }
                return R.drawable.ship_middle_up;
            case EAST:
                if (this.equals( ship.getFirstCell() )){
                    //return East-start
                    return R.drawable.ship_front_right;
                }
                if (this.equals( ship.getLastCell() )){
                    //return East-end
                    return R.drawable.ship_back_right;
                }
                return R.drawable.ship_middle_right;
            case SOUTH:
                if (this.equals( ship.getFirstCell() )){
                    //return South-start
                    return R.drawable.ship_front_down;
                }
                if (this.equals( ship.getLastCell() )){
                    //return South-end
                    return R.drawable.ship_back_down;
                }
                return R.drawable.ship_middle_down;
            case WEST:
                if (this.equals( ship.getFirstCell() )){
                    //return West-start
                    return R.drawable.ship_front_left;
                }
                if (this.equals( ship.getLastCell() )){
                    //return West-end
                    return R.drawable.ship_back_left;
                }
                return R.drawable.ship_middle_left;
        }
        return R.drawable.ic_info_black_24dp;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.col);
        out.writeInt(this.row);
        out.writeBooleanArray( new boolean[] {this.isShip, this.isHit} );
    }

    public static final Parcelable.Creator<GameCell> CREATOR = new Parcelable.Creator<GameCell>() {
        public GameCell createFromParcel(Parcel in) {
            return new GameCell(in);
        }

        public GameCell[] newArray(int size) {
            return new GameCell[size];
        }
    };

    private GameCell(Parcel in) {
        this.col = in.readInt();
        this.row = in.readInt();
        boolean[] shipHit = new boolean[2];
        in.readBooleanArray(shipHit);
        this.isShip = shipHit[0];
        this.isHit = shipHit[1];
        this.grid = null;
    }

    void setGrid(GameGrid grid) {
        this.grid = grid;
    }

}
