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
package org.netomi.sudoku.model

import java.util.*
import kotlin.streams.asSequence

class Grid internal constructor(val type: Type) {
    private val cells: MutableList<Cell>
    @Transient
    private val rows: MutableList<Row>
    @Transient
    private val columns: MutableList<Column>
    @Transient
    private val blocks: MutableList<Block>
    @Transient
    private val _potentialPositions: MutableList<MutableCellSet>

    private var stateValid: Boolean

    /**
     * Copy constructor for grids.
     */
    internal constructor(other: Grid) : this(other.type) {
        // Copy values
        for (otherCell in other.cells) {
            val cell = getCell(otherCell.cellIndex)
            cell.setValue(otherCell.value, false)
            cell.isGiven = otherCell.isGiven
            cell.excludePossibleValues(otherCell.excludedValues, false)
        }
        updateState()
    }

    val gridSize: Int
        get() = type.gridSize

    val cellCount: Int
        get() = type.cellCount

    fun copy(): Grid {
        return Grid(this)
    }

    fun cells(): Iterable<Cell> {
        return cells
    }

    fun assignedCells(): Sequence<Cell> {
        return cells.stream()
                    .filter { cell -> cell.isAssigned }
                    .asSequence()
    }

    fun unassignedCells(): Sequence<Cell> {
        return cells.stream()
                    .filter { cell -> !cell.isAssigned }
                    .asSequence()
    }

    fun rows(): Iterable<Row> {
        return rows
    }

    fun columns(): Iterable<Column> {
        return columns
    }

    fun blocks(): Iterable<Block> {
        return blocks
    }

    fun houses(): Iterable<House> {
        return rows() + columns() + blocks()
    }

    fun regionsAfter(house: House): Iterable<House> {
        return when (house.type) {
            HouseType.ROW    -> rows.subList(house.regionIndex + 1, rows.size)
            HouseType.COLUMN -> columns.subList(house.regionIndex + 1, columns.size)
            HouseType.BLOCK  -> blocks.subList(house.regionIndex + 1, blocks.size)
        }
    }

    fun getCell(cellIndex: Int): Cell {
        return cells[cellIndex]
    }

    fun getCell(row: Int, column: Int): Cell {
        return cells[type.getCellIndex(row, column)]
    }

    fun getRow(rowIndex: Int): Row {
        return rows[rowIndex]
    }

    fun getColumn(columnIndex: Int): Column {
        return columns[columnIndex]
    }

    fun getBlock(blockIndex: Int): Block {
        return blocks[blockIndex]
    }

    /**
     * Returns whether the sudoku grid is fully solved with a valid solution.
     */
    val isSolved: Boolean
        get() {
            return houses().all { house -> house.isSolved }
        }

    /**
     * Returns whether the current state of the sudoku grid is valid wrt the
     * normal sudoku constraints. The grid might not be fully solved yet.
     */
    val isValid: Boolean
        get() {
            return houses().all { house -> house.isValid }
        }

    val conflicts: Collection<Conflict>
        get() {
            val foundConflicts: MutableSet<CellSet> = HashSet()
            val conflicts: MutableCollection<Conflict> = ArrayList()
            for (house in houses()) {
                for (cell in house.assignedCells()) {
                    val value = cell.value
                    val conflictPeers = cell.peers.filteredCells(this, { c -> c.isAssigned && c.value == value })
                    val conflictCells = this.toCellSet(conflictPeers)
                    if (conflictCells.cardinality() > 0) {
                        conflictCells.set(cell.cellIndex)
                        if (!foundConflicts.contains(conflictCells)) {
                            foundConflicts.add(conflictCells)
                            conflicts.add(Conflict(conflictCells.toCellList(this)))
                        }
                    }
                }
            }
            return conflicts
        }

    // Visitor methods.

    fun <T> accept(visitor: GridVisitor<T>): T {
        return visitor.visitGrid(this)
    }

    fun acceptCells(visitor: CellVisitor) {
        cells().forEach { cell -> visitor.visitCell(cell) }
    }

    fun acceptRows(visitor: HouseVisitor) {
        rows.forEach { row -> visitor.visitRow(row) }
    }

    fun acceptColumns(visitor: HouseVisitor) {
        columns.forEach { column -> visitor.visitColumn(column) }
    }

    fun acceptBlocks(visitor: HouseVisitor) {
        blocks.forEach { block -> visitor.visitBlock(block) }
    }

    fun acceptHouses(visitor: HouseVisitor) {
        acceptRows(visitor)
        acceptColumns(visitor)
        acceptBlocks(visitor)
    }

    // Internal state related methods.

    internal fun notifyCellValueChanged(cell: Cell, oldValue: Int, newValue: Int) {
        stateValid = true
        // If the value did not really change, there is nothing to do.
        if (oldValue == newValue) {
            return
        }
        // First: update assigned values in affected houses.
        cell.row   .updateAssignedValues()
        cell.column.updateAssignedValues()
        cell.block .updateAssignedValues()

        // Second: update possible values in affected cells.
        for (affectedCell in (sequenceOf(cell) + cell.peers())) {
            affectedCell.resetPossibleValues()
            affectedCell.updatePossibleValues(affectedCell.row.assignedValues)
            affectedCell.updatePossibleValues(affectedCell.column.assignedValues)
            affectedCell.updatePossibleValues(affectedCell.block.assignedValues)
        }
        // Third: update potential positions for affected cells.
        val peers = cell.peers
        for (positions in _potentialPositions) {
            positions.clear(cell.cellIndex)
            positions.andNot(peers)
        }
        for (affectedCell in (sequenceOf(cell) + cell.peers())) {
            for (value in affectedCell.possibleValues.allSetBits()) {
                _potentialPositions[value - 1].set(affectedCell.cellIndex)
            }
        }
    }

