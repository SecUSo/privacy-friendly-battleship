package org.secuso.privacyfriendlybattleships;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameGrid;
import org.secuso.privacyfriendlybattleships.game.GameMode;
import org.secuso.privacyfriendlybattleships.game.GameShip;
import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends BaseActivity {

    private Bundle bundle;
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

        // Set up the grids, the timer and the toolbar for player one
        setupGridViews();
        setupTimer();
        setupToolBar();

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
    protected void onResume() {
        super.onResume();
        this.controller.startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.controller.stopTimer();
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game;
    }

    public void onClickButton(View view) {

        // Stop the time of the current player
        this.controller.stopTimer();

        // Get the grid of the current player
        Boolean currentPlayer = this.controller.getCurrentPlayer();
        GameGrid GridUnderAttack = currentPlayer ?
                this.controller.getGridFirstPlayer() :
                this.controller.getGridSecondPlayer();

        // Get the cell attacked by the current player
        int column = this.positionGridCell % this.gridSize;
        int row = this.positionGridCell / this.gridSize;
        GameCell attackedCell = GridUnderAttack.getCell(column, row);

        //Don't attack same cell twice
        if(attackedCell.isHit()){
            return;
        }

        // Attack the cell
        boolean isHit = this.controller.makeMove(currentPlayer, column, row);
        adapterMainGrid.notifyDataSetChanged();
        if(isHit) {
            // Check if a ship is destroyed. Show a dialog.
            GameShip ship = GridUnderAttack.getShipSet().findShipContainingCell(attackedCell);
            if(ship.isDestroyed()){
                // Show dialog
                new GameDialog().show(getFragmentManager(), GameDialog.class.getSimpleName());

                // Check if the current player has won the game and show a dialog. Exit the GameActivity.
                if(GridUnderAttack.getShipSet().allShipsDestroyed()){
                    timerUpdate.cancel();
                    // Show dialog
                    new WinOrLoseDialog().show(getFragmentManager(), WinOrLoseDialog.class.getSimpleName());
                }
            }
        }
        else{

            // Distinguish the game modes
            if(currentPlayer && (this.gameMode == GameMode.VS_AI_EASY || this.gameMode == GameMode.VS_AI_HARD) ){
                /*
                Make move for AI. Note that the AI implicitly is the second player, therefore it is
                necessary to switch the players in order to determine a winner.
                 */
                this.controller.switchPlayers();
                this.controller.getOpponentAI().makeMove();
                adapterMiniGrid.notifyDataSetChanged();

                if(this.controller.getGridFirstPlayer().getShipSet().allShipsDestroyed()){
                    // Show lose dialog
                    timerUpdate.cancel();
                    new WinOrLoseDialog().show(getFragmentManager(), WinOrLoseDialog.class.getSimpleName());
                }
            }
            else{
                // Fade out the grids
                gridViewBig.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
                gridViewSmall.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION);

                /*
                Build a handler. Delay the switch of the players and the display of the message after
                the grids have been faded out.
                */
                this.handler = new Handler();
                this.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Switch the players and how the corresponding dialog
                        controller.switchPlayers();
                        new SwitchDialog().show(getFragmentManager(), SwitchDialog.class.getSimpleName());
                        setupGridViews();
                    }
                }, MAIN_CONTENT_FADEOUT_DURATION);

                // Fade in the grids
                gridViewBig.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION);
                gridViewSmall.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION);
            }
        }

        // Update the toolbar
        setupToolBar();

        // Start the timer
        controller.startTimer();
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

    public void setupTimer(){
        /*
        Setup the timer view and the timer task. The timer task shall update the current time and
        display it on the view.
         */
        final TextView timerView = (TextView) findViewById(R.id.timerView);
        timerUpdate = new Timer();
        timerUpdate.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerView.setText(controller.timeToString(controller.getTime()));
                    }
                });
            }
        }, 0, 1000);
    }

    public void setupToolBar(){
        // Setup the text of the current player and the number of attempts
        TextView player = (TextView) findViewById(R.id.game_player_name);
        TextView attempts = (TextView) findViewById(R.id.game_attempts);
        if(!controller.getCurrentPlayer()){
            player.setText(R.string.game_player_one);
            attempts.setText(this.controller.getAttemptsPlayerOne());
        }
        else{
            player.setText(R.string.game_player_two);
            attempts.setText(this.controller.getAttemptsPlayerTwo());
        }

    }

    public static class GameDialog extends DialogFragment{

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

    public static class SwitchDialog extends DialogFragment{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.game_dialog_next_player)
                    .setPositiveButton("OK", null);
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class WinOrLoseDialog extends DialogFragment{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.game_win_dialog_title)
                    .setMessage(R.string.game_win_dialog_text_single_2)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Go back to the (old) MainActivity.
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            // Exit the GameActivity
                            getActivity().finish();
                        }
                    });

            return builder.create();
        }

        }
}