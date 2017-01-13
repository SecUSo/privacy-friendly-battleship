package org.secuso.privacyfriendlybattleships;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends BaseActivity {

    private SharedPreferences preferences = null;
    private ViewPager viewPagerMode = null;//ViewPager for selection of game mode
    private ViewPager viewPagerSize = null;//ViewPager for selection of grid size

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPreferences();

        // Show the welcome dialog only once after the app has been started
        if (isFirstAppStart()) {
            showWelcomeDialog();
            setAppStarted();
        }

        // Initialize the main page
        setContentView(R.layout.activity_main);
        setupViewPagerMode();
        setupViewPagerSize();
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_example;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PageFragment (defined as a static inner class below).
            return GameActivity.PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }



    public void setupViewPagerMode(){
        final ImageView arrowLeft = (ImageView) findViewById(R.id.mode_arrow_left);
        final ImageView arrowRight = (ImageView) findViewById(R.id.mode_arrow_right);
        arrowLeft.setVisibility(View.INVISIBLE);
        arrowRight.setVisibility(View.VISIBLE);

        final SectionsPagerAdapter sectionPagerAdapter = new SectionsPagerAdapter (getSupportFragmentManager());
        viewPagerMode = (ViewPager) findViewById(R.id.modeScroller);
        viewPagerMode.setAdapter(sectionPagerAdapter);
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

    public void setupViewPagerSize(){
        final ImageView arrowLeft = (ImageView) findViewById(R.id.size_arrow_left);
        final ImageView arrowRight = (ImageView) findViewById(R.id.size_arrow_right);
        arrowLeft.setVisibility(View.INVISIBLE);
        arrowRight.setVisibility(View.VISIBLE);

        final SectionsPagerAdapter sectionPagerAdapter = new SectionsPagerAdapter (getSupportFragmentManager());
        viewPagerMode = (ViewPager) findViewById(R.id.sizeScroller);
        viewPagerMode.setAdapter(sectionPagerAdapter);
        viewPagerMode.setCurrentItem(0);

        viewPagerMode.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
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

    //called at first app start
    private void showWelcomeDialog() {
        new WelcomeDialog().show(getFragmentManager(), WelcomeDialog.class.getSimpleName());
    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
    
    private void setAppStarted() {
        preferences.edit().putBoolean(Constants.FIRST_APP_START, false).commit();
    }

    private boolean isFirstAppStart() {
        return preferences.getBoolean(Constants.FIRST_APP_START, true);
    }

    public static class WelcomeDialog extends DialogFragment {


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            /*
            builder.setView(i.inflate(R.layout.welcome_dialog, null));
            builder.setIcon(R.mipmap.icon);
            builder.setTitle(getActivity().getString(R.string.welcome_title));
            builder.setPositiveButton(getActivity().getString(R.string.button_ok), null);
            builder.setNegativeButton(getActivity().getString(R.string.button_help), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getActivity(), HelpActivity.class);
                    intent.putExtra(HelpActivity.EXTRA_SHOW_FRAGMENT, HelpActivity.HelpFragment.class.getName());
                    intent.putExtra(HelpActivity.EXTRA_NO_HEADERS, true);
                    startActivity(intent);
                }
            });
            */
            return builder.create();
        }
    }


    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.mode_arrow_left:
                viewPagerMode.arrowScroll(View.FOCUS_LEFT);
                break;
            case R.id.mode_arrow_right:
                viewPagerMode.arrowScroll(View.FOCUS_RIGHT);
                break;
            case R.id.size_arrow_left:
                viewPagerSize.arrowScroll(View.FOCUS_LEFT);
                break;
            case R.id.size_arrow_right:
                viewPagerSize.arrowScroll(View.FOCUS_RIGHT);
                break;

            default:
                break;
        }
    }
}
