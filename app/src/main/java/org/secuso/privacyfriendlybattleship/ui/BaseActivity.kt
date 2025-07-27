/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * BaseActivity.java is part of Privacy Friendly Battleship.
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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.TaskStackBuilder
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import org.secuso.privacyfriendlybattleship.R
import org.secuso.privacyfriendlybattleship.util.LogTag
import org.secuso.privacyfriendlybattleship.util.PrefManager

/**
 * Created by Chris on 04.07.2016. Edited by Ali Kalsen on 08.03.2017
 */
open class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // Navigation drawer:
    private var mDrawerLayout: DrawerLayout? = null
    private var mNavigationView: NavigationView? = null

    // Helper
    private var mHandler: Handler? = null
    protected lateinit var mSharedPreferences: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_main);
        mSharedPreferences = PrefManager(this.baseContext)
        mHandler = Handler()

        //ActionBar ab = getSupportActionBar();
        //if (ab != null) {
        //    mActionBar = ab;
        //    ab.setDisplayHomeAsUpEnabled(true);
        //}
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    protected open val navigationDrawerID: Int
        get() = 0

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        return goToNavigationItem(itemId)
    }

    protected fun goToNavigationItem(itemId: Int): Boolean {
        if (itemId == navigationDrawerID) {
            // just close drawer because we are already in this activity
            mDrawerLayout!!.closeDrawer(GravityCompat.START)
            return true
        }

        // delay transition so the drawer can close
        mHandler!!.postDelayed({ callDrawerItem(itemId) }, NAVDRAWER_LAUNCH_DELAY.toLong())

        mDrawerLayout!!.closeDrawer(GravityCompat.START)

        selectNavigationItem(itemId)

        // fade out the active activity
        val mainContent = findViewById<View>(R.id.main_content)
        mainContent?.animate()?.alpha(0f)
            ?.setDuration(MAIN_CONTENT_FADEOUT_DURATION.toLong())
        return true
    }

    // set active navigation item
    private fun selectNavigationItem(itemId: Int) {
        for (i in 0 until mNavigationView!!.menu.size()) {
            val b = itemId == mNavigationView!!.menu.getItem(i).itemId
            mNavigationView!!.menu.getItem(i).setChecked(b)
        }
    }

    /**
     * Enables back navigation for activities that are launched from the NavBar. See
     * `AndroidManifest.xml` to find out the parent activity names for each activity.
     * @param intent
     */
    private fun createBackStack(intent: Intent) {
        val builder = TaskStackBuilder.create(this)
        builder.addNextIntentWithParentStack(intent)
        builder.startActivities()
    }

    private fun callDrawerItem(itemId: Int) {
        val intent: Intent

        when (itemId) {
            R.id.nav_main -> {
                intent = Intent(this, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            R.id.nav_tutorial -> {
                intent = Intent(this, TutorialActivity::class.java)
                intent.setAction(TutorialActivity.ACTION_SHOW_ANYWAYS)
                startActivity(intent)
            }
            R.id.nav_settings -> {
                intent = Intent(this, SettingsActivity::class.java)
                createBackStack(intent)
            }
            R.id.nav_help -> {
                intent = Intent(this, HelpActivity::class.java)
                createBackStack(intent)
            }
            R.id.nav_about -> {
                intent = Intent(this, AboutActivity::class.java)
                createBackStack(intent)
            }
            else -> Log.w(TAG, "Unhandled navigation drawer item ID: $itemId.")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (supportActionBar == null) {
            setSupportActionBar(toolbar)
        }

        mDrawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        mDrawerLayout!!.addDrawerListener(toggle)
        toggle.syncState()

        mNavigationView = findViewById<NavigationView>(R.id.nav_view)
        mNavigationView!!.setNavigationItemSelectedListener(this)

        selectNavigationItem(navigationDrawerID)

        val mainContent = findViewById<View>(R.id.main_content)
        if (mainContent != null) {
            mainContent.alpha = 0f
            mainContent.animate().alpha(1f).setDuration(MAIN_CONTENT_FADEIN_DURATION.toLong())
        }
    }


    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        // delay to launch nav drawer item, to allow close animation to play
        const val NAVDRAWER_LAUNCH_DELAY: Int = 250

        // fade in and fade out durations for the main content when switching between
        // different Activities of the app through the Nav Drawer
        const val MAIN_CONTENT_FADEOUT_DURATION: Int = 150
        const val MAIN_CONTENT_FADEIN_DURATION: Int = 250
    }
}
