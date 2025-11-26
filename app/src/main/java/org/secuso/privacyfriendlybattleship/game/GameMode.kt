/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * GameMode.java is part of Privacy Friendly Battleship.
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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.secuso.privacyfriendlybattleship.R

/**
 * This file represents the mode for a battleships game. It is used to
 * define whether the game is played in the two player mode or against the
 * AI in one of two difficulty levels.
 *
 * @author Alexander Müller, Ali Kalsen
 */
enum class GameMode(@param:StringRes val stringResID: Int, @param:DrawableRes val imageResID: Int) {
    VS_PLAYER(R.string.mode_two_player, R.drawable.ic_people_black_24px),
    VS_AI_EASY(R.string.mode_vs_cpu_easy, R.drawable.ic_cpu_easy),
    VS_AI_HARD(R.string.mode_vs_cpu_hard, R.drawable.ic_cpu_hard),
    CUSTOM(R.string.mode_custom, R.drawable.ic_people_black_24px);

    companion object {
        /** Number of enumeration entries. */
        @JvmField
        val LENGTH = entries.size

        /**
         * Provides the enumeration value that matches the given ordinal number.
         *
         * Note: GameMode ´CUSTOM´ is excluded by this function.
         *
         * @param ordinal The ordinal number of the requested enumeration value.
         * @return The requested enumeration value if the given ordinal is valid. Otherwise the
         * default value.
         */
        fun fromOrdinal(ordinal: Int, defaultValue: GameMode): GameMode {
            return if (ordinal in 0..<LENGTH - 1) entries[ordinal] else defaultValue
        }
    }
}
