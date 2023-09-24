/*
 * Copyright 2020 The TensorFlow Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.planestrike

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

/**
 * The main activity to provide interactions with users.
 */
class MainActivity : AppCompatActivity() {

    private var agent: PlaneStrikeAgent? = null

    private val agentBoard: Array<Array<BoardCellStatus>> =
        Array(Constants.BOARD_SIZE) { Array(Constants.BOARD_SIZE) { BoardCellStatus.UNTRIED } }

    private var agentBoardGridView: GridView? = null

    private val agentHiddenBoard =
        Array(Constants.BOARD_SIZE) { Array(Constants.BOARD_SIZE) { HiddenBoardCellStatus.UNOCCUPIED } }

    private var agentHits = 0

    private var agentHitsTextView: TextView? = null

    private val playerBoard: Array<Array<BoardCellStatus>> =
        Array(Constants.BOARD_SIZE) { Array(Constants.BOARD_SIZE) { BoardCellStatus.UNTRIED } }

    private var playerBoardGridView: GridView? = null

    private val playerHiddenBoard =
        Array(Constants.BOARD_SIZE) { Array(Constants.BOARD_SIZE) { HiddenBoardCellStatus.UNOCCUPIED } }

    private var playerHits = 0

    private var playerHitsTextView: TextView? = null
    private var resetButton: Button? = null

    /**
     *
     */
    private fun initBoard(board: Array<Array<BoardCellStatus>>) {

        for (i in 0 until Constants.BOARD_SIZE) {
            Arrays.fill(board[i], 0, Constants.BOARD_SIZE, BoardCellStatus.UNTRIED)
        }
    }

    /**
     *
     */
    private fun initGame() {

        initBoard(playerBoard)
        placePlaneOnHiddenBoard(playerHiddenBoard)
        initBoard(agentBoard)
        placePlaneOnHiddenBoard(agentHiddenBoard)
        agentBoardGridView!!.invalidateViews()
        playerBoardGridView!!.invalidateViews()
        agentHits = 0
        playerHits = 0
        agentHitsTextView!!.text = "Player board:\n0 hits"
        playerHitsTextView!!.text = "Agent board:\n0 hits"
    }

    /**
     *
     */
    private fun initHiddenBoard(board: Array<Array<HiddenBoardCellStatus>>) {

        for (i in 0 until Constants.BOARD_SIZE) {
            Arrays.fill(board[i], 0, Constants.BOARD_SIZE, HiddenBoardCellStatus.UNOCCUPIED)
        }
    }

