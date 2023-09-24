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

import android.app.Activity
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The class that implements a policy gradient agent to play the game, assuming model is trained
 * using TensorFlow or JAX.
 */
class RLAgent(activity: Activity) : PlaneStrikeAgent(activity) {

    private var boardData: ByteBuffer

    private val outputProbArrays = Array(1) {
        FloatArray(Constants.BOARD_SIZE * Constants.BOARD_SIZE)
    }

    init {
        boardData = ByteBuffer.allocateDirect(Constants.BOARD_SIZE * Constants.BOARD_SIZE * 4)
        boardData.order(ByteOrder.nativeOrder())
    }

    /**
     * Predict the next move based on current board state.
     */
    override fun predictNextMove(board: Array<Array<BoardCellStatus>>): Int {

        if (tflite == null) {
            Log.e(Constants.TAG, "Game agent failed to initialize. Please restart the app.")
            return -1
        } else {
            prepareModelInput(board)
            runInference()
        }

        // Post-processing (non-repeat argmax)
        val probArray = outputProbArrays[0] // batch size is 1 so we use [0] here
        var agentStrikePosition = -1
        var maxProb = 0f

        for (i in probArray.indices) {
            val x: Int = i / Constants.BOARD_SIZE
            val y: Int = i % Constants.BOARD_SIZE

            if (board[x][y] === BoardCellStatus.UNTRIED && probArray[i] > maxProb) {
                agentStrikePosition = i
                maxProb = probArray[i]
            }
        }

        return agentStrikePosition
    }

    override fun prepareModelInput(board: Array<Array<BoardCellStatus>>) {

        if (board == null) {
            return
        }

        var boardCellStatusValue = 0f

        for (i in 0 until Constants.BOARD_SIZE) {
            for (j in 0 until Constants.BOARD_SIZE) {
                boardCellStatusValue = when (board[i][j]) {
                    BoardCellStatus.HIT -> Constants.CELL_STATUS_VALUE_HIT
                    BoardCellStatus.MISS -> Constants.CELL_STATUS_VALUE_MISS
                    else -> Constants.CELL_STATUS_VALUE_UNTRIED
                }
                boardData.putFloat(boardCellStatusValue)
            }
        }
    }

    /**
     * Run model inference on current board state.
     */
    override fun runInference() {
        tflite.run(boardData, outputProbArrays)
        boardData.rewind()
    }
}
