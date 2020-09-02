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

/**
 * A class representing a certain region within a sudoku grid.
 *
 * Possible region types are:
 *  - row
 *  - column
 *  - block
 */
abstract class House internal constructor(private val owner: Grid, val regionIndex: Int)
{
    private val _cells: MutableCellSet = MutableCellSet.empty(owner)
    val cells: CellSet
        get() = _cells.asCellSet()

    internal val _assignedValues: MutableValueSet = MutableValueSet.empty(owner)
    val assignedValues: ValueSet
        /**
         * Returns the assigned values of all cells contained in this [House] as [ValueSet].
         */
        get() {
            owner.throwIfStateIsInvalid()
            return _assignedValues.asValueSet()
        }

    /**
     * Returns the specific type of region of this [House].
     */
    abstract val type: HouseType

    /**
     * Returns the number of cells contained in this [House].
     */
    val size: Int
        get() = _cells.cardinality()

    /**
     * Adds the given [Cell] to this [House].
     */
    internal fun addCell(cell: Cell) {
        _cells.set(cell.cellIndex)
    }

    /**
     * Checks whether the given cell, identified by its cell index,
     * is contained in this [House].
     *
     * @param cellIndex the index of the cell to check
     */
    fun containsCell(cellIndex: Int): Boolean {
        return _cells[cellIndex]
    }

    /**
     * Checks whether the given cell is contained in this [House].
     */
    fun containsCell(cell: Cell): Boolean {
        return containsCell(cell.cellIndex)
    }

    /**
     * Checks whether all cells marked in the given [CellSet] are contained
     * in this [House].
     *
     * Note: an empty input always returns true.
     *
     * @param otherCells a [CellSet] whose bits represent cells in the grid
     */
    fun containsAllCells(otherCells: CellSet): Boolean {
        return otherCells.allSetBits().all { cellIndex -> containsCell(cellIndex) }
    }

    /**
     * Returns a [Sequence] containing all cells of this [House],
     * whose cell index is >= startIndex.
     */
    fun allCells(startIndex: Int = 0): Sequence<Cell> {
        return _cells.allCells(owner, startIndex)
    }

    /**
     * Returns a [Sequence] containing all assigned cells of this [House],
     * whose cell index is >= startIndex.
     */
    fun assignedCells(startIndex: Int = 0): Sequence<Cell> {
        return _cells.filteredCells(owner, { obj: Cell -> obj.isAssigned }, startIndex)
    }

    /**
     * Returns a [Sequence] containing all unassigned cells of this [House],
     * whose cell index is >= startIndex.
     */
    fun unassignedCells(startIndex: Int = 0): Sequence<Cell> {
        return _cells.filteredCells(owner, { cell -> !cell.isAssigned }, startIndex)
    }

    /**
     * Returns a [Sequence] containing all cells of this [House]
     * excluding all cells contained in the provided houses.
     */
    fun cellsExcluding(vararg excludedHouses: House): Sequence<Cell> {
        val ownCells = cells.toMutableCellSet()
        for (house in excludedHouses) {
            ownCells.andNot(house._cells)
        }
        return ownCells.allCells(owner)
    }

    /**
     * Returns an [Sequence] containing all cells of this [House]
     * excluding all cells contained in the provided [CellSet].
     */
    fun cellsExcluding(excludedCells: CellSet): Sequence<Cell> {
        val ownCells = cells.toMutableCellSet()
        ownCells.andNot(excludedCells)
        return ownCells.allCells(owner)
    }

    /**
     * Checks whether all cells in this [House] have assigned unique values.
     */
    val isValid: Boolean
        get() {
            val foundValues = BitSet()
            for (cell in assignedCells()) {
                if (!foundValues[cell.value]) {
                    foundValues.set(cell.value)
                } else {
                    return false
                }
            }
            return true
        }

    /**
     * Checks whether all cells in this [House] have unique values assigned.
     */
    val isSolved: Boolean
        get() = assignedValues.cardinality() == owner.gridSize

    fun assignedValues(): Iterable<Int> {
        return assignedValues.allSetBits()
    }

    fun unassignedValues(): Iterable<Int> {
        return assignedValues.allUnsetBits()
    }

    fun unassignedValues(startValue: Int): Iterable<Int> {
        return assignedValues.allUnsetBits(startValue)
    }

    /**
     * Returns a [Sequence] containing all cells of this `#House`
     * that can potentially be assigned to the given value.
     *
     * @param value the value to check for
     */
    fun potentialCells(value: Int): Sequence<Cell> {
        return getPotentialPositionsAsSet(value).allCells(owner)
    }

    fun getPotentialPositionsAsSet(value: Int): CellSet {
        owner.throwIfStateIsInvalid()
        val possiblePositions = owner.getPotentialPositions(value)
        val result = possiblePositions.toMutableCellSet()
        result.and(_cells)
        return result
    }

    internal fun updateAssignedValues() {
        _assignedValues.clearAll()
        for (cell in assignedCells()) {
            _assignedValues.set(cell.value)
        }
    }

    internal fun updatePossibleValuesInCell(cell: Cell) {
        cell.updatePossibleValues(_assignedValues)
    }

    internal fun updatePossibleValuesInCells() {
        unassignedCells().forEach { cell -> cell.updatePossibleValues(_assignedValues) }
    }

    internal fun clear() {
        _assignedValues.clearAll()
    }
}

/**
 * An enumeration of possible types of houses (regions) as contained
 * in a sudoku grid.
 */
enum class HouseType {
    ROW, COLUMN, BLOCK
}

class Row internal constructor(owner: Grid, rowIndex: Int) : House(owner, rowIndex)
{
    override val type: HouseType
        get() = HouseType.ROW

    val rowNumber: Int
        get() = regionIndex + 1

    override fun toString(): String {
        return "r%d = %s".format(rowNumber, _assignedValues)
    }
}

class Column internal constructor(owner: Grid, columnIndex: Int) : House(owner, columnIndex)
{
    override val type: HouseType
        get() = HouseType.COLUMN

    val columnNumber: Int
        get() = regionIndex + 1

    override fun toString(): String {
        return "c%d = %s".format(columnNumber, _assignedValues)
    }
}

class Block internal constructor(owner: Grid, blockIndex: Int) : House(owner, blockIndex)
{
    override val type: HouseType
        get() = HouseType.BLOCK

    val blockNumber: Int
        get() = regionIndex + 1

    override fun toString(): String {
        return "b%d = %s".format(blockNumber, _assignedValues)
    }
}
