package org.secuso.privacyfriendlybattleships.game;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public enum GameMode {
    VS_PLAYER, VS_AI_EASY, VS_AI_HARD, CUSTOM;

    public static GameMode fromInteger(int x) {
        switch(x) {
            case 0:
                return VS_PLAYER;
            case 1:
                return VS_AI_EASY;
            case 2:
                return VS_AI_HARD;
            case 3:
                return CUSTOM;
        }
        return null;
    }
}
