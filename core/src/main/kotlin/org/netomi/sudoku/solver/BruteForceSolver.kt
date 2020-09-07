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
package org.netomi.sudoku.solver

import org.netomi.sudoku.model.Cell
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.solver.techniques.HiddenSingleFinder
import org.netomi.sudoku.solver.techniques.NakedSingleFinder
import java.util.*

class BruteForceSolver : GridSolver
{
    private val hintSolver: HintSolver = HintSolver(NakedSingleFinder(), HiddenSingleFinder())

    // some statistics

    var guesses = 0
        private set

    var backtracks = 0
        private set

    var directPropagations = 0
        private set

    override fun solve(grid: Grid): Grid {
        return solve(grid, true)
    }

    fun solve(grid: Grid, forward: Boolean): Grid {
        guesses            = 0
        backtracks         = 0
        directPropagations = 0

        val searchGrid = grid.copy()
        val cellSet: MutableSet<Cell> = LinkedHashSet()

        searchGrid.unassignedCells().forEach { cell -> cellSet.add(cell) }
        solveRecursive(searchGrid, cellSet, forward)

        return searchGrid
    }

    private fun solveRecursive(grid: Grid, unassignedCells: MutableSet<Cell>, forward: Boolean): Boolean {
        if (unassignedCells.isEmpty()) {
            return true
        }

        val hint = hintSolver.findNextHint(grid)
        hint?.apply {
            val assignmentHint = this as AssignmentHint

            assignmentHint.apply(grid, true)
            directPropagations++

            val cell = grid.getCell(assignmentHint.cellIndex)
            unassignedCells.remove(cell)

            if (solveRecursive(grid, unassignedCells, forward)) {
                return true
            }

            cell.reset()
            unassignedCells.add(cell)
            backtracks++
            return false
        }

        val nextCell = selectNextCell(unassignedCells)
        val possibleValues = nextCell.possibleValueSet.toMutableValueSet()

        // try all remaining possible values of the current cell
        // and traverse all other cells recursively. Slow but guaranteed
        // to solve the sudoku grid.
        while (possibleValues.cardinality() > 0) {
            if (possibleValues.cardinality() > 1) {
                guesses++
            }

            val value = if (forward) {
                possibleValues.firstSetBit()
            } else {
                possibleValues.previousSetBit(possibleValues.lastBitIndex)
            }

            possibleValues.clear(value)
            nextCell.value = value

            if (solveRecursive(grid, unassignedCells, forward)) {
                return true
            }
        }

        nextCell.reset()
        unassignedCells.add(nextCell)
        backtracks++
        return false
    }

    /**
     * Select the next cell. The decision is based on the remaining
     * possible values to be used. The cell with the fewest possible
     * values is selected.
     */
    private fun selectNextCell(cellSet: MutableSet<Cell>): Cell {
        val domains = arrayOfNulls<Cell>(9)

        for (cell in cellSet) {
            val cardinality = cell.possibleValueSet.cardinality()
            if (cardinality <= 1) {
                return cell
            }

            if (domains[cardinality - 1] == null) {
                domains[cardinality - 1] = cell
            }
        }

        for (cell in domains) {
            cell?.apply {
                cellSet.remove(this)
                return this
            }
        }

        throw AssertionError("impossible")
    }
}