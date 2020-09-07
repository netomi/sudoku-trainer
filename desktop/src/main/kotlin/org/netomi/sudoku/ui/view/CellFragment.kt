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

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableIntegerArray
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.input.*
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import org.netomi.sudoku.model.Cell
import org.netomi.sudoku.model.Conflict
import org.netomi.sudoku.solver.AssignmentHint
import org.netomi.sudoku.solver.EliminationHint
import org.netomi.sudoku.solver.Hint
import org.netomi.sudoku.solver.HintVisitor
import org.netomi.sudoku.ui.Styles
import org.netomi.sudoku.ui.model.DisplayOptions
import tornadofx.*
import java.lang.RuntimeException
import java.util.function.Consumer

/**
 * The view to display the state of an individual cell within a sudoku grid.
 */
class CellFragment(private val cell: Cell) : Fragment()
{
    private val valueProperty: IntegerProperty = SimpleIntegerProperty(0)
    private val possibleValuesProperty         = FXCollections.observableIntegerArray()

    private val possibleValuesPane: GridPane
    private val assignedValueLabel: Label

    override val root = stackpane {}

    fun refreshView(conflicts: Array<Conflict>, displayedHint: Hint?) {
        assignedValueLabel.apply {
            if (cell.isGiven) {
                addClass(Styles.cellGivenValue)
            } else {
                addClass(Styles.cellAssignedValue)
            }
        }

        var foundConflict = false
        for (conflict in conflicts) {
            if (conflict.contains(cell)) {
                foundConflict = true
                break
            }
        }

        if (foundConflict) {
            assignedValueLabel.removeClass(Styles.cellValueConflict)
            assignedValueLabel.addClass(Styles.cellValueConflict)
        } else {
            assignedValueLabel.removeClass(Styles.cellValueConflict)
        }

        possibleValuesPane.children.forEach(Consumer { child: Node -> child.removeClass(Styles.cellAssigmentHint) })
        possibleValuesPane.children.forEach(Consumer { child: Node -> child.removeClass(Styles.cellEliminationHint) })

        root.removeClass(Styles.cellHighlight)
        displayedHint?.accept(object : HintVisitor {
            override fun visitAnyHint(hint: Hint) {}

            override fun visitAssignmentHint(hint: AssignmentHint) {
                if (hint.cellIndex == cell.cellIndex) {
                    possibleValuesPane.children[hint.value - 1].addClass(Styles.cellAssigmentHint)
                }

                for (cellIndex in hint.peerSet.allSetBits()) {
                    if (cellIndex == cell.cellIndex) {
                        root.addClass(Styles.cellHighlight)
                    }
                }
            }

            override fun visitEliminationHint(hint: EliminationHint) {
                for (cellIndex in hint.matchingCells.allSetBits()) {
                    if (cellIndex == cell.cellIndex) {
                        for (value in hint.matchingValues.allSetBits()) {
                            possibleValuesPane.children[value - 1].addClass(Styles.cellAssigmentHint)
                        }
                    }
                }

                for ((i, cellIndex) in hint.affectedCells.allSetBits().withIndex()) {
                    if (cellIndex == cell.cellIndex) {
                        val excludedValues = hint.excludedValues[i]
                        for (value in excludedValues.allSetBits()) {
                            possibleValuesPane.children[value - 1].addClass(Styles.cellEliminationHint)
                        }
                    }
                }

                for (cellIndex in hint.peerSet.allSetBits()) {
                    if (cellIndex == cell.cellIndex) {
                        root.addClass(Styles.cellHighlight)
                    }
                }
            }
        })

        valueProperty.set(cell.value)

        if (DisplayOptions.displayPossibleValues) {
            possibleValuesProperty.setAll(*cell.possibleValueSet.toArray())
        } else {
            possibleValuesProperty.setAll(*cell.excludedValueSet.inverse().toArray())
        }
    }

