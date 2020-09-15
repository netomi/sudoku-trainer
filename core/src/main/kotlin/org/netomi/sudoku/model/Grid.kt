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

import kotlin.properties.Delegates

class Grid
{
    val type: Type

    val gridSize: Int
        get() = type.gridSize

    val cellCount: Int
        get() = type.cellCount

    private val cells: MutableList<Cell>
    private val peerSets: Array<MutableCellSet>

    private val rows:    MutableList<Row>
    private val columns: MutableList<Column>
    private val blocks:  MutableList<Block>

    private val cellSets: Array<MutableCellSet>

    private val potentialPositions: Array<MutableCellSet>

    private var stateValid: Boolean by Delegates.observable(false) { _, _, newValue ->
        if (newValue) _onUpdate.invoke()
    }

    private var _onUpdate: () -> Unit = {}
    fun onUpdate(target: () -> Unit) {
        _onUpdate = target
    }

    internal constructor(type: Type) {
        this.type = type

        val gridSize  = type.gridSize
        val cellCount = type.cellCount

        cells    = ArrayList(cellCount)
        peerSets = Array(cellCount) { MutableCellSet.empty(this) }

        rows     = ArrayList(gridSize)
        columns  = ArrayList(gridSize)
        blocks   = ArrayList(gridSize)
        cellSets = Array(3 * gridSize) { MutableCellSet.empty(this) }

        var houseIndex = 0
        for (i in 0 until gridSize) {
            rows.add(Row(this, i, houseIndex++))
            columns.add(Column(this, i, houseIndex++))
            blocks.add(Block(this, i, houseIndex++))
        }

        for (i in 0 until cellCount) {
            val rowIndex    = type.getRowIndex(i)
            val columnIndex = type.getColumnIndex(i)
            val blockIndex  = type.getBlockIndex(i)

            val cell = Cell(this, i, rowIndex, columnIndex, blockIndex)
            cells.add(cell)

            addCell(rows[rowIndex].houseIndex, cell)
            addCell(columns[columnIndex].houseIndex, cell)
            if (blockIndex >= 0) {
                addCell(blocks[blockIndex].houseIndex, cell)
            }
        }

        houses().forEach { house: House -> house.cells().forEach { cell: Cell -> addPeers(cell, house.cellSet) } }

        potentialPositions = Array(gridSize) { MutableCellSet.empty(this) }

        stateValid = true
    }

    /**
     * Copy constructor for grids.
     */
    internal constructor(other: Grid) {
        type = other.type

        cells   = ArrayList(cellCount)
        rows    = ArrayList(gridSize)
        columns = ArrayList(gridSize)
        blocks  = ArrayList(gridSize)

        // structural components are immutable and can be re-used
        peerSets = other.peerSets
        cellSets = other.cellSets

        // Copy houses
        other.rows.forEach    { rows.add(it.copy(this)) }
        other.columns.forEach { columns.add(it.copy(this)) }
        other.blocks.forEach  { blocks.add(it.copy(this)) }

        // Copy cells
        other.cells.forEach { cells.add(it.copy(this)) }

        potentialPositions = Array(gridSize) { MutableCellSet.empty(this) }

        stateValid = false

        updateState()
    }

    fun copy(): Grid {
        return Grid(this)
    }

    fun cells(): Iterable<Cell> {
        return cells
    }

    fun assignedCells(): Sequence<Cell> {
        return cells.asSequence()
                    .filter { cell -> cell.isAssigned }
    }

