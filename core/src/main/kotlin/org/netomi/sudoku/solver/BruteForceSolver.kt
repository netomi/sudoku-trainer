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
import org.netomi.sudoku.model.MutableValueSet
import org.netomi.sudoku.model.ValueSet
import org.netomi.sudoku.solver.techniques.HiddenSingleFinder
import org.netomi.sudoku.solver.techniques.NakedSingleFinder
import java.lang.IllegalStateException
import java.util.*

class BruteForceSolver : GridSolver {
    private val hintSolver: HintSolver = HintSolver(NakedSingleFinder(), HiddenSingleFinder())

    private var guesses = 0
    private var backtracks = 0
    private var direct_propagation = 0

    override fun solve(grid: Grid): Grid {
        return solve(grid, true)
    }

    fun solve(grid: Grid, forward: Boolean): Grid {
        backtracks = 0
        guesses = 0
        direct_propagation = 0
        val searchGrid = grid.copy()
        val cellSet: MutableSet<Cell> = LinkedHashSet()
        searchGrid.unassignedCells().forEach { cell -> cellSet.add(cell) }
        val success = solveRecursive(searchGrid, cellSet, forward)
        return searchGrid
    }

    private fun solveRecursive(grid: Grid, unassignedCells: MutableSet<Cell>, forward: Boolean): Boolean {
        if (unassignedCells.isEmpty()) {
            return true
        }

        val hints = hintSolver.findDirectHint(grid)
        if (hints.hints.isNotEmpty()) {
            val hint = hints.hints.iterator().next() as DirectHint
            val cellIndex = hint.cellIndex
            hint.apply(grid, true)
            direct_propagation++

            val cell = grid.getCell(cellIndex)
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
        val possibleValues: MutableValueSet = nextCell.possibleValues.toMutableValueSet()
        while (possibleValues.cardinality() > 0) {
            if (possibleValues.cardinality() > 1) {
                guesses++
            }

            var value: Int = if (forward) {
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

    private fun selectNextCell(cellSet: MutableSet<Cell>): Cell {
        val domains = arrayOfNulls<Cell>(9)
        for (cell in cellSet) {
            val cardinality = cell.possibleValues.cardinality()
            if (cardinality == 0) {
                return cell
            }
            if (domains[cardinality - 1] == null) {
                domains[cardinality - 1] = cell
            }
        }
        for (cell in domains) {
            if (cell != null) {
                cellSet.remove(cell)
                return cell
            }
        }

        throw AssertionError("impossible")
    }

}