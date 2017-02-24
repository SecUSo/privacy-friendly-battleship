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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameGrid;
import org.secuso.privacyfriendlybattleships.game.GameMode;
import org.secuso.privacyfriendlybattleships.game.GameShip;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends BaseActivity {

    private Handler handler;
    private Timer timerUpdate;
    private SharedPreferences preferences = null;

    private TextView playerName;
    private TextView attempts;
    private GameMode gameMode;
    private int gridSize;
    private GameController controller;
    private GameGridAdapter adapterMainGrid;
    private GameGridAdapter adapterMiniGrid;
    private GridView gridViewBig;
    private GridView gridViewSmall;
    private GameActivityLayoutProvider layoutProvider;

    private boolean isHit;
    private GameCell attackedCell;
    private GameGrid gridUnderAttack;
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

        // Set up the time
        setUpTimer();

        // Initialize the toolbar by setting the name of the current player and the number of attempts
        this.playerName = (TextView) findViewById(R.id.player_name);
        this.attempts = (TextView) findViewById(R.id.game_attempts);

        // Update the toolbar
        updateToolbar();

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

        // Start the timer for player one
        this.controller.startTimer();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        // Go back to the MainActivity
        this.controller.stopTimer();
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

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game;
    }


    public void onClickDoneButton(View view){

        // Fade out the grids
        gridViewBig.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        gridViewSmall.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION);

        /*
        Build a handler. Delay the switch of the players and the dialog after the grids have been
        faded out.
        */
        this.handler = new Handler();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SwitchDialog switchDialog = new SwitchDialog();
                switchDialog.setCancelable(false);
                switchDialog.show(getFragmentManager(), SwitchDialog.class.getSimpleName());
            }
        }, MAIN_CONTENT_FADEOUT_DURATION);

        /*
        Change the listener and the text of the "Done" button, such that the grids fade out
        after the button has been clicked.
        */
        Button fireButton = (Button) findViewById(R.id.game_button_fire);
        fireButton.setText(R.string.game_button_fire);
        fireButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                gridViewBig.setClickable(true);
                onClickFireButton(view);
            }
        });

    }


    public void onClickFireButton(View view) {

        this.gridUnderAttack = this.controller.gridUnderAttack();

        // Get the cell, which shall be attacked
        int column = this.positionGridCell % this.gridSize;
        int row = this.positionGridCell / this.gridSize;
        this.attackedCell = gridUnderAttack.getCell(column, row);

        //Do not attack the same cell twice and do not click the fire button without clicking on a cell.
        if(attackedCell.isHit() || this.prevCell == null){
            return;
        }

        // Attack the cell and update the main grid
        this.isHit = this.controller.makeMove(this.controller.getCurrentPlayer(), column, row);
        updateToolbar();
        adapterMainGrid.notifyDataSetChanged();
        gridViewBig.setClickable(false);

        GameShip ship = this.gridUnderAttack.getShipSet().findShipContainingCell(attackedCell);
        if(isHit){
            if(ship.isDestroyed()){
                // Show dialog
                new GameDialog().show(getFragmentManager(), GameDialog.class.getSimpleName());
                /*
                //check if player has won
                if (this.gridUnderAttack.getShipSet().allShipsDestroyed() ){
                    //current player has won the game
                }
                */
            }
        }
        else{
            this.controller.stopTimer();

            // If the attacked cell does not contain a ship, then stop the timer and switch the player
            if(this.gameMode == GameMode.VS_AI_EASY || this.gameMode == GameMode.VS_AI_HARD){
                controller.switchPlayers();
                //make move for AI
                this.controller.getOpponentAI().makeMove();
                adapterMiniGrid.notifyDataSetChanged();
                if(this.controller.getOpponentAI().isAIWinner()){
                    timerUpdate.cancel();

                    /*
                    Create a dialog. Therefore, instantiate a bundle which transfers the data from the
                    current game to the dialog.
                     */
                    /*
                    Bundle bundle = new Bundle();
                    bundle.putString("Time", this.controller.timeToString(this.controller.getTime()));
                    bundle.putString("Attempts", this.controller.attemptsToString(this.controller.getAttemptsPlayerOne()));
                    */
                    // Instantiate the lose dialog and show it
                    LoseDialog loseDialog = new LoseDialog();
                    //loseDialog.newInstance(bundle);
                    loseDialog.setCancelable(false);
                    loseDialog.show(getFragmentManager(), LoseDialog.class.getSimpleName());
                }
                else{
                    // Restart the timer for player one
                    this.controller.startTimer();
                }
            }
            else{

                /*
                Change the listener and the text of the "Fire" button, such that the grids fade out
                after the button has been clicked.
                 */
                Button doneButton = (Button) findViewById(R.id.game_button_fire);
                doneButton.setText(R.string.game_button_done);
                doneButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        onClickDoneButton(view);
                    }
                });
            }
        }
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

    public void updateToolbar(){
        if(this.gameMode == GameMode.VS_PLAYER || this.gameMode == GameMode.CUSTOM){
            int currentPlayerName = this.controller.getCurrentPlayer() ? R.string.game_player_two : R.string.game_player_one;
            this.playerName.setText(currentPlayerName);
        }
        else{
            this.playerName.setText("");
        }

        int attemptsCurrentPlayer = this.controller.getCurrentPlayer() ? this.controller.getAttemptsPlayerTwo() : this.controller.getAttemptsPlayerOne();
        this.attempts.setText(this.controller.attemptsToString(attemptsCurrentPlayer));
    }

    public void setUpTimer(){
        // Setup timer task and timer view. This setup updates the current time of a player every second.
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
                            ((GameActivity)getActivity()).controller.switchPlayers();

                            // Update the toolbar
                            ((GameActivity) getActivity()).updateToolbar();
                            ((GameActivity) getActivity()).fadeInGrids();
                            ((GameActivity) getActivity()).controller.startTimer();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class LoseDialog extends DialogFragment {

        private String time;
        private String attempts;

        public void newInstance(Bundle bundle){
            this.time = bundle.getString("Time");
            this.attempts = bundle.getString("Attempts");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            // Set the current time and the number of attempts for player one
            //TextView textTime = (TextView) getActivity().findViewById(R.id.lose_dialog_time);
            //textTime.setText(this.time);

            //TextView textAttempts = (TextView) getActivity().findViewById(R.id.lose_dialog_attempts);
            //textAttempts.setText(this.attempts);

            // Build the dialog
            LayoutInflater inflater = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(inflater.inflate(R.layout.lose_dialog, null))
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