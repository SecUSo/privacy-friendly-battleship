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
 * along with Privacy Friendly Battleship. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlybattleship.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.secuso.privacyfriendlybattleship.R;
import org.secuso.privacyfriendlybattleship.game.GameCell;
import org.secuso.privacyfriendlybattleship.game.GameController;

/**
 * This class implements an adapter for the grid view in the GameActivity, which changes the color
 * of a cell when an action is executed on the grid, e.g clicking on the grid. Created on 27.01.2017.
 *
 * @author Ali Kalsen
 */

public class GameGridAdapter extends BaseAdapter {

    public static final String SMALL_GRID = "SMALL";

    Activity context;
    GameController game;
    GameActivityLayoutProvider layoutProvider;
    int gridSize;
    Boolean isMainGrid;// Denotes whether the big or the small grid view is chosen
    private static final String TAG = GameGridAdapter.class.getSimpleName();
    private Boolean showShips;

    public GameGridAdapter(Activity context,
                           GameActivityLayoutProvider layout,
                           GameController game,
                           Boolean isMainGrid){
        this.context = context;
        this.layoutProvider = layout;
        this.game = game;
        this.gridSize = game.getGridSize();
        this.isMainGrid = isMainGrid;
        this.showShips = false;
    }

    public GameGridAdapter(Activity context,
                           GameActivityLayoutProvider layout,
                           GameController game,
                           Boolean isMainGrid,
                           Boolean showShips){
        this.context = context;
        this.layoutProvider = layout;
        this.game = game;
        this.gridSize = game.getGridSize();
        this.isMainGrid = isMainGrid;
        this.showShips = showShips;
    }

    // Return the number of all grid cells.
    @Override
    public int getCount() {
        return this.gridSize * this.gridSize;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int cellIndex, View view, ViewGroup viewGroup) {

        ImageView gridCell;
        /*
        Get the grid cell of the current player. Therefore, get the row and the column of that
        grid cell. Note that the GridView enumerates the grid cells from left to right and
        from the top to the bottom.
        */
        int cellColumn = cellIndex % this.gridSize;
        int cellRow = cellIndex / this.gridSize;
        GameCell currentCell;
        if (showShips) {
            currentCell = game.getCurrentGrid().getCell(cellColumn, cellRow);
        } else {
            currentCell = (isMainGrid ^ game.getCurrentPlayer()) ?
                    game.getGridSecondPlayer().getCell(cellColumn, cellRow) :
                    game.getGridFirstPlayer().getCell(cellColumn, cellRow);
        }

        /*
         If the grid cell was not initialized, set the color of the current grid to black or white
         in case that the grid cell is a part of a ship or not.
          */


        if(view == null){
            gridCell = new ImageView(this.context);

            // Scale the grid cells by using the GameActivityLayoutProvider
            int cellSize;
            if(!isMainGrid){
                cellSize = this.layoutProvider.getMiniGridCellSizeInPixel();
            }
            else{//is main grid
                cellSize = this.layoutProvider.getMainGridCellSizeInPixel();
            }

            gridCell.setLayoutParams(new GridView.LayoutParams(cellSize,cellSize));
            gridCell.setScaleType(ImageView.ScaleType.CENTER_CROP);
            gridCell.setBackgroundColor(Color.WHITE);

            // Set the grid cell of the current player
            if(currentCell.isShip() && !isMainGrid || currentCell.isShip() && showShips){
                gridCell.setImageResource(currentCell.getResourceId());
            }
        } else{
            gridCell = (ImageView) view;
        }

        if(currentCell.isHit()) {
            if(currentCell.isShip()) {
                gridCell.setBackgroundColor(context.getResources().getColor(R.color.red));
            } else {
                gridCell.setBackgroundColor(context.getResources().getColor(R.color.lightblue));
            }
        }
        return gridCell;
    }
}