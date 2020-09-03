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

import org.netomi.sudoku.model.*
import java.util.ArrayList

interface HintFinder {
    val solvingTechnique: SolvingTechnique
    fun findHints(grid: Grid, hintAggregator: HintAggregator)
}

internal interface BaseHintFinder : HintFinder
{
    /**
     * Adds a direct placement hint to the `HintAggregator`.
     */
    fun placeValueInCell(grid:           Grid,
                         hintAggregator: HintAggregator,
                         cellIndex:      Int,
                         value:          Int)
    {
        hintAggregator.addHint(DirectHint(grid.type,
                solvingTechnique,
                cellIndex,
                value))
    }

    /**
     * Adds an elimination hint to remove the given candidate value
     * from all cells in the affected house excluding cell in the excluded
     * house.
     *
     * @param affectedHouse the affected house for this elimination hint
     * @param excludedHouse the cells to be excluded from the affected house
     * @param excludedValue the candidate value to remove
     */
    fun eliminateValueFromCells(grid:           Grid,
                                hintAggregator: HintAggregator,
                                affectedHouse: House,
                                excludedHouse: House,
                                excludedValue:  Int)
    {
        val cellsToModify = MutableCellSet.empty(grid)
        for (cell in affectedHouse.cellsExcluding(excludedHouse)) {
            // only consider cells which have the excluded value as candidate.
            if (!cell.isAssigned &&
                    cell.possibleValueSet[excludedValue]) {
                cellsToModify.set(cell.cellIndex)
            }
        }

        val eliminations = MutableValueSet.of(grid, excludedValue)
        if (cellsToModify.cardinality() > 0) {
            hintAggregator.addHint(IndirectHint(grid.type,
                    solvingTechnique,
                    cellsToModify,
                    eliminations))
        }
    }

    /**
     * Adds an elimination hint to remove all candidate values from the affected
     * cells that are not contained in the allowedValues array.
     *
     * @param affectedCells  the set of affected cell indices
     * @param allowedValues  the allowed set of candidates in the affected cells
     */
    fun eliminateNotAllowedValuesFromCells(grid:           Grid,
                                           hintAggregator: HintAggregator,
                                           affectedCells: CellSet,
                                           allowedValues: ValueSet)
    {
        val cellsToModify = MutableCellSet.empty(grid)
        val excludedValues: MutableList<ValueSet> = ArrayList()
        for (cell in affectedCells.allCells(grid)) {
            if (!cell.isAssigned) {
                val valuesToExclude = valuesExcluding(cell.possibleValueSet, allowedValues)
                if (valuesToExclude.cardinality() > 0) {
                    cellsToModify.set(cell.cellIndex)
                    excludedValues.add(valuesToExclude)
                }
            }
        }

        if (cellsToModify.cardinality() > 0) {
            hintAggregator.addHint(IndirectHint(grid.type,
                    solvingTechnique,
                    cellsToModify,
                    excludedValues.toTypedArray()))
        }
    }

    /**
     * Adds an elimination hint to remove all candidate values from the affected
     * cells (except the excluded ones) that are contained in the excludedValues bitset.
     *
     * @param affectedCells  the affected cells for this elimination hint
     * @param excludedValues the candidate value to remove
     */
    fun eliminateValuesFromCells(grid:           Grid,
                                 hintAggregator: HintAggregator,
                                 affectedCells: CellSet,
                                 excludedValues: ValueSet): Boolean
    {
        val cellsToModify = MutableCellSet.empty(grid)
        val valuesToExcludeList: MutableList<ValueSet> = ArrayList()

        for (cell in affectedCells.allCells(grid)) {
            if (!cell.isAssigned) {
                val valuesToExclude = valuesIncluding(cell.possibleValueSet, excludedValues)
                if (valuesToExclude.cardinality() > 0) {
                    cellsToModify.set(cell.cellIndex)
                    valuesToExcludeList.add(valuesToExclude)
                }
            }
        }

        return if (cellsToModify.cardinality() > 0) {
            hintAggregator.addHint(IndirectHint(grid.type,
                    solvingTechnique,
                    cellsToModify,
                    valuesToExcludeList.toTypedArray()))
            true
        } else {
            false
        }
    }

    companion object {
        /**
         * Returns a BitSet containing all values that have been set in the given bitset
         * excluding the values contained in the excludedValues array.
         */
        private fun valuesExcluding(values: ValueSet, excludedValues: ValueSet): ValueSet {
            val result = values.toMutableValueSet()
            result.andNot(excludedValues)
            return result
        }

        /**
         * Returns an array containing all values that have been set in the given bitset
         * only including the values contained in the includedValues bitset.
         */
        private fun valuesIncluding(values: ValueSet, includedValues: ValueSet): ValueSet {
            val result = includedValues.toMutableValueSet()
            result.and(values)
            return result
        }
    }
}