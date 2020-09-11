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

/**
 * A [HintFinder] implementation that looks for blocks, where a certain
 * candidate value is restricted to a single row or column. The same candidate
 * can not appear in this row / column outside the block.
 */
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
                row?.let { eliminateValueFromCells(grid, hintAggregator, it, house.cellSet, house, value) }

                // Check if all possible cells are in the same column.
                val column = possiblePositions.getSingleColumn(grid)
                column?.let { eliminateValueFromCells(grid, hintAggregator, it, house.cellSet, house, value) }
            }
        }
    }
}

/**
 * A [HintFinder] implementation that looks for rows / columns where a certain
 * candidate value is constrained to a single block. The same candidate can
 * not appear in other cells of this block.
 */
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
                block?.let { eliminateValueFromCells(grid, hintAggregator, block, house.cellSet, house, value) }
            }
        }

        // Check all rows and columns.
        grid.acceptRows(visitor)
        grid.acceptColumns(visitor)
    }
}

/**
 * A [HintFinder] implementation that looks for blocks, where a pair
 * of cells has the same two candidates left, forming a locked pair if they
 * are on the same row or column. The same candidates in other cells of the
 * same block and row / column can be removed.
 */
open class LockedPairFinder : BaseHintFinder
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.LOCKED_PAIR

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        grid.acceptBlocks { house ->
            for (cell in house.cells()) {
                val possibleValues = cell.possibleValueSet
                if (possibleValues.cardinality() != 2) {
                    continue
                }
                for (otherCell in house.cells(cell.cellIndex + 1)) {
                    val otherPossibleValues = otherCell.possibleValueSet
                    if (otherPossibleValues.cardinality() != 2) {
                        continue
                    }

                    // If the two [CellSet]s containing the possible candidate values
                    // have the same candidates, we potentially have found a locked pair.
                    if (possibleValues == otherPossibleValues) {
                        val affectedCells = house.cellSet.toMutableCellSet()

                        val matchingCells = MutableCellSet.of(cell, otherCell)
                        val row = matchingCells.getSingleRow(grid)
                        row?.let { affectedCells.or(it.cellSet) }

                        val col = matchingCells.getSingleColumn(grid)
                        col?.let { affectedCells.or(it.cellSet) }

                        // if the two cells are neither on the same
                        // row or column, we have not found a locked pair
                        // (just a naked one).
                        if (row == null && col == null) {
                            continue
                        }

                        val relatedCells = affectedCells.copy()

                        affectedCells.clear(cell.cellIndex)
                        affectedCells.clear(otherCell.cellIndex)
                        eliminateValuesFromCells(grid, hintAggregator, matchingCells, relatedCells, affectedCells, possibleValues.copy())
                    }
                }
            }
        }
    }
}

/**
 * A [HintFinder] implementation that looks for blocks where a subset of 3 cells
 * has the same three candidates left, forming a locked triple if they are on the same
 * row or column. The candidates in other cells of the same block and row / columns can
 * be removed.
 */
class LockedTripleFinder : BaseHintFinder {

    private val subSetSize = 3

    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.LOCKED_TRIPLE

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        grid.acceptBlocks { house ->
            if (!house.isSolved) {
                for (cell in house.unassignedCells()) {
                    findSubset(grid,
                               hintAggregator,
                               house,
                               MutableCellSet.empty(grid),
                               cell,
                               MutableValueSet.empty(grid),
                               1)
                }
            }
        }
    }

    private fun findSubset(grid:           Grid,
                           hintAggregator: HintAggregator,
                           house:          House,
                           visitedCells:   MutableCellSet,
                           currentCell:    Cell,
                           visitedValues:  MutableValueSet,
                           level:          Int): Boolean
    {
        if (level > subSetSize) {
            return false
        }

        val allVisitedValues = visitedValues.copy()
        allVisitedValues.or(currentCell.possibleValueSet)
        if (allVisitedValues.cardinality() > subSetSize) {
            return false
        }
        visitedCells.set(currentCell.cellIndex)

        if (level == subSetSize) {
            var foundHint = false
            if (allVisitedValues.cardinality() == subSetSize) {
                val affectedCells = house.cellSet.toMutableCellSet()

                val row = visitedCells.getSingleRow(grid)
                row?.let { affectedCells.or(it.cellSet) }

                val col = visitedCells.getSingleColumn(grid)
                col?.let { affectedCells.or(it.cellSet) }

                // if the cells are either on the same
                // row or column, we have found a locked triple.
                if (row != null || col != null) {
                    val relatedCells = affectedCells.copy()

                    affectedCells.andNot(visitedCells)
                    eliminateValuesFromCells(grid, hintAggregator, visitedCells.copy(), relatedCells, affectedCells, allVisitedValues)
                    foundHint = true
                }
            }
            visitedCells.clear(currentCell.cellIndex)
            return foundHint
        }

        var foundHint = false
        for (nextCell in house.unassignedCells(currentCell.cellIndex + 1)) {
            foundHint = foundHint or findSubset(grid,
                    hintAggregator,
                    house,
                    visitedCells,
                    nextCell,
                    allVisitedValues,
                    level + 1)
        }

        visitedCells.clear(currentCell.cellIndex)
        return foundHint
    }
}
