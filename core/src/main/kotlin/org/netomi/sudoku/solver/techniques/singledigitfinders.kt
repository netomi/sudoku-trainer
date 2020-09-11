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
import org.netomi.sudoku.solver.HintFinder
import org.netomi.sudoku.solver.SolvingTechnique

class SkyscraperFinder : BaseHintFinder
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.SKYSCRAPER

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        // TODO: implement
    }
}

/**
 * A [HintFinder] implementation that looks for a pair of a row and a column
 * which have a candidate value with only 2 possible positions left. If a pair of
 * these positions is in the same block, any cell seeing the other two cells
 * can not contain the candidate value.
 *
 * Every 2-String Kite is also a Turbot Fish (X-Chain of cell length 4). This [HintFinder]
 * implementation creates hint patterns that are easier to spot / learn, but are conceptually
 * equivalent to an X-Chain hint.
 */
class TwoStringKiteFinder : BaseHintFinder
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.TWO_STRING_KITE

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        // find rows for which a possible value has only 2 positions left.
        for (row in grid.rows()) {
            if (row.isSolved) continue

            for (candidate in row.unassignedValues()) {
                val potentialPositions = row.getPotentialPositionsAsSet(candidate)
                if (potentialPositions.cardinality() == 2) {
                    findMatchingColumn(grid, hintAggregator, row, potentialPositions, candidate)
                }
            }
        }
    }

    private fun findMatchingColumn(grid: Grid, hintAggregator: HintAggregator, row: Row, potentialRowPositions: CellSet, candidate: Int) {
        for (col in grid.columns()) {
            if (col.isSolved) continue

            val assignedValues = col.assignedValueSet
            if (assignedValues[candidate]) continue

            val potentialColPositions = col.getPotentialPositionsAsSet(candidate)
            if (potentialColPositions.cardinality() != 2) continue

            // check that the position sets are mutually exclusive.
            val combinedPositions = potentialRowPositions.toMutableCellSet()
            combinedPositions.and(potentialColPositions)
            if (combinedPositions.cardinality() != 0) continue

            checkMatchingRowAndCol(grid, hintAggregator, row, potentialRowPositions, col, potentialColPositions, candidate)
        }
    }

    private fun checkMatchingRowAndCol(grid:                  Grid,
                                       hintAggregator:        HintAggregator,
                                       row:                   Row,
                                       potentialRowPositions: CellSet,
                                       col:                   Column,
                                       potentialColPositions: CellSet,
                                       candidate:             Int)
    {
        for (rowCell in potentialRowPositions.allSetBits()) {
            for (colCell in potentialColPositions.allSetBits()) {
                val combinedSet = MutableCellSet.of(grid, rowCell, colCell)
                val block = combinedSet.getSingleBlock(grid)

                // we found a pair of cells from row / col that are in the same block.
                block?.apply {
                    val otherRowCell = potentialRowPositions.filteredSetBits { cellIndex -> cellIndex != rowCell }.first()
                    val otherColCell = potentialColPositions.filteredSetBits { cellIndex -> cellIndex != colCell }.first()

                    // find all cells that see both, the cell in the matching row and col.
                    val affectedCells = grid.getCell(otherRowCell).peerSet.toMutableCellSet()
                    affectedCells.and(grid.getCell(otherColCell).peerSet)

                    val excludedValues = MutableValueSet.of(grid, candidate)

                    val matchingCells = potentialRowPositions.toMutableCellSet()
                    matchingCells.or(potentialColPositions)

                    val relatedCells = row.cellSet.toMutableCellSet()
                    relatedCells.or(col.cellSet)

                    eliminateValuesFromCells(grid, hintAggregator, matchingCells, relatedCells, affectedCells, excludedValues)
                }
            }
        }
    }
}