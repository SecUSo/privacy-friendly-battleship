/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * ShipSetActivity.java is part of Privacy Friendly Battleship.
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

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import org.secuso.privacyfriendlybattleship.R
import org.secuso.privacyfriendlybattleship.game.GameController
import org.secuso.privacyfriendlybattleship.game.GameMode
import org.secuso.privacyfriendlybattleship.game.GameShipSet
import org.secuso.privacyfriendlybattleship.ui.PlaceShipActivity.TutorialDialog

/**
 * This activity is called from the MainActivity and allows a user to customize the number of
 * ships by clicking on the plus or minus button fot the respective ship size. Created on 07.03.2017.
 *
 * @author Ali Kalsen
 */
class ShipSetActivity : BaseActivity() {
    private lateinit var intentIn: Intent
    private lateinit var controller: GameController
    private lateinit var newShipCount: IntArray
    private lateinit var gameMode: GameMode
    private lateinit var shipSet: GameShipSet
    private var numberGridCells = 0

    private var shipsSize5 = 0
    private var shipsSize4 = 0
    private var shipsSize3 = 0
    private var shipsSize2 = 0

    private var boundShipSet5 = 0
    private var boundShipSet4 = 0
    private var boundShipSet3 = 0
    private var boundShipSet2 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ship_set)

        /*
        Get the parameters from the MainActivity and initialize the
        parameters necessary for this activity.
         */
        this.intentIn = intent
        this.controller = intentIn.getParcelableExtra("controller")!!

        // Check if a previous instance can be recreated after the configuration has changed.
        if (savedInstanceState != null) {
            this.controller = savedInstanceState.getParcelable("controller")!!
        }
        this.gameMode = controller.mode
        this.shipSet = controller.gridFirstPlayer.shipSet
        this.numberGridCells = controller.gridSize * controller.gridSize

        this.newShipCount = IntArray(3)
        this.shipsSize2 = shipSet.numberOfShipsSize2
        this.shipsSize3 = shipSet.numberOfShipsSize3
        this.shipsSize4 = shipSet.numberOfShipsSize4
        this.shipsSize5 = shipSet.numberOfShipsSize5

        this.newShipCount = intArrayOf(shipsSize2, shipsSize3, shipsSize4, shipsSize5)

        updateShipsOfSize2()
        updateShipsOfSize3()
        updateShipsOfSize4()
        updateShipsOfSize5()

        /*
         Set the bounds for the ship sizes. A bound is determined by the number of grid cells
         divided by two in order to avoid too many cells covered by ships. This result will again be
         divided by the size of the respective ship in order to get the bound for the respective
         ship size.
          */
        val bound = numberGridCells * 2 / 5

        this.boundShipSet2 = bound / 2
        this.boundShipSet3 = bound / 3
        this.boundShipSet4 = bound / 4
        this.boundShipSet5 = bound / 5

        // Show the tutorial dialog if first time in activity
        if (mSharedPreferences.isFirstShipSetStart) {
            showTutorialDialog()
            mSharedPreferences.isFirstShipSetStart = false
        }
    }

    private fun showTutorialDialog() {
        TutorialShipSetDialog().show(fragmentManager, TutorialDialog::class.java.simpleName)
    }

    fun addShipOfSize2(view: View?) {
        if (this.shipsSize2 <= boundShipSet2) {
            val temporaryShipCount = intArrayOf(shipsSize2 + 1, shipsSize3, shipsSize4, shipsSize5)
            if (controller.isShipCountLegit(temporaryShipCount)) {
                this.shipsSize2 += 1
                newShipCount[0] = this.shipsSize2
                updateShipsOfSize2()
            }
        }
    }

    fun addShipOfSize3(view: View?) {
        if (this.shipsSize3 <= boundShipSet3) {
            val temporaryShipCount = intArrayOf(shipsSize2, shipsSize3 + 1, shipsSize4, shipsSize5)
            if (controller.isShipCountLegit(temporaryShipCount)) {
                this.shipsSize3 += 1
                newShipCount[1] = this.shipsSize3
                updateShipsOfSize3()
            }
        }
    }

    fun addShipOfSize4(view: View?) {
        if (this.shipsSize4 <= boundShipSet4) {
            val temporaryShipCount = intArrayOf(shipsSize2, shipsSize3, shipsSize4 + 1, shipsSize5)
            if (controller.isShipCountLegit(temporaryShipCount)) {
                this.shipsSize4 += 1
                newShipCount[2] = this.shipsSize4
                updateShipsOfSize4()
            }
        }
    }

    fun addShipOfSize5(view: View?) {
        if (this.shipsSize5 <= boundShipSet5) {
            val temporaryShipCount = intArrayOf(shipsSize2, shipsSize3, shipsSize4, shipsSize5 + 1)
            if (controller.isShipCountLegit(temporaryShipCount)) {
                this.shipsSize5 += 1
                newShipCount[3] = this.shipsSize5
                updateShipsOfSize5()
            }
        }
    }

    fun subtractShipOfSize2(view: View?) {
        if (this.shipsSize2 > 0) {
            this.shipsSize2 -= 1
            newShipCount[0] = this.shipsSize2
            updateShipsOfSize2()
        }
    }

    fun subtractShipOfSize3(view: View?) {
        if (this.shipsSize3 > 0) {
            this.shipsSize3 -= 1
            newShipCount[1] = this.shipsSize3
            updateShipsOfSize3()
        }
    }

    fun subtractShipOfSize4(view: View?) {
        if (this.shipsSize4 > 0) {
            this.shipsSize4 -= 1
            newShipCount[2] = this.shipsSize4
            updateShipsOfSize4()
        }
    }

    fun subtractShipOfSize5(view: View?) {
        if (this.shipsSize5 > 0) {
            this.shipsSize5 -= 1
            newShipCount[3] = this.shipsSize5
            updateShipsOfSize5()
        }
    }

    fun updateShipsOfSize2() {
        val shipSet2 = if (shipsSize2 < 10) "0$shipsSize2" else shipsSize2.toString()
        val ships2 = findViewById<TextView>(R.id.ship_set_size_two_number)
        ships2.text = shipSet2
    }

    fun updateShipsOfSize3() {
        val shipSet3 = if (shipsSize3 < 10) "0$shipsSize3" else shipsSize3.toString()
        val ships3 = findViewById<TextView>(R.id.ship_set_size_three_number)
        ships3.text = shipSet3
    }

    fun updateShipsOfSize4() {
        val shipSet4 = if (shipsSize4 < 10) "0$shipsSize4" else shipsSize4.toString()
        val ships4 = findViewById<TextView>(R.id.ship_set_size_four_number)
        ships4.text = shipSet4
    }

    fun updateShipsOfSize5() {
        val shipSet5 = if (shipsSize5 < 10) "0$shipsSize5" else shipsSize5.toString()
        val ships5 = findViewById<TextView>(R.id.ship_set_size_five_number)
        ships5.text = shipSet5
    }

    fun onClickShipSetReady(view: View?) {
        if (newShipCount[0] == 0 && newShipCount[1] == 0 && newShipCount[2] == 0 && newShipCount[3] == 0) {
            ShipSetAlertDialog().show(fragmentManager, ShipSetAlertDialog::class.java.simpleName)
        } else {
            this.controller = GameController(
                this.gameMode,
                controller.gridSize, newShipCount
            )
            controller.placeAllShips()
            // Go back to PlaceShipActivity
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("controller", this.controller)
            startActivity(intent)
            finish()
        }
    }

    fun onClickPlaceShips(view: View?) {
        if (newShipCount[0] == 0 && newShipCount[1] == 0 && newShipCount[2] == 0 && newShipCount[3] == 0) {
            ShipSetAlertDialog().show(fragmentManager, ShipSetAlertDialog::class.java.simpleName)
        } else {
            this.controller = GameController(
                this.gameMode,
                controller.gridSize, newShipCount
            )
            controller.placeAllShips()
            // Go back to PlaceShipActivity
            val intent = Intent(this, PlaceShipActivity::class.java)
            intent.putExtra("controller", this.controller)
            startActivity(intent)
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        this.controller = GameController(
            this.gameMode,
            controller.gridSize, newShipCount
        )
        savedInstanceState.putParcelable("controller", this.controller)
        super.onSaveInstanceState(savedInstanceState)
    }

    class TutorialShipSetDialog : DialogFragment() {
        override fun onAttach(activity: Activity) {
            super.onAttach(activity)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)

            builder.setIcon(R.mipmap.icon_drawer)
            builder.setTitle(R.string.ship_set_title)
            builder.setMessage(R.string.ship_set_message)
            builder.setPositiveButton(activity.getString(R.string.okay), null)

            return builder.create()
        }
    }

    class ShipSetAlertDialog : DialogFragment() {
        override fun onAttach(activity: Activity) {
            super.onAttach(activity)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)

            builder.setIcon(R.mipmap.icon_drawer)
            builder.setTitle(R.string.ship_set_alert_title)
            builder.setMessage(R.string.ship_set_alert_message)
            builder.setPositiveButton(activity.getString(R.string.okay), null)

            return builder.create()
        }
    }
}
