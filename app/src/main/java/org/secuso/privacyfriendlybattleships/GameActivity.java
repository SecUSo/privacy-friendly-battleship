package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;

public class GameActivity extends BaseActivity {
    GameController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intentIn = getIntent();
        this.controller = (GameController) intentIn.getParcelableExtra("controller");
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            default:
        }
    }
}
