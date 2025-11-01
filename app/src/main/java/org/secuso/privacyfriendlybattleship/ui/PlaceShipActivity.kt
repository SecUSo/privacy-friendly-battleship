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
 * along with Privacy Friendly Battleship. If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlybattleship.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlybattleship.R
import org.secuso.privacyfriendlybattleship.game.Direction
import org.secuso.privacyfriendlybattleship.game.GameCell
import org.secuso.privacyfriendlybattleship.game.GameController
import org.secuso.privacyfriendlybattleship.game.GameMode
import org.secuso.privacyfriendlybattleship.game.GameShip

/**
 * This class is used to implement the activity for the placement of
 * ships. It provides a simple way to place ships on the grid using
 * buttons and checks if the placement is legitimate before starting the
 * game. It also includes a basic tutorial and notification in case of
 * illegal ship placement.
 *
 * @author Alexander Müller, Ali Kalsen
 */
class PlaceShipActivity : BaseActivity() {
    private var preferences: SharedPreferences? = null
    private lateinit var controller: GameController
    private var gridSize = 0
    private lateinit var layoutProvider: GameActivityLayoutProvider
    private lateinit var rootView: ViewGroup
    private lateinit var gridView: GridView
    private lateinit var readyButton: Button

    private var gridAdapter: GameGridAdapter? = null
    private var mSelectedShip: GameShip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPreferences()
        setContentView(R.layout.activity_place_ship)

        // Get the grid views of the respective XML-files
        gridView = findViewById<GridView>(R.id.game_gridview_big)
        rootView = gridView.rootView as ViewGroup
        readyButton = findViewById<Button>(R.id.placement_ready)

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        val intentIn = intent
        this.controller = intentIn.getParcelableExtra("controller")!!
        this.gridSize = controller.gridSize
        layoutProvider = GameActivityLayoutProvider(this, this.gridSize)

        setupGridView(this.gridSize)

