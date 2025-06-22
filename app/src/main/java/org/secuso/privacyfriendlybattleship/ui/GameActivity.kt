/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * GameActivity.java is part of Privacy Friendly Battleship.
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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import org.secuso.privacyfriendlybattleship.Constants
import org.secuso.privacyfriendlybattleship.R
import org.secuso.privacyfriendlybattleship.game.GameController
import org.secuso.privacyfriendlybattleship.game.GameMode
import java.util.Timer
import java.util.TimerTask

/**
 * This activity enables a user to play the game depending on the game mode and size of the game
 * board he has chosen in the MainActivity. This activity is called either in the MainActivity by
 * pressing on the QUICK START button or when the PlaceShipActivity has finished.
 *
 * @author Alexander Müller, Ali Kalsen
 */
class GameActivity : BaseActivity() {
    private var handler: Handler? = null
    private var timerUpdate: Timer? = null

    private var playerName: TextView? = null
    private var attempts: TextView? = null
    private var gameMode: GameMode? = null
    private var gridSize = 0
    private var controller: GameController? = null
    private var adapterMainGrid: GameGridAdapter? = null
    private var adapterMiniGrid: GameGridAdapter? = null
    private var gridViewBig: GridView? = null
    private var gridViewSmall: GridView? = null
    private var layoutProvider: GameActivityLayoutProvider? = null
    private lateinit var mainGameLayout: ViewGroup
    private lateinit var fireButton: Button

