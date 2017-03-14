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
 * This class represents the battleships game and provides the possibility
 * to make turns, switch the player and access underlying classes for
 * ship placement, grids or AI.
 *
 * @author Alexander Müller, Ali Kalsen
 */

public class GameController implements Parcelable {

    private int attemptsPlayerOne;
    private int attemptsPlayerTwo;
    private BattleshipsTimer timePlayerOne;
    private BattleshipsTimer timePlayerTwo;

    private GameGrid gridFirstPlayer;
    private GameGrid gridSecondPlayer;
    private int gridSize;
    private GameMode mode;
    private boolean currentPlayer;//false if first players turn, true if second players turn
    private GameAI opponentAI;

    // Amount of ships for standard grid sizes.
    private final static int[] SHIPCOUNTFIVE = {2,1,0,0};
    private final static int[] SHIPCOUNTTEN = {1,2,1,1};

    public GameController(int gridSize, int[] shipCount) {
        this.gridSize = gridSize;
        this.mode = GameMode.CUSTOM;
        this.currentPlayer = false;
        this.opponentAI = null; //only player vs player with custom game
        this.gridFirstPlayer = new GameGrid(gridSize, shipCount);
        this.gridSecondPlayer = new GameGrid(gridSize, shipCount);
        this.timePlayerOne = new BattleshipsTimer();
        this.timePlayerTwo = new BattleshipsTimer();
        this.attemptsPlayerOne = 0;
        this.attemptsPlayerTwo = 0;
    }

    public GameController(int gridSize, GameMode mode) {
        if (mode == GameMode.CUSTOM)
            throw new IllegalArgumentException("Provide ship-count for custom game-mode.");
        if (gridSize != 5 && gridSize != 10)
            throw new IllegalArgumentException("Provide ship-count for custom game-size.");
        this.gridSize = gridSize;
        this.currentPlayer = false;
        this.mode = mode;

        switch (gridSize) {
            case 5:
                this.gridFirstPlayer = new GameGrid(gridSize, SHIPCOUNTFIVE);
                this.gridSecondPlayer = new GameGrid(gridSize, SHIPCOUNTFIVE);
                break;
            default:
                this.gridFirstPlayer = new GameGrid(gridSize, SHIPCOUNTTEN);
                this.gridSecondPlayer = new GameGrid(gridSize, SHIPCOUNTTEN);
                break;
        }

        if (this.mode == GameMode.VS_AI_EASY || this.mode == GameMode.VS_AI_HARD) {
            this.opponentAI = new GameAI(this.gridSize, this.mode, this);
        } else if (this.mode == GameMode.VS_PLAYER) {
            this.opponentAI = null;
        }
        this.timePlayerOne = new BattleshipsTimer();
        this.timePlayerTwo = new BattleshipsTimer();
        this.attemptsPlayerOne = 0;
        this.attemptsPlayerTwo = 0;
    }

    public GameGrid getGridFirstPlayer() {
        return gridFirstPlayer;
    }

    public GameGrid getGridSecondPlayer() {
        return gridSecondPlayer;
    }

    /**
     * Places all ships for both players randomly, resulting in a legit placement to start the game.
     */
    public void placeAllShips() {
        this.getGridFirstPlayer().getShipSet().placeShipsRandomly();
        this.getGridSecondPlayer().getShipSet().placeShipsRandomly();
    }

    /**
     * Performs the move for the current player.
     * @param player Current player. False for player one, true for player two.
     * @param col Column that shall be attacked.
     * @param row Row that shall be attacked.
     * @return True if move was a hit, false if not.
     */
    public boolean makeMove(boolean player, int col, int row) {
        if (this.currentPlayer != player) {
            throw new IllegalArgumentException("It is the other players turn.");
        }

        GameCell cellUnderAttack = this.gridUnderAttack().getCell(col, row);
        if ( cellUnderAttack.isHit() ) {
            throw new IllegalArgumentException("This cell has already been attacked");
        }

        //mark cell hit
        cellUnderAttack.setHit(true);
        increaseAttempts();

        //return if move was a hit
        if( cellUnderAttack.isShip() ) return true;
        return false;
    }

