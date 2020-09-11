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
class RemotePairFinder : BaseChainFinder() {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.REMOTE_PAIR

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        val visitedChains: MutableSet<CellSet> = HashSet()
        grid.unassignedCells().forEach { cell ->
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
            val matchingCells = addChainEliminationHint(grid, hintAggregator, currentCell, currentChain, currentCell.possibleValueSet.copy())
            matchingCells?.apply { visitedChains.add(this) }
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

class XChainFinder : BaseChainFinder() {
    override val solvingTechnique: SolvingTechnique
        get() = SolvingTechnique.X_CHAIN

    override fun findHints(grid: Grid, hintAggregator: HintAggregator) {
        val visitedChains: MutableSet<CellSet> = HashSet()
        grid.unassignedCells().forEach { cell ->
            val possibleValues = cell.possibleValueSet
            for (value in possibleValues.allSetBits()) {
                val chain = Chain(grid, cell.cellIndex, value)
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

        val chainCandidate = currentChain.lastNode.candidate

        // to find a x-chain, the chain has to start and end with a strong link.
        if (cellCount >= 4 && currentChain.lastLinkType() == LinkType.STRONG) {
            val excludedValues = MutableValueSet.of(grid, chainCandidate)
            val matchingCells = addChainEliminationHint(grid, hintAggregator, currentCell, currentChain, excludedValues)
            matchingCells?.apply { visitedChains.add(this) }
        }

        val nextLinkType = currentChain.lastLinkType()?.opposite() ?: LinkType.STRONG

        for (house in currentCell.houses()) {
            val potentialPositionSet = house.getPotentialPositionsAsSet(chainCandidate)

            if (potentialPositionSet.cardinality() <= 1) {
                continue
            }

            // if the number of possible positions within a house is equal to 2 we have found
            // a strong link, otherwise its a weak link. Strong links can be downgraded to weak
            // links if needed.
            val possibleLinkType = if (potentialPositionSet.cardinality() == 2) LinkType.STRONG else LinkType.WEAK

            // if the house does not contain a link of the expected type,
            // no need to look at individual cells. Strong links can be downgraded
            // to weak links if needed.
            if (possibleLinkType < nextLinkType) {
                continue
            }

            for (nextCell in house.potentialCells(chainCandidate)) {
                if (currentChain.contains(nextCell)) {
                    continue
                }

                currentChain.addLink(nextLinkType, nextCell.cellIndex, chainCandidate)
                findChain(grid, hintAggregator, nextCell, currentChain, visitedChains, cellCount + 1)
                currentChain.removeLastLink()
            }
        }
    }
}

abstract class BaseChainFinder : BaseHintFinder
{
    protected fun addChainEliminationHint(grid:           Grid,
                                          hintAggregator: HintAggregator,
                                          currentCell:    Cell,
                                          currentChain:   Chain,
                                          excludedValues: ValueSet) : CellSet?
    {
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
        return if (eliminateValuesFromCells(grid,
                                            hintAggregator,
                                            matchingCells,
                                            excludedValues,
                                            affectedCells,
                                            currentChain.copy(),
                                            affectedCells,
                                            excludedValues)) {
            matchingCells
        } else {
            null
        }
    }
}