package org.secuso.privacyfriendlybattleships;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameGrid;
import org.secuso.privacyfriendlybattleships.game.GameMode;
import org.secuso.privacyfriendlybattleships.game.GameShip;

import java.util.Timer;

public class GameActivity extends BaseActivity {

    private Handler handler;
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
    private static final String TAG = GameActivity.class.getSimpleName();

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

        //set correct size for small grid
        gridViewSmall.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = gridViewSmall.getLayoutParams();
                layoutParams.width = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
                layoutParams.height = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
                Log.d(TAG, "" + layoutParams.width);
                gridViewSmall.setLayoutParams(layoutParams);
            }
        });
    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game;
    }

    public void onClickButton(View view) {

        GameGrid GridUnderAttack = this.controller.gridUnderAttack();

        int column = this.positionGridCell % this.gridSize;
        int row = this.positionGridCell / this.gridSize;
        GameCell attackedCell = GridUnderAttack.getCell(column, row);

        //Do not attack the same cell twice and do not click the fire button without clicking on a cell.
        if(attackedCell.isHit() || this.prevCell == null){
            return;
        }

        boolean isHit = this.controller.makeMove(this.controller.getCurrentPlayer(), column, row);
        if(!isHit) {
            controller.switchPlayers();
            if(this.controller.getCurrentPlayer() &&
                    (this.gameMode == GameMode.VS_AI_EASY || this.gameMode == GameMode.VS_AI_HARD) )
            {
                //make move for AI
                this.controller.getOpponentAI().makeMove();
                adapterMiniGrid.notifyDataSetChanged();
            }
            else{
                // Build a handler in order to delay the fade out of the grids.
                this.handler = new Handler();
                this.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // Fade out the grids
                        gridViewBig.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
                        gridViewSmall.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION);

                        /*
                        Build a second handler. Delay the switch of the players and the dialog after
                        the grids have been faded out.
                        */
                        Handler innerHandler = new Handler();
                        innerHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SwitchDialog switchDialog = new SwitchDialog();
                                switchDialog.setCancelable(false);
                                switchDialog.show(getFragmentManager(), SwitchDialog.class.getSimpleName());
                            }
                        }, MAIN_CONTENT_FADEOUT_DURATION);
                    }
                }, 1000);
            }
        }
        else{
            GameShip ship = GridUnderAttack.getShipSet().findShipContainingCell(attackedCell);
            if(ship.isDestroyed()){
                // Show dialog
                new GameDialog().show(getFragmentManager(), GameDialog.class.getSimpleName());
            }
        }
        adapterMainGrid.notifyDataSetChanged();
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

        // Set the layout of the grids
        final ViewGroup.MarginLayoutParams marginLayoutParamsBig =
                (ViewGroup.MarginLayoutParams) gridViewBig.getLayoutParams();
        final ViewGroup.MarginLayoutParams marginLayoutParamsSmall =
                (ViewGroup.MarginLayoutParams) gridViewSmall.getLayoutParams();

        marginLayoutParamsBig.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMargin(), layoutProvider.getMarginRight(),0);
        marginLayoutParamsSmall.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMargin(), layoutProvider.getMarginRight(),layoutProvider.getMargin());

        gridViewBig.setLayoutParams(marginLayoutParamsBig);
        gridViewSmall.setLayoutParams(marginLayoutParamsSmall);

        ViewGroup.LayoutParams layoutParams = gridViewSmall.getLayoutParams();
        layoutParams.width = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
        layoutParams.height = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
        Log.d(TAG, "" + layoutParams.width);
        gridViewSmall.setLayoutParams(layoutParams);

        gridViewBig.setHorizontalSpacing(1);
        gridViewBig.setVerticalSpacing(1);

        gridViewSmall.setHorizontalSpacing(1);
        gridViewSmall.setVerticalSpacing(1);

        // Initialize the grid for player one
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

    public void fadeInGrids(){

        setupGridViews();
        // Fade in the grids
        gridViewBig.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        gridViewSmall.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION);
    }

    public static class GameDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.game_dialog_hit)
                    .setPositiveButton("OK", null);
            // Create the AlertDialog object and return it
            return builder.create();
        }

    }

    public static class SwitchDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.game_dialog_next_player)
                    .setMessage(R.string.game_dialog_ready)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Fade in the grids after the next player has clicked on the button
                            ((GameActivity) getActivity()).fadeInGrids();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
