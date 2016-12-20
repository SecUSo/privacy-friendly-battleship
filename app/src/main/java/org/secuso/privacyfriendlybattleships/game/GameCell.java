package org.secuso.privacyfriendlybattleships.game;

import static java.lang.Math.max;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameCell {

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
}
