/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * GameActivity.java is part of Privacy Friendly Battleship.
 *
 * Privacy Friendly Battleship is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Friendly Battleship is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Battleship. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlybattleship.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.secuso.privacyfriendlybattleship.Constants;
import org.secuso.privacyfriendlybattleship.R;
import org.secuso.privacyfriendlybattleship.game.GameCell;
import org.secuso.privacyfriendlybattleship.game.GameController;
import org.secuso.privacyfriendlybattleship.game.GameGrid;
import org.secuso.privacyfriendlybattleship.game.GameMode;
import org.secuso.privacyfriendlybattleship.game.GameShip;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This activity enables a user to play the game depending on the game mode and size of the game
 * board he has chosen in the MainActivity. This activity is called either in the MainActivity by
 * pressing on the QUICK START button or when the PlaceShipActivity has finished.
 *
 * @author Alexander Müller, Ali Kalsen
 */

public class GameActivity extends BaseActivity {

    private Handler handler;
    private Timer timerUpdate;

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

    private boolean isCellClicked;
    private boolean hasStarted;
    private boolean moveMade;       // Necessary for the help and the back button in order to control the timer and the configuration changes
    private boolean isGameFinished;
    private boolean isShowAllShipsButtonClicked;
    private boolean isSwitchDialogDisplayed;
    private GameCell attackedCell;
    private GameGrid gridUnderAttack;
    private int positionGridCell;   // Save the current position of the grid cell clicked
    private View prevCell = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);

        // Since the GameActivity is created, the game has not finished and the "Show all ships" button has not been clicked
        this.isGameFinished = false;
        this.isShowAllShipsButtonClicked = false;

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");
        this.gridSize = controller.getGridSize();
        this.gameMode = controller.getMode();

        // Set up the handler, which will be needed later in the code.
        this.handler = new Handler();

        // Create a GameActivityLayoutProvider in order to scale the grids appropriately
        layoutProvider = new GameActivityLayoutProvider(this, this.gridSize);

        // Check if the configuration has changed before the grid views are set up
        if(savedInstanceState != null){
            this.moveMade = savedInstanceState.getBoolean("move made");
            this.hasStarted = savedInstanceState.getBoolean("has started");
            this.isGameFinished = savedInstanceState.getBoolean("game finished");
            this.isSwitchDialogDisplayed = savedInstanceState.getBoolean("switch dialog shown");
        }

        if(this.isGameFinished){
            /*
            Re-switch the player such that the correct toolbar and grids are shown after the game
            has finished and the configuration has changed. Note that the number of switches has to
            be even in order to get the correct player every time the GameActivity is recreated.
            */
            this.controller.switchPlayers();
        }

        this.playerName = (TextView) findViewById(R.id.player_name);
        this.attempts = (TextView) findViewById(R.id.game_attempts);

        // Update the toolbar
        updateToolbar();

        // Set up the grids for the current player and make them invisible until the player is ready.
        setupGridViews();

        // Set up the time
        setUpTimer();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            //set correct size for small grid
            gridViewSmall.post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.LayoutParams layoutParams = gridViewSmall.getLayoutParams();
                    layoutParams.width = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
                    layoutParams.height = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
                    gridViewSmall.setLayoutParams(layoutParams);
                }
            });
        }

        if(controller.getMode() == GameMode.VS_PLAYER || controller.getMode() == GameMode.CUSTOM){
            // Check if the configuration has changed
            if(savedInstanceState == null){
                showSwitchDialog();
                // Show the help dialog on top of the switch dialog in case the app has started for the first time.
                showHelpDialog();
            }
            else{
                // Do the following steps if the configuration has changed

                // Check if the game has been finished
                if(this.isGameFinished){
                    gridViewBig.setEnabled(false);
                    onClickShowMainGridButton(null);
                    onClickFinishButton(null);
                }
                else{
                    // Check if a cell was attacked
                    if(moveMade){
                        /*
                        Change the listener and the text of the "Fire" button, such that a move can
                        be finished after the button has been clicked.
                        */
                        gridViewBig.setEnabled(false);
                        Button doneButton = (Button) findViewById(R.id.game_button_fire);
                        doneButton.setText(R.string.game_button_done);
                        doneButton.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                                onClickDoneButton(view);
                            }
                        });
                    }
                    else{
                        if(this.isSwitchDialogDisplayed || !this.hasStarted){
                            gridViewBig.setAlpha(0.0f);
                            gridViewSmall.setAlpha(0.0f);
                        }
                    }
                }
            }
        }
        else{
            //setup GridViews again after layout is finished to avoid wrong icon rendering
            final LinearLayout layout = (LinearLayout) findViewById(R.id.game_linear_layout);
            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    setupGridViews();
                }
            });

            if(this.isGameFinished){
                gridViewBig.setEnabled(false);
                onClickShowMainGridButton(null);
                onClickFinishButton(null);
            }
            showHelpDialog();
        }
    }

    private boolean isFirstActivityStart() {
        return mSharedPreferences.getBoolean(Constants.FIRST_GAME_START, true);

    }

    private void setActivityStarted(){
        mSharedPreferences.edit().putBoolean(Constants.FIRST_GAME_START, false).commit();
    }

    /**
     * Shows the help dialog when the app has started for the first time.
     */
    public void showHelpDialog(){
        if (isFirstActivityStart()) {
            HelpDialog helpDialog = new HelpDialog();
            helpDialog.setCancelable(false);
            helpDialog.show(getFragmentManager(), HelpDialog.class.getSimpleName());
        }
    }

    public void showSwitchDialog(){
        this.hasStarted = false;
        // Make the grids invisible until player one is ready
        gridViewBig.setAlpha(0.0f);
        gridViewSmall.setAlpha(0.0f);

        // Create a bundle for transferring data to the SwitchDialog
        Bundle bundle = new Bundle();
        int currentPlayerName = this.controller.getCurrentPlayer() ? R.string.game_player_two : R.string.game_player_one;
        bundle.putInt("Name", currentPlayerName);

        // Ask if player one is ready
        SwitchDialog newSwitchDialog = SwitchDialog.newInstance(bundle);
        newSwitchDialog.setCancelable(false);
        newSwitchDialog.show(getFragmentManager(), SwitchDialog.class.getSimpleName());
    }

    @Override
    public void onBackPressed(){
        // Check if the menu drawer is open
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Display a dialog which asks the current player, if he wants to quit the game
            this.controller.stopTimer();
            GoBackDialog goBackDialog = new GoBackDialog();
            goBackDialog.setCancelable(false);
            goBackDialog.show(getFragmentManager(), GoBackDialog.class.getSimpleName());
        }
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.hasStarted || this.gameMode == GameMode.VS_AI_EASY || this.gameMode == GameMode.VS_AI_HARD){
            this.controller.startTimer();
            if(this.moveMade || this.isSwitchDialogDisplayed || this.isGameFinished || this.controller.getOpponentAI().isAIWinner()){
                this.controller.stopTimer();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.controller.stopTimer();
    }

    /*
    this method saves the auxiliary variables of the GameActivity, such that the game can be
    recreated correctly once the configuration has changed.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(this.isGameFinished && !this.isShowAllShipsButtonClicked){
            this.controller.switchPlayers();
        }
        savedInstanceState.putParcelable("controller", this.controller);
        savedInstanceState.putBoolean("move made", this.moveMade);
        savedInstanceState.putBoolean("has started", this.hasStarted);
        savedInstanceState.putBoolean("game finished", this.isGameFinished);
        savedInstanceState.putBoolean("switch dialog shown", this.isSwitchDialogDisplayed);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onClickHelpButton(View view){
        this.controller.stopTimer();
        // Show a help dialog
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setCancelable(false);
        helpDialog.show(getFragmentManager(), HelpDialog.class.getSimpleName());
    }

    public void onClickDoneButton(View view){

        // Fade out the grids
        gridViewBig.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        gridViewSmall.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION);

        this.moveMade = false;
        this.isSwitchDialogDisplayed = true;

        /*
        Build a handler. Delay the switch of the players and the dialog after the grids have been
        faded out.
        */
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                /*
                Get the name of the next player. Note that the players are switched when the
                SwitchDialog is executed.
                 */
                int playerName = controller.getCurrentPlayer() ?  R.string.game_player_one : R.string.game_player_two;

                // Create a bundle for transferring data to the SwitchDialog
                Bundle bundle = new Bundle();
                bundle.putInt("Name", playerName);

                SwitchDialog switchDialog = SwitchDialog.newInstance(bundle);
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
        if(attackedCell.isHit() || this.prevCell == null || !isCellClicked){
            return;
        }

        // Attack the cell and update the main grid.
        this.controller.makeMove(this.controller.getCurrentPlayer(), column, row);
        this.moveMade = true;
        // Denote that the cells are not clicked anymore such that fire button can only be executed if a cell has been clicked
        this.isCellClicked = false;
        updateToolbar();
        adapterMainGrid.notifyDataSetChanged();

        final GameShip ship = this.gridUnderAttack.getShipSet().findShipContainingCell(attackedCell);
        this.controller.stopTimer();
        // Check if the current hit has destroyed a ship
        if(ship != null && ship.isDestroyed()){

            int playerName = controller.getCurrentPlayer() ?  R.string.game_player_two : R.string.game_player_one;
            Bundle bundle = new Bundle();
            bundle.putInt("Name", playerName);
            bundle.putInt("Size", ship.getSize());
            /*
            Show a dialog. The dialog will check if the current player has won after the player
            has clicked on the OK button, cf. the respective onCreateDialog method.
            */
            GameDialog gameDialog = GameDialog.newInstance(bundle);
            gameDialog.setCancelable(false);
            gameDialog.show(getFragmentManager(), GameDialog.class.getSimpleName());

        }
        else{
            // Terminate the fire button
            terminateFireButton();
        }
    }

    // Switch the player or make the move for the AI
    private void terminateFireButton(){
        // If the attacked cell does not contain a ship, then stop the timer and switch the player
        if(this.gameMode == GameMode.VS_AI_EASY || this.gameMode == GameMode.VS_AI_HARD){

            controller.switchPlayers();
            //make move for AI
            controller.getOpponentAI().makeMove();
            this.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapterMiniGrid.notifyDataSetChanged();
                    if(controller.getOpponentAI().isAIWinner()){
                        timerUpdate.cancel();

                        /*
                        Create a dialog. Therefore, instantiate a bundle which transfers the data from the
                        current game to the dialog.
                        */
                        Bundle bundle = new Bundle();
                        bundle.putString("Time", controller.timeToString(controller.getTime()));
                        bundle.putString("Attempts", controller.attemptsToString(controller.getAttemptsPlayerOne()));

                        // Instantiate the lose dialog and show it
                        LoseDialog loseDialog = LoseDialog.newInstance(bundle);
                        loseDialog.setCancelable(false);
                        loseDialog.show(getFragmentManager(), LoseDialog.class.getSimpleName());
                    }
                    else {
                        // Restart the timer for player one
                        controller.startTimer();
                    }
                }
            }, 250);
            this.moveMade = false;
        }
        else{
            /*
            Change the listener and the text of the "Fire" button, such that the grids fade out
            after the button has been clicked.
            */
            gridViewBig.setEnabled(false);
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

    public void onClickFinishButton(View view){
        Button finishButton = (Button) findViewById(R.id.game_button_fire);
        finishButton.setText(R.string.finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainActivity();
            }
        });
    }

    public void onClickShowMainGridButton(View view){
        // Only switch the players once after the game has finished in order to display the ships on the grid
        if(!this.isGameFinished){
            this.isGameFinished = true;
        }

        /*
        Change the listener and the text of the "HELP" button, such that the ships on the main grid
        are displayed after the button has been clicked.
        */
        final Button showAllShipsButton = (Button) findViewById(R.id.game_button_help);
        showAllShipsButton.setText(R.string.game_button_show_ships);
        showAllShipsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                controller.switchPlayers();
                isShowAllShipsButtonClicked = true;
                showAllShipsButton.setBackground(getResources().getDrawable(R.drawable.button_disabled));
                showAllShipsButton.setEnabled(false);
                showShipsOnMainGrid();
            }
        });
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

        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            marginLayoutParamsBig.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMargin(), layoutProvider.getMarginRight(),0);
        }
        else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            marginLayoutParamsBig.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMargin(), layoutProvider.getMarginRight(),layoutProvider.getMargin());
        }

        marginLayoutParamsSmall.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMargin(), layoutProvider.getMarginRight(),layoutProvider.getMargin());
        gridViewBig.setLayoutParams(marginLayoutParamsBig);
        gridViewSmall.setLayoutParams(marginLayoutParamsSmall);

        ViewGroup.LayoutParams layoutParams = gridViewSmall.getLayoutParams();
        layoutParams.width = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
        layoutParams.height = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
        gridViewSmall.setLayoutParams(layoutParams);

        gridViewBig.setHorizontalSpacing(1);
        gridViewBig.setVerticalSpacing(1);

        gridViewSmall.setHorizontalSpacing(1);
        gridViewSmall.setVerticalSpacing(1);

        adapterMainGrid = new GameGridAdapter(this, this.layoutProvider, this.controller, true);
        adapterMiniGrid= new GameGridAdapter(this, this.layoutProvider, this.controller, false);
        gridViewBig.setAdapter(adapterMainGrid);
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
                view.setBackgroundColor(
                        adapterMainGrid.context.getResources().getColor(R.color.yellow));
                prevCell = view;
                // Display the grid cell, which was clicked.
                adapterMainGrid.notifyDataSetChanged();
                isCellClicked = true;
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

    public void fadeInGrids(){

        setupGridViews();
        // Fade in the grids
        gridViewBig.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        gridViewBig.setEnabled(true);
        gridViewSmall.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        if(!this.hasStarted){
            this.hasStarted = true;
        }
        else{
            this.isSwitchDialogDisplayed = false;
        }
    }

    public void goToMainActivity(){

        // Go back to the (old) MainActivity.
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // Exit the GameActivity
        this.finish();
    }

    public void terminate(){
        //check if player has won
        if (this.controller.gridUnderAttack().getShipSet().allShipsDestroyed() ){
            timerUpdate.cancel();
            gridViewBig.setEnabled(false);
            /*
            Create a dialog. Therefore, instantiate a bundle which transfers the data from the
            current game to the dialog.
            */
            int nameWinner = this.controller.getCurrentPlayer() ? R.string.game_player_two : R.string.game_player_one;
            int attemptsWinner = this.controller.getCurrentPlayer() ? this.controller.getAttemptsPlayerTwo()
                    : this.controller.getAttemptsPlayerOne();
            Bundle bundle = new Bundle();
            bundle.putInt("Player", nameWinner);
            bundle.putString("Time", this.controller.timeToString(this.controller.getTime()));
            bundle.putString("Attempts", this.controller.attemptsToString(attemptsWinner));

            // Instantiate the win dialog and show it
            WinDialog winDialog = WinDialog.newInstance(bundle);
            winDialog.setCancelable(false);
            winDialog.show(getFragmentManager(), WinDialog.class.getSimpleName());
        }
        else {
            terminateFireButton();
        }
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

    public void showShipsOnMainGrid(){
        GameGridAdapter newAdapter = new GameGridAdapter(this, this.layoutProvider, this.controller, true, true);
        gridViewBig.setAdapter(newAdapter);
        gridViewBig.setEnabled(false);
    }

    public static class GameDialog extends DialogFragment {

        private int size;
        private int playerName;

        public static GameDialog newInstance(Bundle bundle){
            GameDialog gameDialog = new GameDialog();
            gameDialog.setArguments(bundle);
            return gameDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            this.size = getArguments().getInt("Size");
            this.playerName = getArguments().getInt("Name");

            // Get the layout for the lose dialog as a view
            View gameDialogView = getActivity().getLayoutInflater().inflate(R.layout.game_dialog, null);

            // Set the size of the ship destroyed
            TextView textShipSize = (TextView) gameDialogView.findViewById(R.id.game_dialog_ship_size);
            textShipSize.setText(String.valueOf(this.size));

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(this.playerName)
                    .setIcon(R.mipmap.icon_drawer)
                    .setView(gameDialogView)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Check if the game has a winner and terminate it in that case.
                            ((GameActivity) getActivity()).terminate();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

    }

    public static class SwitchDialog extends DialogFragment {

        private int playerName;

        public static SwitchDialog newInstance(Bundle bundle){
            SwitchDialog switchDialog = new SwitchDialog();
            switchDialog.setArguments(bundle);
            return switchDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            this.playerName = getArguments().getInt("Name");

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(this.playerName)
                    .setIcon(R.mipmap.icon_drawer)
                    .setMessage(R.string.game_dialog_next_player)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Fade in the grids after the next player has clicked on the button
                            if(((GameActivity) getActivity()).hasStarted){
                                ((GameActivity) getActivity()).controller.switchPlayers();
                            }

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

        public static LoseDialog newInstance(Bundle bundle){
            LoseDialog loseDialog = new LoseDialog();
            loseDialog.setArguments(bundle);
            return loseDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            this.time = getArguments().getString("Time");
            this.attempts = getArguments().getString("Attempts");

            // Get the layout for the lose dialog as a view
            View loseDialogView = getActivity().getLayoutInflater().inflate(R.layout.lose_dialog, null);

            // Set the current time and the number of attempts.
            TextView textTime = (TextView) loseDialogView.findViewById(R.id.lose_dialog_time);
            textTime.setText(this.time);

            TextView textAttempts = (TextView) loseDialogView.findViewById(R.id.lose_dialog_attempts);
            textAttempts.setText(this.attempts);

            // Build the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(loseDialogView)
                    .setTitle(R.string.game_dialog_loss)
                    .setIcon(R.mipmap.icon_drawer)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((GameActivity) getActivity()).goToMainActivity();
                        }
                    })
                    .setNegativeButton(R.string.game_dialog_show_game_board, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((GameActivity) getActivity()).onClickShowMainGridButton(getView());
                            ((GameActivity) getActivity()).onClickFinishButton(getView());
                        }
                    });

            return builder.create();
        }
    }

    public static class WinDialog extends DialogFragment {

        private String time;
        private String attempts;
        private int playerName;

        public static WinDialog newInstance(Bundle bundle){
            WinDialog winDialog = new WinDialog();
            winDialog.setArguments(bundle);
            return winDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            this.time = getArguments().getString("Time");
            this.attempts = getArguments().getString("Attempts");
            this.playerName = getArguments().getInt("Player");

            // Get the layout for the lose dialog as a view
            View winDialogView = getActivity().getLayoutInflater().inflate(R.layout.win_dialog, null);

            // Set the current time, the name of the player and the number of attempts.
            TextView textTime = (TextView) winDialogView.findViewById(R.id.win_dialog_time);
            textTime.setText(this.time);

            TextView textAttempts = (TextView) winDialogView.findViewById(R.id.win_dialog_attempts);
            textAttempts.setText(this.attempts);

            TextView textPlayerName = (TextView) winDialogView.findViewById(R.id.win_dialog_player_name);
            textPlayerName.setText(this.playerName);

            // Build the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.game_dialog_win)
                    .setIcon(R.mipmap.icon_drawer)
                    .setView(winDialogView)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((GameActivity) getActivity()).goToMainActivity();
                        }
                    })
                    .setNegativeButton(R.string.game_dialog_show_game_board, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((GameActivity) getActivity()).onClickShowMainGridButton(getView());
                            ((GameActivity) getActivity()).onClickFinishButton(getView());
                        }
                    });

            return builder.create();
        }

    }

    public static class GoBackDialog extends DialogFragment{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.game_dialog_quit)
                    .setIcon(R.mipmap.icon_drawer)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((GameActivity) getActivity()).goToMainActivity();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if( !((GameActivity) getActivity()).moveMade ){
                                // Resume the timer
                                ((GameActivity) getActivity()).controller.startTimer();
                            }
                        }
                    });

            return builder.create();
        }

    }

    public static class HelpDialog extends DialogFragment{

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setView(i.inflate(R.layout.help_dialog, null));
            builder.setTitle(getActivity().getString(R.string.help_dialog_title));
            builder.setIcon(R.mipmap.icon_drawer);

            builder.setPositiveButton(getActivity().getString(R.string.okay), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if( !((GameActivity) getActivity()).isFirstActivityStart() && !((GameActivity) getActivity()).moveMade){
                        ((GameActivity) getActivity()).controller.startTimer();
                    }
                    else{
                        ((GameActivity) getActivity()).setActivityStarted();
                    }
                }
            });
            return builder.create();
        }
    }
}