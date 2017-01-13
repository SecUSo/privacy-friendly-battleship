package org.secuso.privacyfriendlybattleships.game;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameController {

    private GameGrid gridFirstPlayer;
    private GameGrid gridSecondPlayer;
    private int gridSize;
    private GameMode mode;
    private boolean currentPlayer;//false if first players turn, true if second players turn
    private GameAI opponentAI;

    public GameController(int gridSize, int[] shipCount) {
        this.gridSize = gridSize;
        this.mode = GameMode.CUSTOM;
        this.currentPlayer = false;
        this.opponentAI = null; //only player vs player with custom game
        this.gridFirstPlayer = new GameGrid(gridSize, shipCount);
        this.gridSecondPlayer = new GameGrid(gridSize, shipCount);
    }

    public GameController(int gridSize, GameMode mode) {
        if (mode == GameMode.CUSTOM)
            throw new IllegalArgumentException("Provide ship-count for custom game-mode.");
        if (gridSize != 10)
            throw new IllegalArgumentException("Provide ship-count for custom game-size.");
        this.gridSize = gridSize;
        this.currentPlayer = false;
        this.mode = mode;
        this.gridFirstPlayer = new GameGrid(gridSize, new int[] {1, 2, 1, 1});
        this.gridSecondPlayer = new GameGrid(gridSize, new int[] {1, 2, 1, 1});


        if (this.mode == GameMode.VS_AI_EASY || this.mode == GameMode.VS_AI_HARD) {
            this.opponentAI = new GameAI(this.gridSize, this.mode, this);
        } else if (this.mode == GameMode.VS_PLAYER) {
            this.opponentAI = null;
        }
    }

    public GameGrid getGridFirstPlayer() {
        return gridFirstPlayer;
    }

    public GameGrid getGridSecondPlayer() {
        return gridSecondPlayer;
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

        //check if player has won
        if (this.gridUnderAttack().getShipSet().allShipsDestroyed() ){
            //current player has won the game
            //TODO: Do some action to finish the game.
        }

        //prepare for next turn
        this.currentPlayer = !this.currentPlayer;

        //return if move was a hit
        if( cellUnderAttack.isShip() ) return true;
        return false;
    }

    private GameGrid gridUnderAttack() {
        if (this.currentPlayer) {
            return gridFirstPlayer;
        }
        return gridSecondPlayer;
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
}
