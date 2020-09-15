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
import java.lang.IllegalArgumentException

/**
 * A [HintFinder] implementation ...
 */
class UniqueRectangleType1Finder : BaseUniqueRectangleHintFinder()
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.UNIQUE_RECTANGLE_TYPE_1

    override fun foundPossibleUniqueRectangle(grid: Grid, hintAggregator: HintAggregator, ur: UR) {
        val expectedPossibleValues = ur.cellsInFirstHouse.first().possibleValueSet

        var cellWithEqualCandidates: Cell? = null
        var cellWithAdditionalCandidates: Cell? = null

        for (cell in ur.cellsInSecondHouse) {
            val possibleValues = cell.possibleValueSet

            if (possibleValues == expectedPossibleValues) {
                when (cellWithEqualCandidates) {
                    null -> cellWithEqualCandidates = cell
                    else -> return
                }
            } else {
                val combinedValues = possibleValues.toMutableValueSet()
                combinedValues.andNot(expectedPossibleValues)
                if (combinedValues.cardinality() > 0) {
                    when (cellWithAdditionalCandidates) {
                        null -> cellWithAdditionalCandidates = cell
                        else -> return
                    }
                }
            }
        }

        // we have found an UR of type 1
        if (cellWithEqualCandidates != null && cellWithAdditionalCandidates != null) {
            val matchingCells = MutableCellSet.of(grid, (ur.cellsInFirstHouse + ur.cellsInSecondHouse).asSequence())

            eliminateValuesFromCells(grid,
                    hintAggregator,
                    matchingCells,
                    expectedPossibleValues.copy(),
                    MutableCellSet.empty(grid),
                    MutableCellSet.of(cellWithAdditionalCandidates),
                    expectedPossibleValues.copy())
        }
    }
}

/**
 * A [HintFinder] implementation ...
 */
class UniqueRectangleType2Finder : BaseUniqueRectangleHintFinder()
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.UNIQUE_RECTANGLE_TYPE_2

    override fun foundPossibleUniqueRectangle(grid: Grid, hintAggregator: HintAggregator, ur: UR) {
        val expectedPossibleValues = ur.cellsInFirstHouse.first().possibleValueSet

        var additionalCandidate: Int? = null

        for (cell in ur.cellsInSecondHouse) {
            val possibleValues = cell.possibleValueSet

            val excludedValues = possibleValues.toMutableValueSet()
            excludedValues.andNot(expectedPossibleValues)

            if (excludedValues.cardinality() == 1)
            {
                val remainingCandidate = excludedValues.firstSetBit()

                if (additionalCandidate == null) {
                    additionalCandidate = remainingCandidate
                } else {
                    if (remainingCandidate != additionalCandidate) {
                        return
                    }
                }
            } else {
                return
            }
        }

        // we have found an UR of type 2
        additionalCandidate?.apply {
            val matchingCells = MutableCellSet.of(grid, (ur.cellsInFirstHouse + ur.cellsInSecondHouse).asSequence())

            val affectedCells = MutableCellSet.empty(grid)
            affectedCells.setAll()

            for (cell in ur.cellsInSecondHouse) {
                affectedCells.and(cell.peerSet)
            }

            eliminateValuesFromCells(grid,
                    hintAggregator,
                    matchingCells,
                    expectedPossibleValues.copy(),
                    affectedCells,
                    affectedCells,
                    MutableValueSet.of(grid, this))
        }
    }
}

/**
 * A [HintFinder] implementation ...
 */
class UniqueRectangleType4Finder : BaseUniqueRectangleHintFinder()
{
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.UNIQUE_RECTANGLE_TYPE_4

