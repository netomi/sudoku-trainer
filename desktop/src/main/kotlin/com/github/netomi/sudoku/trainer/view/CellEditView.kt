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
package com.github.netomi.sudoku.trainer.view

import com.github.netomi.sudoku.model.Cell
import com.github.netomi.sudoku.model.Grid
import com.github.netomi.sudoku.trainer.Styles
import com.github.netomi.sudoku.trainer.controller.AssignValueEvent
import com.github.netomi.sudoku.trainer.controller.ExcludePossibleValueEvent
import com.github.netomi.sudoku.trainer.controller.RemoveExcludedPossibleValueEvent
import com.github.netomi.sudoku.trainer.model.DisplayOptions
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import tornadofx.*

class CellEditView : View()
{
    val gridProperty: ObjectProperty<Grid> = SimpleObjectProperty()
    private val grid: Grid by gridProperty

    val cellProperty: ObjectProperty<Cell?> = SimpleObjectProperty()
    private val cell: Cell? by cellProperty

    private var valuesPane: GridPane by singleAssign()

    private val valueFragments     = ArrayList<CellValueFragment>()
    private val candidateFragments = ArrayList<CellValueFragment>()

    override val root =
        vbox {
            useMaxSize = true
            padding = Insets(15.0, 30.0, 15.0, 30.0)

            gridpane {
                valuesPane = this

                hgap = 10.0
                vgap = 20.0
            }
        }

    private fun rebuildViewFromModel(oldGrid: Grid?, newGrid: Grid) {
        // if the grid size has not changed, we dont have to change anything
        if (oldGrid?.gridSize == newGrid.gridSize) return

        valuesPane.children.clear()
        valuesPane.rowConstraints.clear()
        valuesPane.columnConstraints.clear()

        valueFragments.clear()
        candidateFragments.clear()

        grid.let {
            for (i in 0 until it.gridSize) {
                val valueFragment = CellValueFragment(i + 1)
                valueFragment.labelId = Styles.editValue.name

                valueFragments.add(valueFragment)
                valueFragment.root.apply {
                    gridpaneConstraints {
                        columnRowIndex(i, 0)
                    }
                }

                valueFragment.root.onMouseClicked = EventHandler {
                    cell?.apply {
                        if (!valueFragment.root.isDisable) {
                            if (this.isAssigned && !this.isGiven) {
                                fire(AssignValueEvent(this, 0))
                            } else if (!this.isAssigned) {
                                fire(AssignValueEvent(this, valueFragment.value))
                            }
                        }
                    }
                }

                valuesPane.add(valueFragment)

                val candidateFragment = CellValueFragment(i + 1)
                candidateFragment.labelId = Styles.editCandidate.name

                candidateFragments.add(candidateFragment)
                candidateFragment.root.apply {
                    gridpaneConstraints {
                        columnRowIndex(i, 1)
                    }
                }

                candidateFragment.root.onMouseClicked = EventHandler {
                    cell?.apply {
                        if (!candidateFragment.root.isDisable) {
                            val value = candidateFragment.value
                            if (this.excludedValueSet[value]) {
                                fire(RemoveExcludedPossibleValueEvent(this, value))
                            } else {
                                fire(ExcludePossibleValueEvent(this, value))
                            }
                        }
                    }
                }

                valuesPane.add(candidateFragment)
            }

            for (i in 0..1) {
                val row = RowConstraints(20.0, 30.0, 100.0)
                row.vgrow = Priority.ALWAYS
                valuesPane.rowConstraints.add(row)
            }

            for (i in 0 until it.gridSize) {
                val col = ColumnConstraints(20.0, 30.0, Double.MAX_VALUE)
                col.hgrow = Priority.ALWAYS
                valuesPane.columnConstraints.add(col)
            }
        }

        valuesPane.children.forEach { it.isDisable = true }
    }

    fun refreshView() {
        valuesPane.children.forEach { it.isDisable = true }

        valueFragments.forEach { it.root.removeClass(Styles.selectAssignedValue) }
        candidateFragments.forEach { it.root.removeClass(Styles.selectPossibleCandidate) }

        cell?.apply {
            if (this.isAssigned) {
                val valueFragment = valueFragments[this.value - 1]

                valueFragment.root.apply {
                    isDisable = false
                    addClass(Styles.selectAssignedValue)
                }
            } else {
                if (DisplayOptions.showPencilMarks) {
                    if (DisplayOptions.showComputedValues) {
                        for (candidate in this.possibleValueSet) {
                            valueFragments[candidate - 1].root.isDisable = false

                            candidateFragments[candidate - 1].root.apply {
                                this.isDisable = false
                                this.addClass(Styles.selectPossibleCandidate)
                            }
                        }

                        for (candidate in this.excludedValueSet) {
                            candidateFragments[candidate - 1].root.isDisable = false
                        }
                    } else {
                        for (candidate in this.excludedValueSet.inverse()) {
                            valueFragments[candidate - 1].root.isDisable = false

                            candidateFragments[candidate - 1].root.apply {
                                this.isDisable = false
                                this.addClass(Styles.selectPossibleCandidate)
                            }
                        }

                        for (candidate in this.excludedValueSet) {
                            candidateFragments[candidate - 1].root.isDisable = false
                        }
                    }
                } else {
                    valueFragments.forEach { it.root.isDisable = false }
                    candidateFragments.forEach { it.root.isDisable = true }
                }
            }
        }
    }

    init {
        // rebuild the view when the model has changed
        gridProperty.addListener(ChangeListener { _, oldGrid: Grid?, newGrid: Grid -> rebuildViewFromModel(oldGrid, newGrid) })

        cellProperty.onChange {
            valuesPane.children.forEach { it.isDisable = true }
            refreshView()
        }
    }
}