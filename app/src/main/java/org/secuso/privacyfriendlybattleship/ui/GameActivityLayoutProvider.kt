/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * GameActivityLayoutProvider.java is part of Privacy Friendly Battleship.
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
package org.secuso.privacyfriendlybattleship.ui

import android.app.Activity
import android.view.View
import org.secuso.privacyfriendlybattleship.R

/**
 * This class computes the size of a grid cell for the big and the small grid view in pixel.
 * Created on 01.02.2017.
 *
 * @author Ali Kalsen
 */
class GameActivityLayoutProvider(context: Activity, private val gridSize: Int) {

    private val gridViewBig = context.findViewById<View>(R.id.game_gridview_big)
    private val gridViewSmall = context.findViewById<View>(R.id.game_gridview_small)

    val mainGridCellSizeInPixel: Int
        get() = getGridCellSizeInPixel(gridViewBig)

    val miniGridCellSizeInPixel: Int
        get() = getGridCellSizeInPixel(gridViewSmall)

    private fun getGridCellSizeInPixel(gridView: View): Int {
        var result = 0
        if (gridView.width > 0) {
            result = (gridView.width - gridSize + 1) / gridSize
        }
        return result
    }
}
