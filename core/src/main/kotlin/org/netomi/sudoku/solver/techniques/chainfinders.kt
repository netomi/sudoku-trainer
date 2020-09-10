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
import org.netomi.sudoku.solver.*
import org.netomi.sudoku.solver.BaseHintFinder

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
                // initial chain setup
                val firstCandidate = possibleValues.firstSetBit()
                val chain = Chain(grid, cell.cellIndex, firstCandidate)
                chain.addLink(LinkType.STRONG, cell.cellIndex, possibleValues.nextSetBit(firstCandidate + 1))

                findChain(grid, hintAggregator, cell, chain, visitedChains, 1)
            }
        }
    }

    private fun findChain(grid:           Grid,
                          hintAggregator: HintAggregator,
                          currentCell:    Cell,
                          currentChain:   Chain,
                          visitedChains:  MutableSet<CellSet>,
                          cellCount:      Int)
    {
        // make sure we do not add chains twice: in forward and reverse order.
        if (visitedChains.contains(currentChain.cellSet)) {
            return
        }

        // to find a remote pair, the chain has to include at least
        // 4 cells. Also the start / end points of the chain need to
        // have opposite active states.
        if (cellCount >= 4 && cellCount % 2 == 0) {
            val affectedCells = currentCell.peerSet.toMutableCellSet()
            affectedCells.andNot(currentChain.cellSet)

            for (affectedCell in affectedCells.allCells(grid)) {
                val peers = affectedCell.peerSet.toMutableCellSet()

                val startCell = currentChain.rootNode.cellIndex
                val endPoints = MutableCellSet.of(grid.getCell(startCell), currentCell)

                peers.and(endPoints)

                // if the cell does not see both endpoints,
                // it can not be considered for elimination.
                if (peers.cardinality() < 2) {
                    affectedCells.clear(affectedCell.cellIndex)
                }
            }

            val matchingCells = currentChain.cellSet.copy()
            val possibleValues = currentCell.possibleValueSet.copy()
            if (eliminateValuesFromCells(grid, hintAggregator, matchingCells, possibleValues, affectedCells, currentChain.copy(), affectedCells, possibleValues)) {
                visitedChains.add(matchingCells)
            }
        }

        for (nextCell in currentCell.peers()) {
            if (currentChain.contains(nextCell)) {
                continue
            }

            val possibleValues           = currentCell.possibleValueSet
            val possibleValuesOfNextCell = nextCell.possibleValueSet

            if (possibleValuesOfNextCell.cardinality() != 2 ||
                    possibleValues != possibleValuesOfNextCell) {
                continue
            }

            val linkedCandidate = currentChain.lastNode.candidate
            currentChain.addLink(LinkType.WEAK, nextCell.cellIndex, linkedCandidate)
            val otherCandidate = possibleValuesOfNextCell.filteredSetBits { it != linkedCandidate }.first()
            currentChain.addLink(LinkType.STRONG, nextCell.cellIndex, otherCandidate)

            findChain(grid, hintAggregator, nextCell, currentChain, visitedChains, cellCount + 1)

            currentChain.removeLastLink()
            currentChain.removeLastLink()
        }
    }
}