    public void switchPlayers() {
        //prepare for next turn
        this.currentPlayer = !this.currentPlayer;
    }

    public GameGrid gridUnderAttack() {
        return this.currentPlayer ? gridFirstPlayer : gridSecondPlayer;
    }

    /**
     * Returns the grid of the current player.
     * @return grid of current player
     */
    public GameGrid getCurrentGrid() {
        if (!this.currentPlayer) {
            return gridFirstPlayer;
        }
        return gridSecondPlayer;
    }

    public boolean isShipCountLegit(int[] shipCount){
        int bound =  (int) Math.floor(getGridSize() * getGridSize() / 2);
        int coveredGridCells = 2 * shipCount[0] + 3 * shipCount[1] + 4 * shipCount[2] + 5 * shipCount[3];
        if (coveredGridCells > bound){
            return false;
        }
        else{
            return true;
        }
    }

    public int getGridSize() {
        return gridSize;
    }

    public GameAI getOpponentAI() {
        return opponentAI;
    }

    public GameMode getMode() {
        return mode;
    }

    public boolean getCurrentPlayer(){ return this.currentPlayer;}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.gridSize);
        out.writeString(this.mode.name());
        out.writeBooleanArray( new boolean[] {this.currentPlayer} );
        out.writeTypedArray( new GameGrid[] {this.gridFirstPlayer, this.gridSecondPlayer}, 0 );
        out.writeTypedArray(new GameAI[] { this.opponentAI }, 0 );
    }

    public static final Parcelable.Creator<GameController> CREATOR = new Parcelable.Creator<GameController>() {
        public GameController createFromParcel(Parcel in) {
            return new GameController(in);
        }

        public GameController[] newArray(int size) {
            return new GameController[size];
        }
    };

    private GameController(Parcel in) {
        this.gridSize = in.readInt();
        this.mode = GameMode.valueOf( in.readString() );
        this.currentPlayer = in.createBooleanArray()[0];
        GameGrid[] grids = in.createTypedArray(GameGrid.CREATOR);
        this.gridFirstPlayer = grids[0];
        this.gridSecondPlayer = grids[1];

        this.opponentAI = in.createTypedArray(GameAI.CREATOR)[0];
        if(this.opponentAI != null) {
            this.opponentAI.setController(this);
        }
        this.timePlayerOne = new BattleshipsTimer();
        this.timePlayerTwo = new BattleshipsTimer();
        this.attemptsPlayerOne = 0;
        this.attemptsPlayerTwo = 0;
    }

    public int getAttemptsPlayerOne(){
        return this.attemptsPlayerOne;
    }

    public int getAttemptsPlayerTwo(){
        return this.attemptsPlayerTwo;
    }

    public void increaseAttempts(){
        if(getCurrentPlayer()){
            this.attemptsPlayerTwo += 1;
        }
        else{
            this.attemptsPlayerOne += 1;
        }
    }

    public void startTimer(){
        if(getCurrentPlayer()){
            this.timePlayerTwo.start();
        }
        else{
            this.timePlayerOne.start();
        }

    }

    public void stopTimer(){
        this.timePlayerOne.stop();
        this.timePlayerTwo.stop();
    }

    public int getTime(){
        if(getMode() == GameMode.VS_AI_EASY || getMode() == GameMode.VS_AI_HARD){
            return this.timePlayerOne.getTime();
        }
        else{
            return getCurrentPlayer() ? this.timePlayerTwo.getTime() : this.timePlayerOne.getTime();
        }
    }

    public String timeToString(int time) {
        int seconds = time % 60;
        int minutes = ((time - seconds) / 60) % 60;
        String m, s;
        s = (seconds < 10) ? "0" + String.valueOf(seconds) : String.valueOf(seconds);
        m = (minutes < 10) ? "0" + String.valueOf(minutes) : String.valueOf(minutes);
        return m + ":" + s;
    }

    public String attemptsToString(int attempts){
        return (attempts < 10) ? "0" + String.valueOf(attempts) : String.valueOf(attempts);
    }

}
