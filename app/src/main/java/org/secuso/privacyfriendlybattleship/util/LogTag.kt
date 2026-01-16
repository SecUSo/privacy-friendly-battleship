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
package org.secuso.privacyfriendlybattleship.util

object LogTag {
    fun create(theClass: Class<*>?): String {
        return create(theClass?.simpleName)
    }

    fun create(className: String?): String {
        // Add common prefix to log-tags of "own" classes to be able to filter for these logs.
        return "PFA $className"
    }
}