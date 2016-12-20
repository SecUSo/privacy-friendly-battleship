package org.secuso.privacyfriendlybattleships.game;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameAI {
    private byte[][] gridUnderAttack;
    private int gridSize;
    private GameMode mode;

    public GameAI(int gridSize, GameMode mode) {
        this.gridSize = gridSize;
        this.gridUnderAttack = new byte[this.gridSize][this.gridSize];
        this.mode = mode;
    }
}
