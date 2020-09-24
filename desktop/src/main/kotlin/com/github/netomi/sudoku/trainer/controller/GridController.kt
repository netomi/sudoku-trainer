/*
 * Sudoku creator / solver / teacher.
 *
 * Copyright (c) 2020 Thomas Neidhart
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

// Workaround for https://youtrack.jetbrains.com/issue/KT-35343
@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")

package com.github.netomi.sudoku.trainer.controller

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.input.Clipboard
import javafx.scene.input.DataFormat
import com.github.netomi.sudoku.io.GridValueLoader
import com.github.netomi.sudoku.model.Grid
import com.github.netomi.sudoku.model.Grid.Companion.of
import com.github.netomi.sudoku.model.GridType
import com.github.netomi.sudoku.model.assigned
import com.github.netomi.sudoku.solver.BruteForceSolver
import com.github.netomi.sudoku.solver.Hint
import com.github.netomi.sudoku.solver.HintSolver
import com.github.netomi.sudoku.solver.ValueSelection
import com.github.netomi.sudoku.trainer.model.LibraryEntry
import tornadofx.Controller
import tornadofx.onChange
import kotlin.random.Random

class GridController : Controller()
{
    val modelProperty: ObjectProperty<Grid> = SimpleObjectProperty(of(GridType.CLASSIC_9x9))
    private val grid: Grid
        get() = modelProperty.get()

    val hintProperty: ObjectProperty<Hint> = SimpleObjectProperty()
    val hintList: ObservableList<Hint>     = FXCollections.observableArrayList()

    fun loadModel() {
        //val grid = of(GridType.JIGSAW_1);
        val grid = of(GridType.CLASSIC_9x9)
        //val input = "3.......4..2.6.1...1.9.8.2...5...6...2.....1...9...8...8.3.4.6...4.1.9..5.......7"; // jigsaw
        //val input = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......" // 9x9
        //val input = "000000010400000000020000000000050407008000300001090000300400200050100000000806000"; // 9x9
        val input = "..+1.+4+9+2+636+3.21+7+9+4.942+63..+7.2634.+17.98.+4.+9..2...+9.+6+2.+34..7..4.9.+4..9+7631..+9+6.+2.+4.7"

        grid.accept(GridValueLoader(input))

        modelProperty.set(grid)
    }

    fun loadModel(entry: LibraryEntry) {
        runAsync {
            val grid = of(GridType.CLASSIC_9x9)
            grid.accept(GridValueLoader(entry.givens))

            for (c in entry.getDeletedCandidates()) {
                val cell = grid.getCell(c.row, c.col)
                cell.excludePossibleValues(false, c.value)
            }

            grid.updateState()
            grid
        } ui { modelProperty.set(it) }
    }

    fun loadModelFromClipboard() {
        val data = Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT) as String?

        data?.apply {
            runAsync {
                try {
                    val newGrid = grid.copy()
                    newGrid.clear(true)
                    newGrid.accept(GridValueLoader(data))
                } catch (ex: RuntimeException) {
                    val alert = Alert(Alert.AlertType.ERROR, "failed to load model from clipboard", ButtonType.OK)
                    alert.showAndWait()
                    throw ex
                }
            } ui { it.apply { modelProperty.set(it) } }
        }
    }

    fun resetModel(type: GridType) {
        if (grid.type == type) {
            grid.clear(true)
        } else {
            val newGrid = of(type)
            modelProperty.set(newGrid)
        }
    }

    // TODO: this is very naive and not working well.
    fun generateSudoku(type: GridType) {
        val emptyGrid = of(type)

        val solver = BruteForceSolver()
        val fullGrid = solver.solve(emptyGrid, ValueSelection.RANDOM)

        var count = 0
        do {
            val testGrid = fullGrid.copy()

            while (testGrid.cells.assigned().count() > 30) {
                val idx = Random.nextInt(grid.cellCount)
                testGrid.getCell(idx).setValue(0, false)
            }
            testGrid.updateState()

            val hintSolver = HintSolver()
            val solvedGrid = hintSolver.solve(testGrid)
            if (solvedGrid.isValid && solvedGrid.isSolved) {
                testGrid.cells.assigned().forEach { it.isGiven = true }
                modelProperty.set(testGrid)
                return
            }
        } while(count++ < 1000)

        modelProperty.set(of(type))
    }

    fun findHints() {
        runAsync {
            HintSolver().findAllHints(grid)
        } ui {
            it.apply { hintList.setAll(it.hints) }
        }
    }

    fun findHintsSingleStep() {
        runAsync {
            HintSolver().findAllHintsSingleStep(grid)
        } ui {
            it.apply { hintList.setAll(it.hints) }
        }
    }

    fun applyHint(hint: Hint) {
        hint.apply(grid, true)
    }

    fun applyHints(from: Int, to: Int) {
        grid.apply {
            for (i in from..to) {
                val hint = hintList[i]
                hint.apply(this, false)
            }
            updateState()
        }
    }

    init {
        modelProperty.onChange { hintList.clear() }
    }
}