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
package org.secuso.privacyfriendlybattleship.tutorial

import android.content.Context
import android.content.SharedPreferences

/**
 * Class structure taken from tutorial at http://www.androidhive.info/2016/05/android-build-intro-slider-app/
 * @author Karola Marky
 * @version 20170518
 */
class PrefManager(context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    // shared pref mode
    private val PRIVATE_MODE = 0

    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }

    var isTutorialLaunch: Boolean
        get() = pref.getBoolean(IS_TUTORIAL_LAUNCH, true)
        set(isTutorial) {
            editor.putBoolean(IS_TUTORIAL_LAUNCH, isTutorial)
            editor.commit()
        }


    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "pfa-pw-generator"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val IS_TUTORIAL_LAUNCH = "IsTutorialLaunch"
    }
}
