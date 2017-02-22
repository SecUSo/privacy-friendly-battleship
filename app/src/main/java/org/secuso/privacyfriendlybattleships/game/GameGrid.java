package org.secuso.privacyfriendlybattleships.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alexander Müller on 16.12.2016. Edited by Ali Kalsen on 16.01.2017
 */

public class GameGrid implements Parcelable{

    // GameGrids needed for the main activity and quick start
    private final static int SIZE_5x5 = 5;
    private final static int SIZE_10x10 = 10;


    //private final int resIDString;
    private static List<Integer> validSizes = new LinkedList<>();

    static{
        validSizes.add(SIZE_5x5);
        validSizes.add(SIZE_10x10);
    }

    private GameCell[][] cellGrid;
    private int size;
    private GameShipSet shipSet;

    public GameGrid(int size, int[] shipCount) {
        this.size = size;
        this.cellGrid = new GameCell[this.size][this.size];
        this.shipSet = new GameShipSet(this, shipCount[0], shipCount[1], shipCount[2], shipCount[3]);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.cellGrid[i][j] = new GameCell(i, j, this);
            }
        }
    }

    /**
     * Returns the cell at the given row and column. Rows and columns start with 0.
     * @param col Column of the cell to be returned
     * @param row Row of the cell to be returned
     * @return The cell at the given row and column
     */
    public GameCell getCell (int col, int row) {
        if(col >= size || row >= size || col < 0 || row < 0) {
            throw new IllegalArgumentException("Column or row exceeds the limits of the grid.");
        }
        return cellGrid[col][row];
    }

    public GameShipSet getShipSet() {
        return shipSet;
    }

    public int getSize() {
        return size;
    }

    public static List<Integer> getValidSizes(){
        return validSizes;
    }

    public GameCell getRandomCell() {
        Random ranGen = new Random();
        return this.getCell( ranGen.nextInt(this.size), ranGen.nextInt(this.size) );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.size);
        for (int i = 0; i < this.size; i++) {
            out.writeTypedArray(this.cellGrid[i], 0);
        }

        out.writeTypedArray(new GameShipSet[] {this.shipSet}, 0);
    }

    public static final Parcelable.Creator<GameGrid> CREATOR = new Parcelable.Creator<GameGrid>() {
        public GameGrid createFromParcel(Parcel in) {
            return new GameGrid(in);
        }

        public GameGrid[] newArray(int size) {
            return new GameGrid[size];
        }
    };

    private GameGrid(Parcel in) {
        this.size = in.readInt();
        this.cellGrid = new GameCell[this.size][this.size];
        for (int i = 0; i < this.size; i++) {
            this.cellGrid[i] = in.createTypedArray(GameCell.CREATOR);
        }
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                this.cellGrid[i][j].setGrid(this);
            }
        }

        this.shipSet = in.createTypedArray(GameShipSet.CREATOR)[0];
        this.shipSet.recreateShipSet(this);
    }
}
