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
 * A [HintFinder] implementation that looks for houses where a pair
 * of cells has the same two candidates left, forming a naked pair. The
 * same candidates in other cells of the same house can be removed.
 */
open class NakedPairFinder protected constructor(private val findLockedHouses: Boolean)
    : AbstractHintFinder()
{
    constructor() : this(false)

    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.NAKED_PAIR

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        grid.acceptHouses(object : HouseVisitor {
            override fun visitAnyHouse(house: House) {
                for (cell in house.cells()) {
                    val possibleValues = cell.possibleValues
                    if (possibleValues.cardinality() != 2) {
                        continue
                    }
                    for (otherCell in house.cells(cell.cellIndex + 1)) {
                        val otherPossibleValues = otherCell.possibleValues
                        if (otherPossibleValues.cardinality() != 2) {
                            continue
                        }

                        // If the two [CellSet]s containing the possible candidate values
                        // have the same candidates, we have found a naked pair.
                        if (possibleValues == otherPossibleValues) {
                            val affectedCells = house.cells.toMutableCellSet()

                            if (findLockedHouses) {
                                val pairCells = MutableCellSet.of(cell, otherCell)

                                val row = pairCells.getSingleRow(grid)
                                row?.let { affectedCells.or(it.cells) }

                                val col = pairCells.getSingleColumn(grid)
                                col?.let { affectedCells.or(it.cells) }
                            }

                            affectedCells.clear(cell.cellIndex)
                            affectedCells.clear(otherCell.cellIndex)
                            eliminateValuesFromCells(grid, hintAggregator, affectedCells, possibleValues)
                        }
                    }
                }
            }
        })
    }
}

/**
 * A [HintFinder] implementation that looks for houses, where a pair
 * of cells has the same two candidates left, forming a locked pair if they
 * are on the same row or column. The same candidates in other cells of the
 * same house and row / column can be removed.
 */
class LockedPairFinder : NakedPairFinder(true) {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.LOCKED_PAIR
}

/**
 * A [HintFinder] implementation that looks for houses where a subset
 * of 3 cells has the same three candidates left, forming a naked triple. The
 * candidates in other cells of the same house can be removed.
 */
class NakedTripleFinder : NakedSubsetFinder(3)
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.NAKED_TRIPLE
}

/**
 * A [HintFinder] implementation that looks for houses where a subset of 3 cells
 * has the same three candidates left, forming a naked triple if they are on the same
 * row or column. The candidates in other cells of the same house and row / columns can
 * be removed.
 */
class LockedTripleFinder : NakedSubsetFinder(3, true) {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.LOCKED_TRIPLE
}

/**
 * A [HintFinder] implementation that looks for houses where a subset
 * of 4 cells has the same four candidates left, forming a naked quadruple.
 * The candidates in other cells of the same house can be removed.
 */
class NakedQuadrupleFinder : NakedSubsetFinder(4)
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.NAKED_QUADRUPLE
}

abstract class NakedSubsetFinder protected constructor(private val subSetSize: Int, private val findLockedHouses: Boolean = false)
    : AbstractHintFinder()
{
    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        grid.acceptHouses(object : HouseVisitor {
            override fun visitAnyHouse(house: House) {
                if (house.isSolved) {
                    return
                }
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
        })
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
        allVisitedValues.or(currentCell.possibleValues)
        if (allVisitedValues.cardinality() > subSetSize) {
            return false
        }
        visitedCells.set(currentCell.cellIndex)

        if (level == subSetSize) {
            var foundHint = false
            if (allVisitedValues.cardinality() == subSetSize) {
                val affectedCells = house.cells.toMutableCellSet()

                if (findLockedHouses) {
                    val row = visitedCells.getSingleRow(grid)
                    row?.let { affectedCells.or(it.cells) }

                    val col = visitedCells.getSingleColumn(grid)
                    col?.let { affectedCells.or(it.cells) }
                }

                affectedCells.andNot(visitedCells)
                eliminateValuesFromCells(grid, hintAggregator, affectedCells, allVisitedValues)
                foundHint = true
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