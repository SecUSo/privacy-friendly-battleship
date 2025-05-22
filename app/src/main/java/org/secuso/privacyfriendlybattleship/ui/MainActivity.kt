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
 * along with Privacy Friendly Battleship. If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlybattleship.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import org.secuso.privacyfriendlybattleship.Constants
import org.secuso.privacyfriendlybattleship.R
import org.secuso.privacyfriendlybattleship.game.GameController
import org.secuso.privacyfriendlybattleship.game.GameGrid
import org.secuso.privacyfriendlybattleship.game.GameMode

/**
 * This activity implements the main menu of the app. Here the player can
 * choose a game mode, the grid size and if he wants to set the number of
 * ships and their placement manually.
 *
 * @author Alexander Müller, Ali Kalsen
 */
class MainActivity : BaseActivity() {
    private var viewPagerMode: ViewPager? = null //ViewPager for selection of game mode
    private var viewPagerSize: ViewPager? = null //ViewPager for selection of grid size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the main page
        setContentView(R.layout.activity_main)
        setupViewPagerMode()
        setupViewPagerSize()
    }

    private val isFirstAppStart: Boolean
        get() = mSharedPreferences.getBoolean(
            Constants.FIRST_APP_START,
            true
        )

    private fun setAppStarted() {
        mSharedPreferences.edit().putBoolean(Constants.FIRST_APP_START, false).commit()
    }

    override val navigationDrawerID: Int
        get() = R.id.nav_main

    inner class SectionsPagerModeAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PageFragment (defined as a static inner class below).
            return GameModeFragment.newInstance(position)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    inner class SectionsPagerSizeAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PageFragment (defined as a static inner class below).
            return GameSizeFragment.newInstance(position)
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }
    }

    class GameModeFragment  // Constructor
        : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_mode_main, container, false)

            // Generate the image for the gameMode
            val gameMode = GameMode.getValidTypes()[arguments!!.getInt(ARG_SECTION_MODE_NUMBER)]
            val imageView = rootView.findViewById<ImageView>(R.id.gameModeImage)
            imageView.setImageResource(gameMode.imageResID)

            // Generate the text for the gameMode
            val textView = rootView.findViewById<TextView>(R.id.section_label)
            textView.text = getString(gameMode.stringResID)

            return rootView
        }

        companion object {
            private const val ARG_SECTION_MODE_NUMBER = "section_mode_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): GameModeFragment {
                val fragment = GameModeFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_MODE_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    class GameSizeFragment  // Constructor
        : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_size_main, container, false)

            // Get the gridSize
            val gridSize = GameGrid.getValidSizes()[arguments!!.getInt(ARG_SECTION_SIZE_NUMBER)]

            // Generate the text for the gridSize, which is either 5x5 or 10x10
            val textView = rootView.findViewById<TextView>(R.id.select_size)
            textView.text = gridSize.toString() + "x" + gridSize.toString()

            return rootView
        }

        companion object {
            const val ARG_SECTION_SIZE_NUMBER: String = "section_size_number"

            fun newInstance(sectionNumber: Int): GameSizeFragment {
                val sizeFragment = GameSizeFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_SIZE_NUMBER, sectionNumber)
                sizeFragment.arguments = args
                return sizeFragment
            }
        }
    }


    // Setup the ViewPager for the Game mode
    fun setupViewPagerMode() {
        val arrowLeft = findViewById<ImageView>(R.id.mode_arrow_left)
        val arrowRight = findViewById<ImageView>(R.id.mode_arrow_right)
        arrowLeft.visibility = View.INVISIBLE
        arrowRight.visibility = View.VISIBLE

        val sectionPagerModeAdapter = SectionsPagerModeAdapter(
            supportFragmentManager
        )
        viewPagerMode = findViewById<ViewPager>(R.id.modeScroller)
        viewPagerMode!!.adapter = sectionPagerModeAdapter
        viewPagerMode!!.currentItem = 0

        viewPagerMode!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // not used
            }

            override fun onPageSelected(position: Int) {
                arrowLeft.visibility =
                    if (position == 0) View.INVISIBLE else View.VISIBLE
                arrowRight.visibility =
                    if (position == 2) View.INVISIBLE else View.VISIBLE
            }

            override fun onPageScrollStateChanged(state: Int) {
                // not used
            }
        })
    }

    // Setup the ViewPager for the Game size
    fun setupViewPagerSize() {
        val arrowLeft = findViewById<ImageView>(R.id.size_arrow_left)
        val arrowRight = findViewById<ImageView>(R.id.size_arrow_right)
        arrowLeft.visibility = View.INVISIBLE
        arrowRight.visibility = View.VISIBLE

        val sectionPagerSizeAdapter = SectionsPagerSizeAdapter(
            supportFragmentManager
        )
        viewPagerSize = findViewById<ViewPager>(R.id.sizeScroller)
        viewPagerSize!!.adapter = sectionPagerSizeAdapter
        viewPagerSize!!.currentItem = 0

        viewPagerSize!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // not used
            }

            override fun onPageSelected(position: Int) {
                arrowLeft.visibility =
                    if (position == 0) View.INVISIBLE else View.VISIBLE
                arrowRight.visibility =
                    if (position == 1) View.INVISIBLE else View.VISIBLE
            }

            override fun onPageScrollStateChanged(state: Int) {
                // not used
            }
        })
    }


    fun onClick(view: View) {
        val sizeIndex: Int
        val gridSize: Int
        val modeIndex: Int
        val intent: Intent
        val gameMode: GameMode
        val game: GameController

        if (view.id == R.id.mode_arrow_left) {
            viewPagerMode!!.arrowScroll(View.FOCUS_LEFT)
        } else if (view.id == R.id.mode_arrow_right) {
            viewPagerMode!!.arrowScroll(View.FOCUS_RIGHT)
        } else if (view.id == R.id.size_arrow_left) {
            viewPagerSize!!.arrowScroll(View.FOCUS_LEFT)
        } else if (view.id == R.id.size_arrow_right) {
            viewPagerSize!!.arrowScroll(View.FOCUS_RIGHT)
        } else if (view.id == R.id.quick_start_button) {
            // Get the selected game mode and the grid size
            modeIndex = viewPagerMode!!.currentItem
            gameMode = GameMode.getValidTypes()[modeIndex]
            sizeIndex = viewPagerSize!!.currentItem
            gridSize = GameGrid.getValidSizes()[sizeIndex]

            game = GameController(gridSize, gameMode)
            game.placeAllShips() //place all ships randomly for both players

            // send game information to GameActivity
            intent = Intent(this, GameActivity::class.java)
            intent.putExtra("controller", game)
            startActivity(intent)
        } else if (view.id == R.id.action_settings) {
            // Get the selected game mode and the grid size
            modeIndex = viewPagerMode!!.currentItem
            gameMode = GameMode.getValidTypes()[modeIndex]
            sizeIndex = viewPagerSize!!.currentItem
            gridSize = GameGrid.getValidSizes()[sizeIndex]

            game = GameController(gridSize, gameMode) //place all ships randomly for both players

            // send game information to ShipSetActivity
            intent = Intent(this, ShipSetActivity::class.java)
            intent.putExtra("controller", game)
            startActivity(intent)
        }
    }
}
