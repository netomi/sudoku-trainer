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

class Conflict(val cells: CellSet)
{
    operator fun contains(cell: Cell): Boolean {
        return cells[cell.cellIndex]
    }

    override fun toString(): String {
        val sb = StringBuilder()
        val firstCell = cells.firstSetBit()
        sb.append("cell[$firstCell]")
        for (idx in cells.allSetBits(firstCell + 1)) {
            sb.append(" == ")
            sb.append("cell[$idx]")

        }
        return sb.toString()
    }

    init {
        require(cells.size >= 2) { "cells must contain at least 2 elements" }
    }
}

