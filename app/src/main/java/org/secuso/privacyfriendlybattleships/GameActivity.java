package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

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
    private GameGridAdapter adapterMainGrid;
    private GameGridAdapter adapterMiniGrid;
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
        setupGridViews();

        // Set up the time, the number of draws and the string for the current player
        setupRest();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        /*
        Create a new GridView based on the changed orientation. Reset the variable layoutProvider,
        such that the layout of the grid can be recalculated.
         */
        layoutProvider.reset();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setupGridViews();
        }
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
        GameGrid GridUnderAttack = currentPlayer ?
                this.controller.getGridFirstPlayer() :
                this.controller.getGridSecondPlayer();

        if(GridUnderAttack.getCell(column, row).isHit()){
            return;
        }

        boolean isHit = this.controller.makeMove(currentPlayer, column, row);
        if(!isHit) {
            controller.switchPlayers();
        }
        adapterMainGrid.notifyDataSetChanged();

        if(this.controller.getMode() == GameMode.VS_AI_EASY || this.controller.getMode() == GameMode.VS_AI_HARD){
            // The AI makes a move
            this.controller.getOpponentAI().makeMove();
            adapterMiniGrid.notifyDataSetChanged();
        }else {
            setupGridViews();
        }
        // TODO: Implement the rest of the method. Think about the game modes and how to realize them
    }

    protected void setupGridViews() {

        // Get the grid views of the respective XML-files
        gridViewBig = (GridView) findViewById(R.id.game_gridview_big);
        gridViewSmall = (GridView) findViewById(R.id.game_gridview_small);

        // Set the background color of the grid
        gridViewBig.setBackgroundColor(Color.GRAY);
        gridViewSmall.setBackgroundColor(Color.GRAY);

        // Set the columns of the grid
        gridViewBig.setNumColumns(this.gridSize);
        gridViewSmall.setNumColumns(this.gridSize);

        // Set the layout and the size of the cells for the big grid
        layoutProvider.computeCellSizeInPixel();
        final ViewGroup.MarginLayoutParams marginLayoutParamsBig = (ViewGroup.MarginLayoutParams) gridViewBig.getLayoutParams();
        marginLayoutParamsBig.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMarginTop(), layoutProvider.getMarginRight(),0);
        gridViewBig.setLayoutParams(marginLayoutParamsBig);
        gridViewBig.setHorizontalSpacing(1);
        gridViewBig.setVerticalSpacing(1);

        // Now set the layout and the size of the cells for the small grid
        layoutProvider.computeCellSizeForSmallGrid();
        final ViewGroup.MarginLayoutParams marginLayoutParamsSmall = (ViewGroup.MarginLayoutParams) gridViewSmall.getLayoutParams();
        marginLayoutParamsSmall.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMarginTop(), layoutProvider.getMarginRight(), layoutProvider.getMarginBottom());
        gridViewSmall.setLayoutParams(marginLayoutParamsSmall);
        gridViewSmall.setHorizontalSpacing(1);
        gridViewSmall.setVerticalSpacing(1);

        /*
        SharedPreferences preferenceGridSize = this.getSharedPreferences("Grid size", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferenceGridSize.edit();
        edit.clear();
        edit.putString("Grid", GameGridAdapter.SMALL_GRID);
        edit.commit();
        */
        adapterMainGrid = new GameGridAdapter(this, this.layoutProvider, this.controller, true);
        gridViewBig.setAdapter(adapterMainGrid);
        adapterMiniGrid= new GameGridAdapter(this, this.layoutProvider, this.controller, false);
        gridViewSmall.setAdapter(adapterMiniGrid);

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
                adapterMainGrid.notifyDataSetChanged();
            }
        });
    }

    protected void setupRest(){

        TextView textPlayer = (TextView) findViewById(R.id.player_name);
        int playerName = this.controller.getCurrentPlayer() ? R.string.game_player_one : R.string.game_player_two;
        textPlayer.setText(getResources().getString(playerName));

        TextView timerView = (TextView) findViewById(R.id.timerView);
        timerView.setText(timeToString(0));

        TextView drawsView = (TextView) findViewById(R.id.draws);
        drawsView.setText(drawsToString(0));
    }

    protected void update(){
        //TODO: Implement this method
    }

    protected String drawsToString(int draws){
        return (draws < 10) ? "0" + String.valueOf(draws) : String.valueOf(draws);
    }

    private String timeToString(int time) {
        int seconds = time % 60;
        int minutes = ((time - seconds) / 60) % 60;
        int hours = (time - minutes - seconds) / (3600);
        String h, m, s;
        s = (seconds < 10) ? "0" + String.valueOf(seconds) : String.valueOf(seconds);
        m = (minutes < 10) ? "0" + String.valueOf(minutes) : String.valueOf(minutes);
        h = (hours < 10) ? "0" + String.valueOf(hours) : String.valueOf(hours);
        return h + ":" + m + ":" + s;
    }
}
