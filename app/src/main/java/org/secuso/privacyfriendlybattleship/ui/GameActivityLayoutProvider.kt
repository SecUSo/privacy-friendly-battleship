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
import android.content.res.Configuration
import android.view.View
import org.secuso.privacyfriendlybattleship.R
import org.secuso.privacyfriendlybattleship.ui.GameActivityLayoutProvider

/**
 * This class computes the size of a grid cell for the big and the small grid view in pixel.
 * Created on 01.02.2017.
 *
 * @author Ali Kalsen
 */
class GameActivityLayoutProvider {
    private var appBarHeight = 0
    private val context: Activity
    private val gridSize: Int

    constructor(context: Activity, gridSize: Int) {
        this.context = context
        this.gridSize = gridSize
    }

    constructor(context: Activity, gridSize: Int, appBarHeight: Int) {
        this.context = context
        this.appBarHeight = appBarHeight
        this.gridSize = gridSize
    }

    val mainGridCellSizeInPixel: Int
        get() {
            var cellSize = 0
            val orientation =
                context.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                val displayWidth =
                    context.resources.displayMetrics.widthPixels
                cellSize =
                    (displayWidth - marginLeft - marginRight - (gridSize - 1)) / this.gridSize
            } else {
                var displayHeight = context.resources.displayMetrics.heightPixels
                displayHeight = displayHeight - actionBarHeight - statusBarHeight
                cellSize =
                    (displayHeight - 2 * margin - (gridSize - 1)) / this.gridSize
            }

            return cellSize
        }
    val miniGridCellSizeInPixel: Int
        get() {
            val cellSize: Int
            val orientation =
                context.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                val layoutHeight =
                    context.findViewById<View>(R.id.game_linear_layout)
                        .height
                cellSize =
                    (layoutHeight - margin * 2 - (gridSize - 1)) / this.gridSize
            } else {
                // TODO: Think about the layout of the grid when the orientation is landscape
                var displayHeight =
                    context.resources.displayMetrics.heightPixels * 2 / 3
                displayHeight = displayHeight - actionBarHeight - statusBarHeight
                cellSize =
                    (displayHeight - 2 * margin - (this.gridSize - 1)) / this.gridSize
            }

            return cellSize
        }

    val actionBarHeight: Int
        get() {
            // action bar height
            var actionBarHeight = 0
            val styledAttributes = context.theme.obtainStyledAttributes(
                intArrayOf(android.R.attr.actionBarSize)
            )
            actionBarHeight = styledAttributes.getDimension(0, 0f).toInt()
            styledAttributes.recycle()
            return actionBarHeight
        }

    val statusBarHeight: Int
        get() {
            var statusBarHeight = 0
            val resourceId =
                context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
            }
            return statusBarHeight
        }

    val navigationBarHeight: Int
        get() {
            // navigation bar height
            var navigationBarHeight = 0
            val resourceId =
                context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                navigationBarHeight = context.resources.getDimensionPixelSize(resourceId)
            }
            return navigationBarHeight
        }

    val margin: Int
        get() {
            /*
             int displayHeight = this.context.getResources().getDisplayMetrics().heightPixels;
             int cellHeight = this.gridSize * (getCellSizeInPixel() + 1);
             int heightLeft = displayHeight - cellHeight;
             return heightLeft / 2;
             */

            val orientation = context.resources.configuration.orientation
            return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                MARGIN_TOP
            } else {
                MARGIN_TOP
            }
        }

    val marginLeft: Int
        get() {
            val orientation = context.resources.configuration.orientation
            return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                MARGIN_LEFT
            } else {
                MARGIN_LEFT
            }
        }

    val marginRight: Int
        get() {
            val orientation = context.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return MARGIN_RIGHT
            } else {
                // Recalculate the right margin
                val displayWidth =
                    context.resources.displayMetrics.widthPixels
                val gridViewWidth = displayWidth / 2
                val marginRight =
                    gridViewWidth - marginLeft - mainGridCellSizeInPixel * this.gridSize - (this.gridSize - 1)
                return marginRight
            }
        }

    companion object {
        private const val MARGIN_LEFT = 30 // in pixel
        private const val MARGIN_RIGHT =
            31 // in pixel; +1 to avoid GridView problems due to rounding error
        private const val MARGIN_TOP = 30 //in pixel
        private val TAG: String = GameActivityLayoutProvider::class.java.simpleName
    }
}
