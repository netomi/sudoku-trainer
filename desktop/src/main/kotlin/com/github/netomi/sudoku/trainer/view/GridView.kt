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

package com.github.netomi.sudoku.trainer.view

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.*
import com.github.netomi.sudoku.model.Cell
import com.github.netomi.sudoku.model.Conflict
import com.github.netomi.sudoku.model.Grid
import com.github.netomi.sudoku.model.ValueSet
import com.github.netomi.sudoku.solver.*
import com.github.netomi.sudoku.trainer.Styles
import com.github.netomi.sudoku.trainer.controller.GridController
import tornadofx.*

/**
 * The main grid view to visualize the state of a sudoku grid.
 */
class GridView : View()
{
    private val gridController by inject<GridController>()
    private val grid by gridController.gridProperty

    private var gridPane: GridPane by singleAssign()
    private var shapeGroup: Pane   by singleAssign()

    private val cellEditView by inject<CellEditView>()

    private val cellFragmentList = ArrayList<CellFragment>()

    private val selectedCellFragmentProperty: ObjectProperty<CellFragment?> = SimpleObjectProperty()
    private val selectedCellProperty: ObjectProperty<Cell?> = SimpleObjectProperty()

    override val root =
        borderpane {
            useMaxSize = true

            center = stackpane {
                useMaxSize = true

                gridPane = gridpane {
                    useMaxSize = true
                    addClass(Styles.sudokuGrid)
                }

                shapeGroup = pane {
                    managedProperty().set(false)
                    useMaxSize = true
                    isMouseTransparent = true
                }
            }

            bottom = cellEditView.root
        }

    private fun rebuildViewFromModel() {
        gridPane.children.clear()
        gridPane.rowConstraints.clear()
        gridPane.columnConstraints.clear()
        cellFragmentList.clear()

        grid.let {
            for (cell in it.cells) {
                val cellFragment = CellFragment(cell)
                cellFragmentList.add(cellFragment)
                gridPane.add(cellFragment).apply {
                    cellFragment.root.gridpaneConstraints {
                        columnRowIndex(cell.columnIndex, cell.rowIndex)
                    }
                }

                cellFragment.selectedProperty.onChange { selected ->
                    if (selected) {
                        selectedCellFragmentProperty.get()?.selected = false
                        selectedCellFragmentProperty.set(cellFragment)
                        selectedCellProperty.set(cell)
                    }
                }
            }

            for (i in 0 until it.gridSize) {
                val row = RowConstraints(40.0, 100.0, Double.MAX_VALUE)
                row.vgrow = Priority.ALWAYS
                gridPane.rowConstraints.add(row)
            }

            for (i in 0 until it.gridSize) {
                val col = ColumnConstraints(40.0, 100.0, Double.MAX_VALUE)
                col.hgrow = Priority.ALWAYS
                gridPane.columnConstraints.add(col)
            }

            it.onUpdate { refreshView() }
            refreshView()
        }
    }

    fun refreshView() {
        var conflicts: Array<Conflict>

        grid.let {
            conflicts = when (it.isValid) {
                true -> emptyArray()
                false -> it.conflicts
            }
        }

        val hint = gridController.hintProperty.get()

        for (child in cellFragmentList) {
            child.refreshView(conflicts, hint)
        }

        cellEditView.refreshView()

        shapeGroup.children.clear()
        hint?.accept(object : HintVisitor {
            override fun visitAnyHint(hint: Hint) {}

            override fun visitChainEliminationHint(hint: ChainEliminationHint) {
                hint.relatedChain.accept(grid, object : ChainVisitor {
                    override fun visitCell(grid: Grid, chain: Chain, cell: Cell, activeValues: ValueSet, inactiveValues: ValueSet) {}

                    override fun visitCellLink(grid: Grid, chain: Chain, fromCell: Cell, fromCandidate: Int, toCell: Cell, toCandidate: Int, linkType: LinkType) {
                        val fromFragment = cellFragmentList[fromCell.cellIndex]
                        val toFragment = cellFragmentList[toCell.cellIndex]

                        val arrow = fromFragment.getArrow(fromCandidate, toFragment, toCandidate, linkType, shapeGroup.localToSceneTransform)
                        shapeGroup.add(arrow)
                    }
                })
            }
        })
    }

    init {
        gridController.gridProperty.onChange { rebuildViewFromModel() }

        // bind the current model and selected cell to the cell edit view
        cellEditView.gridProperty.bind(gridController.gridProperty)
        cellEditView.cellProperty.bind(selectedCellProperty)

        // refresh the grid view in case of a resize event
        val updater: (Double) -> Unit = { Platform.runLater { refreshView() } }

        gridPane.widthProperty().onChange(updater)
        gridPane.heightProperty().onChange(updater)
    }
}