package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
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
        setupGridView();
    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game;
    }

    public void onClickButton(View view) {
        if(view.getId() == R.id.game_button_fire){
            // TODO: Implement this case
        }
    }

    protected void setupGridView() {

        // Get the grid views of the respective XML-files
        gridViewBig = (GridView) findViewById(R.id.game_gridview_big);
        gridViewSmall = (GridView) findViewById(R.id.game_gridview_small);

        // Set the background color of the grid
        gridViewBig.setBackgroundColor(Color.BLACK);
        gridViewSmall.setBackgroundColor(Color.BLACK);

        // Set the columns of the grid
        gridViewBig.setNumColumns(this.gridSize);
        gridViewSmall.setNumColumns(this.gridSize);

        // Set the size of the grids

        // Initialize the grid for player one
        adapterPlayerOne = new GameGridAdapter(this, this.controller);
        gridViewBig.setAdapter(adapterPlayerOne);



    }

}
