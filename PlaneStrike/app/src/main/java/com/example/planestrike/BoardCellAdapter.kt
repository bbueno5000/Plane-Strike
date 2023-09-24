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

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/**
 * The gridview adapter for filling the board.
 */
class BoardCellAdapter(
  private val context: Context,
  board: Array<Array<BoardCellStatus>>,
  hiddenBoard: Array<Array<HiddenBoardCellStatus>>,
  isAgentBoard: Boolean
) : BaseAdapter() {

  private val board: Array<Array<BoardCellStatus>>
  private val hiddenBoard: Array<Array<HiddenBoardCellStatus>>
  private val isAgentBoard: Boolean

  init {
    this.board = board
    this.hiddenBoard = hiddenBoard
    this.isAgentBoard = isAgentBoard
  }

  override fun getView(position: Int, convertView: View, parent: ViewGroup): View {

    val cellTextView = TextView(context)
    val x = position / BOARD_SIZE
    val y = position % BOARD_SIZE

    if (board[x][y] === BoardCellStatus.UNTRIED) {
      // Untried cell
      cellTextView.setBackgroundColor(Color.WHITE)
      if (hiddenBoard[x][y] === HiddenBoardCellStatus.OCCUPIED_BY_PLANE && !isAgentBoard) {
        cellTextView.setBackgroundColor(Color.BLUE)
      }
    } else if (board[x][y] === BoardCellStatus.HIT) {
      // Hit
      cellTextView.setBackgroundColor(Color.RED)
    } else {
      // Miss
      cellTextView.setBackgroundColor(Color.YELLOW)
    }

    cellTextView.height = 80
    return cellTextView
  }

  override fun getCount(): Int {
    return BOARD_SIZE * BOARD_SIZE
  }

  override fun getItem(position: Int): Any? {
    return null
  }

  override fun getItemId(position: Int): Long {
    return 0
  }

  companion object {
    // We always use square board, so only one size is needed
    private const val BOARD_SIZE = 8
  }
}