    internal fun notifyPossibleValuesChanged(cell: Cell) {
        stateValid = true
        _potentialPositions.forEach { potentialPosition -> potentialPosition.clear(cell.cellIndex) }
        cell.possibleValues.allSetBits().forEach { value -> _potentialPositions[value - 1].set(cell.cellIndex) }
    }

    internal fun invalidateState() {
        stateValid = false
    }

    internal fun throwIfStateIsInvalid() {
        if (!stateValid) {
            throw RuntimeException("Cache data is invalidated, need to call refreshCache() before accessing cached data.")
        }
    }

    internal fun getPotentialPositions(value: Int): CellSet {
        return _potentialPositions[value - 1].asCellSet()
    }

    fun updateState() {
        // we are currently updating the internal state, ensure that we can safely
        // access any getters.
        stateValid = true
        // First: reset the possible values in all cells.
        cells().forEach { obj: Cell -> obj.resetPossibleValues() }

        // Second: refresh all assigned values in each house.
        houses().forEach { obj: House -> obj.updateAssignedValues() }

        // Third: remove potential values in each cell which
        //        are already assigned in the houses it is contained.
        houses().forEach { obj: House -> obj.updatePossibleValuesInCells() }

        // Fourth: refresh all possible positions for each cell.
        _potentialPositions.forEach { obj: MutableCellSet -> obj.clearAll() }
        for (cell in cells()) {
            for (value in cell.possibleValues.allSetBits()) {
                _potentialPositions[value - 1].set(cell.cellIndex)
            }
        }
    }

    fun clear() {
        cells().forEach  { cell: Cell -> cell.clear(false) }
        houses().forEach { obj: House -> obj.clear() }

        _potentialPositions.forEach { obj: MutableCellSet -> obj.clearAll() }

        updateState()
    }

    fun reset() {
        cells().forEach { obj: Cell -> obj.reset() }
        updateState()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Grid [").append(type).append("]:\n")
        cells().forEach { cell: Cell -> sb.append("  ").append(cell).append("\n") }
        return sb.toString()
    }

    // Inner helper classes.
    interface BlockFunction
    {
        fun getBlockIndex(cellIndex: Int): Int
    }

    class Type constructor(val gridSize: Int, val blockFunction: BlockFunction)
    {
        val cellCount: Int = gridSize * gridSize

        fun getRowIndex(cellIndex: Int): Int {
            return cellIndex / gridSize
        }

        fun getColumnIndex(cellIndex: Int): Int {
            return cellIndex % gridSize
        }

        fun getBlockIndex(cellIndex: Int): Int {
            return blockFunction.getBlockIndex(cellIndex)
        }

        fun getCellIndex(row: Int, column: Int): Int {
            return (row - 1) * gridSize + (column - 1)
        }

        fun getCellName(cellIndex: Int): String {
            return "r%dc%d".format(getRowIndex(cellIndex) + 1, getColumnIndex(cellIndex) + 1)
        }

        override fun toString(): String {
            return "%dx%d".format(gridSize, gridSize)
        }
    }

    class Conflict(cells: List<Cell>?) {
        private val cellsInConflict: List<Cell>

        fun getCellsInConflict(): Iterable<Cell> {
            return cellsInConflict
        }

        operator fun contains(otherCell: Cell?): Boolean {
            return cellsInConflict.contains(otherCell)
        }

        override fun toString(): String {
            val sb = StringBuilder()
            val firstCell = cellsInConflict[0]
            sb.append(firstCell.name)
            for (idx in 1 until cellsInConflict.size) {
                sb.append(" = ")
                sb.append(cellsInConflict[idx].name)
            }
            sb.append(" = ")
            sb.append(firstCell.value)
            return sb.toString()
        }

        init {
            require(!(cells == null || cells.size < 2)) { "cells must not be null or contain less than 2 elements" }
            cellsInConflict = cells
        }
    }

    companion object {
        fun of(type: PredefinedType): Grid {
            return Grid(Type(type.gridSize, type.blockFunction))
        }

        fun of(gridSize: Int, blockFunction: BlockFunction): Grid {
            return Grid(Type(gridSize, blockFunction))
        }
    }

    init {
        val gridSize  = type.gridSize
        val cellCount = type.cellCount

        cells   = ArrayList(cellCount)
        rows    = ArrayList(gridSize)
        columns = ArrayList(gridSize)
        blocks  = ArrayList(gridSize)

        for (i in 0 until gridSize) {
            rows   .add(Row(this, i))
            columns.add(Column(this, i))
            blocks .add(Block(this, i))
        }

        for (i in 0 until cellCount) {
            val rowIndex    = type.getRowIndex(i)
            val columnIndex = type.getColumnIndex(i)
            val blockIndex  = type.getBlockIndex(i)

            val cell = Cell(this, i, rowIndex, columnIndex, blockIndex)
            cells.add(cell)

            getRow(rowIndex).addCell(cell)
            getColumn(columnIndex).addCell(cell)
            if (blockIndex >= 0) {
                blocks[blockIndex].addCell(cell)
            }
        }

        // Initialize peers for each cell.
        houses().forEach { house: House -> house.cells().forEach { cell: Cell -> cell.addPeers(house.cells) } }

        _potentialPositions = ArrayList(gridSize)
        for(idx in 0 until gridSize) {
            _potentialPositions.add(MutableCellSet.empty(this))
        }

        stateValid = false
    }
}