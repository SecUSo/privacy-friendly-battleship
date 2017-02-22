package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.secuso.privacyfriendlybattleships.R;
import org.secuso.privacyfriendlybattleships.game.Direction;
import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameMode;
import org.secuso.privacyfriendlybattleships.game.GameShip;

/*
In this activity, the ships are set on the grid, after the button "Settings" has been pushed.
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
            this.gridView.getChildAt( row * this.gridSize + col ).setBackgroundColor(Color.YELLOW);
        }
    }

    private void unhighlightCells(GameCell[] cells) {
        for( GameCell cell : cells ) {
            int col = cell.getCol();
            int row = cell.getRow();
            int shipsOnCell = this.controller.getCurrentGrid().getShipSet().shipsOnCell(cell);
            if (shipsOnCell == 0) {
                this.gridView.getChildAt( row * this.gridSize + col ).setBackgroundColor(Color.WHITE);
            } else if (shipsOnCell == 1) {
                this.gridView.getChildAt( row * this.gridSize + col ).setBackgroundColor(Color.BLACK);
            } else if (shipsOnCell == 2) {
                this.gridView.getChildAt( row * this.gridSize + col ).setBackgroundColor(Color.RED);
            }
        }
    }

    public void onClickButton(View view) {
        GameCell[] oldCells = this.selectedShip.getShipsCells();
        switch (view.getId()) {
            case R.id.placement_arrow_right:
                this.selectedShip.moveShip(Direction.EAST);
                break;
            case R.id.placement_arrow_left:
                this.selectedShip.moveShip(Direction.WEST);
                break;
            case R.id.placement_arrow_up:
                this.selectedShip.moveShip(Direction.NORTH);
                break;
            case R.id.placement_arrow_down:
                this.selectedShip.moveShip(Direction.SOUTH);
                break;
        }
        unhighlightCells(oldCells);
        highlightCells(this.selectedShip.getShipsCells());
    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
