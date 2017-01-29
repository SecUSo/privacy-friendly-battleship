package org.secuso.privacyfriendlybattleships;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.secuso.privacyfriendlybattleships.R;
import org.secuso.privacyfriendlybattleships.game.GameMode;

/*
In this activity, the ships are set on the grid, after the button "Settings" has been pushed.
 */
public class PlaceShipActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_ship);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize the grid and place the ships
    }


    protected void setupGridView(int size){
        //TODO: Implement this method. Maybe use the same method of the class GameActivity
    }

    protected void setShips(){
        //TODO: Implement this method
    }
}
