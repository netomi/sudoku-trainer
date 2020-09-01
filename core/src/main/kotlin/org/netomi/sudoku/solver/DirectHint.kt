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
package org.netomi.sudoku.solver

import org.netomi.sudoku.model.Grid
import java.util.*

class DirectHint(type: Grid.Type, solvingTechnique: SolvingTechnique, val cellIndex: Int, val value: Int) : Hint(type, solvingTechnique) {

    override fun apply(targetGrid: Grid, updateGrid: Boolean) {
        targetGrid.getCell(cellIndex).setValue(value, updateGrid)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + Objects.hash(cellIndex, value)
        return result
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DirectHint
        return super.equals(o) &&
               cellIndex == that.cellIndex &&
               value     == that.value
    }

    fun asString(): String {
        return "%s=%d".format(gridType.getCellName(cellIndex), value)
    }

    override fun toString(): String {
        return "%s: %s=%d".format(solvingTechnique.techniqueName, gridType.getCellName(cellIndex), value)
    }
}