    fun unassignedCells(): Sequence<Cell> {
        return cells.asSequence()
                    .filter { cell -> !cell.isAssigned }
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

    internal fun getPeerSet(cellIndex: Int): CellSet {
        return peerSets[cellIndex]
    }

    private fun addPeers(cell: Cell, cells: CellSet) {
        val cellIndex = cell.cellIndex
        val peerSet   = peerSets[cellIndex]
        peerSet.or(cells)
        peerSet.clear(cell.cellIndex)
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

    internal fun getCellSet(houseIndex: Int): CellSet {
        return cellSets[houseIndex]
    }

    /**
     * Adds the given [Cell] to this [House].
     */
    private fun addCell(houseIndex: Int, cell: Cell) {
        cellSets[houseIndex].set(cell.cellIndex)
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

    val conflicts: Array<Conflict>
        get() = accept(ConflictDetector())

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
        // If the value did not really change, there is nothing to do.
        if (oldValue == newValue) {
            stateValid = true
            return
        }

        // First: update assigned values in affected houses.
        cell.row   .updateAssignedValues()
        cell.column.updateAssignedValues()
        cell.block .updateAssignedValues()

        // Second: update possible values in affected cells.
        for (affectedCell in (sequenceOf(cell) + cell.peers())) {
            affectedCell.resetPossibleValues()
            affectedCell.updatePossibleValues(affectedCell.row._assignedValueSet)
            affectedCell.updatePossibleValues(affectedCell.column._assignedValueSet)
            affectedCell.updatePossibleValues(affectedCell.block._assignedValueSet)
        }
        // Third: update potential positions for affected cells.
        val peers = cell.peerSet
        for (positions in potentialPositions) {
            positions.clear(cell.cellIndex)
            positions.andNot(peers)
        }

        for (affectedCell in (sequenceOf(cell) + cell.peers())) {
            for (value in affectedCell._possibleValueSet.allSetBits()) {
                potentialPositions[value - 1].set(affectedCell.cellIndex)
            }
        }

        stateValid = true
    }

    internal fun notifyPossibleValuesChanged(cell: Cell) {
        potentialPositions.forEach { potentialPosition -> potentialPosition.clear(cell.cellIndex) }
        cell._possibleValueSet.allSetBits().forEach { value -> potentialPositions[value - 1].set(cell.cellIndex) }
        stateValid = true
    }

    internal fun invalidateState() {
        stateValid = false
    }

    internal fun throwIfStateIsInvalid() {
        check(stateValid) { "cache data is invalidated, need to call refreshCache() before accessing cached data" }
    }

    internal fun getPotentialPositions(value: Int): CellSet {
        return potentialPositions[value - 1].asCellSet()
    }

    fun updateState() {
        if (stateValid) {
            return
        }

        // First: reset the possible values in all cells.
        cells().forEach { obj -> obj.resetPossibleValues() }

        // Second: refresh all assigned values in each house.
        houses().forEach { obj -> obj.updateAssignedValues() }

        // Third: remove potential values in each cell which
        //        are already assigned in the houses it is contained.
        houses().forEach { obj -> obj.updatePossibleValuesInCells() }

        // Fourth: refresh all possible positions for each cell.
        potentialPositions.forEach { obj -> obj.clearAll() }
        for (cell in cells()) {
            for (value in cell._possibleValueSet.allSetBits()) {
                potentialPositions[value - 1].set(cell.cellIndex)
            }
        }

        stateValid = true
    }

    fun clear(updateGrid: Boolean = true) {
        cells().forEach  { cell: Cell -> cell.clear(false) }
        houses().forEach { obj: House -> obj.clear() }

        potentialPositions.forEach { obj: MutableCellSet -> obj.clearAll() }

        if (updateGrid) {
            updateState()
        }
    }

    fun reset(updateGrid: Boolean = true) {
        cells().forEach { obj: Cell -> obj.reset() }

        if (updateGrid) {
            updateState()
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Grid [").append(type).append("]:\n")
        cells().forEach { cell: Cell -> sb.append("  ").append(cell).append("\n") }
        return sb.toString()
    }

    // Inner helper classes.
    fun interface BlockFunction
    {
        fun getBlockIndex(cellIndex: Int): Int
    }

    class Type constructor(val gridSize: Int, private val blockFunction: BlockFunction)
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
            return "r${getRowIndex(cellIndex) + 1}c${getColumnIndex(cellIndex) + 1}"
        }

        override fun toString(): String {
            return "${gridSize}x${gridSize}"
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
}
