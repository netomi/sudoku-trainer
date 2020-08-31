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
package org.netomi.sudoku.solver.techniques;

import org.netomi.sudoku.model.*;
import org.netomi.sudoku.solver.HintAggregator;

/**
 * A {@code HintFinder} implementation that looks for houses where a subset
 * of cells has the same candidates left, forming a naked subset. All matching
 * candidates in other cells of the same house can be removed.
 */
public abstract class NakedSubsetFinder extends AbstractHintFinder
{
    private final int     subSetSize;
    private final boolean findLockedHouses;

    protected NakedSubsetFinder(int subSetSize) {
        this(subSetSize, false);
    }

    protected NakedSubsetFinder(int subSetSize, boolean findLockedHouses) {
        this.subSetSize       = subSetSize;
        this.findLockedHouses = findLockedHouses;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {
        grid.acceptHouses(house -> {
            if (house.isSolved()) {
                return;
            }

            for (Cell cell : house.unassignedCells()) {
                findSubset(grid,
                           hintAggregator,
                           house,
                           CellSet.empty(grid),
                           cell,
                           ValueSet.empty(grid),
                           1);
            }
        });
    }

    private boolean findSubset(Grid           grid,
                               HintAggregator hintAggregator,
                               House          house,
                               CellSet        visitedCells,
                               Cell           currentCell,
                               ValueSet       visitedValues,
                               int            level) {

        if (level > subSetSize) {
            return false;
        }

        ValueSet allVisitedValues = visitedValues.copy();
        allVisitedValues.or(currentCell.getPossibleValues());

        if (allVisitedValues.cardinality() > subSetSize) {
            return false;
        }

        visitedCells.set(currentCell.getCellIndex());

        if (level == subSetSize) {
            boolean foundHint = false;

            if (allVisitedValues.cardinality() == subSetSize) {
                CellSet affectedCells = Grids.getCells(house);

                if (findLockedHouses) {
                    House.Row row = Grids.getSingleRow(grid, visitedCells);
                    if (row != null) {
                        affectedCells.or(Grids.getCells(row));
                    }

                    House.Column col = Grids.getSingleColumn(grid, visitedCells);
                    if (col != null) {
                        affectedCells.or(Grids.getCells(col));
                    }
                }

                affectedCells.andNot(visitedCells);

                eliminateValuesFromCells(grid, hintAggregator, affectedCells, allVisitedValues);
                foundHint = true;
            }

            visitedCells.clear(currentCell.getCellIndex());
            return foundHint;
        }

        boolean foundHint = false;
        for (Cell nextCell : house.unassignedCells(currentCell.getCellIndex() + 1)) {
            foundHint |= findSubset(grid,
                                    hintAggregator,
                                    house,
                                    visitedCells,
                                    nextCell,
                                    allVisitedValues,
                                    level + 1);
        }

        visitedCells.clear(currentCell.getCellIndex());
        return foundHint;
    }
}
