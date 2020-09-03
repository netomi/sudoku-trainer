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
import java.util.*

/**
 * A [HintFinder] implementation ...
 */
class RemotePairFinder : BaseHintFinder {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.REMOTE_PAIR

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        val visitedChains: MutableSet<CellSet> = HashSet()
        grid.acceptCells { cell ->
            val possibleValues = cell.possibleValueSet
            if (possibleValues.cardinality() == 2) {
                findChain(grid, hintAggregator, cell, Chain(grid, cell), visitedChains, 1)
            }
        }
    }

    private fun findChain(grid:           Grid,
                          hintAggregator: HintAggregator,
                          currentCell:    Cell,
                          currentChain:   Chain,
                          visitedChains:  MutableSet<CellSet>,
                          length:         Int)
    {
        currentChain.addLink(currentCell)
        val possibleValues = currentCell.possibleValueSet
        // make sure we do not add chains twice: in forward and reverse order.
        if (visitedChains.contains(currentChain.cells)) {
            return
        }

        if (length > 3 && length % 2 == 0) {
            val affectedCells = currentCell.peerSet.toMutableCellSet()
            affectedCells.andNot(currentChain.cells)

            for (affectedCell in affectedCells.allCells(grid)) {
                val peers = affectedCell.peerSet.toMutableCellSet()
                val endPoints = MutableCellSet.of(currentChain.startCell, currentCell)

                peers.and(endPoints)

                if (peers.cardinality() < 2) {
                    affectedCells.clear(affectedCell.cellIndex)
                }
            }

            if (eliminateValuesFromCells(grid, hintAggregator, affectedCells, possibleValues)) {
                visitedChains.add(currentChain.cells.copy())
            }
        }

        for (nextCell in currentCell.peers()) {
            if (currentChain.contains(nextCell)) {
                continue
            }

            val possibleValuesOfNextCell = nextCell.possibleValueSet
            if (possibleValuesOfNextCell.cardinality() != 2 ||
                possibleValues != possibleValuesOfNextCell) {
                continue
            }

            findChain(grid, hintAggregator, nextCell, currentChain, visitedChains, length + 1)
        }

        currentChain.removeLink(currentCell)
    }

    private class Chain(grid: Grid, val startCell: Cell) {
        val cells: MutableCellSet = MutableCellSet.empty(grid)

        fun addLink(cell: Cell) {
            cells.set(cell.cellIndex)
        }

        fun removeLink(cell: Cell) {
            cells.clear(cell.cellIndex)
        }

        operator fun contains(cell: Cell): Boolean {
            return cells[cell.cellIndex]
        }

        init {
            cells.set(startCell.cellIndex)
        }
    }
}