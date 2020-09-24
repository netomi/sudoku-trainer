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
    val modelProperty: ObjectProperty<Grid> = SimpleObjectProperty()
    val cellProperty: ObjectProperty<Cell?> = SimpleObjectProperty()

    private val model: Grid
        get() = modelProperty.get()

    private val cell: Cell?
        get() = cellProperty.get()

    private lateinit var valuesPane: GridPane
    private val valueFragments: MutableList<CellValueFragment> = ArrayList()
    private val candidateFragments: MutableList<CellValueFragment> = ArrayList()

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

    private fun rebuildViewFromModel() {
        valuesPane.children.clear()
        valuesPane.rowConstraints.clear()
        valuesPane.columnConstraints.clear()

        valueFragments.clear()
        candidateFragments.clear()

        model.let {
            for (i in 0 until it.gridSize) {
                val valueFragment = CellValueFragment(i + 1, Styles.selectValue)
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
                                this.value = 0
                            } else if (!this.isAssigned) {
                                this.value = valueFragment.value
                            }
                        }
                    }
                }

                valuesPane.add(valueFragment)

                val candidateFragment = CellValueFragment(i + 1, Styles.selectCandidate)
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
                                this.removeExcludedPossibleValues(true, value)
                            } else {
                                this.excludePossibleValues(true, value)
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
        refreshView(cellProperty.get())
    }

    private fun refreshView(cell: Cell?) {
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
                        for (candidate in cell.possibleValueSet) {
                            valueFragments[candidate - 1].root.isDisable = false

                            candidateFragments[candidate - 1].root.apply {
                                this.isDisable = false
                                this.addClass(Styles.selectPossibleCandidate)
                            }
                        }

                        for (candidate in cell.excludedValueSet) {
                            candidateFragments[candidate - 1].root.isDisable = false
                        }
                    } else {
                        for (candidate in cell.excludedValueSet.inverse()) {
                            valueFragments[candidate - 1].root.isDisable = false

                            candidateFragments[candidate - 1].root.apply {
                                this.isDisable = false
                                this.addClass(Styles.selectPossibleCandidate)
                            }
                        }

                        for (candidate in cell.excludedValueSet) {
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
        modelProperty.addListener { _, _, _ -> rebuildViewFromModel() }

        cellProperty.onChange { cell ->
            valuesPane.children.forEach { it.isDisable = true }
            cell?.apply { refreshView(this) }
        }
    }
}