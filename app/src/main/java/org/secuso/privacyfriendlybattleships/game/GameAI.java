package org.secuso.privacyfriendlybattleships.game;

import java.util.Random;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameAI {

    private byte[][] gridUnderAttack;//represents the opponents grid; 0: unknown, 1: ship, 2: water
    private int gridSize;
    private GameMode mode;
    private GameController controller;
    private Random ranGen;


    public GameAI(int gridSize, GameMode mode, GameController controller) {
        if (mode == GameMode.VS_PLAYER) {
            throw new IllegalArgumentException("No AI possible in player vs player matches.");
        }
        this.gridSize = gridSize;
        this.gridUnderAttack = new byte[this.gridSize][this.gridSize];
        this.mode = mode;
        this.controller = controller;

        //initialize local grid
        for(int i = 0; i < this.gridSize*this.gridSize; i++) {
            this.gridUnderAttack[i / this.gridSize][i % this.gridSize] = 0;
        }

        //initialize random number generator
        this.ranGen = new Random();
    }

    public void makeMove(){
        if(this.mode == GameMode.VS_AI_EASY) {
            makeRandomMove();
        } else if(this.mode == GameMode.VS_AI_HARD) {
            //TODO: implementation of AI for higher difficulty
        }
    }

    private void makeRandomMove(){
        int col;
        int row;

        //get random coordinate to attack
        do {
            col = ranGen.nextInt(this.gridSize);
            row = ranGen.nextInt(this.gridSize);
        } while (this.gridUnderAttack[col][row] != 0);

        //attack opponent and update local grid
        if ( this.controller.makeMove(true, col, row) ) {
            this.gridUnderAttack[row][col] = 1;
        } else {
            this.gridUnderAttack[row][col] = 2;
        }
    }
}