package org.secuso.privacyfriendlybattleships.game;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import org.secuso.privacyfriendlybattleships.R;

/**
 * Created by Ali Kalsen on 01.02.2017.
 */

public class GameActivityLayoutProvider {

    private final static int MARGIN_LEFT = 30;  // in pixel
    private final static int MARGIN_RIGHT = 30; // in pixel
    private final static int MARGIN_TOP = 30; //in pixel
    private final static int MARGIN_BOTTOM = 30; //in pixel
    private final Context context;
    private final int gridSize;

    private int cellSize;
    private int cellSizeSmall;
    private int leftMargin;
    private int rightMargin;
    private int topMargin;
    private int bottomMargin;

    public GameActivityLayoutProvider(Context context, int gridSize){
        this.context = context;
        this.gridSize = gridSize;
        // Set the default values of the margins
        this.leftMargin = MARGIN_LEFT;
        this.rightMargin = MARGIN_RIGHT;
        this.topMargin = MARGIN_TOP;
        this.bottomMargin = MARGIN_BOTTOM;
    }

    public int getStatusBarHeight(){
        int statusBarHeight = 0;
        int resourceId = this.context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = this.context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public int getNavigationBarHeight(int orientation){
        int navigationBarHeight = 0;
        int resourceId = this.context.getResources().getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ?
                "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = this.context.getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    public int getAppBarHeight(){
        int actionBarHeight = 0;
        final TypedArray styledAttributes = this.context.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize }
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarHeight;
    }

    public int computeCellSizeInPixel() {
        int cellSize = 0;
        int orientation = getOrientation();
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            int displayWidth = this.context.getResources().getDisplayMetrics().widthPixels;
            /*
            The cell size is computed as follows:
            1. Subtract the left margin, right margin (default: 30 pixel per side) and the lines
               between the grid cells from the display width. The number of lines between the grid
               cells equals the grid size minus 1. A line itself has a width of one pixel.
            2. Divide this result by the grid size.
             */
            int numberLines = this.gridSize - 1;
            cellSize = ((displayWidth - getMarginLeft() - getMarginRight() - numberLines) / this.gridSize);

            /*
            Rearrange the horizontal margins, such that the grid is fully displayed. The width of the
            grid including its margins should equal the display width.
             */
            int deltaWidth = displayWidth - this.gridSize * cellSize - numberLines;
            setMarginLeft((int) Math.ceil(deltaWidth / 2));
            setMarginRight(deltaWidth - getMarginLeft());
        } else {
            // TODO: Think about the layout of the grid when the orientation is landscape
            int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
            cellSize = (displayHeight - 2 * getMargin()) / this.gridSize;
        }

        this.cellSize = cellSize;
        return this.cellSize;
    }

    public int computeCellSizeForSmallGrid(){
        int cellSizeSmallGrid = 0;
        int orientation = getOrientation();
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            int displayWidth = this.context.getResources().getDisplayMetrics().widthPixels;
            int displayHeight = this.context.getResources().getDisplayMetrics().heightPixels;
            int numberLines = this.gridSize - 1;

            /*
            First of all note that the big grid including its horizontal margins equals the display
            width. Since the relation between the height and the width is at most 2 for smartphones,
            the remaining height for the small grid is smaller than the display width.

            So, we need to use the remaining height in order to set the layout for the small grid.

            The height for the rest of the display is computed as follows:
            Subtract the heights of the status bar, the app bar and the navigation bar from the real
            display height. Furthermore, subtract the display width, the lines of the small grid and
            the vertical margins of the small grid from the real display height.
             */

            //int heightOfBigGrid = 2 * getMargin() + this.gridSize * getCellSize() + lines;
            /*int restHeight = displayHeight - displayWidth - getMarginTop() - getMarginBottom() - lines
                    - getStatusBarHeight() - getAppBarHeight() - getNavigationBarHeight(orientation);
            */
            int heightOfBigGrid = this.gridSize * getCellSize() + numberLines + getMarginTop();
            int restHeight = displayHeight - heightOfBigGrid - numberLines - getMarginTop() - getMarginBottom();
            int restWidth = (int) Math.ceil((displayWidth - getMarginLeft() - numberLines) / 2);

            cellSizeSmallGrid = Math.min(restHeight, restWidth) / this.gridSize;

            /*
            Rearrange the vertical margins, such that the grid is fully displayed. The height of the
            grid including its margins should equal the remaining display height.
             */
            int deltaHeight = restHeight - this.gridSize * cellSizeSmallGrid - numberLines;
            //setMarginBottom((int) Math.ceil(deltaHeight / 2));
            setMarginTop(deltaHeight - getMarginBottom());
        }
        else{
            // TODO: Think about the layout of the small grid when the orientation is landscape

        }

        this.cellSizeSmall = cellSizeSmallGrid;
        return this.cellSizeSmall;

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

    public int getMarginTop(){
        return this.topMargin;
    }

    public int getMarginBottom(){
        return this.bottomMargin;
    }

    public int getCellSize(){
        return this.cellSize;
    }

    public int getCellSizeSmall(){
        return this.cellSizeSmall;
    }

    // If the left margin has not been reset, then the left margin equals MARGIN_LEFT
    public int getMarginLeft() {
        int orientation=context.getResources().getConfiguration().orientation;
        if(orientation== Configuration.ORIENTATION_PORTRAIT){
            return this.leftMargin;
        }else{
            return calculateLandscapeSideMargin();
        }
    }

    // If the right margin has not been reset, then the left margin equals MARGIN_RIGHT
    public int getMarginRight() {
        int orientation=context.getResources().getConfiguration().orientation;
        if(orientation== Configuration.ORIENTATION_PORTRAIT){
            return this.rightMargin;
        }else{
            return calculateLandscapeSideMargin();
        }
    }

    public void setMarginLeft(int leftMargin){
        this.leftMargin = leftMargin;
    }

    public void setMarginRight(int rightMargin){
        this.rightMargin = rightMargin;
    }

    public void setMarginBottom(int bottomMargin) {this.bottomMargin = bottomMargin;}

    public void setMarginTop(int topMargin) {this.topMargin = topMargin;}

    public int getOrientation(){
        return this.context.getResources().getConfiguration().orientation;
    }

    public void reset(){
        this.cellSize = 0;
        this.cellSizeSmall = 0;
        setMarginLeft(MARGIN_LEFT);
        setMarginRight(MARGIN_RIGHT);
        setMarginBottom(MARGIN_BOTTOM);
        setMarginTop(MARGIN_TOP);
    }

    private int calculateLandscapeSideMargin(){
        int cellSpaceWidth = this.gridSize * (getCellSize() + 1);
        int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
        int spaceLeft = displayWidth - cellSpaceWidth;
        return spaceLeft / 2;
    }

}