    override fun foundPossibleUniqueRectangle(grid: Grid, hintAggregator: HintAggregator, ur: UR) {
        val expectedPossibleValues = ur.cellsInFirstHouse.first().possibleValueSet

        val foundCandidateValues = MutableValueSet.empty(grid)

        for (cell in ur.cellsInSecondHouse) {
            val possibleValues = cell.possibleValueSet

            // the UR must have at least 1 UR candidate value.
            var excludedValues = possibleValues.toMutableValueSet()
            excludedValues.and(expectedPossibleValues)
            if (excludedValues.cardinality() == 0) return
            foundCandidateValues.or(excludedValues)

            // the UR cells must have at least 1 extra value.
            excludedValues = possibleValues.toMutableValueSet()
            excludedValues.andNot(expectedPossibleValues)
            if (excludedValues.cardinality() == 0) return
        }

        if (foundCandidateValues.cardinality() != 2) return

        for (candidate in expectedPossibleValues.allSetBits()) {
            val cellsWithExtraCandidates = MutableCellSet.of(grid, ur.cellsInSecondHouse.asSequence())

            val column = cellsWithExtraCandidates.getSingleColumn(grid)
            val row = cellsWithExtraCandidates.getSingleRow(grid)
            val block = cellsWithExtraCandidates.getSingleBlock(grid)

            val searchPositions: (House) -> Unit = {
                val potentialPositions = it.getPotentialPositionsAsSet(candidate).toMutableCellSet()
                potentialPositions.andNot(cellsWithExtraCandidates)
                if (potentialPositions.cardinality() == 0) {
                    val matchingCells = MutableCellSet.of(grid, (ur.cellsInFirstHouse + ur.cellsInSecondHouse).asSequence())

                    val excludedValues = expectedPossibleValues.toMutableValueSet()
                    excludedValues.clear(candidate)

                    eliminateValuesFromCells(grid,
                                             hintAggregator,
                                             matchingCells,
                                             expectedPossibleValues.copy(),
                                             it.cellSet.copy(),
                                             cellsWithExtraCandidates,
                                             excludedValues)
                }
            }

            column?.apply(searchPositions)
            row?.apply(searchPositions)
            block?.apply(searchPositions)
        }
    }
}

abstract class BaseUniqueRectangleHintFinder : BaseHintFinder
{
    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        val visitor = HouseVisitor {  house ->
            for (cell in house.unassignedCells()) {
                val possibleValues = cell.possibleValueSet
                if (possibleValues.cardinality() == 2) {
                    for (otherCell in house.unassignedCells(cell.cellIndex + 1)) {
                        val otherPossibleValues = otherCell.possibleValueSet
                        if (otherPossibleValues != possibleValues) continue

                        findPossibleUniqueRectangle(grid, hintAggregator, house, cell, otherCell)
                    }
                }
            }
        }

        grid.acceptRows(visitor)
        grid.acceptColumns(visitor)
    }

    private fun findPossibleUniqueRectangle(grid: Grid, hintAggregator: HintAggregator, house: House, cell: Cell, otherCell: Cell) {
        val expectedPossibleValues = cell.possibleValueSet

        for (correspondingHouse in getCorrespondingHouses(grid, house)) {
            val correspondingCells = mutableListOf<Cell>()

            for (referenceCell in sequenceOf(cell, otherCell)) {
                val correspondingCell = getCorrespondingCellInOtherHouse(referenceCell, correspondingHouse)

                val possibleValues = correspondingCell.possibleValueSet
                if (possibleValues == expectedPossibleValues ||
                    possibleValues.intersects(expectedPossibleValues)) {
                    correspondingCells.add(correspondingCell)
                }
            }

            if (correspondingCells.size == 2) {
                val urCellSet = MutableCellSet.of(cell, otherCell, correspondingCells[0], correspondingCells[1])
                if (urCellSet.toBlockSet(grid).cardinality() == 2) {
                    foundPossibleUniqueRectangle(grid, hintAggregator, UR(mutableListOf(cell, otherCell), correspondingCells))
                }
            }
        }
    }

    protected abstract fun foundPossibleUniqueRectangle(grid: Grid, hintAggregator: HintAggregator, ur: UR)

    private fun getCorrespondingHouses(grid: Grid, house: House): Sequence<House> {
        return when(house.type) {
            HouseType.ROW    -> grid.rows()
            HouseType.COLUMN -> grid.columns()
            else             -> error("unexpected house type ${house.type}")
        }.asSequence().filter { it. regionIndex != house.regionIndex }
    }

    private fun getCorrespondingCellInOtherHouse(cell: Cell, house: House): Cell {
        return when (house.type) {
            HouseType.ROW    -> house.cells().filter { it.columnIndex == cell.columnIndex }
            HouseType.COLUMN -> house.cells().filter { it.rowIndex == cell.rowIndex }
            else             -> error("unexpected house type ${house.type}")
        }.first()
    }

    protected class UR(val cellsInFirstHouse: MutableList<Cell>, val cellsInSecondHouse: MutableList<Cell>)
}