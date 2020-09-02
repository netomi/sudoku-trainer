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
import kotlin.NoSuchElementException
import kotlin.streams.asSequence
import kotlin.streams.toList

interface SimpleBitSet
{
    val bits: BitSet
    val size: Int

    val firstBitIndex: Int
    val lastBitIndex:  Int

    fun cardinality(): Int

    operator fun get(bit: Int): Boolean

    fun firstSetBit(): Int
    fun firstUnsetBit(): Int

    fun previousSetBit(startBit: Int): Int

    fun allSetBits(): Iterable<Int>
    fun allUnsetBits(): Iterable<Int>
    fun allUnsetBits(startBit: Int): Iterable<Int>

    fun toCollection(): Collection<Int>
    fun toArray(): IntArray
}

interface MutableBitSet<in T, out R> : SimpleBitSet
{
    fun setAll()
    fun clearAll()

    fun set(bit: Int)
    fun clear(bit: Int)

    fun and(other: T)
    fun or(other: T)
    fun andNot(other: T)

    fun copy(): R
}

abstract class AbstractBitSetImpl<in T : SimpleBitSet, out R>(final override val size: Int) : MutableBitSet<T, R>
{
    override val bits: BitSet = BitSet(size)

    protected open val offset: Int
        get() = 0

    protected open fun checkInput(bit: Int) {
        require(!(bit < 0 || bit >= size)) { "illegal value $bit" }
    }

    override val firstBitIndex: Int
        get() = offset

    override val lastBitIndex: Int
        get() = size - 1

    override fun cardinality(): Int {
        return bits.cardinality()
    }

    override fun setAll() {
        bits.set(offset, size)
    }

    override fun clearAll() {
        bits.clear(offset, size)
    }

    override operator fun get(bit: Int): Boolean {
        checkInput(bit)
        return bits[bit]
    }

    override fun set(bit: Int) {
        checkInput(bit)
        bits.set(bit)
    }

    override fun clear(bit: Int) {
        checkInput(bit)
        bits.clear(bit)
    }

    override fun firstSetBit(): Int {
        return bits.nextSetBit(offset)
    }

    override fun firstUnsetBit(): Int {
        return bits.nextClearBit(offset)
    }

    override fun previousSetBit(startBit: Int): Int {
        return bits.previousSetBit(startBit)
    }

    override fun allSetBits(): Iterable<Int> {
        return Iterable { BitIterator(offset, size, false) }
    }

    override fun allUnsetBits(): Iterable<Int> {
        return Iterable { BitIterator(offset, size, true) }
    }

    override fun allUnsetBits(startBit: Int): Iterable<Int> {
        return Iterable { BitIterator(startBit, size, true) }
    }

    override fun and(other: T) {
        bits.and(other.bits)
    }

    override fun or(other: T) {
        bits.or(other.bits)
    }

    override fun andNot(other: T) {
        bits.andNot(other.bits)
    }

    override fun toCollection(): Collection<Int> {
        return bits.stream().toList()
    }

    override fun toArray(): IntArray {
        return bits.stream().toArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractBitSetImpl<*, *>) return false
        return size == other.size &&
               bits == other.bits
    }

    override fun hashCode(): Int {
        return Objects.hash(size, bits)
    }

    override fun toString(): String {
        return bits.toString()
    }

    // inner helper classes.

    private inner class BitIterator : Iterator<Int>
    {
        private val toIndex: Int
        private val inverse: Boolean

        private var nextOffset: Int

        constructor(fromIndex: Int, toIndex: Int, inverse: Boolean) {
            this.toIndex = toIndex
            this.inverse = inverse
            nextOffset = nextBit(fromIndex)
        }

        private fun nextBit(offset: Int): Int {
            return when (inverse) {
                true  -> bits.nextClearBit(offset)
                false -> bits.nextSetBit(offset)
            }
        }

        override fun hasNext(): Boolean {
            return nextOffset in 0 until toIndex
        }

        override fun next(): Int {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            val bit    = nextOffset
            nextOffset = nextBit(nextOffset + 1)
            return bit
        }
    }
}

interface ValueSet : SimpleBitSet
{
    fun toMutableValueSet(): MutableValueSet {
        return MutableValueSet(this)
    }
}

class MutableValueSet : AbstractBitSetImpl<ValueSet, MutableValueSet>, ValueSet
{
    private constructor(valueRange: Int) : super(valueRange + 1)

    internal constructor(other: ValueSet) : super(other.size) {
        or(other)
    }

    override val offset: Int
        get() = 1

    override fun checkInput(bit: Int) {
        require(!(bit < 1 || bit >= size)) { "illegal value $bit" }
    }

    override fun copy(): MutableValueSet {
        return MutableValueSet(this)
    }

