package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameGrid;
import org.secuso.privacyfriendlybattleships.game.GameMode;

import java.util.Timer;

public class GameActivity extends BaseActivity {

    private Timer timerUpdate;
    private SharedPreferences preferences = null;

    private GameMode gameMode;
    private int gridSize;
    private GameController controller;
    private GameGridAdapter adapterPlayer;
    private GridView gridViewBig;
    private GridView gridViewSmall;
    private GameActivityLayoutProvider layoutProvider;
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

        // Create a GameActivityLayoutProvider in order to scale the grids appropriately
        layoutProvider = new GameActivityLayoutProvider(this, this.gridSize);

        // Set up the grids for player one
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
        int column = this.positionGridCell % this.gridSize;
        int row = this.positionGridCell / this.gridSize;

        Boolean currentPlayer = this.controller.getCurrentPlayer();
        GameGrid currentGrid = !currentPlayer ?
                this.controller.getGridFirstPlayer() :
                this.controller.getGridSecondPlayer();

        if(currentGrid.getCell(column, row).isHit())
            return;

        this.controller.makeMove(currentPlayer, column, row);

        if(this.controller.getMode() == GameMode.VS_AI_EASY || this.controller.getMode() == GameMode.VS_AI_HARD){
            // The AI makes a move
            this.controller.getOpponentAI().makeMove();
            adapterPlayer.notifyDataSetChanged();
        }else {
            setupGridView();
        }
        // TODO: Implement the rest of the method. Think about the game modes and how to realize them
    }

    protected void setupGridView() {

        // Get the grid views of the respective XML-files
        gridViewBig = (GridView) findViewById(R.id.game_gridview_big);
        gridViewSmall = (GridView) findViewById(R.id.game_gridview_small);

        // Set the background color of the grid
        gridViewBig.setBackgroundColor(Color.GRAY);
        gridViewSmall.setBackgroundColor(Color.GRAY);

        // Set the columns of the grid
        gridViewBig.setNumColumns(this.gridSize);
        gridViewSmall.setNumColumns(this.gridSize);

        // Set the layout of the grids
        final ViewGroup.MarginLayoutParams marginLayoutParamsBig = (ViewGroup.MarginLayoutParams) gridViewBig.getLayoutParams();
        final ViewGroup.MarginLayoutParams marginLayoutParamsSmall = (ViewGroup.MarginLayoutParams) gridViewSmall.getLayoutParams();
        marginLayoutParamsBig.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMargin(), layoutProvider.getMarginRight(),0);
        // NOTE: The big grid shall be
        marginLayoutParamsSmall.setMargins(layoutProvider.getMarginLeft() / 3, layoutProvider.getMargin() / 3, layoutProvider.getMarginRight() / 3,0);
        gridViewBig.setLayoutParams(marginLayoutParamsBig);
        gridViewSmall.setLayoutParams(marginLayoutParamsSmall);

        gridViewBig.setHorizontalSpacing(1);
        gridViewBig.setVerticalSpacing(1);

        // Initialize the grid for player one

        /*
        SharedPreferences preferenceGridSize = this.getSharedPreferences("Grid size", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferenceGridSize.edit();
        edit.clear();
        edit.putString("Grid", GameGridAdapter.SMALL_GRID);
        edit.commit();
        */
        final GameGridAdapter adapter = new GameGridAdapter(this, this.layoutProvider, this.controller, "");
        gridViewBig.setAdapter(adapter);
        adapterPlayer = new GameGridAdapter(this, this.layoutProvider, this.controller, GameGridAdapter.SMALL_GRID);
        gridViewSmall.setAdapter(adapterPlayer);

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