    private fun setupEventListeners() {
        with(root) {
            onMousePressed = EventHandler { requestFocus() }
            onKeyPressed = EventHandler setOnKeyPressed@{ event: KeyEvent ->
                if (cell.isGiven) {
                    return@setOnKeyPressed
                }
                if (event.code == KeyCode.DELETE ||
                    event.code == KeyCode.BACK_SPACE) {
                    cell.value = 0
                    valueProperty.value = 0
                } else {
                    try {
                        val newValue = event.text.toInt()
                        cell.value = newValue
                        valueProperty.value = newValue
                    } catch (ex: NumberFormatException) {}
                }
            }

            focusedProperty().addListener { _, _, newPropertyValue: Boolean ->
                if (newPropertyValue) {
                    removeClass(Styles.cellFocus)
                    addClass(Styles.cellFocus)
                } else {
                    removeClass(Styles.cellFocus)
                }
            }
        }
    }

    private fun getBorderStyle(cell: Cell): String {
        val borderStyle = StringBuilder()
        val borderColor = StringBuilder()
        val borderWidth = StringBuilder()

        borderStyle.append("-fx-border-style: ")
        borderColor.append("-fx-border-color: ")
        borderWidth.append("-fx-border-width: ")

        selectBorder(getAdjacentCell(cell, cell.cellIndex - cell.owner.gridSize), borderStyle, borderColor, borderWidth)

        val nextCellIndex = if ((cell.cellIndex + 1) % cell.owner.gridSize == 0) -1 else cell.cellIndex + 1
        selectBorder(getAdjacentCell(cell, nextCellIndex), borderStyle, borderColor, borderWidth)

        selectBorder(getAdjacentCell(cell, cell.cellIndex + cell.owner.gridSize), borderStyle, borderColor, borderWidth)

        val previousCellIndex = if (cell.cellIndex % cell.owner.gridSize == 0) -1 else cell.cellIndex - 1
        selectBorder(getAdjacentCell(cell, previousCellIndex), borderStyle, borderColor, borderWidth)

        borderStyle.append(";")
        borderColor.append(";")
        borderWidth.append(";")

        return borderStyle.toString() + borderColor.toString() + borderWidth.toString()
    }

    private fun selectBorder(neighbour: Cell?, borderStyle: StringBuilder, borderColor: StringBuilder, borderWidth: StringBuilder) {
        if (neighbour != null) {
            if (neighbour.blockIndex == cell.blockIndex) {
                borderStyle.append(" solid")
                borderColor.append(" grey")
                borderWidth.append(" 1px")
            } else {
                borderStyle.append(" solid")
                borderColor.append(" black")
                borderWidth.append(" 2px")
            }
        } else {
            borderStyle.append(" solid")
            borderColor.append(" black")
            borderWidth.append(" 4px")
        }
    }

    private fun getAdjacentCell(cell: Cell, adjacentCellIndex: Int): Cell? {
        return if (adjacentCellIndex >= 0 &&
                   adjacentCellIndex < cell.owner.cellCount) { cell.owner.getCell(adjacentCellIndex) } else null
    }

    private fun buildAndDisplayContextMenu(event: ContextMenuEvent) {
        val contextMenu = ContextMenu()

        if (!cell.isAssigned) {
            if (DisplayOptions.displayPossibleValues) {
                // add assignment items
                for (value in cell.possibleValueSet.allSetBits()) {
                    contextMenu.item("Assign $value").action {
                        cell.setValue(value, true)
                    }
                }

                contextMenu.separator()

                // add exclude possible values items
                for (value in cell.possibleValueSet.allSetBits()) {
                    contextMenu.item("Exclude $value").action {
                        cell.excludePossibleValues(true, value)
                    }
                }

                contextMenu.separator()

                // add include previously excluded value items
                for (value in cell.excludedValueSet.allSetBits()) {
                    contextMenu.item("Include $value").action {
                        cell.removeExcludedPossibleValues(true, value)
                    }
                }
            } else {
                // add assignment items
                for (value in cell.excludedValueSet.allUnsetBits()) {
                    contextMenu.item("Assign $value").action {
                        cell.setValue(value, true)
                    }
                }

                contextMenu.separator()

                // add exclude possible values items
                for (value in cell.excludedValueSet.allUnsetBits()) {
                    contextMenu.item("Exclude $value").action {
                        cell.excludePossibleValues(true, value)
                    }
                }

                contextMenu.separator()

                // add include previously excluded value items
                for (value in cell.excludedValueSet.allSetBits()) {
                    contextMenu.item("Include $value").action {
                        cell.removeExcludedPossibleValues(true, value)
                    }
                }
            }
        } else if (!cell.isGiven) {
            contextMenu.item("Delete value from cell").action {
                cell.setValue(0, true)
            }
        }

        contextMenu.show(root, event.screenX, event.screenY)
    }