    fun asValueSet(): ValueSet {
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other == null || javaClass != other.javaClass) false else super.equals(other)
    }

    companion object {
        fun empty(grid: Grid): MutableValueSet {
            return MutableValueSet(grid.gridSize)
        }

        fun of(grid: Grid, vararg values: Int): MutableValueSet {
            val valueSet = empty(grid)
            for (value in values) {
                valueSet.set(value)
            }
            return valueSet
        }

        fun fullySet(grid: Grid): MutableValueSet {
            val valueSet = empty(grid)
            valueSet.setAll()
            return valueSet
        }
    }
}

class MutableHouseSet : AbstractBitSetImpl<MutableHouseSet, MutableHouseSet>
{
    private constructor(size: Int) : super(size)

    private constructor(other: MutableHouseSet) : super(other.size) {
        or(other)
    }

    override fun copy(): MutableHouseSet {
        return MutableHouseSet(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other == null || javaClass != other.javaClass) false else super.equals(other)
    }

    companion object {
        fun empty(grid: Grid): MutableHouseSet {
            return MutableHouseSet(grid.gridSize)
        }
    }
}

interface CellSet : SimpleBitSet
{
    fun allCells(grid: Grid, startIndex: Int = 0): Sequence<Cell> {
        return bits.stream()
                   .filter   { idx -> idx >= startIndex }
                   .mapToObj { cellIndex -> grid.getCell(cellIndex) }
                   .asSequence()
    }

    fun filteredCells(grid: Grid, predicate: (Cell) -> Boolean, startIndex: Int = 0): Sequence<Cell> {
        return bits.stream()
                   .filter   { idx -> idx >= startIndex }
                   .mapToObj { cellIndex -> grid.getCell(cellIndex) }
                   .filter(predicate)
                   .asSequence()
    }

    fun toRowSet(grid: Grid): MutableHouseSet {
        val rows = MutableHouseSet.empty(grid)
        allCells(grid).forEach { cell -> rows.set(cell.rowIndex) }
        return rows
    }

    /**
     * Returns the row in which the cells are contained if all cells
     * are contained in the same row.
     *
     * @return the row all cells are contained in, or `null` if the
     * cells are contained in different rows.
     */
    fun getSingleRow(grid: Grid): Row? {
        val rows = toRowSet(grid)
        return if (rows.cardinality() == 1) grid.getRow(rows.firstSetBit()) else null
    }

    fun toColumnSet(grid: Grid): MutableHouseSet {
        val columns = MutableHouseSet.empty(grid)
        allCells(grid).forEach { cell -> columns.set(cell.columnIndex) }
        return columns
    }

    /**
     * Returns the column in which the cells are contained if all cells
     * are contained in the same column.
     *
     * @return the column all cells are contained in, or `null` if the
     * cells are contained in different columns.
     */
    fun getSingleColumn(grid: Grid): Column? {
        val columns = toColumnSet(grid)
        return if (columns.cardinality() == 1) grid.getColumn(columns.firstSetBit()) else null
    }

    fun toBlockSet(grid: Grid): MutableHouseSet {
        val blocks = MutableHouseSet.empty(grid)
        allCells(grid).forEach { cell -> blocks.set(cell.blockIndex) }
        return blocks
    }

    /**
     * Returns the block in which the cells are contained if all cells
     * are contained in the same block.
     *
     * @return the block all cells are contained in, or `null` if the
     * cells are contained in different blocks.
     */
    fun getSingleBlock(grid: Grid): Block? {
        val blocks = toBlockSet(grid)
        return if (blocks.cardinality() == 1) grid.getBlock(blocks.firstSetBit()) else null
    }

    fun toCellList(grid: Grid): MutableList<Cell> {
        return allCells(grid).toMutableList()
    }

    fun toMutableCellSet(): MutableCellSet {
        return MutableCellSet(this)
    }
}

class MutableCellSet : AbstractBitSetImpl<CellSet, MutableCellSet>, CellSet
{
    private constructor(size: Int) : super(size)

    internal constructor(other: CellSet) : super(other.size) {
        or(other)
    }

    override fun copy(): MutableCellSet {
        return MutableCellSet(this)
    }

    fun asCellSet(): CellSet {
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other == null || javaClass != other.javaClass) false else super.equals(other)
    }

    companion object {
        fun empty(grid: Grid): MutableCellSet {
            return MutableCellSet(grid.cellCount)
        }

        fun of(grid: Grid, cells: Sequence<Cell>): MutableCellSet {
            val cellSet = empty(grid)
            cells.forEach { cell -> cellSet.set(cell.cellIndex) }
            return cellSet
        }

        fun of(cell: Cell, vararg otherCells: Cell): MutableCellSet {
            val cellSet = empty(cell.owner)
            cellSet.set(cell.cellIndex)
            otherCells.forEach { otherCell -> cellSet.set(otherCell.cellIndex) }
            return cellSet
        }

        fun of(grid: Grid, vararg cellIndices: Int): MutableCellSet {
            val cellSet = empty(grid)
            for (cellIndex in cellIndices) {
                cellSet.set(cellIndex)
            }
            return cellSet
        }
    }
}