        // Show the tutorial dialog if first time in activity
        if (mSharedPreferences.isFirstPlacementStart) {
            showTutorialDialog()
            mSharedPreferences.isFirstPlacementStart = false
        }
    }

    private fun showTutorialDialog() {
        TutorialDialog(rootView).show(
            supportFragmentManager,
            TutorialDialog::class.java.simpleName)
    }

    private fun showInvalidPlacementDialog() {
        InvalidPlacementDialog(rootView).show(
            supportFragmentManager,
            InvalidPlacementDialog::class.java.simpleName)
    }

    private fun showSwitchPlayerDialog() {
        val switchDialog: DialogFragment = SwitchPlayerDialog(rootView)
        switchDialog.isCancelable = false
        switchDialog.show(supportFragmentManager, SwitchPlayerDialog::class.java.simpleName)
    }

    private fun setupGridView(size: Int) {
        // Set the background color of the grid
        gridView.setBackgroundColor(Color.GRAY)

        // Set the columns of the grid
        gridView.numColumns = size

        // Initialize the grid for player one
        gridAdapter = GameGridAdapter(this, this.layoutProvider, this.controller, true, true)
        gridView.adapter = gridAdapter

        // Define the listener for the big grid view, such that it is possible to click on it. When
        // clicking on that grid, the corresponding cell should be yellow.
        gridView.onItemClickListener =
            OnItemClickListener { adapterView, view, i, l ->
                val column = i % size
                val row = i / size

                if (mSelectedShip != null) {
                    //mark ships cells not highlighted
                    unhighlightCells(mSelectedShip!!.shipsCells)
                }

                val selectedCell = controller.currentGrid.getCell(column, row)
                mSelectedShip = controller.currentGrid.shipSet.findShipContainingCell(selectedCell)

                //highlight ships cells
                if (mSelectedShip != null) highlightCells(mSelectedShip!!.shipsCells)
                gridAdapter!!.notifyDataSetChanged()
            }
    }

    private fun highlightCells(cells: Array<GameCell>) {
        for (cell in cells) {
            val col = cell.col
            val row = cell.row
            val cellView = gridView.getChildAt(row * this.gridSize + col) as ImageView
            cellView.setImageResource(cell.getDrawableResourceId())
            cellView.imageAlpha = 128

            val shipsOnCell = controller.currentGrid.shipSet.shipsOnCell(cell)
            if (shipsOnCell == 1) {
                cellView.setBackgroundColor(ContextCompat.getColor(this, R.color.selection))
            } else {
                cellView.setBackgroundColor(ContextCompat.getColor(this, R.color.shipConflict))
            }
        }
    }

    private fun unhighlightCells(cells: Array<GameCell>) {
        for (cell in cells) {
            val col = cell.col
            val row = cell.row
            val shipsOnCell = controller.currentGrid.shipSet.shipsOnCell(cell)
            val cellView = gridView.getChildAt(row * this.gridSize + col) as ImageView
            if (shipsOnCell == 0) {
                cellView.setBackgroundColor(ContextCompat.getColor(this, R.color.water))
                cellView.setImageResource(0)
            } else if (shipsOnCell == 1) {
                cellView.setBackgroundColor(ContextCompat.getColor(this, R.color.water))
                cellView.setImageResource(cell.getDrawableResourceId())
                cellView.imageAlpha = 255
            } else if (shipsOnCell >= 2) {
                cellView.setBackgroundColor(ContextCompat.getColor(this, R.color.shipConflict))
                cellView.setImageResource(cell.getDrawableResourceId())
                cellView.imageAlpha = 255
            }
        }
    }

    fun onClickButton(view: View) {
        val selectedShip = mSelectedShip ?: return

        val oldCells = selectedShip.shipsCells

        if (view.id == R.id.arrow_left) {
            selectedShip.moveShip(Direction.WEST)
        } else if (view.id == R.id.arrow_right) {
            selectedShip.moveShip(Direction.EAST)
        } else if (view.id == R.id.arrow_up) {
            selectedShip.moveShip(Direction.NORTH)
        } else if (view.id == R.id.arrow_down) {
            selectedShip.moveShip(Direction.SOUTH)
        } else if (view.id == R.id.rotate_left) {
            selectedShip.turnShipLeft()
        } else if (view.id == R.id.rotate_right) {
            selectedShip.turnShipRight()
        }
        unhighlightCells(oldCells)
        highlightCells(selectedShip.shipsCells)
        readyButton.background = if (controller.currentGrid.shipSet.placementLegit()) {
            ResourcesCompat.getDrawable(resources, R.drawable.button_enabled, null)
        } else {
            ResourcesCompat.getDrawable(resources, R.drawable.button_disabled, null)
        }
    }

    fun onClickReady(@Suppress("unused") view: View?) {
        if (!controller.currentGrid.shipSet.placementLegit()) {
            showInvalidPlacementDialog()
            return
        }

        if (controller.mode == GameMode.VS_AI_EASY ||
            controller.mode == GameMode.VS_AI_HARD
        ) {
            //Call GameActivity and provide GameController

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("controller", this.controller)
            startActivity(intent)
        } else if (controller.mode == GameMode.VS_PLAYER) {
            if (controller.secondPlayerIsCurrent) {
                this.fadeOutGridView()

                // Re-switch the current player, such that player one starts
                controller.switchPlayers()
                //Call GameActivity and provide GameController
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("controller", this.controller)
                startActivity(intent)

                // Finish the PlaceShipActivity
                this.finish()
            } else {
                this.fadeOutGridView()
                showSwitchPlayerDialog()
            }
        }
    }

    private fun fadeOutGridView() {
        val fadeOut: Animation = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 300
        gridView.startAnimation(fadeOut)
        gridView.visibility = View.INVISIBLE
    }

    private fun fadeInGridView() {
        val fadeIn: Animation = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = 500
        gridView.startAnimation(fadeIn)
        gridView.visibility = View.VISIBLE
    }

    private fun switchPlayers() {
        controller.switchPlayers()
        setupGridView(controller.gridSize)
    }

    private fun setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    class TutorialDialog(val rootView: ViewGroup) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val i = requireActivity().layoutInflater
            val builder = AlertDialog.Builder(activity)

            builder.setView(i.inflate(R.layout.placement_dialog, rootView, false))
            builder.setIcon(R.mipmap.icon_drawer)
            builder.setTitle(R.string.placement_tutorial_title)
            builder.setPositiveButton(R.string.okay) { _, _ -> }

            return builder.create()
        }
    }

    class InvalidPlacementDialog(val rootView: ViewGroup) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val i = requireActivity().layoutInflater
            val builder = AlertDialog.Builder(activity)

            builder.setView(i.inflate(R.layout.placement_invalid_dialog, rootView, false))
            builder.setIcon(R.mipmap.icon_drawer)
            builder.setTitle(R.string.placement_tutorial_title)
            builder.setPositiveButton(R.string.okay) { _, _ -> }

            return builder.create()
        }
    }

    class SwitchPlayerDialog(val rootView: ViewGroup) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val i = requireActivity().layoutInflater
            val builder = AlertDialog.Builder(activity)

            builder.setView(i.inflate(R.layout.placement_switch_player_dialog, rootView, false))
            builder.setIcon(R.mipmap.icon_drawer)
            if (!(activity as PlaceShipActivity).controller.secondPlayerIsCurrent) {
                builder.setTitle(requireActivity().getString(R.string.player_n, 2)) //player will be switched now
            } else {
                builder.setTitle(requireActivity().getString(R.string.player_n, 1)) //player will be switched now
            }

            builder.setPositiveButton(R.string.okay) { dialog, id ->
                (activity as PlaceShipActivity).switchPlayers()
                (activity as PlaceShipActivity).fadeInGridView()
            }

            return builder.create()
        }
    }
}