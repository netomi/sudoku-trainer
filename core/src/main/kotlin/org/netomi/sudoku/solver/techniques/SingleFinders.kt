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
package org.netomi.sudoku.solver.techniques

import org.netomi.sudoku.model.*
import org.netomi.sudoku.solver.HintAggregator
import org.netomi.sudoku.solver.HintFinder
import org.netomi.sudoku.solver.SolvingTechnique

/**
 * A [HintFinder] implementation to look for houses which have a
 * single missing digit to place.
 */
class FullHouseFinder : AbstractHintFinder() {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.FULL_HOUSE

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        val expectedCardinality = grid.gridSize - 1
        grid.acceptHouses { house ->
            val assignedValues = house.assignedValueSet
            if (assignedValues.cardinality() == expectedCardinality) {
                val value = assignedValues.firstUnsetBit()
                // Create a hint for all unassigned cells.
                for (cell in house.unassignedCells()) {
                    placeValueInCell(grid, hintAggregator, cell.cellIndex, value)
                }
            }
        }
    }
}

/**
 * A [HintFinder] implementation that looks for houses where
 * a certain digit can only be placed in a single cell anymore.
 */
class HiddenSingleFinder : AbstractHintFinder() {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.HIDDEN_SINGLE

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        grid.acceptHouses { house ->
            for (value in house.unassignedValues()) {
                val possiblePositions: CellSet = house.getPotentialPositionsAsSet(value)
                if (possiblePositions.cardinality() == 1) {
                    val cellIndex = possiblePositions.firstSetBit()
                    placeValueInCell(grid, hintAggregator, cellIndex, value)
                }
            }
        }
    }
}

/**
 * A [HintFinder] implementation that checks if digit can only
 * be placed in a single cell within a specific house.
 */
class NakedSingleFinder : AbstractHintFinder() {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.NAKED_SINGLE

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        grid.acceptCells { cell ->
            if (!cell.isAssigned) {
                val possibleValues = cell.possibleValueSet
                if (possibleValues.cardinality() == 1) {
                    val value = possibleValues.firstSetBit()
                    placeValueInCell(grid, hintAggregator, cell.cellIndex, value)
                }
            }
        }
    }
}