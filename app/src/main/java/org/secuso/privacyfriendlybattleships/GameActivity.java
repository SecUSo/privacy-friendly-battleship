package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameMode;

import java.util.Timer;

public class GameActivity extends BaseActivity {

    private Timer timerUpdate;
    private SharedPreferences preferences = null;

    private GameMode gameMode;
    private int gridSize;
    private GameController controller;
    private GameGridAdapter adapterPlayerOne;
    private GameGridAdapter adapterPlayerTwo;
    private GridView gridViewBig;
    private GridView gridViewSmall;
    private int positionGridCell;   // Save the current position of the grid cell clicked
    private View prevCell = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupPreferences();
        setContentView(R.layout.activity_game);

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");

        this.gridSize = controller.getGridSize();
        this.gameMode = controller.getMode();

        // Set up the grids
        setupGridView(this.controller.getCurrentPlayer());
    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game;
    }

    public void onClickButton(View view) {
        int column = this.positionGridCell % this.gridSize;
        int row = (this.positionGridCell - column) % this.gridSize;
        this.controller.makeMove(this.controller.getCurrentPlayer(), column, row);
        // TODO: Implement the rest of the method. Think about the game modes and how to realize them
    }

    protected void setupGridView(boolean currentPlayer) {

        // Get the grid views of the respective XML-files
        gridViewBig = (GridView) findViewById(R.id.game_gridview_big);
        gridViewSmall = (GridView) findViewById(R.id.game_gridview_small);

        // Set the background color of the grid
        gridViewBig.setBackgroundColor(Color.GRAY);
        gridViewSmall.setBackgroundColor(Color.GRAY);

        // Set the columns of the grid
        gridViewBig.setNumColumns(this.gridSize);
        gridViewSmall.setNumColumns(this.gridSize);

        // Set the size of the grids
        //TODO: Make the grid scalable


        // Initialize the grid for player one

        /*
        SharedPreferences preferenceGridSize = this.getSharedPreferences("Grid size", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferenceGridSize.edit();
        edit.clear();
        edit.putString("Grid", GameGridAdapter.SMALL_GRID);
        edit.commit();
        */
        final GameGridAdapter adapter = new GameGridAdapter(this, this.controller, "");
        gridViewBig.setAdapter(adapter);
        adapterPlayerOne = new GameGridAdapter(this, this.controller, GameGridAdapter.SMALL_GRID);
        gridViewSmall.setAdapter(adapterPlayerOne);

        // Define the listener for the big grid view, such that it is possible to click on it. When
        // clicking on that grid, the corresponding cell should be yellow.
        gridViewBig.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(prevCell != null){
                    prevCell.setBackgroundColor(Color.WHITE);
                }
                positionGridCell = i;
                view.setBackgroundColor(Color.YELLOW);
                prevCell = view;
                // Display the grid cell, which was clicked.
                adapter.notifyDataSetChanged();
            }
        });
    }

}
