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

/** The class that holds all the constants.  */
object Constants {
    // We always use square board, so only one size is needed
    const val BOARD_SIZE = 8
    const val PLANE_CELL_COUNT = 8
    const val USE_MODEL_FROM_TF = true
    const val TF_TFLITE_MODEL = "planestrike_tf.tflite"
    const val TF_AGENTS_TFLITE_MODEL = "planestrike_tf_agents.tflite"
    const val TAG = "TfLiteRLDemo"
    // Cell status values to feed into the model
    const val CELL_STATUS_VALUE_HIT = 1f
    const val CELL_STATUS_VALUE_UNTRIED = 0f
    const val CELL_STATUS_VALUE_MISS = -1f
}