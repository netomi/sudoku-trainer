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
package org.netomi.sudoku.ui.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import org.netomi.sudoku.model.ConflictDetector
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.solver.Hint
import org.netomi.sudoku.ui.Styles
import org.netomi.sudoku.ui.controller.GridController
import tornadofx.View
import tornadofx.addClass
import tornadofx.gridpane
import tornadofx.gridpaneConstraints

/**
 * The main grid view to visualize the state of a sudoku grid.
 */
class GridView : View()
{
    private val gridController: GridController by inject()

    val modelProperty: ObjectProperty<Grid> = SimpleObjectProperty()
    val hintProperty:  ObjectProperty<Hint> = SimpleObjectProperty()

    private val cellFragmentList: ArrayList<CellFragment> = ArrayList()

    private val model: Grid
        get() = modelProperty.get()

    override val root = gridpane {
        addClass(Styles.grid)
    }

    private fun rebuildViewFromModel() {
        root?.let {
            it.children.clear()
            it.rowConstraints.clear()
            it.columnConstraints.clear()

            cellFragmentList.clear()

            for (cell in model.cells()) {
                val cellFragment = CellFragment(cell)
                cellFragment.dirtyProperty.addListener { _, _, newValue -> if (newValue) refreshView() }
                cellFragmentList.add(cellFragment)
                it.add(cellFragment).apply { cellFragment.root.gridpaneConstraints { columnRowIndex(cell.columnIndex, cell.rowIndex) } }
            }

            for (i in 0 until model.gridSize) {
                val row = RowConstraints(3.0, 100.0, Double.MAX_VALUE)
                row.vgrow = Priority.ALWAYS
                it.rowConstraints.add(row)
            }

            for (i in 0 until model.gridSize) {
                val col = ColumnConstraints(3.0, 100.0, Double.MAX_VALUE)
                col.hgrow = Priority.ALWAYS
                it.columnConstraints.add(col)
            }

            refreshView()
        }
    }

    fun resetGrid() {
        model.clear(true)
    }

    fun refreshView() {
        val conflicts = if (model.isValid) emptyArray() else model.accept(ConflictDetector())

        for (child in cellFragmentList) {
            child.refreshView(conflicts, hintProperty.value)
        }
    }

    init {
        modelProperty.addListener { _, _, _ -> rebuildViewFromModel() }
        modelProperty.bind(gridController.modelProperty)
    }
}