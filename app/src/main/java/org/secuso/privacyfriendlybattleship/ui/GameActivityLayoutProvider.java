package org.secuso.privacyfriendlybattleship.ui;

import android.app.Activity;
import android.content.res.Configuration;

import org.secuso.privacyfriendlybattleship.R;

/**
 * Created by Ali Kalsen on 01.02.2017.
 */

public class GameActivityLayoutProvider {

    private final static int MARGIN_LEFT = 30;  // in pixel
    private final static int MARGIN_RIGHT = 30; // in pixel
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
            // Subtract the cell size with 1, such that the lines of the grid are visible.
            // TODO: Edit the cell size, if the lines of the grid are not visible
            cellSize = ((displayWidth - getMarginLeft() - getMarginRight() ) / this.gridSize);
        } else {
            // TODO: Think about the layout of the grid when the orientation is landscape
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
            cellSize =  (layoutHeight - getMargin()*2) / this.gridSize;
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
