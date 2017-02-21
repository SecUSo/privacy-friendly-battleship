package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.secuso.privacyfriendlybattleships.R;
import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameMode;
import org.secuso.privacyfriendlybattleships.game.GameShip;

/*
In this activity, the ships are set on the grid, after the button "Settings" has been pushed.
 */
public class PlaceShipActivity extends BaseActivity {

    private GameController controller;
    private int gridSize;
    private GameActivityLayoutProvider layoutProvider;
    private GridView gridView;
    private GameGridAdapter gridAdapter;
    private GameShip selectedShip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_ship);

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");
        this.gridSize = controller.getGridSize();

        layoutProvider = new GameActivityLayoutProvider(this, this.gridSize);

        setupGridView(this.gridSize);
        // Initialize the grid and place the ships
    }


    protected void setupGridView(int size){
        // Get the grid views of the respective XML-files
        gridView = (GridView) findViewById(R.id.placement_gridview);

        // Set the background color of the grid
        gridView.setBackgroundColor(Color.GRAY);

        // Set the columns of the grid
        gridView.setNumColumns(this.gridSize);

        // Set the layout of the grids
        final ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) gridView.getLayoutParams();

        marginLayoutParams.setMargins(
                layoutProvider.getMarginLeft(),
                layoutProvider.getMargin(),
                layoutProvider.getMarginRight(),
                layoutProvider.getMargin());

        gridView.setLayoutParams(marginLayoutParams);
        gridView.setHorizontalSpacing(1);
        gridView.setVerticalSpacing(1);

        // Initialize the grid for player one
        gridAdapter = new GameGridAdapter(this, this.layoutProvider, this.controller, true);
        gridView.setAdapter(gridAdapter);

        // Define the listener for the big grid view, such that it is possible to click on it. When
        // clicking on that grid, the corresponding cell should be yellow.
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /*

                if(prevCell != null){
                    prevCell.setBackgroundColor(Color.WHITE);
                }
                positionGridCell = i;
                view.setBackgroundColor(Color.YELLOW);
                prevCell = view;
                // Display the grid cell, which was clicked.
                adapterMainGrid.notifyDataSetChanged();
                */
            }
        });

    }

    public void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.placement_arrow_right:
                //TODO: move ship
                break;
        }
    }
}
