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
import org.netomi.sudoku.solver.BaseHintFinder
import org.netomi.sudoku.solver.HintAggregator
import org.netomi.sudoku.solver.SolvingTechnique

class LockedCandidatesType1Finder : BaseHintFinder
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.LOCKED_CANDIDATES_TYPE_1

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        grid.acceptBlocks { house ->
            for (value in house.unassignedValues()) {
                val possiblePositions: CellSet = house.getPotentialPositionsAsSet(value)
                if (possiblePositions.cardinality() <= 1) {
                    continue
                }
                // Check if all possible cells are in the same row.
                val row = possiblePositions.getSingleRow(grid)
                row?.let { eliminateValueFromCells(grid, hintAggregator, it, house, value) }

                // Check if all possible cells are in the same column.
                val column = possiblePositions.getSingleColumn(grid)
                column?.let { eliminateValueFromCells(grid, hintAggregator, it, house, value) }
            }
        }
    }
}

class LockedCandidatesType2Finder : BaseHintFinder
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.LOCKED_CANDIDATES_TYPE_2

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        val visitor = HouseVisitor { house ->
            for (value in house.unassignedValues()) {
                val possiblePositions: CellSet = house.getPotentialPositionsAsSet(value)
                if (possiblePositions.cardinality() <= 1) {
                    continue
                }
                // Check if all possible cells are in the same block.
                val block = possiblePositions.getSingleBlock(grid)
                block?.let { eliminateValueFromCells(grid, hintAggregator, block, house, value) }
            }
        }

        // Check all rows and columns.
        grid.acceptRows(visitor)
        grid.acceptColumns(visitor)
    }
}
