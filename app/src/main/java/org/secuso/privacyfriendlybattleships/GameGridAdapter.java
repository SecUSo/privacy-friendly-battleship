package org.secuso.privacyfriendlybattleships;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;

/**
 * Created by Ali Kalsen on 27.01.2017. Last edit on 01.02.2017.
 */

public class GameGridAdapter extends BaseAdapter {

    public static final String SMALL_GRID = "SMALL";

    // TODO: Add the images of the ships to this adapter

    GameActivity context;
    GameController game;
    GameActivityLayoutProvider layoutProvider;
    int gridSize;
    Boolean isMainGrid;   // Denotes whether the big or the small grid view is chosen
    private static final String TAG = GameGridAdapter.class.getSimpleName();

    public GameGridAdapter(GameActivity context, GameActivityLayoutProvider layout, GameController game, Boolean isMainGrid){
        this.context = context;
        this.layoutProvider = layout;
        this.game = game;
        this.gridSize = game.getGridSize();
        this.isMainGrid = isMainGrid;
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
        GameCell currentCell = (game.getCurrentPlayer() ^ this.isMainGrid) ?
                game.getGridSecondPlayer().getCell(cellColumn, cellRow) :
                game.getGridFirstPlayer().getCell(cellColumn, cellRow);

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
                gridCell.setLayoutParams(new GridView.LayoutParams(cellSize,cellSize));
            }
            else{
                cellSize = this.layoutProvider.getMainGridCellSizeInPixel();
                gridCell.setLayoutParams(new GridView.LayoutParams(cellSize,cellSize));
            }
            gridCell.setScaleType(ImageView.ScaleType.CENTER_CROP);
            gridCell.setBackgroundColor(Color.WHITE);

            // Set the grid cell of the current player
            if(!isMainGrid && currentCell.isShip()){
                /*
                If the current cell contains a ship, then set the color of the cell to black, but
                only if we set the small grid, since the current player shall not know where
                the ships of the opponent are.
                */
                //TODO: Add the icon of the ship
                gridCell.setBackgroundColor(Color.BLACK);
            }
        }
        else{
            gridCell = (ImageView) view;
        }

        if(currentCell.isHit() && currentCell.isShip()){
            gridCell.setBackgroundColor(Color.RED);
        }
        else{
            if(currentCell.isHit()){
                gridCell.setBackgroundColor(Color.BLUE);
            }
        }
        return gridCell;
    }
}