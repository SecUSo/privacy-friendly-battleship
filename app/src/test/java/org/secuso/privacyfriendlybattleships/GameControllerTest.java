package org.secuso.privacyfriendlybattleships;

import org.junit.Test;
import org.junit.Before;
import org.secuso.privacyfriendlybattleships.game.Direction;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameGrid;
import org.secuso.privacyfriendlybattleships.game.GameMode;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alexander Müller on 18.12.2016.
 */

public class GameControllerTest {
    GameController controller;
    GameController controllerSmall;

    public void printGrid(GameGrid grid) {
        for(int i = 0; i < grid.getSize(); i++){
            for(int j = 0; j < grid.getSize(); j++) {
                if(grid.getCell(i, j).isShip())
                    System.out.print("1");
                else
                    System.out.print("0");
            }
            System.out.println();
        }
    }

    @Before
    public void init() {
        controller = new GameController(10, GameMode.VS_AI_EASY);
        controllerSmall = new GameController(5, new int[] {0, 1, 0, 0} );

        //place ships for first player
        controller.getGridFirstPlayer().getShipSet().placeShip(6, 4, 5, Direction.EAST);
        controller.getGridFirstPlayer().getShipSet().placeShip(4, 7, 4, Direction.WEST);
        controller.getGridFirstPlayer().getShipSet().placeShip(1, 1, 3, Direction.WEST);
        controller.getGridFirstPlayer().getShipSet().placeShip(8, 1, 2, Direction.SOUTH);
        controller.getGridFirstPlayer().getShipSet().placeShip(1, 6, 3, Direction.NORTH);

        //place ships for short game
        controllerSmall.getGridFirstPlayer().getShipSet().placeShip(3, 3, 3, Direction.EAST);
        controllerSmall.getGridSecondPlayer().getShipSet().placeShip(1, 1, 3, Direction.NORTH);
    }

    @Test (expected = IllegalArgumentException.class)
    public void placeShipTest() {
        //test grid of controller
        assertEquals(controller.getGridFirstPlayer().getCell(2, 4).isShip(), true);
        assertEquals(controller.getGridFirstPlayer().getCell(2, 1).isShip(), true);
        assertEquals(controller.getGridFirstPlayer().getCell(4, 7).isShip(), true);
        assertEquals(controller.getGridFirstPlayer().getCell(8, 0).isShip(), true);
        assertEquals(controller.getGridFirstPlayer().getCell(0, 9).isShip(), false);
        assertEquals(controller.getGridFirstPlayer().getCell(0, 0).isShip(), false);
        assertEquals(controller.getGridFirstPlayer().getCell(9, 9).isShip(), false);
        assertEquals(controller.getGridFirstPlayer().getCell(9, 0).isShip(), false);
        assertEquals(controller.getGridFirstPlayer().getCell(0, 9).isShip(), false);

        //test grid of controllerSmall
        assertEquals(controllerSmall.getGridSecondPlayer().getCell(1,0).isShip(), false);
        assertEquals(controllerSmall.getGridSecondPlayer().getCell(1,1).isShip(), true);
        assertEquals(controllerSmall.getGridSecondPlayer().getCell(1,2).isShip(), true);
        assertEquals(controllerSmall.getGridSecondPlayer().getCell(1,3).isShip(), true);
        assertEquals(controllerSmall.getGridSecondPlayer().getCell(1,4).isShip(), false);

        //place too much ships -> exception
        controller.getGridFirstPlayer().getShipSet().placeShip(3, 9, 9,Direction.EAST);
    }

    @Test
    public void placementLegitTest() {
        //test controller
        assertEquals(controller.getGridFirstPlayer().getShipSet().placementLegit(), true);

        controller.getGridSecondPlayer().getShipSet().placeShip(6, 4, 5, Direction.EAST);
        assertEquals(controller.getGridSecondPlayer().getShipSet().placementLegit(), true);

        controller.getGridSecondPlayer().getShipSet().placeShip(2, 5, 3, Direction.NORTH);
        assertEquals(controller.getGridSecondPlayer().getShipSet().placementLegit(), false);

        //test controllerSmall
        assertEquals(controllerSmall.getGridFirstPlayer().getShipSet().placementLegit(), true);
        assertEquals(controllerSmall.getGridSecondPlayer().getShipSet().placementLegit(), true);
    }

    @Test
    public void placeShipsRandomlyTest() {
        controller.getGridSecondPlayer().getShipSet().placeShipsRandomly();
        assertEquals(controller.getGridSecondPlayer().getShipSet().placementLegit(), true);
    }

    @Test
    public void makeMoveTest() {
        controller.makeMove(false, 5, 5);
        controller.makeMove(true, 1, 1);
        controller.makeMove(false, 4, 4);
        controller.makeMove(true, 1, 2);

        assertEquals(controller.getGridFirstPlayer().getCell(1, 1).isHit(), true);
        assertEquals(controller.getGridFirstPlayer().getCell(2, 1).isHit(), false);
        assertEquals(controller.getGridFirstPlayer().getCell(1, 2).isHit(), true);
        assertEquals(controller.getGridFirstPlayer().getCell(9, 9).isHit(), false);
        assertEquals(controller.getGridFirstPlayer().getCell(0, 0).isHit(), false);

        assertEquals(controllerSmall.makeMove(false, 1, 1), true);
        assertEquals(controllerSmall.makeMove(true, 2, 2), false);
        assertEquals(controllerSmall.makeMove(false, 1, 2), true);
        assertEquals(controllerSmall.makeMove(true, 3, 3), true);
        assertEquals(controllerSmall.makeMove(false, 1, 3), true);
        assertEquals(controllerSmall.makeMove(true, 3, 2), false);
    }

    @Test
    public void allShipsDestroyedTest() {
        assertEquals(controller.getGridFirstPlayer().getShipSet().allShipsDestroyed(), false);
        assertEquals(controller.getGridSecondPlayer().getShipSet().allShipsDestroyed(), true);//no ships placed

        controllerSmall.makeMove(false, 1, 1);
        controllerSmall.makeMove(true, 2, 2);
        controllerSmall.makeMove(false, 1, 2);
        controllerSmall.makeMove(true, 3, 3);
        controllerSmall.makeMove(false, 1, 3);
        controllerSmall.makeMove(true, 3, 2);

        assertEquals(controllerSmall.getGridFirstPlayer().getShipSet().allShipsDestroyed(), false);
        assertEquals(controllerSmall.getGridSecondPlayer().getShipSet().allShipsDestroyed(), true);
    }
}