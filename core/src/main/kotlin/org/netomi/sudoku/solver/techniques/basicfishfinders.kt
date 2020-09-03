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
import java.util.ArrayList

class XWingHintFinder : BasicFishFinder(2) {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.X_WING
}

class SwordFishFinder : BasicFishFinder(3) {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.SWORDFISH
}

class JellyFishFinder : BasicFishFinder(4) {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.JELLYFISH
}

/**
 * A [HintFinder] implementation that looks for houses
 * where a subset of candidates is constrained to some cells,
 * forming a hidden subset. All other candidates in these cells
 * can be removed.
 */
abstract class BasicFishFinder protected constructor(private val size: Int) : BaseHintFinder
{
    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        val visitor = HouseVisitor { house ->
            if (!house.isSolved) {
                for (value in house.unassignedValues()) {
                    findBaseSet(grid,
                                hintAggregator,
                                ArrayList(),
                                house,
                                value,
                                MutableHouseSet.empty(grid),
                                1)
                }
            }
        }
        grid.acceptRows(visitor)
        grid.acceptColumns(visitor)
    }

    private fun findBaseSet(grid:           Grid,
                            hintAggregator: HintAggregator,
                            visitedRegions: MutableList<House>,
                            house:          House,
                            value:          Int,
                            coverSet:       MutableHouseSet,
                            level:          Int): Boolean {
        if (level > size) {
            return false
        }

        val potentialPositions: CellSet = house.getPotentialPositionsAsSet(value)
        if (potentialPositions.cardinality() > size) {
            return false
        }

        val mergedCoverSet = coverSet.copy()
        mergedCoverSet.or(getCoverSet(grid, house, potentialPositions))
        if (mergedCoverSet.cardinality() > size) {
            return false
        }

        visitedRegions.add(house)
        if (level == size) { // get affected cells from cover sets.
            val affectedCells = getCellsOfCoverSet(grid, house.type, mergedCoverSet)
            // remove all cells from base sets.
            for (row in visitedRegions) {
                affectedCells.andNot(row.cellSet)
            }
            val excludedValue = MutableValueSet.of(grid, value)
            // eliminate the detected fish value from all affected cells,
            // affected cells = cells of cover set - cells of base set
            eliminateValuesFromCells(grid, hintAggregator, affectedCells, excludedValue)
            visitedRegions.removeAt(visitedRegions.size - 1)
            return true
        }

        var foundHint = false
        for (nextHouse in grid.regionsAfter(house)) {
            if (!nextHouse.isSolved &&
                !nextHouse.assignedValueSet[value]) {
                foundHint = foundHint or findBaseSet(grid, hintAggregator, visitedRegions, nextHouse, value, mergedCoverSet, level + 1)
            }
        }
        visitedRegions.removeAt(visitedRegions.size - 1)
        return foundHint
    }

    private fun getCoverSet(grid: Grid, house: House, potentialPositions: CellSet): MutableHouseSet {
        return when (house.type) {
            HouseType.ROW    -> potentialPositions.toColumnSet(grid)
            HouseType.COLUMN -> potentialPositions.toRowSet(grid)
            else -> throw IllegalArgumentException("unsupported region type " + house.type)
        }
    }

    private fun getCellsOfCoverSet(grid: Grid, baseSetType: HouseType, coverSet: MutableHouseSet): MutableCellSet {
        val affectedCells = MutableCellSet.empty(grid)
        for (i in coverSet.allSetBits()) {
            val house = if (baseSetType === HouseType.ROW) grid.getColumn(i) else grid.getRow(i)
            affectedCells.or(house.cellSet)
        }
        return affectedCells
    }
}