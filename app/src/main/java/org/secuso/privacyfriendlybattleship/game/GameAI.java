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

/**
 * This class represents the AI for the battleships game. The AI can be
 * initialized in one of two difficulty levels and and will make its
 * moves accordingly.
 *
 * @author Alexander Müller, Ali Kalsen
 */

package org.secuso.privacyfriendlybattleship.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameAI implements Parcelable{

    private enum Cell {
        UNKNOWN(0), WATER(1), SHIP(2);

        int val;
        Cell(int i) {
            val = i;
        }
    }

    private int[][] gridUnderAttack;//represents the opponents grid; 0: unknown, 1: ship, 2: water
    private boolean hasAIWon;
    private int gridSize;
    private GameMode mode;
    private GameController controller;
    private Random ranGen;
    private List<int[]> shipCandidates = new ArrayList();


    public GameAI(int gridSize, GameMode mode, GameController controller) {
        if (mode == GameMode.VS_PLAYER) {
            throw new IllegalArgumentException("No AI possible in player vs player matches.");
        }
        this.gridSize = gridSize;
        this.gridUnderAttack = new int[this.gridSize][this.gridSize];
        this.mode = mode;
        this.controller = controller;

        //initialize local grid
        for(int i = 0; i < this.gridSize*this.gridSize; i++) {
            this.gridUnderAttack[i / this.gridSize][i % this.gridSize] = Cell.UNKNOWN.val;
        }

        //initialize random number generator
        this.ranGen = new Random();
        this.hasAIWon = false;
    }

    public void makeMove() {
        if(this.mode == GameMode.VS_AI_EASY) {
            while ( makeRandomMove() ) {};
            this.controller.switchPlayers();
        } else if(this.mode == GameMode.VS_AI_HARD) {
            while ( makeSmartMove() ) {};
            this.controller.switchPlayers();
        }
    }

    private boolean makeRandomMove(){
        int col;
        int row;

        //get random coordinate to attack
        do {
            col = ranGen.nextInt(this.gridSize);
            row = ranGen.nextInt(this.gridSize);
        } while (this.gridUnderAttack[col][row] != Cell.UNKNOWN.val);

        //attack opponent and update local grid
        boolean isHit = this.controller.makeMove(true, col, row);

        if ( isHit ) {
            this.gridUnderAttack[col][row] = Cell.SHIP.val;
            // Check if the AI has won set hasAIWon to true in that case.
            if (this.controller.gridUnderAttack().getShipSet().allShipsDestroyed() ){
                this.hasAIWon = true;
                return false;
            }
        } else {
            this.gridUnderAttack[col][row] = Cell.WATER.val;
        }

        return isHit;
    }

    private boolean makeSmartMove() {
        if ( this.shipCandidates.isEmpty() ){
            return makeSearchingMove();
        } else {
            return makeCandidateMove();
        }
    }

    private boolean makeSearchingMove(){
        int col;
        int row;

        //get random coordinate to attack; choose no adjacent coordinates;
        do {
            col = ranGen.nextInt(this.gridSize);
            row = ranGen.nextInt(this.gridSize);
        } while (this.gridUnderAttack[col][row] != Cell.UNKNOWN.val || (col + row)%2 != 1 );

        //attack opponent and update local grid
        boolean isHit = this.controller.makeMove(true, col, row);

        if ( isHit ) {
            this.gridUnderAttack[col][row] = Cell.SHIP.val;

            //add adjacent cells to candidates
            if (isValidTarget(col - 1, row)) this.shipCandidates.add(new int[] {col-1, row});
            if (isValidTarget(col + 1, row)) this.shipCandidates.add(new int[] {col+1, row});
            if (isValidTarget(col, row - 1)) this.shipCandidates.add(new int[] {col, row-1});
            if (isValidTarget(col, row + 1)) this.shipCandidates.add(new int[] {col, row+1});

            // Check if the AI has won
            if (this.controller.gridUnderAttack().getShipSet().allShipsDestroyed() ){
                this.hasAIWon = true;
                return false;
            }
        } else {
            this.gridUnderAttack[col][row] = Cell.WATER.val;
        }

        return isHit;
    }

    private boolean makeCandidateMove() {
        int index = this.ranGen.nextInt(this.shipCandidates.size());
        int col = this.shipCandidates.get(index)[0];
        int row = this.shipCandidates.get(index)[1];
        this.shipCandidates.remove(index);

        //attack opponent and update local grid
        if (!isValidTarget(col, row)){
            return true;
        }
        boolean isHit = this.controller.makeMove(true, col, row);

        if ( isHit ) {
            this.gridUnderAttack[col][row] = Cell.SHIP.val;

            //add adjacent cells to candidates
            if (isValidTarget(col - 1, row)) this.shipCandidates.add(new int[] {col-1, row});
            if (isValidTarget(col + 1, row)) this.shipCandidates.add(new int[] {col+1, row});
            if (isValidTarget(col, row - 1)) this.shipCandidates.add(new int[] {col, row-1});
            if (isValidTarget(col, row + 1)) this.shipCandidates.add(new int[] {col, row+1});

            // Check if the AI has won set hasAIWon to true in that case.
            if (this.controller.gridUnderAttack().getShipSet().allShipsDestroyed() ){
                this.hasAIWon = true;
                return false;
            }
        } else {
            this.gridUnderAttack[col][row] = Cell.WATER.val;
        }

        return isHit;
    }

    private boolean isValidTarget(int col, int row) {
        if (col < 0 || col >= this.gridSize || row < 0 || row >= this.gridSize)
            return false;
        if (this.gridUnderAttack[col][row] != Cell.UNKNOWN.val)
            return false;
        return true;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.gridSize);
        out.writeString(this.mode.name());
        for( int i = 0; i < this.gridSize; i++) {
            out.writeIntArray(this.gridUnderAttack[i]);
        }
    }

    public static final Parcelable.Creator<GameAI> CREATOR = new Parcelable.Creator<GameAI>() {
        public GameAI createFromParcel(Parcel in) {
            return new GameAI(in);
        }

        public GameAI[] newArray(int size) {
            return new GameAI[size];
        }
    };

    private GameAI(Parcel in) {
        this.gridSize = in.readInt();
        this.mode = GameMode.valueOf( in.readString() );
        this.gridUnderAttack = new int[this.gridSize][this.gridSize];
        for ( int i = 0; i < this.gridSize; i++) {
            this.gridUnderAttack[i] = in.createIntArray();
        }

        this.ranGen = new Random();
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public boolean isAIWinner(){
        return this.hasAIWon;
    }
}