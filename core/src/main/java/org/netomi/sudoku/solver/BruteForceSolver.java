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
package org.netomi.sudoku.solver;

import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.solver.techniques.HiddenSingleFinder;
import org.netomi.sudoku.solver.techniques.NakedSingleFinder;

import java.util.*;

public class BruteForceSolver implements GridSolver {

    private final HintSolver hintSolver;
    private int guesses;
    private int backtracks;
    private int direct_propagation;

    public BruteForceSolver() {
        hintSolver = new HintSolver(new NakedSingleFinder(), new HiddenSingleFinder());
    }

    public Grid solve(Grid grid) {
        return solve(grid, true);
    }

    public Grid solve(Grid grid, boolean forward) {
        backtracks = 0;
        guesses = 0;
        direct_propagation = 0;

        Grid searchGrid = grid.copy();

        Set<Cell> cellSet = new LinkedHashSet<>();

        for (Cell cell : searchGrid.cells()) {
            if (!cell.isAssigned()) {
                cellSet.add(cell);
            }
        }

        boolean success = solveRecursive(searchGrid, cellSet, forward);

        return searchGrid;
    }

    private boolean solveRecursive(Grid grid, Set<Cell> unassignedCells, boolean forward) {
        if (unassignedCells.isEmpty()) {
            return true;
        }

        HintAggregator hints = hintSolver.findDirectHint(grid);
        if (hints.hints.size() > 0) {
            DirectHint hint = (DirectHint) hints.hints.iterator().next();

            int cellIndex = hint.getCellIndex();

            hint.apply(grid, true);

            direct_propagation++;

            Cell cell = grid.getCell(cellIndex);
            unassignedCells.remove(cell);

            if (solveRecursive(grid, unassignedCells, forward)) {
                return true;
            }

            cell.reset();
            unassignedCells.add(cell);
            backtracks++;

            return false;
        }

        Cell nextCell = selectNextCell(unassignedCells);

        BitSet possibleValues = (BitSet) nextCell.getPossibleValues().clone();

        while (possibleValues.cardinality() > 0) {
            if (possibleValues.cardinality() > 1) {
                guesses++;
            }

            int value;

            if (forward) {
                value = possibleValues.nextSetBit(1);
            } else {
                value = possibleValues.previousSetBit(grid.getGridSize() + 1);
            }

            possibleValues.clear(value);

            nextCell.setValue(value);

            if (solveRecursive(grid, unassignedCells, forward)) {
                return true;
            }
        }

        nextCell.reset();
        unassignedCells.add(nextCell);
        backtracks++;

        return false;
    }

    private Cell selectNextCell(Set<Cell> cellSet) {
        Cell[] domains = new Cell[9];

        for (Cell cell : cellSet) {
            int cardinality = cell.getPossibleValues().cardinality();

            if (cardinality == 0) { return cell; }

            if (domains[cardinality - 1] == null) {
                domains[cardinality - 1] = cell;
            }
        }

        for (Cell cell : domains) {
            if (cell != null) {
                cellSet.remove(cell);
                return cell;
            }
        }

        return null;
    }
}
