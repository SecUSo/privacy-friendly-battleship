package org.secuso.privacyfriendlybattleships;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

    private boolean move;
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

        // Show the welcome dialog only once after the app has been started
        if (isFirstAppStart()) {
            // Show a help dialog
            new HelpDialog().show(getFragmentManager(), HelpDialog.class.getSimpleName());
            setAppStarted();
        }

        setContentView(R.layout.activity_game);

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");

        this.gridSize = controller.getGridSize();
        this.gameMode = controller.getMode();

        // Set up the handler, which will be needed later in the code.
        this.handler = new Handler();

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
                gridViewSmall.setLayoutParams(layoutParams);
            }
        });

        // Start the timer for player one
        this.controller.startTimer();
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

    public void goBack(){
        // Go Back to the MainActivity
        timerUpdate.cancel();
        super.onBackPressed();
    }

    protected void onResume() {
        super.onResume();
        this.controller.startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.controller.stopTimer();
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

        GameShip ship = this.gridUnderAttack.getShipSet().findShipContainingCell(attackedCell);
        if(isHit){
            if(ship.isDestroyed()){
                this.controller.stopTimer();

                Bundle bundle = new Bundle();
                bundle.putInt("Size", ship.getSize());
                /*
                 Show dialog. The dialog will check if the current player has won after the player
                 has clicked on the OK button, cf. the respective onCreateDialog method.
                  */
                GameDialog gameDialog = GameDialog.newInstance(bundle);
                gameDialog.setCancelable(false);
                gameDialog.show(getFragmentManager(), GameDialog.class.getSimpleName());

            }
        }
        else{
            this.controller.stopTimer();
            gridViewBig.setClickable(false);
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

                    Bundle bundle = new Bundle();
                    bundle.putString("Time", this.controller.timeToString(this.controller.getTime()));
                    bundle.putString("Attempts", this.controller.attemptsToString(this.controller.getAttemptsPlayerOne()));

                    // Instantiate the lose dialog and show it
                    LoseDialog loseDialog = LoseDialog.newInstance(bundle);
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

    public void onClickFinishButton(View view){
        Button finishButton = (Button) findViewById(R.id.game_button_fire);
        finishButton.setText(R.string.finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainActivity(null);
            }
        });
    }

    /*
    public void makeMoveAI() {

        if(this.gameMode == GameMode.VS_AI_EASY) {
            this.move = true;
            while(move){
                move = controller.getOpponentAI().makeRandomMove();
                adapterMiniGrid.notifyDataSetChanged();
                if(!move){
                    controller.switchPlayers();
                }
            }
        } else if(this.gameMode == GameMode.VS_AI_HARD) {
            //TODO: implementation of AI for higher difficulty
        }
    }
    */

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
                view.setBackgroundColor(
                        adapterMainGrid.context.getResources().getColor(R.color.yellow));
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

    public void fadeInGrids(){

        setupGridViews();
        // Fade in the grids
        gridViewBig.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        gridViewSmall.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION);
    }

    public void goToMainActivity(View view){

        // Go back to the (old) MainActivity.
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // Exit the GameActivity
        this.finish();
    }

    public void terminate(){
        //check if player has won
        if (this.gridUnderAttack.getShipSet().allShipsDestroyed() ){
            //current player has won the game
            this.controller.stopTimer();
            timerUpdate.cancel();
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
        else{
            this.controller.startTimer();
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

    public static class GameDialog extends DialogFragment {

        private int size;

        public static GameDialog newInstance(Bundle bundle){
            GameDialog gameDialog = new GameDialog();
            gameDialog.setArguments(bundle);
            return gameDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            this.size = getArguments().getInt("Size");

            // Get the layout for the lose dialog as a view
            View gameDialogView = getActivity().getLayoutInflater().inflate(R.layout.game_dialog, null);

            // Set the size of the ship destroyed
            TextView textShipSize = (TextView) gameDialogView.findViewById(R.id.game_dialog_ship_size);
            textShipSize.setText(String.valueOf(this.size));

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(gameDialogView)
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

            // Get the layout for the lose dialog as a view
            View switchDialogView = getActivity().getLayoutInflater().inflate(R.layout.switch_dialog, null);

            TextView textPlayerName = (TextView) switchDialogView.findViewById(R.id.switch_dialog_title_player_name);
            textPlayerName.setText(this.playerName);

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(switchDialogView)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Fade in the grids after the next player has clicked on the button
                            ((GameActivity) getActivity()).controller.switchPlayers();

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
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((GameActivity) getActivity()).goToMainActivity(null);
                }
            })
                    .setNegativeButton(R.string.game_dialog_show_gamefield, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
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
                    .setView(winDialogView)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((GameActivity) getActivity()).goToMainActivity(null);
                        }
                    })
                    .setNegativeButton(R.string.game_dialog_show_gamefield, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
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
            builder.setMessage(R.string.game_dialog_quit)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((GameActivity) getActivity()).goBack();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Resume the timer
                            ((GameActivity) getActivity()).controller.startTimer();
                        }
                    });

            return builder.create();
        }

    }

    public static class HelpDialog extends DialogFragment{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            View helpDialogView = getActivity().getLayoutInflater().inflate(R.layout.help_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(helpDialogView)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if( !((GameActivity) getActivity()).isFirstAppStart() ){
                                ((GameActivity) getActivity()).controller.startTimer();
                            }
                        }
                    });


            return builder.create();
        }

    }
}