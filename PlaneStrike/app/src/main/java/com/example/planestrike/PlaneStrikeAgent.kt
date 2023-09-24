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
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * The class that defines a policy gradient agent to play the game.
 */
abstract class PlaneStrikeAgent(activity: Activity) {

    protected var agentStrikePosition = 0
    protected var tflite: Interpreter
    protected var tfliteOptions: Interpreter.Options

    init {
        tfliteOptions = Interpreter.Options()
        tflite = Interpreter(loadModelFile(activity), tfliteOptions)
    }

    /**
     * Memory-map the model file in Assets.
     */
    @Throws(IOException::class)
    protected fun loadModelFile(activity: Activity): MappedByteBuffer {

        val model: String = if (Constants.USE_MODEL_FROM_TF) {
            Constants.TF_TFLITE_MODEL
        } else {
            Constants.TF_AGENTS_TFLITE_MODEL
        }

        val fileDescriptor = activity.assets.openFd(model)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Predict the next move based on current board state.
     */
    abstract fun predictNextMove(board: Array<Array<BoardCellStatus>>): Int

    protected abstract fun prepareModelInput(board: Array<Array<BoardCellStatus>>)

    /**
     * Run model inference on current board state.
     */
    protected abstract fun runInference()
}
