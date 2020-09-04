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

/**
 * An interface used to visit a [Grid].
 */
fun interface GridVisitor<out T>
{
    fun visitGrid(grid: Grid): T
}

/**
 * An interface used to visit a [Cell].
 *
 * This interface exists only for convenience reasons.
 */
fun interface CellVisitor
{
    fun visitCell(cell: Cell)
}

/**
 * An interface used to visit a [House].
 */
fun interface HouseVisitor
{
    /**
     * By default, this method is called for any visited [House].
     */
    fun visitAnyHouse(house: House)

    /**
     * Called when visiting a row.
     *
     * By default it delegates to [visitAnyHouse].
     */
    fun visitRow(row: Row) {
        visitAnyHouse(row)
    }

    /**
     * Called when visiting a column.
     *
     * By default it delegates to [visitAnyHouse].
     */
    fun visitColumn(column: Column) {
        visitAnyHouse(column)
    }

    /**
     * Called when visiting a block.
     *
     * By default it delegates to [visitAnyHouse].
     */
    fun visitBlock(block: Block) {
        visitAnyHouse(block)
    }
}