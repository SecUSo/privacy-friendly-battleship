package org.secuso.privacyfriendlybattleship.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.secuso.privacyfriendlybattleship.Constants;
import org.secuso.privacyfriendlybattleship.R;
import org.secuso.privacyfriendlybattleship.game.GameController;
import org.secuso.privacyfriendlybattleship.game.GameMode;
import org.secuso.privacyfriendlybattleship.game.GameShipSet;

/**
 * This activity is called from the PlaceShipActivity and allows a user to customize the number of
 * ships.
 */

public class ShipSetActivity extends BaseActivity {

    private GameController controller;
    private int[] newShipCount;
    private GameMode gameMode;
    private GameShipSet shipSet;
    private int numberGridCells;

    private int shipsSize5;
    private int shipsSize4;
    private int shipsSize3;
    private int shipsSize2;

    private int boundShipSet5;
    private int boundShipSet4;
    private int boundShipSet3;
    private int boundShipSet2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_set);

        /*
        Get the parameters from the MainActivity or the PlaceShipActivity and initialize the
        parameters necessary for this activity.
         */
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");
        this.gameMode = this.controller.getMode();
        this.shipSet = this.controller.getGridFirstPlayer().getShipSet();
        this.numberGridCells = this.controller.getGridSize() * this.controller.getGridSize();

        this.newShipCount = new int[3];
        this.shipsSize2 = this.shipSet.getNumberOfShipsSize2();
        this.shipsSize3 = this.shipSet.getNumberOfShipsSize3();
        this.shipsSize4 = this.shipSet.getNumberOfShipsSize4();
        this.shipsSize5 = this.shipSet.getNumberOfShipsSize5();

        this.newShipCount = new int[]{shipsSize2, shipsSize3, shipsSize4, shipsSize5};

        updateShipsofSize2();
        updateShipsofSize3();
        updateShipsofSize4();
        updateShipsofSize5();

        /*
         Set the bounds for the ship sizes. A bound is determined by the number of grid cells
         divided by two in order to avoid too many cells covered by ships. This result will again be
         divided by the size of the respective ship in order to get the bound for the respective
         ship size.
          */

        int bound = numberGridCells * 2 / 5;

        this.boundShipSet2 = bound / 2;
        this.boundShipSet3 = bound / 3;
        this.boundShipSet4 = bound / 4;
        this.boundShipSet5 = bound / 5;

        // Show the tutorial dialog if first time in activity
        if (isFirstActivityStart()) {
            showTutorialDialog();
            setActivityStarted();
        }
    }

    private boolean isFirstActivityStart() {
        return mSharedPreferences.getBoolean(Constants.FIRST_SHIP_SET_START, true);
    }

    private void showTutorialDialog() {
        new TutorialShipSetDialog().show(getFragmentManager(), PlaceShipActivity.TutorialDialog.class.getSimpleName());
    }

    private void setActivityStarted() {
        mSharedPreferences.edit().putBoolean(Constants.FIRST_SHIP_SET_START, false).commit();
    }

    public void addShipOfSize2(View view){
        if(this.shipsSize2 <= boundShipSet2){
            int[] temporaryShipCount = new int[]{shipsSize2 + 1, shipsSize3, shipsSize4, shipsSize5};
            if(this.controller.isShipCountLegit(temporaryShipCount)){
                this.shipsSize2 +=1;
                this.newShipCount[0] = this.shipsSize2;
                updateShipsofSize2();
            }
        }
    }

    public void addShipOfSize3(View view){
        if(this.shipsSize3 <= boundShipSet3){
            int[] temporaryShipCount = new int[]{shipsSize2, shipsSize3 + 1, shipsSize4, shipsSize5};
            if(this.controller.isShipCountLegit(temporaryShipCount)){
                this.shipsSize3 +=1;
                this.newShipCount[1] = this.shipsSize3;
                updateShipsofSize3();
            }
        }
    }

    public void addShipOfSize4(View view){
        if(this.shipsSize4 <= boundShipSet4){
            int[] temporaryShipCount = new int[]{shipsSize2, shipsSize3, shipsSize4 + 1, shipsSize5};
            if(this.controller.isShipCountLegit(temporaryShipCount)){
                this.shipsSize4 +=1;
                this.newShipCount[2] = this.shipsSize4;
                updateShipsofSize4();
            }
        }
    }

    public void addShipOfSize5(View view){
        if(this.shipsSize5 <= boundShipSet5){
            int[] temporaryShipCount = new int[]{shipsSize2, shipsSize3, shipsSize4, shipsSize5 + 1};
            if(this.controller.isShipCountLegit(temporaryShipCount)){
                this.shipsSize5 +=1;
                this.newShipCount[3] = this.shipsSize5;
                updateShipsofSize5();
            }
        }
    }

    public void subtractShipOfSize2(View view){
        if(this.shipsSize2 > 0){
            this.shipsSize2 -= 1;
            this.newShipCount[0] = this.shipsSize2;
            updateShipsofSize2();
        }
    }

    public void subtractShipOfSize3(View view){
        if(this.shipsSize3 > 0){
            this.shipsSize3 -= 1;
            this.newShipCount[1] = this.shipsSize3;
            updateShipsofSize3();
        }
    }

    public void subtractShipOfSize4(View view){
        if(this.shipsSize4 > 0){
            this.shipsSize4 -= 1;
            this.newShipCount[2] = this.shipsSize4;
            updateShipsofSize4();
        }
    }

    public void subtractShipOfSize5(View view){
        if(this.shipsSize5 > 0){
            this.shipsSize5 -= 1;
            this.newShipCount[3] = this.shipsSize5;
            updateShipsofSize5();
        }
    }

    public void updateShipsofSize2(){
        TextView ships2 = (TextView) findViewById(R.id.ship_set_size_two_number);
        ships2.setText(String.valueOf(shipsSize2));
    }

    public void updateShipsofSize3(){
        TextView ships3 = (TextView) findViewById(R.id.ship_set_size_three_number);
        ships3.setText(String.valueOf(shipsSize3));
    }

    public void updateShipsofSize4(){
        TextView ships4 = (TextView) findViewById(R.id.ship_set_size_four_number);
        ships4.setText(String.valueOf(shipsSize4));
    }

    public void updateShipsofSize5(){
        TextView ships5 = (TextView) findViewById(R.id.ship_set_size_five_number);
        ships5.setText(String.valueOf(shipsSize5));
    }

    public void onClickShipSetReady(View view){
        if(newShipCount[0] == 0 && newShipCount[1] == 0 && newShipCount[2] == 0 && newShipCount[3] == 0){
            new ShipSetAlertDialog().show(getFragmentManager(), ShipSetAlertDialog.class.getSimpleName());
        }
        else{
            this.controller = new GameController(this.gameMode, this.controller.getGridSize(), newShipCount);
            this.controller.placeAllShips();
            // Go back to PlaceShipActivity
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("controller", this.controller);
            startActivity(intent);
            finish();
        }
    }

    public void onClickPlaceShips(View view){
        if(newShipCount[0] == 0 && newShipCount[1] == 0 && newShipCount[2] == 0 && newShipCount[3] == 0){
            new ShipSetAlertDialog().show(getFragmentManager(), ShipSetAlertDialog.class.getSimpleName());
        }
        else{
            this.controller = new GameController(this.gameMode, this.controller.getGridSize(), newShipCount);
            this.controller.placeAllShips();
            // Go back to PlaceShipActivity
            Intent intent = new Intent(this, PlaceShipActivity.class);
            intent.putExtra("controller", this.controller);
            startActivity(intent);
        }
    }

    public static class TutorialShipSetDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setIcon(R.mipmap.icon_drawer);
            builder.setTitle(R.string.ship_set_title);
            builder.setMessage(R.string.ship_set_message);
            builder.setPositiveButton(getActivity().getString(R.string.okay), null);

            return builder.create();
        }
    }

    public static class ShipSetAlertDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setIcon(R.mipmap.icon_drawer);
            builder.setTitle(R.string.ship_set_alert_title);
            builder.setMessage(R.string.ship_set_alert_message);
            builder.setPositiveButton(getActivity().getString(R.string.okay), null);

            return builder.create();
        }
    }
}
