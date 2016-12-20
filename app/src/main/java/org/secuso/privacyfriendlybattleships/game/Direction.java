package org.secuso.privacyfriendlybattleships.game;

import java.util.Random;

/**
 * Created by Alexander Müller on 16.12.2016.
 */

public enum Direction {
    NORTH, EAST, SOUTH, WEST;

    public static Direction getRandomDirection() {
        Random ranGen = new Random();
        int direction = ranGen.nextInt(4);
        if (direction == 0) return NORTH;
        else if (direction == 1) return EAST;
        else if (direction == 2) return SOUTH;
        return WEST;
    }
}
