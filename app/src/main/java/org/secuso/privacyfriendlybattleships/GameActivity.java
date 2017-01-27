package org.secuso.privacyfriendlybattleships;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameMode;

public class GameActivity extends BaseActivity {

    private ViewPager mViewPager;
    private ImageView mArrowLeft;
    private ImageView mArrowRight;

    private GameGridAdapter adapterPlayerOne;
    private GameGridAdapter adapterPlayerTwo;
    private GameMode gameMode;
    private int gridSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);

        initGame();
        setupGridView();

    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game;
    }

    public void onClick(View view) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PageFragment (defined as a static inner class below).
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    /*
    Initialize the game settings
     */
    protected void initGame() {

        GameController game;

        // Initialize the game settings
        Bundle intentGame = getIntent().getExtras();
        gameMode = (GameMode) intentGame.get(Constants.GAME_MODE);
        gridSize = (int) intentGame.get(Constants.GRID_SIZE);
        boolean quickStart = (boolean) intentGame.get(Constants.QUICK_START);
        boolean placeShips = (boolean) intentGame.get(Constants.PLACE_SHIPS);

        // Initialize the game
        if(quickStart && !placeShips){
            // Quick start button in the MainActivity is pushed. Set the ships randomly
            game = new GameController(gameMode, gridSize);
            game.setShipsRandomly(game.getGridFirstPlayer(), game.getShipSetFirstPlayer(), game.getShipCount());
        }
        else if(!quickStart && placeShips){
            // Initialize the game and set the ships according to the PlaceShipsActivity
            // TODO: Implement this case
        }
        else{
            // NOTE: This case should never be reached
            // Show an error dialog
            throw new Error("You selected the wrong settings!");
        }
    }

    protected void setupGridView() {

        // Get the grid view of the respective XML-file
        GridView gridView = (GridView) findViewById(R.id.game_gridview_big);
        gridView.setBackgroundColor(0);

        // Set the margins and the size of a grid
        gridView.setNumColumns(gridSize);

        gridView.setAdapter(new GameGridAdapter());

    }


    public static class GameGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }


    public static class PageFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PageFragment newInstance(int sectionNumber) {
            PageFragment fragment = new PageFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PageFragment() {

        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int id = 0;
            if(getArguments() != null) {
                id = getArguments().getInt(ARG_SECTION_NUMBER);
            }

            View rootView = inflater.inflate(R.layout.fragment_mode_main, container, false);

            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Mode: "+String.valueOf(id));
            return rootView;
        }
    }
}
