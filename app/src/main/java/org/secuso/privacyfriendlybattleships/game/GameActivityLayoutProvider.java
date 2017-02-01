package org.secuso.privacyfriendlybattleships.game;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by Athene on 01.02.2017.
 */

public class GameActivityLayoutProvider {

    private final static int MARGIN_LEFT = 35;  // in pixel
    private final static int MARGIN_RIGHT = 35; // in pixel
    private final Context context;
    private final GameController game;
    private final int gridSize;

    public GameActivityLayoutProvider(Context context, GameController controller){
        this.context = context;
        this.game = controller;
        this.gridSize = this.game.getGridSize();
    }

    public int getCellSizeInPixel() {
        int cellSize = 0;
        int orientation = this.context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            int displayWidth = this.context.getResources().getDisplayMetrics().widthPixels;
            cellSize = (displayWidth - MARGIN_LEFT - MARGIN_RIGHT) / this.gridSize;
        } else {
            // TODO: Think about the layout of the grid when the orientation is landscape
        }

        return cellSize;
    }

    public int getMargin(){
        int displayHeiht = this.context.getResources().getDisplayMetrics().heightPixels;

    }


}
