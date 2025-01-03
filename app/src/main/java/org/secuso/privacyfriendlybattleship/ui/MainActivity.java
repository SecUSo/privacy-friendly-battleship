/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * MainActivity.java is part of Privacy Friendly Battleship.
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

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlybattleship.Constants;
import org.secuso.privacyfriendlybattleship.R;
import org.secuso.privacyfriendlybattleship.game.GameController;
import org.secuso.privacyfriendlybattleship.game.GameGrid;
import org.secuso.privacyfriendlybattleship.game.GameMode;

/**
 * This activity implements the main menu of the app. Here the player can
 * choose a game mode, the grid size and if he wants to set the number of
 * ships and their placement manually.
 *
 * @author Alexander Müller, Ali Kalsen
 */

public class MainActivity extends BaseActivity {

    private ViewPager viewPagerMode = null;//ViewPager for selection of game mode
    private ViewPager viewPagerSize = null;//ViewPager for selection of grid size

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the main page
        setContentView(R.layout.activity_main);
        setupViewPagerMode();
        setupViewPagerSize();
    }

    private boolean isFirstAppStart() {
        return mSharedPreferences.getBoolean(Constants.FIRST_APP_START, true);
    }

    private void setAppStarted() {
        mSharedPreferences.edit().putBoolean(Constants.FIRST_APP_START, false).commit();
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_main;
    }

    public class SectionsPagerModeAdapter extends FragmentPagerAdapter {

        public SectionsPagerModeAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PageFragment (defined as a static inner class below).
            return GameModeFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    public class SectionsPagerSizeAdapter extends FragmentPagerAdapter {

        public SectionsPagerSizeAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PageFragment (defined as a static inner class below).
            return GameSizeFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }

    public static class GameModeFragment extends Fragment {

        private static final String ARG_SECTION_MODE_NUMBER = "section_mode_number";

        // Constructor
        public GameModeFragment() {}

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static GameModeFragment newInstance(int sectionNumber){
            GameModeFragment fragment = new GameModeFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_MODE_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_mode_main, container, false);

            // Generate the image for the gameMode
            GameMode gameMode = GameMode.getValidTypes().get(getArguments().getInt(ARG_SECTION_MODE_NUMBER));
            ImageView imageView = (ImageView) rootView.findViewById(R.id.gameModeImage);
            imageView.setImageResource(gameMode.getImageResID());

            // Generate the text for the gameMode
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(gameMode.getStringResID()));

            return rootView;
        }

    }

    public static class GameSizeFragment extends Fragment {

        public static final String ARG_SECTION_SIZE_NUMBER = "section_size_number";

        // Constructor
        public GameSizeFragment(){}

        public static GameSizeFragment newInstance(int sectionNumber){
            GameSizeFragment sizeFragment = new GameSizeFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_SIZE_NUMBER, sectionNumber);
            sizeFragment.setArguments(args);
            return sizeFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_size_main, container, false);

            // Get the gridSize
            Integer gridSize = GameGrid.getValidSizes().get(getArguments().getInt(ARG_SECTION_SIZE_NUMBER));

            // Generate the text for the gridSize, which is either 5x5 or 10x10
            TextView textView = (TextView) rootView.findViewById(R.id.select_size);
            textView.setText(gridSize.toString() + "x" + gridSize.toString());

            return rootView;
        }

    }


    // Setup the ViewPager for the Game mode
    public void setupViewPagerMode(){
        final ImageView arrowLeft = (ImageView) findViewById(R.id.mode_arrow_left);
        final ImageView arrowRight = (ImageView) findViewById(R.id.mode_arrow_right);
        arrowLeft.setVisibility(View.INVISIBLE);
        arrowRight.setVisibility(View.VISIBLE);

        final SectionsPagerModeAdapter sectionPagerModeAdapter = new SectionsPagerModeAdapter (getSupportFragmentManager());
        viewPagerMode = (ViewPager) findViewById(R.id.modeScroller);
        viewPagerMode.setAdapter(sectionPagerModeAdapter);
        viewPagerMode.setCurrentItem(0);

        viewPagerMode.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // not used
            }
            @Override
            public void onPageSelected(int position) {
                arrowLeft.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
                arrowRight.setVisibility(position == 2 ? View.INVISIBLE : View.VISIBLE);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                // not used
            }
        });
    }

    // Setup the ViewPager for the Game size
    public void setupViewPagerSize(){
        final ImageView arrowLeft = (ImageView) findViewById(R.id.size_arrow_left);
        final ImageView arrowRight = (ImageView) findViewById(R.id.size_arrow_right);
        arrowLeft.setVisibility(View.INVISIBLE);
        arrowRight.setVisibility(View.VISIBLE);

        final SectionsPagerSizeAdapter sectionPagerSizeAdapter = new SectionsPagerSizeAdapter (getSupportFragmentManager());
        viewPagerSize = (ViewPager) findViewById(R.id.sizeScroller);
        viewPagerSize.setAdapter(sectionPagerSizeAdapter);
        viewPagerSize.setCurrentItem(0);

        viewPagerSize.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // not used
            }
            @Override
            public void onPageSelected(int position) {
                arrowLeft.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
                arrowRight.setVisibility(position == 1 ? View.INVISIBLE : View.VISIBLE);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                // not used
            }
        });
    }


    public void onClick(View view) {

        int sizeIndex;
        int gridSize;
        int modeIndex;
        Intent intent;
        GameMode gameMode;
        GameController game;

        if (view.getId() == R.id.mode_arrow_left) {
            viewPagerMode.arrowScroll(View.FOCUS_LEFT);
        } else if (view.getId() == R.id.mode_arrow_right) {
            viewPagerMode.arrowScroll(View.FOCUS_RIGHT);
        } else if (view.getId() == R.id.size_arrow_left) {
            viewPagerSize.arrowScroll(View.FOCUS_LEFT);
        } else if (view.getId() == R.id.size_arrow_right) {
            viewPagerSize.arrowScroll(View.FOCUS_RIGHT);
        } else if (view.getId() == R.id.quick_start_button) {
            // Get the selected game mode and the grid size
            modeIndex = viewPagerMode.getCurrentItem();
            gameMode = GameMode.getValidTypes().get(modeIndex);
            sizeIndex = viewPagerSize.getCurrentItem();
            gridSize = GameGrid.getValidSizes().get(sizeIndex);

            game = new GameController(gridSize, gameMode);
            game.placeAllShips();//place all ships randomly for both players

            // send game information to GameActivity
            intent = new Intent(this, GameActivity.class);
            intent.putExtra("controller", game);
            startActivity(intent);
        } else if (view.getId() == R.id.action_settings) {
            // Get the selected game mode and the grid size
            modeIndex = viewPagerMode.getCurrentItem();
            gameMode = GameMode.getValidTypes().get(modeIndex);
            sizeIndex = viewPagerSize.getCurrentItem();
            gridSize = GameGrid.getValidSizes().get(sizeIndex);

            game = new GameController(gridSize, gameMode);//place all ships randomly for both players

            // send game information to ShipSetActivity
            intent = new Intent(this, ShipSetActivity.class);
            intent.putExtra("controller", game);
            startActivity(intent);
        }
    }
}
