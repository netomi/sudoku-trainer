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

import org.netomi.sudoku.model.CellSet
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.model.ValueSet
import java.util.*

class IndirectHint(type: Grid.Type,
                   solvingTechnique: SolvingTechnique,
                   val cellIndices: CellSet,
                   val excludedValues: Array<ValueSet>) : Hint(type, solvingTechnique) {

    constructor(type: Grid.Type,
                solvingTechnique: SolvingTechnique,
                cellIndices: CellSet,
                excludedValues: ValueSet) : this(type, solvingTechnique, cellIndices, expand(excludedValues, cellIndices.cardinality()))

    override fun apply(targetGrid: Grid, updateGrid: Boolean) {
        var index = 0
        for (cell in cellIndices.allCells(targetGrid)) {
            cell.excludePossibleValues(excludedValues[index++], updateGrid)
        }
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + Objects.hash(cellIndices)
        result = 31 * result + excludedValues.contentHashCode()
        return result
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as IndirectHint
        return super.equals(o) &&
                cellIndices == that.cellIndices &&
                excludedValues.contentEquals(that.excludedValues)
    }

    override fun toString(): String {
        val eliminations = StringBuilder()
        var index = 0
        for (cellIndex in cellIndices.allSetBits()) {
            eliminations.append(gridType.getCellName(cellIndex))
            eliminations.append("<>")
            eliminations.append(excludedValues[index++].toCollection())
            eliminations.append(", ")
        }
        eliminations.delete(eliminations.length - 2, eliminations.length)
        return java.lang.String.format("%s: => %s", solvingTechnique.techniqueName, eliminations.toString())
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        private fun expand(values: ValueSet, copies: Int): Array<ValueSet> {
            val result = arrayOfNulls<ValueSet>(copies)
            for (i in 0 until copies) {
                result[i] = values
            }
            return result as Array<ValueSet>
        }
    }
}