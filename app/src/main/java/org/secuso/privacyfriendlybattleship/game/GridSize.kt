/**
 * Copyright (c) 2025, Christian Adams. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * GridSize.java is part of Privacy Friendly Battleship.
 *
 * Privacy Friendly Battleship is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Friendly Battleship is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Battleship. If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlybattleship.game

/**
 * This file represents the sizes for a battleships game.
 *
 * @author Christian Adams
 */
enum class GridSize(val width: Int, val height: Int) {
    SIZE_5X5(5, 5),
    SIZE_10X10(10, 10);

    companion object {
        /** Number of enumeration entries. */
        @JvmField
        val LENGTH = entries.size

        /**
         * Provides the enumeration value that matches the given ordinal number.
         *
         * @param ordinal The ordinal number of the requested enumeration value.
         * @return The requested enumeration value if the given ordinal is valid. Otherwise the
         * default value.
         */
        fun fromOrdinal(ordinal: Int, defaultValue: GridSize): GridSize {
            return if (ordinal in 0..<LENGTH) entries[ordinal] else defaultValue
        }
    }
}
