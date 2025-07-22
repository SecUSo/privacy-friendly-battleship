package org.secuso.privacyfriendlybattleship

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.secuso.privacyfriendlybattleship.game.Direction
import org.secuso.privacyfriendlybattleship.game.GameController
import org.secuso.privacyfriendlybattleship.game.GameGrid
import org.secuso.privacyfriendlybattleship.game.GameMode
import java.util.Timer

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 *
 * Created by Alexander Müller on 18.12.2016.
 */
class GameControllerTest {
    private lateinit var controller: GameController
    private lateinit var controllerSmall: GameController
    private lateinit var timer: Timer

    fun printGrid(grid: GameGrid) {
        for (i in 0..<grid.size) {
            for (j in 0..<grid.size) {
                if (grid.getCell(i, j).isShip) print("1")
                else print("0")
            }
            println()
        }
    }

    @Before
    fun init() {
        controller = GameController(10, GameMode.VS_AI_EASY)
        controllerSmall = GameController(GameMode.CUSTOM, 5, intArrayOf(0, 1, 0, 0))
        timer = Timer()

        //place ships for first player
        controller.gridFirstPlayer.shipSet.placeShip(6, 4, 5, Direction.EAST)
        controller.gridFirstPlayer.shipSet.placeShip(4, 7, 4, Direction.WEST)
        controller.gridFirstPlayer.shipSet.placeShip(1, 1, 3, Direction.WEST)
        controller.gridFirstPlayer.shipSet.placeShip(8, 1, 2, Direction.SOUTH)
        controller.gridFirstPlayer.shipSet.placeShip(1, 6, 3, Direction.NORTH)

        //place ships for short game
        controllerSmall.gridFirstPlayer.shipSet.placeShip(3, 3, 3, Direction.EAST)
        controllerSmall.gridSecondPlayer.shipSet.placeShip(1, 1, 3, Direction.NORTH)
    }

