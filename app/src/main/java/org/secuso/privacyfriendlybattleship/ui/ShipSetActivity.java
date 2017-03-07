package org.secuso.privacyfriendlybattleship.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import org.secuso.privacyfriendlybattleship.Constants;
import org.secuso.privacyfriendlybattleship.R;
import org.secuso.privacyfriendlybattleship.game.GameController;
import org.secuso.privacyfriendlybattleship.game.GameShipSet;

/**
 * This activity is called from the PlaceShipActivity and allows a user to customize the number of
 * ships.
 */

public class ShipSetActivity extends BaseActivity {

    private GameController controller;
    private GameShipSet shipSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_set);

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");
        this.shipSet = controller.getGridFirstPlayer().getShipSet();

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
        new TutorialDialog().show(getFragmentManager(), PlaceShipActivity.TutorialDialog.class.getSimpleName());
    }

    private void setActivityStarted() {
        mSharedPreferences.edit().putBoolean(Constants.FIRST_PLACEMENT_START, false).commit();
    }

    public void onClickShipSetReady(View view){
        //TODO: Implement this method
        // Check if ship set is legit

        // In case the ship set is legit, create a new ship count.
        int[] newShipCount = new int[4];
        // TODO: Fill the ship count with information!

        this.controller = new GameController(this.controller.getGridSize(), newShipCount);
        // Go back to PlaceShipActivity
        Intent intent = new Intent(this, PlaceShipActivity.class);
        intent.putExtra("controller", this.controller);
        startActivity(intent);
        finish();
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_ship_set;
    }

    public static class TutorialDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setView(i.inflate(R.layout.placement_dialog, null));
            builder.setIcon(R.mipmap.icon_drawer);
            builder.setTitle(getActivity().getString(R.string.placement_tutorial_title));
            builder.setPositiveButton(getActivity().getString(R.string.okay), null);

            return builder.create();
        }
    }
}
