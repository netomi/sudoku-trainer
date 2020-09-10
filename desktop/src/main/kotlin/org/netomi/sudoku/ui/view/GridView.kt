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
import javafx.scene.Group
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import org.netomi.sudoku.model.*
import org.netomi.sudoku.solver.*
import org.netomi.sudoku.ui.Styles
import org.netomi.sudoku.ui.controller.GridController
import org.netomi.sudoku.ui.model.DisplayOptions
import tornadofx.*

/**
 * The main grid view to visualize the state of a sudoku grid.
 */
class GridView : View()
{
    private val gridController: GridController by inject()

    private lateinit var grid: GridPane
    private lateinit var shapeGroup: Pane

    private val modelProperty: ObjectProperty<Grid?> = SimpleObjectProperty()
    private val cellFragmentList: ArrayList<CellFragment> = ArrayList()

    private val model: Grid?
        get() = modelProperty.get()

    override val root =
        stackpane {
            grid = gridpane {
                useMaxSize = true
                addClass(Styles.sudokuGrid)
            }

            shapeGroup = pane {
                managedProperty().set(false)
                useMaxSize = true
            }
        }

    private fun rebuildViewFromModel() {
        grid.children.clear()
        grid.rowConstraints.clear()
        grid.columnConstraints.clear()
        cellFragmentList.clear()

        model?.let {
            for (cell in it.cells()) {
                val cellFragment = CellFragment(cell)
                cellFragmentList.add(cellFragment)
                grid.add(cellFragment).apply {
                    cellFragment.root.gridpaneConstraints {
                        columnRowIndex(cell.columnIndex, cell.rowIndex)
                    }
                }
            }

            for (i in 0 until it.gridSize) {
                val row = RowConstraints(3.0, 100.0, Double.MAX_VALUE)
                row.vgrow = Priority.ALWAYS
                grid.rowConstraints.add(row)
            }

            for (i in 0 until it.gridSize) {
                val col = ColumnConstraints(3.0, 100.0, Double.MAX_VALUE)
                col.hgrow = Priority.ALWAYS
                grid.columnConstraints.add(col)
            }

            it.onUpdate { refreshView() }
            refreshView()
        }
    }

    fun resetGrid() {
        model?.clear(true)
    }

    fun refreshView() {
        var conflicts = emptyArray<Conflict>()

        model?.let {
            conflicts = when (it.isValid) {
                true ->  emptyArray()
                false -> it.accept(ConflictDetector())
            }
        }

        val hint = gridController.hintProperty.get()

        for (child in cellFragmentList) {
            child.refreshView(conflicts, hint)
        }

        shapeGroup.children.clear()
        hint?.accept(object: HintVisitor {
            override fun visitAnyHint(hint: Hint) {}

            override fun visitChainEliminationHint(hint: ChainEliminationHint) {
                hint.relatedChain.accept(model!!, object: ChainVisitor {
                    override fun visitCell(grid: Grid, chain: Chain, cell: Cell, activeValues: ValueSet, inactiveValues: ValueSet) {}

                    override fun visitCellLink(grid: Grid, chain: Chain, fromCell: Cell, fromCandidate: Int, toCell: Cell, toCandidate: Int, linkType: LinkType) {
                        val fromFragment = cellFragmentList[fromCell.cellIndex]
                        val toFragment = cellFragmentList[toCell.cellIndex]

                        val arrow = fromFragment.getArrow(fromCandidate, toFragment, toCandidate, linkType)
                        shapeGroup.add(arrow)
                    }
                })
            }
        })
    }

    init {
        modelProperty.addListener { _, _, _ -> rebuildViewFromModel() }
        modelProperty.bind(gridController.modelProperty)
    }
}