package org.secuso.privacyfriendlybattleships;

import android.content.Context;
import android.content.SharedPreferences;
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

    Context context;
    GameController game;
    GameActivityLayoutProvider layoutProvider;
    int gridSize;
    String whichGrid;   // Denotes whether the big or the small grid view is chosen

    public GameGridAdapter(Context context, GameActivityLayoutProvider layout, GameController game, String whichGrid){
        this.context = context;
        this.layoutProvider = layout;
        this.game = game;
        this.gridSize = game.getGridSize();
        this.whichGrid = whichGrid;
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
    public View getView(int i, View view, ViewGroup viewGroup) {

        ImageView gridCell;
        /*
        Get the grid cell of the current player. Therefore, get the row and the column of that
        grid cell. Note that the GridView enumerates the grid cells from left to right and
        from the top to the bottom.
        */
        int cellColumn = i % this.gridSize;
        int cellRow = (i - cellColumn) / this.gridSize;
        GameCell currentCell = game.getGridFirstPlayer().getCell(cellColumn, cellRow);
        if(game.getCurrentPlayer()){
            currentCell = game.getGridSecondPlayer().getCell(cellColumn, cellRow);
        }

        /*
         If the grid cell was not initialized, set the color of the current grid to black or white
         in case that the grid cell is a part of a ship or not.
          */
        if(view == null){
            gridCell = new ImageView(this.context);

            // Scale the grid cells by using the GameActivityLayoutProvider
            int cellSize = this.layoutProvider.getCellSizeInPixel();
            if(this.whichGrid.equals(SMALL_GRID)){
                cellSize = cellSize / 3;
                gridCell.setLayoutParams(new GridView.LayoutParams(cellSize,cellSize));
            }
            else{
                gridCell.setLayoutParams(new GridView.LayoutParams(cellSize,cellSize));
            }
            gridCell.setScaleType(ImageView.ScaleType.CENTER_CROP);
            gridCell.setBackgroundColor(Color.WHITE);

            // Set the grid cell of the current player
            if(this.whichGrid.equals(SMALL_GRID) && currentCell.isShip()){
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
            gridCell.setBackgroundColor(Color.GREEN);
        }
        else{
            if(currentCell.isHit()){
                gridCell.setBackgroundColor(Color.RED);
            }
        }
        return gridCell;
    }
}