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

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableIntegerArray
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Point2D
import javafx.geometry.VPos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.input.*
import javafx.scene.transform.Transform
import com.github.netomi.sudoku.model.Cell
import com.github.netomi.sudoku.model.Conflict
import com.github.netomi.sudoku.model.Grid
import com.github.netomi.sudoku.model.ValueSet
import com.github.netomi.sudoku.solver.*
import com.github.netomi.sudoku.solver.LinkType.WEAK
import com.github.netomi.sudoku.trainer.Styles
import com.github.netomi.sudoku.trainer.model.DisplayOptions
import com.github.netomi.sudoku.trainer.pseudoClass
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.css.PseudoClass
import javafx.geometry.Insets
import javafx.scene.layout.*
import tornadofx.*
import kotlin.math.sign

/**
 * The view to display the state of an individual cell within a sudoku grid.
 */
class CellFragment(private val cell: Cell) : Fragment()
{
    private val valueProperty: IntegerProperty = SimpleIntegerProperty(0)
    private var value by valueProperty

    private val possibleValuesProperty = FXCollections.observableIntegerArray()

    val selectedProperty: BooleanProperty = SimpleBooleanProperty(false)
    var selected by selectedProperty

    private val candidatesPane: GridPane
    private val assignedValueLabel: Label
    // a simple pane to easily indicate the currently focused cell.
    private val selectPane: Pane

    override val root = stackpane {}

    fun refreshView(conflicts: Array<Conflict>, displayedHint: Hint?) {
        assignedValueLabel.apply {
            pseudoClassStateChanged(Styles.assigned.pseudoClass, cell.isAssigned)
            pseudoClassStateChanged(Styles.given.pseudoClass, cell.isGiven)

            val foundConflict = conflicts.any { conflict -> conflict.contains(cell) }
            pseudoClassStateChanged(Styles.conflict.pseudoClass, foundConflict)
        }

        // pencil mark filter
        val cellActiveFiltered = DisplayOptions.pencilMarkFilter?.invoke(cell.possibleValueSet) ?: false
        root.pseudoClassStateChanged(Styles.active.pseudoClass, cellActiveFiltered)

        val cellHighlighted = displayedHint?.relatedCells?.get(cell.cellIndex) ?: false
        root.pseudoClassStateChanged(Styles.highlighted.pseudoClass, cellHighlighted)

        val candidatePseudoStates = processHint(displayedHint)
        candidatesPane.children.forEachIndexed { index, node ->
            val displayState = candidatePseudoStates[index + 1]
            for (state in CandidateState.values()) {
                state.pseudoClass?.let { pseudoClass ->
                    node.pseudoClassStateChanged(pseudoClass, state == displayState)
                }
            }
        }

        value = cell.value

        if (DisplayOptions.showPencilMarks) {
            if (DisplayOptions.showComputedValues) {
                possibleValuesProperty.setAll(*cell.possibleValueSet.toArray())
            } else {
                possibleValuesProperty.setAll(*cell.excludedValueSet.inverse().toArray())
            }
        } else {
            possibleValuesProperty.clear()
        }
    }

