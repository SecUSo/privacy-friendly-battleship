package org.secuso.privacyfriendlybattleships.game;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alexander Müller on 16.12.2016. Edited by Ali Kalsen on 15.02.2017
 */

public class GameController implements Parcelable {

    private BattleshipsTimer timerPlayerOne;
    private BattleshipsTimer timerPlayerTwo;
    private int attemptsPlayerOne;
    private int attemptsPlayerTwo;
    private boolean playerWins; // Falg for identifying the winner. If true, then player one has won, otherwise player two has won.
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
        this.timerPlayerOne = new BattleshipsTimer();
        this.timerPlayerTwo = new BattleshipsTimer();
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

        this.timerPlayerOne = new BattleshipsTimer();
        this.timerPlayerTwo = new BattleshipsTimer();
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
        increaseNumberOfAttempts();

        //check if the current player has won
        if (this.gridUnderAttack().getShipSet().allShipsDestroyed() ){
            //Current player has won the game. If the current player is player one, then set
            // playerWins = true, else player two wins. Therefore set playerWins = false
            playerWins = !getCurrentPlayer() ? true : false;

        }

        //return if move was a hit
        if( cellUnderAttack.isShip() ) return true;
        return false;
    }

    public void switchPlayers() {
        //prepare for next turn
        this.currentPlayer = !getCurrentPlayer();
    }

    private GameGrid gridUnderAttack() {
        if (this.currentPlayer) {
            return gridFirstPlayer;
        }
        return gridSecondPlayer;
    }

    public boolean isShipCountLegit(int[] shipCount){
        // TODO: Think about the bound for the cells covered by the ships. The current bound is set
        // to the half of the total amount of grid cells, such that the probability of randomly
        // hitting a ship is at most 1/2.
        int bound =  (int) Math.floor(getGridSize() * getGridSize() / 2);
        System.out.println("Bound:");
        System.out.println(bound);
        int coveredGridCells = 2 * shipCount[0] + 3 * shipCount[1] + 4 * shipCount[2] + 5 * shipCount[3];
        System.out.println("Covered grid cells:");
        System.out.println(coveredGridCells);
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

        this.timerPlayerOne = new BattleshipsTimer();
        this.timerPlayerTwo = new BattleshipsTimer();
        this.attemptsPlayerOne = 0;
        this.attemptsPlayerTwo = 0;
    }

    public int getAttemptsPlayerOne(){
        return attemptsPlayerOne;
    }

    public int getAttemptsPlayerTwo(){
        // Note: The AI implicitly is the second player. Since its number of atttemtps are not relevant for the game,
        // we display the number of attempts for the first player instead.
        if(this.mode == GameMode.VS_AI_EASY || this.mode == GameMode.VS_AI_HARD){
            return attemptsPlayerOne;
        }
        return attemptsPlayerTwo;
    }

    public void increaseNumberOfAttempts(){
        if(!getCurrentPlayer()){
            attemptsPlayerOne += 1;
        }
        else{
            attemptsPlayerTwo += 1;
        }
    }

    public int getTime(){
        // Note: The AI implicitly is the second player. Since its time is not relevant for the game,
        // we display the time of the first player instead.
        if(this.mode == GameMode.VS_AI_EASY || this.mode == GameMode.VS_AI_HARD){
            return timerPlayerOne.getTime();
        }
        else{
            return !getCurrentPlayer() ? timerPlayerOne.getTime() : timerPlayerTwo.getTime();
        }
    }

    public void startTimer(){
        if(!getCurrentPlayer()){
            timerPlayerOne.start();
        }
        else{
            timerPlayerTwo.start();
        }
    }

    public void stopTimer(){
        if(!getCurrentPlayer()){
            timerPlayerOne.stop();
        }
        else{
            timerPlayerTwo.stop();
        }
    }

    public String timeToString(int time) {
        int seconds = time % 60;
        int minutes = ((time - seconds) / 60) % 60;
        int hours = (time - minutes - seconds) / (3600);
        String h, m, s;
        s = (seconds < 10) ? "0" + String.valueOf(seconds) : String.valueOf(seconds);
        m = (minutes < 10) ? "0" + String.valueOf(minutes) : String.valueOf(minutes);
        h = (hours < 10) ? "0" + String.valueOf(hours) : String.valueOf(hours);
        return h + ":" + m + ":" + s;
    }

    public boolean getWinner(){
        return playerWins;
    }

}
