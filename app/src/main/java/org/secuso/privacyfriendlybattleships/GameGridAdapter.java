package org.secuso.privacyfriendlybattleships;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;

/**
 * Created by Ali Kalsen on 27.01.2017.
 */

public class GameGridAdapter extends BaseAdapter {

    // TODO: Add the images of the ships to this adapter

    Context context;
    GameController game;
    int gridSize;

    public GameGridAdapter(Context context, GameController game){
        this.context = context;
        this.game = game;
        this.gridSize = game.getGridSize();
    }

    // Return the grid size.
    @Override
    public int getCount() {
        return this.gridSize;
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
        Get the current grid cell. Therefore, get the row and the column of the current
        grid cell. Note that the GridView enumerates the grid cells from left to right and
        from the top to the bottom. Furthermore it is not relevant which GameGrid one chooses.
        */
        int cellColumn = i % this.gridSize;
        int cellRow = (i - cellColumn) / this.gridSize;
        GameCell currentCell = game.getGridFirstPlayer().getCell(cellColumn, cellRow);

        /*
         If the grid cell was not initialized, set the color of the current grid to black or white
         in case that the grid cell is a part of a ship or not.
          */
        if(view == null){
            gridCell = new ImageView(this.context);
            // TODO: Set the Layout parameters such that the grid is scalable
            gridCell.setLayoutParams(new GridView.LayoutParams(30,30));
            gridCell.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // Set the grid cell of the current player
            if(!game.getCurrentPlayer()){
                if(currentCell.isShip()){
                    /*
                    TODO: Add an icon for the ship.
                    Currently the grid cell consists of an empty grid with a black background
                     */
                    gridCell.setBackgroundColor(Color.BLACK);
                }
                else{
                    gridCell.setBackgroundColor(Color.WHITE);
                }
            }
            else{
                currentCell = game.getGridSecondPlayer().getCell(cellColumn, cellRow);
                if(currentCell.isShip()){
                    //TODO: Add the icon for the ship
                    gridCell.setBackgroundColor(Color.BLACK);
                }
                else{
                    gridCell.setBackgroundColor(Color.WHITE);
                }
            }
            // Return the initialized grid cell
            return gridCell;
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