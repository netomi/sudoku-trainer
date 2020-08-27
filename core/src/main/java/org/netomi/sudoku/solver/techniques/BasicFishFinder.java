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
import org.netomi.sudoku.model.Grids;
import org.netomi.sudoku.model.House;
import org.netomi.sudoku.model.HouseVisitor;
import org.netomi.sudoku.solver.HintAggregator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *  A {@code HintFinder} implementation that looks for houses
 *  where a subset of candidates is constrained to some cells,
 *  forming a hidden subset. All other candidates in these cells
 *  can be removed.
 */
public abstract class BasicFishFinder extends AbstractHintFinder {

    private final int size;

    protected BasicFishFinder(int size) {
        this.size = size;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {

        HouseVisitor visitor = house -> {
            if (house.isSolved()) {
                return;
            }

            for (int value : house.unassignedValues()) {
                findBaseSet(grid,
                            hintAggregator,
                            new ArrayList<>(),
                            house,
                            value,
                            new BitSet(grid.getGridSize()),
                            1);
            }
        };

        grid.acceptRows(visitor);
        grid.acceptColumns(visitor);
    }

    private boolean findBaseSet(Grid           grid,
                                HintAggregator hintAggregator,
                                List<House>    visitedRegions,
                                House          house,
                                int            value,
                                BitSet         coverSet,
                                int            level) {

        if (level > size) {
            return false;
        }

        BitSet potentialPositions = house.getPotentialPositions(value);
        if (potentialPositions.cardinality() > size) {
            return false;
        }

        BitSet mergedCoverSet = (BitSet) coverSet.clone();
        mergedCoverSet.or(getCoverSet(grid, house, potentialPositions));
        if (mergedCoverSet.cardinality() > size) {
            return false;
        }

        visitedRegions.add(house);

        if (level == size) {

            // get affected cells from cover sets.
            BitSet affectedCells = getCellsOfCoverSet(grid, house.getType(), mergedCoverSet);

            // remove all cells from base sets.
            for (House row : visitedRegions) {
                affectedCells.andNot(Grids.getCells(row));
            }

            BitSet excludedValue = new BitSet();
            excludedValue.set(value);

            // eliminate the detected fish value from all affected cells,
            // affected cells = cells of cover set - cells of base set
            eliminateValuesFromCells(grid, hintAggregator, affectedCells, excludedValue);

            visitedRegions.remove(visitedRegions.size() - 1);
            return true;
        }

        boolean foundHint = false;
        for (House nextHouse : grid.regionsAfter(house)) {
            if (!nextHouse.isSolved() &&
                !nextHouse.getAssignedValues().get(value)) {
                foundHint |= findBaseSet(grid, hintAggregator, visitedRegions, nextHouse, value, mergedCoverSet, level + 1);
            }
        }

        visitedRegions.remove(visitedRegions.size() - 1);
        return foundHint;
    }

    private BitSet getCoverSet(Grid grid, House house, BitSet potentialPositions) {
        switch (house.getType()) {
            case ROW:
                return Grids.toColumnSet(grid, potentialPositions);

            case COLUMN:
                return Grids.toRowSet(grid, potentialPositions);

            default:
                throw new IllegalArgumentException("unsupported region type " + house.getType());
        }
    }

    private BitSet getCellsOfCoverSet(Grid grid, House.Type baseSetType, BitSet coverSet) {
        BitSet affectedCells = new BitSet(grid.getCellCount());

        for (int i = coverSet.nextSetBit(0); i >= 0; i = coverSet.nextSetBit(i + 1)) {
            House house =
                baseSetType == House.Type.ROW ?
                    grid.getColumn(i) :
                    grid.getRow(i);

            affectedCells.or(Grids.getCells(house));
        }

        return affectedCells;
    }
}
