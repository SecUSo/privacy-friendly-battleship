package org.secuso.privacyfriendlybattleship.ui;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 * Created by Ali Kalsen on 27.01.2017. Last edit on 01.02.2017.
 */

public class GameGridAdapter extends BaseAdapter {

    public static final String SMALL_GRID = "SMALL";

    // TODO: Add the images of the ships to this adapter

    Activity context;
    GameController game;
    GameActivityLayoutProvider layoutProvider;
    int gridSize;
    Boolean isMainGrid;// Denotes whether the big or the small grid view is chosen
    private static final String TAG = GameGridAdapter.class.getSimpleName();
    private Boolean shipsNotPlaced;

    public GameGridAdapter(Activity context,
                           GameActivityLayoutProvider layout,
                           GameController game,
                           Boolean isMainGrid){
        this.context = context;
        this.layoutProvider = layout;
        this.game = game;
        this.gridSize = game.getGridSize();
        this.isMainGrid = isMainGrid;
        this.shipsNotPlaced = false;
    }

    public GameGridAdapter(Activity context,
                           GameActivityLayoutProvider layout,
                           GameController game,
                           Boolean isMainGrid,
                           Boolean shipsNotPlaced){
        this.context = context;
        this.layoutProvider = layout;
        this.game = game;
        this.gridSize = game.getGridSize();
        this.isMainGrid = isMainGrid;
        this.shipsNotPlaced = shipsNotPlaced;
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
        if (shipsNotPlaced) {
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
            if(currentCell.isShip() && !isMainGrid || currentCell.isShip() && shipsNotPlaced){
                //TODO: Add the icon of the ship
                gridCell.setImageResource(R.drawable.ic_info_black_24dp);
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

    /**
     * This method scales the images for a ship.
     * @param options:
     * @param reqCellSize: The size of the cell, which should not be exceeded by the image of the ship.
     * @return The size of the image.
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqCellSize) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqCellSize || width > reqCellSize) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqCellSize
                    && (halfWidth / inSampleSize) >= reqCellSize) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqCellSize) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqCellSize);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}