    /**
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        agentBoardGridView = findViewById<View>(R.id.agent_board_gridview) as GridView
        playerBoardGridView = findViewById<View>(R.id.player_board_gridview) as GridView
        agentHitsTextView = findViewById<View>(R.id.agent_hits_textview) as TextView
        playerHitsTextView = findViewById<View>(R.id.player_hits_textview) as TextView
        initGame()

        try {
            if (Constants.USE_MODEL_FROM_TF) {
                agent = RLAgent(this)
            } else {
                agent = RLAgentFromTFAgents(this)
            }
        } catch (e: IOException) {
            Log.e(Constants.TAG, e.message!!)
            return
        }

        playerBoardGridView!!.adapter =
            BoardCellAdapter(this, playerBoard, playerHiddenBoard, false)

        agentBoardGridView!!.adapter = BoardCellAdapter(this, agentBoard, agentHiddenBoard,true)

        agentBoardGridView!!.onItemClickListener =
            OnItemClickListener { adapterView, view, position, l -> // Player action
                val playerActionX: Int = position / Constants.BOARD_SIZE
                val playerActionY: Int = position % Constants.BOARD_SIZE
                if (agentBoard[playerActionX][playerActionY] === BoardCellStatus.UNTRIED) {
                    if (agentHiddenBoard[playerActionX][playerActionY] === HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
                        agentBoard[playerActionX][playerActionY] = BoardCellStatus.HIT
                        playerHits++
                        playerHitsTextView!!.text = "Agent board:\n$playerHits hits"
                    } else {
                        agentBoard[playerActionX][playerActionY] = BoardCellStatus.MISS
                    }
                }

                // Agent action
                val agentStrikePosition: Int = agent!!.predictNextMove(playerBoard)

                if (agentStrikePosition == -1) {
                    Toast.makeText(
                        this@MainActivity,
                        "Something went wrong with the RL agent! Please restart the app.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnItemClickListener
                }

                val agentStrikePositionX: Int = agentStrikePosition / Constants.BOARD_SIZE
                val agentStrikePositionY: Int = agentStrikePosition % Constants.BOARD_SIZE

                if (playerHiddenBoard[agentStrikePositionX][agentStrikePositionY] === HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
                    // Hit
                    playerBoard[agentStrikePositionX][agentStrikePositionY] = BoardCellStatus.HIT
                    agentHits++
                    agentHitsTextView!!.text = "Player board:\n$agentHits hits"
                } else {
                    // Miss
                    playerBoard[agentStrikePositionX][agentStrikePositionY] = BoardCellStatus.MISS
                }
                if (agentHits == Constants.PLANE_CELL_COUNT || playerHits == Constants.PLANE_CELL_COUNT) {
                    // Game ends
                    val gameEndMessage: String = if (agentHits == Constants.PLANE_CELL_COUNT && playerHits == Constants.PLANE_CELL_COUNT) {
                        "Draw game!"
                    } else if (agentHits == Constants.PLANE_CELL_COUNT) {
                        "Agent wins!"
                    } else {
                        "You win!"
                    }
                    Toast.makeText(this@MainActivity, gameEndMessage, Toast.LENGTH_LONG).show()
                    // Automatically reset game UI after 2 seconds
                    val resetGameTimer = Timer()
                    resetGameTimer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                runOnUiThread { initGame() }
                            }
                        },
                        2000
                    )
                }
                agentBoardGridView!!.invalidateViews()
                playerBoardGridView!!.invalidateViews()
            }

        resetButton = findViewById<View>(R.id.reset_button) as Button
        resetButton!!.setOnClickListener { initGame() }
    }

    /**
     *
     */
    private fun placePlaneOnHiddenBoard(hiddenBoard: Array<Array<HiddenBoardCellStatus>>) {

        initHiddenBoard(hiddenBoard)

        // Place the plane on the board
        // First, decide the plane's orientation
        //   0: heading right
        //   1: heading up
        //   2: heading left
        //   3: heading down
        val rand = Random()
        val planeOrientation = rand.nextInt(4)

        // Next, figure out the location of plane core as the '*' below
        //   | |      |      | |    ---
        //   |-*-    -*-    -*-|     |
        //   | |      |      | |    -*-
        //           ---             |
        val planeCoreX: Int
        val planeCoreY: Int

        when (planeOrientation) {
            0 -> {
                planeCoreX = rand.nextInt(Constants.BOARD_SIZE - 2) + 1
                planeCoreY = rand.nextInt(Constants.BOARD_SIZE - 3) + 2
                // Populate the tail
                hiddenBoard[planeCoreX][planeCoreY - 2] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX - 1][planeCoreY - 2] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX + 1][planeCoreY - 2] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
            }
            1 -> {
                planeCoreX = rand.nextInt(Constants.BOARD_SIZE - 3) + 1
                planeCoreY = rand.nextInt(Constants.BOARD_SIZE - 2) + 1
                // Populate the tail
                hiddenBoard[planeCoreX + 2][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX + 2][planeCoreY + 1] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX + 2][planeCoreY - 1] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
            }
            2 -> {
                planeCoreX = rand.nextInt(Constants.BOARD_SIZE - 2) + 1
                planeCoreY = rand.nextInt(Constants.BOARD_SIZE - 3) + 1
                // Populate the tail
                hiddenBoard[planeCoreX][planeCoreY + 2] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX - 1][planeCoreY + 2] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX + 1][planeCoreY + 2] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
            }
            else -> {
                planeCoreX = rand.nextInt(Constants.BOARD_SIZE - 3) + 2
                planeCoreY = rand.nextInt(Constants.BOARD_SIZE - 2) + 1
                // Populate the tail
                hiddenBoard[planeCoreX - 2][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX - 2][planeCoreY + 1] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX - 2][planeCoreY - 1] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
            }
        }

        // Finally, populate the 'cross' in the plane
        hiddenBoard[planeCoreX][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
        hiddenBoard[planeCoreX + 1][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
        hiddenBoard[planeCoreX - 1][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
        hiddenBoard[planeCoreX][planeCoreY + 1] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
        hiddenBoard[planeCoreX][planeCoreY - 1] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
    }
}