    override fun toString(): String {
        return cell.toString()
    }

    init {
        with(root) {
            addClass(Styles.cell)

            style += getBorderStyle(cell)

            setMinSize(30.0, 30.0)
            useMaxSize = true

            val rows = when (cell.owner.gridSize) {
                4    -> 2
                6    -> 2
                9    -> 3
                else -> throw RuntimeException("unexpected grid size " + cell.owner.gridSize)
            }
            val cols = cell.owner.gridSize / rows

            val percentageRow = 100.0 / rows
            val percentageCol = 100.0 / cols

            possibleValuesPane = gridpane {
                for (i in 0 until rows) {
                    val row = RowConstraints(10.0, 20.0, Double.MAX_VALUE)
                    row.vgrow         = Priority.ALWAYS
                    row.valignment    = VPos.CENTER
                    row.percentHeight = percentageRow
                    rowConstraints.add(row)
                }

                for (i in 0 until cols) {
                    val col = ColumnConstraints(10.0, 20.0, Double.MAX_VALUE)
                    col.hgrow        = Priority.ALWAYS
                    col.halignment   = HPos.CENTER
                    col.percentWidth = percentageCol
                    columnConstraints.add(col)
                }
            }

            var possibleValue = 1
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    val possibleValueLabel = label((possibleValue++).toString()) {
                        useMaxHeight = true
                        maxWidth     = 36.0
                        addClass(Styles.cellPossibleValue)
                    }
                    possibleValuesPane.add(possibleValueLabel, j, i)
                }
            }

            assignedValueLabel = label {
                if (cell.isGiven) {
                    addClass(Styles.cellGivenValue)
                } else {
                    addClass(Styles.cellAssignedValue)
                }
                isVisible = false
            }

            setOnContextMenuRequested {
                buildAndDisplayContextMenu(it)
            }

            setOnMouseClicked { event: MouseEvent ->
                if (event.button === MouseButton.PRIMARY &&
                    event.clickCount == 2) {
                    if (DisplayOptions.displayPossibleValues) {
                        if (cell.possibleValueSet.cardinality() == 1) {
                            cell.setValue(cell.possibleValueSet.firstSetBit(), true)
                        }
                    } else {
                        if (cell.excludedValueSet.cardinality() == cell.owner.gridSize - 1) {
                            cell.setValue(cell.excludedValueSet.firstUnsetBit(), true)
                        }
                    }
                }
            }
        }

        assignedValueLabel.textProperty().bind(Bindings.createStringBinding({ valueProperty.value.toString() }, valueProperty))

        valueProperty.addListener { _, _, newValue: Number ->
            possibleValuesPane.isVisible = newValue.toInt() == 0
            assignedValueLabel.isVisible = newValue.toInt() != 0
        }

        possibleValuesProperty.addListener { observableArray: ObservableIntegerArray, _, from: Int, to: Int ->
            val children = possibleValuesPane.children
            for (node in children) {
                node.isVisible = false
            }
            for (index in from until to) {
                val value = observableArray[index]
                children[value - 1].isVisible = true
            }
        }
        setupEventListeners()
    }
}