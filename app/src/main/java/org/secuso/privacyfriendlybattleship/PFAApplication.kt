/**
 * This file is part of Privacy Friendly Battleship.
 * Copyright (C) 2025  Christian Adams
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
package org.secuso.privacyfriendlybattleship

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import org.secuso.privacyfriendlybattleship.game.GameResources
import org.secuso.privacyfriendlybattleship.util.LogTag
import org.secuso.privacyfriendlybattleship.util.PrefManager
import org.secuso.privacyfriendlybattleship.util.PreferenceObserver

class PFAApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferenceObserver.initialize(this)
        PreferenceObserver.registerPreferenceChangeListener { _, key ->
            if (key == PrefManager.PREF_APP_THEME) {
                applyAppTheme()
            }
        }
        applyAppTheme()
        GameResources.loadGameResources(this)
    }

    private fun applyAppTheme() {
        val prefManager = PrefManager(this)
        when (val appTheme = prefManager.prefAppTheme) {
            APP_THEME_LIGHT  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            APP_THEME_DARK   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            APP_THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else     -> {
                Log.w(TAG, "Unknown value for preference ${PrefManager.PREF_APP_THEME}: $appTheme. Following system.")
                prefManager.prefAppTheme = APP_THEME_SYSTEM
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        private const val APP_THEME_LIGHT = "LIGHT"
        private const val APP_THEME_DARK = "DARK"
        private const val APP_THEME_SYSTEM = "SYSTEM"
    }
}