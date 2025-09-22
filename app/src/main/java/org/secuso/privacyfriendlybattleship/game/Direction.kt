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
package org.secuso.privacyfriendlybattleship.game

import java.util.Random

/**
 * This file represents the direction, used for ship placement.
 *
 * @author Alexander Müller, Ali Kalsen
 */
enum class Direction {
    NORTH, EAST, SOUTH, WEST;

    companion object {
        fun getRandomDirection(): Direction {
            val ranGen = Random()
            val direction = ranGen.nextInt(4)
            return when (direction) {
                0 -> NORTH
                1 -> EAST
                2 -> SOUTH
                else -> WEST
            }
        }
    }
}