    private var isCellClicked = false
    private var hasStarted = false
    private var moveMade =
        false // Necessary for the help and the back button in order to control the timer and the configuration changes
    private var isGameFinished = false
    private var isShowAllShipsButtonClicked = false
    private var isSwitchDialogDisplayed = false
    private var positionGridCell = 0 // Save the current position of the grid cell clicked
    private var prevCell: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_game)

        // Since the GameActivity is created, the game has not finished and the "Show all ships" button has not been clicked
        this.isGameFinished = false
        this.isShowAllShipsButtonClicked = false

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        val intentIn = intent
        this.controller = intentIn.getParcelableExtra("controller")
        this.gridSize = controller!!.gridSize
        this.gameMode = controller!!.mode

        // Set up the handler, which will be needed later in the code.
        this.handler = Handler()

        // Create a GameActivityLayoutProvider in order to scale the grids appropriately
        layoutProvider = GameActivityLayoutProvider(this, this.gridSize)

        // Check if the configuration has changed before the grid views are set up
        if (savedInstanceState != null) {
            this.moveMade = savedInstanceState.getBoolean("move made")
            this.hasStarted = savedInstanceState.getBoolean("has started")
            this.isGameFinished = savedInstanceState.getBoolean("game finished")
            this.isSwitchDialogDisplayed = savedInstanceState.getBoolean("switch dialog shown")
        }

        if (this.isGameFinished) {
            /*
            Re-switch the player such that the correct toolbar and grids are shown after the game
            has finished and the configuration has changed. Note that the number of switches has to
            be even in order to get the correct player every time the GameActivity is recreated.
            */
            controller!!.switchPlayers()
        }

        mainGameLayout = findViewById(R.id.game_main_layout)
        fireButton = findViewById(R.id.game_button_fire)
        playerName = findViewById(R.id.player_name)
        attempts = findViewById(R.id.game_attempts)

        findViewById<Button>(R.id.game_button_help).setOnClickListener { buttonView ->
            onClickHelpButton(buttonView)
        }

        fireButton.setText(R.string.game_button_fire)
        fireButton.setOnClickListener { buttonView ->
            onClickFireButton(buttonView)
        }

        // Update the toolbar
        updateToolbar()

        // Set up the grids for the current player and make them invisible until the player is ready.
        setupGridViews()

        // Set up the time
        setUpTimer()

        if (controller!!.mode == GameMode.VS_PLAYER || controller!!.mode == GameMode.CUSTOM) {
            // Check if the configuration has changed
            if (savedInstanceState == null) {
                showSwitchDialog()
                // Show the help dialog on top of the switch dialog in case the app has started for the first time.
                showHelpDialog()
            } else {
                // Do the following steps if the configuration has changed

                // Check if the game has been finished

                if (this.isGameFinished) {
                    gridViewBig!!.isEnabled = false
                    onClickShowMainGridButton(null)
                    onClickFinishButton(null)
                } else {
                    // Check if a cell was attacked
                    if (moveMade) {
                        /*
                        Change the listener and the text of the "Fire" button, such that a move can
                        be finished after the button has been clicked.
                        */
                        gridViewBig!!.isEnabled = false
                        fireButton.setText(R.string.game_button_done)
                        fireButton.setOnClickListener { view ->
                            onClickDoneButton(view)
                        }
                    } else {
                        if (this.isSwitchDialogDisplayed || !this.hasStarted) {
                            gridViewBig!!.alpha = 0.0f
                            gridViewSmall!!.alpha = 0.0f
                        }
                    }
                }
            }
        } else {
            //setup GridViews again after layout is finished to avoid wrong icon rendering
            val vto = mainGameLayout.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mainGameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    setupGridViews()
                }
            })

            if (this.isGameFinished) {
                gridViewBig!!.isEnabled = false
                onClickShowMainGridButton(null)
                onClickFinishButton(null)
            }
            showHelpDialog()
        }
    }

    private val isFirstActivityStart: Boolean
        get() = mSharedPreferences.getBoolean(
            Constants.FIRST_GAME_START,
            true
        )

    private fun setActivityStarted() {
        mSharedPreferences.edit().putBoolean(Constants.FIRST_GAME_START, false).commit()
    }

    /**
     * Shows the help dialog when the app has started for the first time.
     */
    fun showHelpDialog() {
        if (isFirstActivityStart) {
            val helpDialog = HelpDialog()
            helpDialog.isCancelable = false
            helpDialog.show(fragmentManager, HelpDialog::class.java.simpleName)
        }
    }

    fun showSwitchDialog() {
        this.hasStarted = false
        // Make the grids invisible until player one is ready
        gridViewBig!!.alpha = 0.0f
        gridViewSmall!!.alpha = 0.0f

        // Create a bundle for transferring data to the SwitchDialog
        val bundle = Bundle()
        val currentPlayerName =
            if (controller!!.currentPlayer) R.string.game_player_two else R.string.game_player_one
        bundle.putInt("Name", currentPlayerName)

        // Ask if player one is ready
        val newSwitchDialog = SwitchDialog.newInstance(bundle)
        newSwitchDialog.isCancelable = false
        newSwitchDialog.show(fragmentManager, SwitchDialog::class.java.simpleName)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Check if the menu drawer is open
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            // Display a dialog which asks the current player, if he wants to quit the game
            controller!!.stopTimer()
            val goBackDialog = GoBackDialog()
            goBackDialog.isCancelable = false
            goBackDialog.show(fragmentManager, GoBackDialog::class.java.simpleName)
        }
    }

    public override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (this.hasStarted || this.gameMode == GameMode.VS_AI_EASY || this.gameMode == GameMode.VS_AI_HARD) {
            controller!!.startTimer()
            if (this.moveMade || this.isSwitchDialogDisplayed || this.isGameFinished ||
                (controller!!.mode != GameMode.VS_PLAYER && controller!!.opponentAI?.isAIWinner == true)
            ) {
                controller!!.stopTimer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        controller!!.stopTimer()
    }

    /*
    this method saves the auxiliary variables of the GameActivity, such that the game can be
    recreated correctly once the configuration has changed.
     */
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (this.isGameFinished && !this.isShowAllShipsButtonClicked) {
            controller!!.switchPlayers()
        }
        savedInstanceState.putParcelable("controller", this.controller)
        savedInstanceState.putBoolean("move made", this.moveMade)
        savedInstanceState.putBoolean("has started", this.hasStarted)
        savedInstanceState.putBoolean("game finished", this.isGameFinished)
        savedInstanceState.putBoolean("switch dialog shown", this.isSwitchDialogDisplayed)
        super.onSaveInstanceState(savedInstanceState)
    }

    fun onClickHelpButton(view: View?) {
        controller!!.stopTimer()
        // Show a help dialog
        val helpDialog = HelpDialog()
        helpDialog.isCancelable = false
        helpDialog.show(fragmentManager, HelpDialog::class.java.simpleName)
    }

    fun onClickDoneButton(view: View?) {
        // Fade out the grids

        gridViewBig!!.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION.toLong())
        gridViewSmall!!.animate().alpha(0.0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION.toLong())

        this.moveMade = false
        this.isSwitchDialogDisplayed = true

        /*
        Build a handler. Delay the switch of the players and the dialog after the grids have been
        faded out.
        */
        handler!!.postDelayed({ /*
                    Get the name of the next player. Note that the players are switched when the
                    SwitchDialog is executed.
                     */
            val playerName =
                if (controller!!.currentPlayer) R.string.game_player_one else R.string.game_player_two

            // Create a bundle for transferring data to the SwitchDialog
            val bundle = Bundle()
            bundle.putInt("Name", playerName)

            val switchDialog = SwitchDialog.newInstance(bundle)
            switchDialog.isCancelable = false
            switchDialog.show(fragmentManager, SwitchDialog::class.java.simpleName)
        }, MAIN_CONTENT_FADEOUT_DURATION.toLong())

        /*
        Change the listener and the text of the "Done" button, such that the grids fade out
        after the button has been clicked.
        */
        fireButton.setText(R.string.game_button_fire)
        fireButton.setOnClickListener { buttonView ->
            onClickFireButton(buttonView)
        }
    }

    fun onClickFireButton(view: View?) {
        val gridUnderAttack = controller!!.gridUnderAttack()

        // Get the cell, which shall be attacked
        val column = this.positionGridCell % this.gridSize
        val row = this.positionGridCell / this.gridSize
        val attackedCell = gridUnderAttack.getCell(column, row)

        //Do not attack the same cell twice and do not click the fire button without clicking on a cell.
        if (attackedCell.isHit || this.prevCell == null || !isCellClicked) {
            return
        }

        // Attack the cell and update the main grid.
        controller!!.makeMove(controller!!.currentPlayer, column, row)
        this.moveMade = true
        // Denote that the cells are not clicked anymore such that fire button can only be executed if a cell has been clicked
        this.isCellClicked = false
        updateToolbar()
        adapterMainGrid!!.notifyDataSetChanged()

        val ship = gridUnderAttack.shipSet.findShipContainingCell(attackedCell)
        controller!!.stopTimer()
        // Check if the current hit has destroyed a ship
        if (ship != null && ship.isDestroyed) {
            val playerName =
                if (controller!!.currentPlayer) R.string.game_player_two else R.string.game_player_one
            val bundle = Bundle()
            bundle.putInt("Name", playerName)
            bundle.putInt("Size", ship.size)
            /*
            Show a dialog. The dialog will check if the current player has won after the player
            has clicked on the OK button, cf. the respective onCreateDialog method.
            */
            val gameDialog = GameDialog.newInstance(bundle)
            gameDialog.isCancelable = false
            gameDialog.show(fragmentManager, GameDialog::class.java.simpleName)
        } else {
            // Terminate the fire button
            terminateFireButton()
        }
    }

    // Switch the player or make the move for the AI
    private fun terminateFireButton() {
        // If the attacked cell does not contain a ship, then stop the timer and switch the player
        if (this.gameMode == GameMode.VS_AI_EASY || this.gameMode == GameMode.VS_AI_HARD) {
            controller!!.switchPlayers()
            //make move for AI
            controller!!.opponentAI?.makeMove()
            handler!!.postDelayed({
                adapterMiniGrid!!.notifyDataSetChanged()
                if (controller!!.opponentAI?.isAIWinner == true) {
                    timerUpdate!!.cancel()

                    /*
                                Create a dialog. Therefore, instantiate a bundle which transfers the data from the
                                current game to the dialog.
                                */
                    val bundle = Bundle()
                    bundle.putString("Time", controller!!.timeToString(controller!!.time))
                    bundle.putString(
                        "Attempts",
                        controller!!.attemptsToString(controller!!.attemptsPlayerOne)
                    )

                    // Instantiate the lose dialog and show it
                    val loseDialog = LoseDialog.newInstance(bundle)
                    loseDialog.isCancelable = false
                    loseDialog.show(fragmentManager, LoseDialog::class.java.simpleName)
                } else {
                    // Restart the timer for player one
                    controller!!.startTimer()
                }
            }, 250)
            this.moveMade = false
        } else {
            /*
            Change the listener and the text of the "Fire" button, such that the grids fade out
            after the button has been clicked.
            */
            gridViewBig!!.isEnabled = false

            fireButton.setText(R.string.game_button_done)
            fireButton.setOnClickListener { view ->
                onClickDoneButton(view)
            }
        }
    }

    fun onClickFinishButton(view: View?) {
        fireButton.setText(R.string.finish)
        fireButton.setOnClickListener {
            goToMainActivity()
        }
    }

    fun onClickShowMainGridButton(view: View?) {
        // Only switch the players once after the game has finished in order to display the ships on the grid
        if (!this.isGameFinished) {
            this.isGameFinished = true
        }

        /*
        Change the listener and the text of the "HELP" button, such that the ships on the main grid
        are displayed after the button has been clicked.
        */
        val showAllShipsButton = findViewById<Button>(R.id.game_button_help)
        showAllShipsButton.setText(R.string.game_button_show_ships)
        showAllShipsButton.setOnClickListener {
            controller!!.switchPlayers()
            isShowAllShipsButtonClicked = true
            showAllShipsButton.background = resources.getDrawable(R.drawable.button_disabled)
            showAllShipsButton.isEnabled = false
            showShipsOnMainGrid()
        }
    }

    protected fun setupGridViews() {
        // Get the grid views of the respective XML-files

        gridViewBig = findViewById<GridView>(R.id.game_gridview_big)
        gridViewSmall = findViewById<GridView>(R.id.game_gridview_small)

        // Set the background color of the grid
        gridViewBig!!.setBackgroundColor(Color.GRAY)
        gridViewSmall!!.setBackgroundColor(Color.GRAY)

        // Set the columns of the grid
        gridViewBig!!.numColumns = gridSize
        gridViewSmall!!.numColumns = gridSize

        adapterMainGrid = GameGridAdapter(
            this,
            layoutProvider!!, controller!!, true
        )
        adapterMiniGrid = GameGridAdapter(
            this,
            layoutProvider!!, controller!!, false
        )
        gridViewBig!!.adapter = adapterMainGrid
        gridViewSmall!!.adapter = adapterMiniGrid

        // Define the listener for the big grid view, such that it is possible to click on it. When
        // clicking on that grid, the corresponding cell should be yellow.
        gridViewBig!!.onItemClickListener =
            OnItemClickListener { adapterView, view, i, l ->
                if (prevCell != null) {
                    prevCell!!.setBackgroundColor(Color.WHITE)
                }
                positionGridCell = i
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                prevCell = view
                // Display the grid cell, which was clicked.
                adapterMainGrid!!.notifyDataSetChanged()
                isCellClicked = true
            }
    }

    fun updateToolbar() {
        if (this.gameMode == GameMode.VS_PLAYER || this.gameMode == GameMode.CUSTOM) {
            val currentPlayerName =
                if (controller!!.currentPlayer) R.string.game_player_two else R.string.game_player_one
            playerName!!.setText(currentPlayerName)
        } else {
            playerName!!.text = ""
        }

        val attemptsCurrentPlayer =
            if (controller!!.currentPlayer) controller!!.attemptsPlayerTwo else controller!!.attemptsPlayerOne
        attempts!!.text =
            controller!!.attemptsToString(attemptsCurrentPlayer)
    }

    fun fadeInGrids() {
        setupGridViews()
        // Fade in the grids
        gridViewBig!!.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION.toLong())
        gridViewBig!!.isEnabled = true
        gridViewSmall!!.animate().alpha(1.0f).setDuration(MAIN_CONTENT_FADEIN_DURATION.toLong())
        if (!this.hasStarted) {
            this.hasStarted = true
        } else {
            this.isSwitchDialogDisplayed = false
        }
    }

    fun goToMainActivity() {
        // Go back to the (old) MainActivity.

        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        // Exit the GameActivity
        this.finish()
    }

    fun terminate() {
        //check if player has won
        if (controller!!.gridUnderAttack().shipSet.allShipsDestroyed()) {
            timerUpdate!!.cancel()
            gridViewBig!!.isEnabled = false
            /*
            Create a dialog. Therefore, instantiate a bundle which transfers the data from the
            current game to the dialog.
            */
            val nameWinner =
                if (controller!!.currentPlayer) R.string.game_player_two else R.string.game_player_one
            val attemptsWinner = if (controller!!.currentPlayer)
                controller!!.attemptsPlayerTwo
            else
                controller!!.attemptsPlayerOne
            val bundle = Bundle()
            bundle.putInt("Player", nameWinner)
            bundle.putString(
                "Time", controller!!.timeToString(
                    controller!!.time
                )
            )
            bundle.putString("Attempts", controller!!.attemptsToString(attemptsWinner))

            // Instantiate the win dialog and show it
            val winDialog = WinDialog.newInstance(bundle)
            winDialog.isCancelable = false
            winDialog.show(fragmentManager, WinDialog::class.java.simpleName)
        } else {
            terminateFireButton()
        }
    }

    fun setUpTimer() {
        // Setup timer task and timer view. This setup updates the current time of a player every second.
        val timerView = findViewById<TextView>(R.id.timerView)
        timerUpdate = Timer()
        timerUpdate!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread { timerView.text = controller!!.timeToString(controller!!.time) }
            }
        }, 0, 1000)
    }

    fun showShipsOnMainGrid() {
        val newAdapter = GameGridAdapter(
            this,
            layoutProvider!!, controller!!, true, true
        )
        gridViewBig!!.adapter = newAdapter
        gridViewBig!!.isEnabled = false
    }

    class GameDialog : DialogFragment() {
        private var size = 0
        private var playerName = 0

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            this.size = arguments.getInt("Size")
            this.playerName = arguments.getInt("Name")

            // Get the layout for the lose dialog as a view
            val gameDialogView = activity.layoutInflater.inflate(R.layout.game_dialog, null)

            // Set the size of the ship destroyed
            val textShipSize =
                gameDialogView.findViewById<TextView>(R.id.game_dialog_ship_size)
            textShipSize.text = size.toString()

            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(this.playerName)
                .setIcon(R.mipmap.icon_drawer)
                .setView(gameDialogView)
                .setPositiveButton(
                    R.string.okay
                ) { dialogInterface, i -> // Check if the game has a winner and terminate it in that case.
                    (activity as GameActivity).terminate()
                }
            // Create the AlertDialog object and return it
            return builder.create()
        }

        companion object {
            fun newInstance(bundle: Bundle?): GameDialog {
                val gameDialog = GameDialog()
                gameDialog.arguments = bundle
                return gameDialog
            }
        }
    }

    class SwitchDialog : DialogFragment() {
        private var playerName = 0

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            this.playerName = arguments.getInt("Name")

            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(this.playerName)
                .setIcon(R.mipmap.icon_drawer)
                .setMessage(R.string.game_dialog_next_player)
                .setPositiveButton(
                    R.string.okay
                ) { dialogInterface, i -> // Fade in the grids after the next player has clicked on the button
                    if ((activity as GameActivity).hasStarted) {
                        (activity as GameActivity).controller!!.switchPlayers()
                    }

                    // Update the toolbar
                    (activity as GameActivity).updateToolbar()
                    (activity as GameActivity).fadeInGrids()
                    (activity as GameActivity).controller!!.startTimer()
                }
            // Create the AlertDialog object and return it
            return builder.create()
        }

        companion object {
            fun newInstance(bundle: Bundle?): SwitchDialog {
                val switchDialog = SwitchDialog()
                switchDialog.arguments = bundle
                return switchDialog
            }
        }
    }

    class LoseDialog : DialogFragment() {
        private var time: String? = null
        private var attempts: String? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            this.time = arguments.getString("Time")
            this.attempts = arguments.getString("Attempts")

            // Get the layout for the lose dialog as a view
            val loseDialogView = activity.layoutInflater.inflate(R.layout.lose_dialog, null)

            // Set the current time and the number of attempts.
            val textTime = loseDialogView.findViewById<TextView>(R.id.lose_dialog_time)
            textTime.text = time

            val textAttempts =
                loseDialogView.findViewById<TextView>(R.id.lose_dialog_attempts)
            textAttempts.text = attempts

            // Build the dialog
            val builder = AlertDialog.Builder(activity)
            builder.setView(loseDialogView)
                .setTitle(R.string.game_dialog_loss)
                .setIcon(R.mipmap.icon_drawer)
                .setPositiveButton(
                    R.string.okay
                ) { dialogInterface, i -> (activity as GameActivity).goToMainActivity() }
                .setNegativeButton(
                    R.string.game_dialog_show_game_board
                ) { dialogInterface, i ->
                    (activity as GameActivity).onClickShowMainGridButton(view)
                    (activity as GameActivity).onClickFinishButton(view)
                }

            return builder.create()
        }

        companion object {
            fun newInstance(bundle: Bundle?): LoseDialog {
                val loseDialog = LoseDialog()
                loseDialog.arguments = bundle
                return loseDialog
            }
        }
    }

    class WinDialog : DialogFragment() {
        private var time: String? = null
        private var attempts: String? = null
        private var playerName = 0

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            this.time = arguments.getString("Time")
            this.attempts = arguments.getString("Attempts")
            this.playerName = arguments.getInt("Player")

            // Get the layout for the lose dialog as a view
            val winDialogView = activity.layoutInflater.inflate(R.layout.win_dialog, null)

            // Set the current time, the name of the player and the number of attempts.
            val textTime = winDialogView.findViewById<TextView>(R.id.win_dialog_time)
            textTime.text = time

            val textAttempts =
                winDialogView.findViewById<TextView>(R.id.win_dialog_attempts)
            textAttempts.text = attempts

            val textPlayerName =
                winDialogView.findViewById<TextView>(R.id.win_dialog_player_name)
            textPlayerName.setText(this.playerName)

            // Build the dialog
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.game_dialog_win)
                .setIcon(R.mipmap.icon_drawer)
                .setView(winDialogView)
                .setPositiveButton(
                    R.string.okay
                ) { dialogInterface, i -> (activity as GameActivity).goToMainActivity() }
                .setNegativeButton(
                    R.string.game_dialog_show_game_board
                ) { dialogInterface, i ->
                    (activity as GameActivity).onClickShowMainGridButton(view)
                    (activity as GameActivity).onClickFinishButton(view)
                }

            return builder.create()
        }

        companion object {
            fun newInstance(bundle: Bundle?): WinDialog {
                val winDialog = WinDialog()
                winDialog.arguments = bundle
                return winDialog
            }
        }
    }

    class GoBackDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.game_dialog_quit)
                .setIcon(R.mipmap.icon_drawer)
                .setPositiveButton(
                    R.string.yes
                ) { dialogInterface, i -> (activity as GameActivity).goToMainActivity() }
                .setNegativeButton(
                    R.string.no
                ) { dialogInterface, i ->
                    if (!(activity as GameActivity).moveMade) {
                        // Resume the timer
                        (activity as GameActivity).controller!!.startTimer()
                    }
                }

            return builder.create()
        }
    }

    class HelpDialog : DialogFragment() {
        override fun onAttach(activity: Activity) {
            super.onAttach(activity)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val i = activity.layoutInflater
            val builder = AlertDialog.Builder(activity)

            builder.setView(i.inflate(R.layout.help_dialog, null))
            builder.setTitle(activity.getString(R.string.help_dialog_title))
            builder.setIcon(R.mipmap.icon_drawer)

            builder.setPositiveButton(
                activity.getString(R.string.okay)
            ) { dialogInterface, i ->
                if (!(activity as GameActivity).isFirstActivityStart && !(activity as GameActivity).moveMade) {
                    (activity as GameActivity).controller!!.startTimer()
                } else {
                    (activity as GameActivity).setActivityStarted()
                }
            }
            return builder.create()
        }
    }
}