    @Test
    fun testFindShipContainingCell() {
        val grid = if (!controller.currentPlayer) {
            controller.gridFirstPlayer
        } else {
            controller.gridSecondPlayer
        }
        val cell = grid.getCell(1, 1)
        val ship = grid.shipSet.findShipContainingCell(cell)
        assertNotNull(ship)
        assertEquals(ship!!.size.toLong(), 3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun placeShipTest() {
        //test grid of controller
        assertEquals(controller.gridFirstPlayer.getCell(2, 4).isShip, true)
        assertEquals(controller.gridFirstPlayer.getCell(2, 1).isShip, true)
        assertEquals(controller.gridFirstPlayer.getCell(4, 7).isShip, true)
        assertEquals(controller.gridFirstPlayer.getCell(8, 0).isShip, true)
        assertEquals(controller.gridFirstPlayer.getCell(0, 9).isShip, false)
        assertEquals(controller.gridFirstPlayer.getCell(0, 0).isShip, false)
        assertEquals(controller.gridFirstPlayer.getCell(9, 9).isShip, false)
        assertEquals(controller.gridFirstPlayer.getCell(9, 0).isShip, false)
        assertEquals(controller.gridFirstPlayer.getCell(0, 9).isShip, false)

        //test grid of controllerSmall
        assertEquals(controllerSmall.gridSecondPlayer.getCell(1, 0).isShip, false)
        assertEquals(controllerSmall.gridSecondPlayer.getCell(1, 1).isShip, true)
        assertEquals(controllerSmall.gridSecondPlayer.getCell(1, 2).isShip, true)
        assertEquals(controllerSmall.gridSecondPlayer.getCell(1, 3).isShip, true)
        assertEquals(controllerSmall.gridSecondPlayer.getCell(1, 4).isShip, false)

        //place too much ships -> exception
        controller.gridFirstPlayer.shipSet.placeShip(3, 9, 9, Direction.EAST)
    }

    @Test
    fun placementLegitTest() {
        //test controller
        assertEquals(controller.gridFirstPlayer.shipSet.placementLegit(), true)

        controller.gridSecondPlayer.shipSet.placeShip(6, 4, 5, Direction.EAST)
        assertEquals(controller.gridSecondPlayer.shipSet.placementLegit(), true)

        controller.gridSecondPlayer.shipSet.placeShip(2, 5, 3, Direction.SOUTH)
        assertEquals(controller.gridSecondPlayer.shipSet.placementLegit(), false)

        //test controllerSmall
        assertEquals(controllerSmall.gridFirstPlayer.shipSet.placementLegit(), true)
        assertEquals(controllerSmall.gridSecondPlayer.shipSet.placementLegit(), true)
    }

    @Test
    fun placeShipsRandomlyTest() {
        controller.gridSecondPlayer.shipSet.placeShipsRandomly()
        assertEquals(controller.gridSecondPlayer.shipSet.placementLegit(), true)
    }

    @Test
    fun makeMoveTest() {
        /*
        controller.makeMove(false, 5, 5)
        controller.makeMove(true, 1, 1)
        controller.makeMove(false, 4, 4)
        controller.makeMove(true, 1, 2)

        assertEquals(controller.gridFirstPlayer.getCell(1, 1).isHit, true)
        assertEquals(controller.gridFirstPlayer.getCell(2, 1).isHit, false)
        assertEquals(controller.gridFirstPlayer.getCell(1, 2).isHit, true)
        assertEquals(controller.gridFirstPlayer.getCell(9, 9).isHit, false)
        assertEquals(controller.gridFirstPlayer.getCell(0, 0).isHit, false)
        */

        assertEquals(controllerSmall.makeMove(false, 1, 1), true)
        assertEquals(controllerSmall.makeMove(false, 2, 2), false)
        controllerSmall.switchPlayers()
        assertEquals(controllerSmall.makeMove(true, 3, 3), true)
        assertEquals(controllerSmall.makeMove(true, 2, 3), true)
        assertEquals(controllerSmall.makeMove(true, 4, 3), false)
        controllerSmall.switchPlayers()
        assertEquals(controllerSmall.makeMove(false, 1, 2), true)
        assertEquals(controllerSmall.makeMove(false, 1, 3), true)
        println("Attempts player one: " + controllerSmall.attemptsPlayerOne)
        println("Attempts AI: " + controllerSmall.attemptsPlayerTwo)
        assertEquals(controllerSmall.attemptsPlayerOne.toLong(), 4)
        assertEquals(controllerSmall.attemptsPlayerTwo.toLong(), 3)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testTime() {
        controllerSmall.startTimer()
        controllerSmall.makeMove(false, 1, 1)
        controllerSmall.makeMove(false, 2, 2)

        // Pause the test for two seconds. This shall simulate the time needed to do the moves
        Thread.sleep(2000)
        controllerSmall.stopTimer()
        println("Time player one: " + controllerSmall.timeToString(controllerSmall.time))

        controllerSmall.switchPlayers()

        controllerSmall.startTimer()
        controllerSmall.makeMove(true, 3, 3)
        controllerSmall.makeMove(true, 2, 3)
        controllerSmall.makeMove(true, 4, 3)

        Thread.sleep(3000)
        controllerSmall.stopTimer()
        println("Time AI: " + controllerSmall.timeToString(controllerSmall.time))

        controllerSmall.switchPlayers()

        controllerSmall.startTimer()
        controllerSmall.makeMove(false, 1, 2)
        controllerSmall.makeMove(false, 1, 3)

        Thread.sleep(2000)
        controllerSmall.stopTimer()
        println("Time player one: " + controllerSmall.timeToString(controllerSmall.time))

        println("Time: " + controllerSmall.timeToString(controllerSmall.time))
    }

    @Test
    fun allShipsDestroyedTest() {
        assertEquals(controller.gridFirstPlayer.shipSet.allShipsDestroyed(), false)
        assertEquals(
            controller.gridSecondPlayer.shipSet.allShipsDestroyed(),
            true
        ) //no ships placed

        controllerSmall.makeMove(false, 1, 1)
        controllerSmall.makeMove(false, 2, 1)
        controllerSmall.switchPlayers()
        controllerSmall.makeMove(true, 2, 2)
        controllerSmall.switchPlayers()
        controllerSmall.makeMove(false, 1, 2)
        controllerSmall.makeMove(false, 1, 3)

        assertEquals(controllerSmall.gridFirstPlayer.shipSet.allShipsDestroyed(), false)
        assertEquals(controllerSmall.gridSecondPlayer.shipSet.allShipsDestroyed(), true)
    }
}