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

/**
 * The class that implements an agent to play the game, assuming model is trained
 * using TensorFlow Agents REINFORCE agent.
 */
class RLAgentFromTFAgents(activity: Activity) : PlaneStrikeAgent(activity) {

    private val inputs = arrayOfNulls<Any>(4)

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

        return agentStrikePosition
    }

    /**
     *
     */
    override fun prepareModelInput(board: Array<Array<BoardCellStatus>>) {

        if (board == null) {
            return
        }

        // Model converted from TF Agents takes 4 tensors as input; only the 3rd one 'observation' is
        // useful for inference
        val stepType = 0
        val discount = 0f
        val reward = 0f

        inputs[0] = stepType
        inputs[1] = discount

        val boardState = Array(1) {
            Array(8) {
                FloatArray(8)
            }
        }

        for (i in 0 until Constants.BOARD_SIZE) {
            for (j in 0 until Constants.BOARD_SIZE) {
                when (board[i][j]) {
                    BoardCellStatus.HIT -> boardState[0][i][j] = Constants.CELL_STATUS_VALUE_HIT
                    BoardCellStatus.MISS -> boardState[0][i][j] = Constants.CELL_STATUS_VALUE_MISS
                    else -> boardState[0][i][j] = Constants.CELL_STATUS_VALUE_UNTRIED
                }
            }
        }

        inputs[2] = boardState
        inputs[3] = reward
    }

    /**
     * Run model inference on current board state.
     */
    override fun runInference() {

        val output: MutableMap<Int, Any> = HashMap()
        // TF Agent directly returns the predicted action
        val prediction = IntArray(1)
        output[0] = prediction
        tflite.runForMultipleInputsOutputs(arrayOf(inputs), output)
        agentStrikePosition = prediction[0]
    }
}
