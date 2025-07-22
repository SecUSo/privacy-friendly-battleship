/**
 * This file is part of Privacy Friendly Battleship.
 *
 * Privacy Friendly Battleship is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 * Privacy Friendly Password Generator is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Password Generator. If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlybattleship.util

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlybattleship.game.GameMode
import org.secuso.privacyfriendlybattleship.game.GridSize

/**
 * Class structure taken from tutorial at http://www.androidhive.info/2016/05/android-build-intro-slider-app/
 * @author Karola Marky
 * @version 20170518
 */
class PrefManager(context: Context) {
    private val pref = PreferenceManager.getDefaultSharedPreferences(context)

    var isFirstTutorialStart: Boolean
        get() = pref.getBoolean(FIRST_TUTORIAL_START, true)
        set(value) = pref.edit(commit = true) { putBoolean(FIRST_TUTORIAL_START, value) }

    var isFirstPlacementStart: Boolean
        get() = pref.getBoolean(FIRST_PLACEMENT_START, true)
        set(value) = pref.edit(commit = true) { putBoolean(FIRST_PLACEMENT_START, value) }

    var isFirstGameStart: Boolean
        get() = pref.getBoolean(FIRST_GAME_START, true)
        set(value) = pref.edit(commit = true) { putBoolean(FIRST_GAME_START, value) }

    var isFirstShipSetStart: Boolean
        get() = pref.getBoolean(FIRST_SHIP_SET_START, true)
        set(value) = pref.edit(commit = true) { putBoolean(FIRST_SHIP_SET_START, value) }

    var lastGameMode: GameMode
        get() = GameMode.fromOrdinal(pref.getInt(LAST_GAME_MODE, GameMode.VS_PLAYER.ordinal), GameMode.VS_PLAYER)
        set(value) = pref.edit(commit = true) { putInt(LAST_GAME_MODE, value.ordinal) }

    var lastGridSize: GridSize
        get() = GridSize.fromOrdinal(pref.getInt(LAST_GRID_SIZE, GridSize.SIZE_5X5.ordinal), GridSize.SIZE_5X5)
        set(value) = pref.edit(commit = true) { putInt(LAST_GRID_SIZE, value.ordinal) }

    companion object {
        // Shared preference keys
        private const val FIRST_TUTORIAL_START: String = "FIRST_TUTORIAL_START"
        private const val FIRST_PLACEMENT_START: String = "FIRST_PLACEMENT_START"
        private const val FIRST_GAME_START: String = "FIRST_GAME_START"
        private const val FIRST_SHIP_SET_START: String = "FIRST_SHIP_SET_START"
        private const val LAST_GAME_MODE: String = "LAST_GAME_MODE"
        private const val LAST_GRID_SIZE: String = "LAST_GRID_SIZE"
    }
}
