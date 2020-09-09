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

class AssignmentHint(type:             Grid.Type,
                     solvingTechnique: SolvingTechnique,
                     val cellIndex:    Int,
                     relatedCells:     CellSet,
                     val value:        Int) : Hint(type, solvingTechnique, relatedCells)
{
    override val description: String
        get() = "%s=%d".format(gridType.getCellName(cellIndex), value)

    override fun apply(targetGrid: Grid, updateGrid: Boolean) {
        targetGrid.getCell(cellIndex).setValue(value, updateGrid)
    }

    override fun revert(targetGrid: Grid, updateGrid: Boolean) {
        targetGrid.getCell(cellIndex).setValue(0, updateGrid)
    }

    override fun accept(visitor: HintVisitor) {
        visitor.visitAssignmentHint(this)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + Objects.hash(cellIndex, value)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AssignmentHint
        return super.equals(other) &&
               cellIndex    == that.cellIndex &&
               value        == that.value
    }
}

class EliminationHint(type:               Grid.Type,
                      solvingTechnique:   SolvingTechnique,
                      val matchingCells:  CellSet,
                      val matchingValues: ValueSet,
                      relatedCells:       CellSet,
                      val affectedCells:  CellSet,
                      val excludedValues: Array<ValueSet>)
    : Hint(type, solvingTechnique, relatedCells)
{
    constructor(type:             Grid.Type,
                solvingTechnique: SolvingTechnique,
                matchingCells:    CellSet,
                matchingValues:   ValueSet,
                relatedCells:     CellSet,
                affectedCells:    CellSet,
                excludedValues:   ValueSet) :
            this(type,
                 solvingTechnique,
                 matchingCells,
                 matchingValues,
                 relatedCells,
                 affectedCells,
                 expand(excludedValues, affectedCells.cardinality()))

    override val description: String
        get() {
            val values = matchingValues.allSetBits().joinToString ("/") { it.toString() }
            val cells  = matchingCells.allSetBits().joinToString { gridType.getCellName(it) }

            val eliminations = StringBuilder()
            for ((index, cellIndex) in affectedCells.allSetBits().withIndex()) {
                eliminations.append(gridType.getCellName(cellIndex))
                eliminations.append("<>")
                eliminations.append(excludedValues[index].toCollection())
                eliminations.append(", ")
            }
            eliminations.delete(eliminations.length - 2, eliminations.length)
            return "%s in %s => %s".format(values, cells, eliminations)
        }

    override fun apply(targetGrid: Grid, updateGrid: Boolean) {
        for ((index, cell) in affectedCells.allCells(targetGrid).withIndex()) {
            cell.excludePossibleValues(excludedValues[index], updateGrid)
        }
    }

    override fun revert(targetGrid: Grid, updateGrid: Boolean) {
        for ((index, cell) in affectedCells.allCells(targetGrid).withIndex()) {
            cell.removeExcludedPossibleValues(excludedValues[index], updateGrid)
        }
    }

    override fun accept(visitor: HintVisitor) {
        visitor.visitEliminationHint(this)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + Objects.hash(affectedCells)
        result = 31 * result + excludedValues.contentHashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as EliminationHint
        return super.equals(other) &&
               affectedCells == that.affectedCells &&
               excludedValues.contentEquals(that.excludedValues)
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