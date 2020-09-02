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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GridTest {
    @Test
    fun cellIterator() {
        val grid = Grid.of(PredefinedType.CLASSIC_9x9)
        val row  = grid.getRow(0)

        assertEquals(grid.gridSize, countItems(row.allCells()))
        grid.getCell(0).value = 1
        assertEquals(grid.gridSize, countItems(row.allCells()))
        assertEquals(grid.gridSize - 1, countItems(row.unassignedCells()))
        assertEquals(1, countItems(row.unassignedCells(8)))

        for (i in 0 until grid.gridSize) {
            grid.getCell(i).value = i + 1
        }

        assertEquals(grid.gridSize, countItems(row.allCells()))
        assertEquals(0, countItems(row.unassignedCells()))
        assertEquals(0, countItems(row.unassignedCells(8)))
    }

    companion object {
        private fun <T> countItems(sequence: Sequence<T>): Int {
            return sequence.count()
        }
    }
}