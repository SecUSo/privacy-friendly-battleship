/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * GameGridAdapter.java is part of Privacy Friendly Battleship.
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
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlybattleship.R
import org.secuso.privacyfriendlybattleship.game.GameController

/**
 * This class implements an adapter for the grid view in the GameActivity, which changes the color
 * of a cell when an action is executed on the grid, e.g clicking on the grid. Created on 27.01.2017.
 *
 * @author Ali Kalsen
 */
class GameGridAdapter : BaseAdapter {
    @JvmField
    var context: Activity
    var game: GameController
    private var layoutProvider: GameActivityLayoutProvider
    private var gridSize: Int
    private var isMainGrid: Boolean // Denotes whether the big or the small grid view is chosen
    private var showShips: Boolean

    constructor(
        context: Activity,
        layout: GameActivityLayoutProvider,
        game: GameController,
        isMainGrid: Boolean
    ) {
        this.context = context
        this.layoutProvider = layout
        this.game = game
        this.gridSize = game.gridSize
        this.isMainGrid = isMainGrid
        this.showShips = false
    }

    constructor(
        context: Activity,
        layout: GameActivityLayoutProvider,
        game: GameController,
        isMainGrid: Boolean,
        showShips: Boolean
    ) {
        this.context = context
        this.layoutProvider = layout
        this.game = game
        this.gridSize = game.gridSize
        this.isMainGrid = isMainGrid
        this.showShips = showShips
    }

    // Return the number of all grid cells.
    override fun getCount(): Int {
        return this.gridSize * this.gridSize
    }

    override fun getItem(i: Int): Any? {
        return null
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(cellIndex: Int, view: View?, viewGroup: ViewGroup): View {
        val gridCell: ImageView
        /*
        Get the grid cell of the current player. Therefore, get the row and the column of that
        grid cell. Note that the GridView enumerates the grid cells from left to right and
        from the top to the bottom.
        */
        val cellColumn = cellIndex % this.gridSize
        val cellRow = cellIndex / this.gridSize
        val currentCell =
            if (showShips) {
                game.currentGrid.getCell(cellColumn, cellRow)
            } else if ((isMainGrid xor game.currentPlayer)) {
                game.gridSecondPlayer.getCell(cellColumn, cellRow)
            } else {
                game.gridFirstPlayer.getCell(cellColumn, cellRow)
            }

        if (view is ImageView) {
            gridCell = view
        } else {
            gridCell = ImageView(this.context)
            gridCell.scaleType = ImageView.ScaleType.CENTER_CROP
            gridCell.setBackgroundColor(Color.WHITE)
            // Set the grid cell of the current player
            if (currentCell.isShip && !isMainGrid || currentCell.isShip && showShips) {
                gridCell.setImageResource(currentCell.resourceId)
            }
        }

        // Scale the grid cells by using the GameActivityLayoutProvider
        // Note: If grid gets set-up, its size is not available which results in cell size 0.
        val cellSize = if (isMainGrid) {
            layoutProvider.mainGridCellSizeInPixel
        } else {
            layoutProvider.miniGridCellSizeInPixel
        }
        if (null == gridCell.layoutParams || gridCell.layoutParams.width != cellSize) {
            gridCell.layoutParams = AbsListView.LayoutParams(cellSize, cellSize)
        }

        if (currentCell.isHit) {
            if (currentCell.isShip) {
                gridCell.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
            } else {
                gridCell.setBackgroundColor(ContextCompat.getColor(context, R.color.lightBlue))
            }
        }
        return gridCell
    }
}