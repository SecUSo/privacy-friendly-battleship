/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * PlaceShipActivity.java is part of Privacy Friendly Battleship.
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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import org.secuso.privacyfriendlybattleship.Constants;
import org.secuso.privacyfriendlybattleship.R;
import org.secuso.privacyfriendlybattleship.game.Direction;
import org.secuso.privacyfriendlybattleship.game.GameCell;
import org.secuso.privacyfriendlybattleship.game.GameController;
import org.secuso.privacyfriendlybattleship.game.GameMode;
import org.secuso.privacyfriendlybattleship.game.GameShip;

/**
 * This class is used to implement the activity for the placement of
 * ships. It provides a simple way to place ships on the grid using
 * buttons and checks if the placement is legitimate before starting the
 * game. It also includes a basic tutorial and notification in case of
 * illegal ship placement.
 *
 * @author Alexander Müller, Ali Kalsen
 */

public class PlaceShipActivity extends BaseActivity {

    private SharedPreferences preferences = null;
    private GameController controller;
    private int gridSize;
    private GameActivityLayoutProvider layoutProvider;
    private GridView gridView;
    private GameGridAdapter gridAdapter;
    private GameShip selectedShip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPreferences();
        setContentView(R.layout.activity_place_ship);

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");
        this.gridSize = controller.getGridSize();
        layoutProvider = new GameActivityLayoutProvider(this, this.gridSize);

        setupGridView(this.gridSize);

