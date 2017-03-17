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
 * along with Privacy Friendly Battleship. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlybattleship.ui;

import android.app.Activity;
import android.content.res.Configuration;

import org.secuso.privacyfriendlybattleship.R;

/**
 * This class computes the size of a grid cell for the big and the small grid view in pixel.
 * Created on 01.02.2017.
 *
 * @author Ali Kalsen
 */

public class GameActivityLayoutProvider {

    private final static int MARGIN_LEFT = 30;  // in pixel
    private final static int MARGIN_RIGHT = 31; // in pixel; +1 to avoid GridView problems due to rounding error
    private final static int MARGIN_TOP = 30; //in pixel
    private final Activity context;
    private final int gridSize;
    private static final String TAG = GameActivityLayoutProvider.class.getSimpleName();

    public GameActivityLayoutProvider(Activity context, int gridSize){
        this.context = context;
        this.gridSize = gridSize;
    }

    public int getMainGridCellSizeInPixel() {
        int cellSize = 0;
        int orientation = this.context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            int displayWidth = this.context.getResources().getDisplayMetrics().widthPixels;
            cellSize = (displayWidth - getMarginLeft()- getMarginRight() - (gridSize-1) ) / this.gridSize;
        } else {
            int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
            cellSize = (displayHeight - 2 * getMargin()) / this.gridSize;
        }

        return cellSize;
    }

    public int getMiniGridCellSizeInPixel() {
        int cellSize;
        int orientation = this.context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            int layoutHeight = this.context.findViewById(R.id.game_linear_layout).getHeight();
            cellSize =  (layoutHeight - getMargin()*2 - (gridSize-1)) / this.gridSize;
        } else {
            // TODO: Think about the layout of the grid when the orientation is landscape
            int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
            cellSize = (displayHeight - 2 * getMargin()) / this.gridSize;
        }

        return cellSize;
    }

    public int getMargin(){
        /*
        int displayHeight = this.context.getResources().getDisplayMetrics().heightPixels;
        int cellHeight = this.gridSize * (getCellSizeInPixel() + 1);
        int heightLeft = displayHeight - cellHeight;
        return heightLeft / 2;
        */
        int orientation=context.getResources().getConfiguration().orientation;
        if(orientation== Configuration.ORIENTATION_PORTRAIT){
            return MARGIN_TOP;
        }else{
            return calculateLandscapeSideMargin();
        }
    }

    public int getMarginLeft() {
        int orientation=context.getResources().getConfiguration().orientation;
        if(orientation== Configuration.ORIENTATION_PORTRAIT){
            return MARGIN_LEFT;
        }else{
            return calculateLandscapeSideMargin();
        }
    }

    public int getMarginRight() {
        int orientation=context.getResources().getConfiguration().orientation;
        if(orientation== Configuration.ORIENTATION_PORTRAIT){
            return MARGIN_RIGHT;
        }else{
            return calculateLandscapeSideMargin();
        }
    }

    private int calculateLandscapeSideMargin(){
        int cellSpaceWidth = this.gridSize * (getMainGridCellSizeInPixel() + 1);
        int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
        int spaceLeft = displayWidth - cellSpaceWidth;
        return spaceLeft / 2;
    }

}
