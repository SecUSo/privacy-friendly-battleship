package org.secuso.privacyfriendlybattleships.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public class GameAI implements Parcelable{

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
            while ( makeRandomMove() ) {};
            this.controller.switchPlayers();
        } else if(this.mode == GameMode.VS_AI_HARD) {
            //TODO: implementation of AI for higher difficulty
        }
    }

    private boolean makeRandomMove(){
        int col;
        int row;

        //get random coordinate to attack
        do {
            col = ranGen.nextInt(this.gridSize);
            row = ranGen.nextInt(this.gridSize);
        } while (this.gridUnderAttack[col][row] != 0);

        //attack opponent and update local grid
        boolean isHit = this.controller.makeMove(true, col, row);

        if ( isHit ) {
            this.gridUnderAttack[col][row] = 1;
        } else {
            this.gridUnderAttack[col][row] = 2;
        }

        return isHit;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.gridSize);
        out.writeString(this.mode.name());
        for( int i = 0; i < this.gridSize; i++) {
            out.writeByteArray(this.gridUnderAttack[i]);
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
        this.gridUnderAttack = new byte[this.gridSize][this.gridSize];
        for ( int i = 0; i < this.gridSize; i++) {
            this.gridUnderAttack[i] = in.createByteArray();
        }

        this.ranGen = new Random();
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }
}