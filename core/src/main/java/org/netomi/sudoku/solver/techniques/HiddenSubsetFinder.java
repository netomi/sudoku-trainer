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

import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.House;
import org.netomi.sudoku.model.HouseVisitor;
import org.netomi.sudoku.solver.HintAggregator;

import java.util.BitSet;

/**
 *  A {@code HintFinder} implementation that looks for houses
 *  where a subset of candidates is constrained to some cells,
 *  forming a hidden subset. All other candidates in these cells
 *  can be removed.
 */
public abstract class HiddenSubsetFinder extends AbstractHintFinder {

    private final int subSetSize;

    protected HiddenSubsetFinder(int subSetSize) {
        this.subSetSize = subSetSize;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {
        grid.acceptHouses(house -> {
            if (house.isSolved()) {
                return;
            }

            for (int value : house.unassignedValues()) {
                findSubset(grid,
                           hintAggregator,
                           house,
                           new BitSet(grid.getGridSize() + 1),
                           value,
                           new BitSet(grid.getCellCount()),
                           1);
            }
        });
    }

    private boolean findSubset(Grid           grid,
                               HintAggregator hintAggregator,
                               House          house,
                               BitSet         visitedValues,
                               int            currentValue,
                               BitSet         visitedPositions,
                               int            level) {

        if (level > subSetSize) {
            return false;
        }

        BitSet potentialPositions = house.getPotentialPositions(currentValue);

        BitSet allPotentialPositions = (BitSet) visitedPositions.clone();
        allPotentialPositions.or(potentialPositions);

        if (allPotentialPositions.cardinality() > subSetSize) {
            return false;
        }

        visitedValues.set(currentValue);

        if (level == subSetSize) {
            boolean foundHint = false;

            if (allPotentialPositions.cardinality() == subSetSize) {
                eliminateNotAllowedValuesFromCells(grid, hintAggregator, allPotentialPositions, visitedValues);
                foundHint = true;
            }

            visitedValues.clear(currentValue);
            return foundHint;
        }

        boolean foundHint = false;
        for (int nextValue : house.unassignedValues(currentValue + 1)) {
            foundHint |= findSubset(grid, hintAggregator, house, visitedValues, nextValue, allPotentialPositions, level + 1);
        }

        visitedValues.clear(currentValue);
        return foundHint;
    }
}