        // Show the tutorial dialog if first time in activity
        if (isFirstActivityStart()) {
            showTutorialDialog();
            setActivityStarted();
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    private boolean isFirstActivityStart() {
        return preferences.getBoolean(Constants.FIRST_PLACEMENT_START, true);
    }

    private void showTutorialDialog() {
        new TutorialDialog().show(getFragmentManager(), TutorialDialog.class.getSimpleName());
    }

    private void setActivityStarted() {
        preferences.edit().putBoolean(Constants.FIRST_PLACEMENT_START, false).commit();
    }

    private void showInvalidPlacementDialog() {
        new InvalidPlacementDialog().show(getFragmentManager(), InvalidPlacementDialog.class.getSimpleName());
    }

    private void showSwitchPlayerDialog() {
        DialogFragment switchDialog = new SwitchPlayerDialog();
        switchDialog.setCancelable(false);
        switchDialog.show(getFragmentManager(), SwitchPlayerDialog.class.getSimpleName());
    }

    protected void setupGridView(int size){
        // Get the grid views of the respective XML-files
        gridView = (GridView) findViewById(R.id.placement_gridview);

        // Set the background color of the grid
        gridView.setBackgroundColor(Color.GRAY);

        // Set the columns of the grid
        gridView.setNumColumns(this.gridSize);

        // Set the layout of the grid
        final ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) gridView.getLayoutParams();

        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            marginLayoutParams.setMargins(
                    layoutProvider.getMarginLeft(),
                    layoutProvider.getMargin(),
                    layoutProvider.getMarginRight(),
                    0);
        }
        else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            marginLayoutParams.setMargins(
                    layoutProvider.getMarginLeft(),
                    layoutProvider.getMargin(),
                    layoutProvider.getMarginRight(),
                    layoutProvider.getMargin());
        }

        gridView.setLayoutParams(marginLayoutParams);
        gridView.setHorizontalSpacing(1);
        gridView.setVerticalSpacing(1);

        // Initialize the grid for player one
        gridAdapter = new GameGridAdapter(this, this.layoutProvider, this.controller, true, true);
        gridView.setAdapter(gridAdapter);


        // Define the listener for the big grid view, such that it is possible to click on it. When
        // clicking on that grid, the corresponding cell should be yellow.
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int column = i % gridSize;
                int row = i / gridSize;

                GameCell[] shipsCells;
                if(selectedShip != null) {
                    //mark ships cells not highlighted
                    unhighlightCells(selectedShip.getShipsCells());
                }

                GameCell selectedCell = controller.getCurrentGrid().getCell(column, row);
                selectedShip = controller.getCurrentGrid().getShipSet().findShipContainingCell(selectedCell);

                //highlight ships cells
                if ( selectedShip != null )
                    highlightCells(selectedShip.getShipsCells());

                gridAdapter.notifyDataSetChanged();
            }
        });

    }

    private void highlightCells(GameCell[] cells) {
        for( GameCell cell : cells ) {
            int col = cell.getCol();
            int row = cell.getRow();
            ImageView cellView = (ImageView) this.gridView.getChildAt( row * this.gridSize + col );
            cellView.setImageResource(cell.getResourceId());
            cellView.setImageAlpha(128);

            int shipsOnCell = this.controller.getCurrentGrid().getShipSet().shipsOnCell(cell);
            if (shipsOnCell == 1){
                cellView.setBackgroundColor(gridAdapter.context.getResources().getColor(R.color.yellow));
            } else {
                cellView.setBackgroundColor(gridAdapter.context.getResources().getColor(R.color.red));
            }
        }
    }

    private void unhighlightCells(GameCell[] cells) {
        for( GameCell cell : cells ) {
            int col = cell.getCol();
            int row = cell.getRow();
            int shipsOnCell = this.controller.getCurrentGrid().getShipSet().shipsOnCell(cell);
            ImageView cellView = (ImageView) this.gridView.getChildAt( row * this.gridSize + col );
            if (shipsOnCell == 0) {
                cellView.setBackgroundColor(Color.WHITE);
                cellView.setImageResource(0);
            } else if (shipsOnCell == 1) {
                cellView.setBackgroundColor(Color.WHITE);
                cellView.setImageResource(cell.getResourceId());
                cellView.setImageAlpha(255);
            } else if (shipsOnCell >= 2) {
                cellView.setBackgroundColor(gridAdapter.context.getResources().getColor(R.color.red));
                cellView.setImageResource(cell.getResourceId());
                cellView.setImageAlpha(255);
            }
        }
    }

    public void onClickButton(View view) {
        if ( this.selectedShip == null )
            return;

        GameCell[] oldCells = this.selectedShip.getShipsCells();

        if (view.getId() == R.id.arrow_left) {
            this.selectedShip.moveShip(Direction.WEST);
        } else if (view.getId() == R.id.arrow_right) {
            this.selectedShip.moveShip(Direction.EAST);
        } else if (view.getId() == R.id.arrow_up) {
            this.selectedShip.moveShip(Direction.NORTH);
        } else if (view.getId() == R.id.arrow_down) {
            this.selectedShip.moveShip(Direction.SOUTH);
        } else if (view.getId() == R.id.rotate_left) {
            this.selectedShip.turnShipLeft();
        } else if (view.getId() == R.id.rotate_right) {
            this.selectedShip.turnShipRight();
        }
        unhighlightCells(oldCells);
        highlightCells(this.selectedShip.getShipsCells());
    }

    public void onClickReady(View view) {
        if (!this.controller.getCurrentGrid().getShipSet().placementLegit()) {
            showInvalidPlacementDialog();
            return;
        }

        if (    this.controller.getMode() == GameMode.VS_AI_EASY ||
                this.controller.getMode() == GameMode.VS_AI_HARD) {

            //Call GameActivity and provide GameController
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("controller", this.controller);
            startActivity(intent);
        } else if (this.controller.getMode() == GameMode.VS_PLAYER) {
            if (this.controller.getCurrentPlayer()) {
                this.fadeOutGridView();

                // Re-switch the current player, such that player one starts
                this.controller.switchPlayers();
                //Call GameActivity and provide GameController
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("controller", this.controller);
                startActivity(intent);

                // Finish the PlaceShipActivity
                this.finish();
            } else {
                this.fadeOutGridView();
                showSwitchPlayerDialog();
            }
        }

    }

    private void fadeOutGridView() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);
        this.gridView.startAnimation(fadeOut);
        this.gridView.setVisibility(View.INVISIBLE);
    }

    private void fadeInGridView() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(500);
        this.gridView.startAnimation(fadeIn);
        this.gridView.setVisibility(View.VISIBLE);
    }

    private void switchPlayers() {
        this.controller.switchPlayers();
        setupGridView(this.controller.getGridSize());
    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    public static class InvalidPlacementDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setView(i.inflate(R.layout.placement_invalid_dialog, null));
            builder.setIcon(R.mipmap.icon_drawer);
            builder.setTitle(getActivity().getString(R.string.placement_tutorial_title));
            builder.setPositiveButton(getActivity().getString(R.string.okay), null);

            return builder.create();
        }
    }

    public static class SwitchPlayerDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setView(i.inflate(R.layout.placement_switch_player_dialog, null));
            builder.setIcon(R.mipmap.icon_drawer);
            if (!((PlaceShipActivity)getActivity()).controller.getCurrentPlayer())
                builder.setTitle(getActivity().getString(R.string.player) + " 2");//player will be switched now
            else
                builder.setTitle(getActivity().getString(R.string.player) + " 1");//player will be switched now

            builder.setPositiveButton(getActivity().getString(R.string.okay), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ((PlaceShipActivity) getActivity()).switchPlayers();
                    ((PlaceShipActivity)getActivity()).fadeInGridView();
                }
            });

            return builder.create();
        }
    }
}