    private fun processHint(hint: Hint?): Array<CandidateState> {
        val result = Array(cell.owner.gridSize + 1) { _ -> CandidateState.NONE }

        hint?.accept(object : HintVisitor {
            override fun visitAnyHint(hint: Hint) {}

            override fun visitAssignmentHint(hint: AssignmentHint) {
                if (hint.cellIndex == cell.cellIndex) {
                    result[hint.value] += CandidateState.MATCHED
                }
            }

            override fun visitEliminationHint(hint: EliminationHint) {
                if (hint.matchingCells[cell.cellIndex]) {
                    for (value in hint.matchingValues) {
                        result[value] += CandidateState.MATCHED
                    }
                }

                for ((i, cellIndex) in hint.affectedCells.setBits().withIndex()) {
                    if (cellIndex == cell.cellIndex) {
                        val excludedValues = hint.excludedValues[i]
                        for (value in excludedValues) {
                            result[value] += CandidateState.ELIMINATED
                        }
                    }
                }
            }

            override fun visitChainEliminationHint(hint: ChainEliminationHint) {
                hint.relatedChain.accept(cell.owner, object: ChainVisitor {
                    override fun visitCell(grid: Grid, chain: Chain, currentCell: Cell, activeValues: ValueSet, inactiveValues: ValueSet) {
                        if (cell.cellIndex == currentCell.cellIndex) {
                            for (value in activeValues) {
                                result[value] += CandidateState.ACTIVE
                            }

                            for (value in inactiveValues) {
                                result[value] += CandidateState.INACTIVE
                            }
                        }
                    }

                    override fun visitCellLink(grid: Grid, chain: Chain, fromCell: Cell, fromCandidate: Int, toCell: Cell, toCandidate: Int, linkType: LinkType) {}
                })

                for ((i, cellIndex) in hint.affectedCells.setBits().withIndex()) {
                    if (cellIndex == cell.cellIndex) {
                        val excludedValues = hint.excludedValues[i]
                        for (value in excludedValues) {
                            result[value] += CandidateState.ELIMINATED
                        }
                    }
                }
            }
        })

        return result
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

    private fun getCandidateLabel(candidate: Int): Node {
        return candidatesPane.children[candidate - 1]
    }

    internal fun getArrow(fromCandidate: Int, toFragment: CellFragment, toCandidate: Int, linkType: LinkType, transform: Transform): Group {
        // FIXME: very simple arrow, improve using cubic curve and dashed lines
        val fromLabel = getCandidateLabel(fromCandidate)
        val toLabel   = toFragment.getCandidateLabel(toCandidate)

        val startPoint = getStartPoint(fromLabel, toLabel, transform)
        val endPoint = getStartPoint(toLabel, fromLabel, transform)

        val arrow = Arrow(startPoint.x, startPoint.y, endPoint.x, endPoint.y)

        arrow.lineStyles.add(Styles.chainLink)
        if (linkType == WEAK) {
            arrow.lineStyles.add(Styles.weakChainLink)
        }
        arrow.arrowHeadStyles.add(Styles.chainLinkArrow)

        arrow.draw()

        return arrow
    }

    private fun getStartPoint(from: Node, to: Node, transform: Transform): Point2D {
        val fromBounds = from.localToScene(from.boundsInLocal)
        val toBounds = to.localToScene(to.boundsInLocal)

        val width  = fromBounds.width
        val height = fromBounds.height

        var startX = fromBounds.centerX
        var startY = fromBounds.centerY

        val endX = toBounds.centerX
        val endY = toBounds.centerY

        startX += if (startX < endX) width / 2.0 else if (startX > endX) -width / 2.0 else 0.0
        startY += if (startY < endY) height / 2.0 else if (startY > endY) -height / 2.0 else 0.0

        return transform.inverseTransform(startX, startY)
    }

    private fun setupEventListeners() {
        with(root) {
            onMousePressed = EventHandler { requestFocus() }
            onKeyPressed = EventHandler setOnKeyPressed@{ event ->
                if (cell.isGiven) {
                    return@setOnKeyPressed
                }
                if (event.code == KeyCode.DELETE ||
                    event.code == KeyCode.BACK_SPACE) {
                    cell.value = 0
                } else {
                    try {
                        val newValue = event.text.toInt()
                        cell.value = newValue
                    } catch (ex: NumberFormatException) {}
                }
            }

            setOnMouseClicked { event ->
                if (event.button === MouseButton.PRIMARY && event.clickCount == 2) {
                    if (cell.isAssigned && !cell.isGiven) {
                        cell.value = 0
                    } else {
                        if (DisplayOptions.showPencilMarks) {
                            if (DisplayOptions.showComputedValues) {
                                if (cell.possibleValueSet.cardinality() == 1) {
                                    cell.value = cell.possibleValueSet.firstSetBit()
                                }
                            } else {
                                if (cell.excludedValueSet.cardinality() == cell.owner.gridSize - 1) {
                                    cell.value = cell.excludedValueSet.firstUnsetBit()
                                }
                            }
                        }
                    }
                }
            }

            focusedProperty().onChange { focused -> if (focused) selected = true }

            selectedProperty.onChange { selected -> selectPane.pseudoClassStateChanged(Styles.selected.pseudoClass, selected) }
        }
    }

    override fun toString(): String {
        return cell.toString()
    }

    init {
        with(root) {
            addClass(Styles.sudokuCell)

            selectPane = pane {
                useMaxSize = true
                id = Styles.cellSelectPane.name
            }

            style += getBorderStyle(cell)

            setMinSize(30.0, 30.0)
            useMaxSize = true

            val rows = when (cell.owner.gridSize) {
                4    -> 2
                6    -> 2
                9    -> 3
                else -> kotlin.error("unexpected grid size $cell.owner.gridSize")
            }
            val cols = cell.owner.gridSize / rows

            val percentageRow = 100.0 / rows
            val percentageCol = 100.0 / cols

            candidatesPane = gridpane {
                var possibleValue = 1
                for (i in 0 until rows) {
                    for (j in 0 until cols) {
                        label {
                            useMaxHeight = true
                            maxWidth     = 36.0

                            id = Styles.cellCandidate.name
                            text = possibleValue.toString()

                            gridpaneConstraints {
                                margin = Insets(3.0)
                                columnRowIndex(j, i)
                            }
                        }
                        possibleValue++
                    }
                }

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

            assignedValueLabel = label {
                id = Styles.cellValue.name
                isVisible = false
            }
        }

        assignedValueLabel.textProperty().bind(valueProperty.asString())

        valueProperty.onChange { newValue ->
            candidatesPane.isVisible     = newValue == 0
            assignedValueLabel.isVisible = newValue != 0
        }

        possibleValuesProperty.addListener { observableArray: ObservableIntegerArray, _, from: Int, to: Int ->
            val children = candidatesPane.children
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

private enum class CandidateState(val pseudoClass: PseudoClass?)
{
    NONE      (null),
    ACTIVE    (Styles.active.pseudoClass),
    INACTIVE  (Styles.inactive.pseudoClass),
    MATCHED   (Styles.matched.pseudoClass),
    ELIMINATED(Styles.eliminated.pseudoClass);

    operator fun plus(other: CandidateState): CandidateState {
        return when ((this.ordinal - other.ordinal).sign) {
            +1   -> this
            -1   -> other
            else -> this
        }
    }
}