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

import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.Grids;
import org.netomi.sudoku.model.House;
import org.netomi.sudoku.solver.*;

import java.util.*;

public abstract class AbstractHintFinder implements HintFinder {

    /**
     * Adds a direct placement hint to the {@code HintAggregator}.
     */
    protected void placeValueInCell(Grid           grid,
                                    HintAggregator hintAggregator,
                                    int            cellIndex,
                                    int            value) {
        hintAggregator.addHint(new DirectHint(grid.getType(),
                                              getSolvingTechnique(),
                                              cellIndex,
                                              value));
    }

    /**
     * Adds an elimination hint to remove the given candidate value
     * from all cells in the affected house excluding cell in the excluded
     * house.
     *
     * @param affectedHouse the affected house for this elimination hint
     * @param excludedHouse the cells to be excluded from the affected house
     * @param excludedValue the candidate value to remove
     */
    protected void eliminateValueFromCells(Grid           grid,
                                           HintAggregator hintAggregator,
                                           House          affectedHouse,
                                           House          excludedHouse,
                                           int            excludedValue) {

        BitSet cellsToModify = new BitSet(grid.getCellCount());
        for (Cell cell : affectedHouse.cellsExcluding(excludedHouse)) {
            // only consider cells which have the excluded value as candidate.
            if (!cell.isAssigned() &&
                cell.getPossibleValues().get(excludedValue)) {
                cellsToModify.set(cell.getCellIndex());
            }
        }

        BitSet eliminations = new BitSet(grid.getGridSize() + 1);
        eliminations.set(excludedValue);

        if (cellsToModify.cardinality() > 0) {
            hintAggregator.addHint(new IndirectHint(grid.getType(),
                                                    getSolvingTechnique(),
                                                    cellsToModify,
                                                    eliminations));
        }
    }

    /**
     * Adds an elimination hint to remove all candidate values from the affected
     * cells that are not contained in the allowedValues array.
     *
     * @param affectedCells  the set of affected cell indices
     * @param allowedValues  the allowed set of candidates in the affected cells
     */
    protected void eliminateNotAllowedValuesFromCells(Grid           grid,
                                                      HintAggregator hintAggregator,
                                                      BitSet         affectedCells,
                                                      BitSet         allowedValues) {

        BitSet       cellsToModify = new BitSet(grid.getCellCount());
        List<BitSet> excludedValues = new ArrayList<>();

        for (Cell cell : Grids.getCells(grid, affectedCells)) {
            if (!cell.isAssigned()) {
                BitSet valuesToExclude = valuesExcluding(cell.getPossibleValues(), allowedValues);

                if (valuesToExclude.cardinality() > 0) {
                    cellsToModify.set(cell.getCellIndex());
                    excludedValues.add(valuesToExclude);
                }
            }
        }

        if (cellsToModify.cardinality() > 0) {
            hintAggregator.addHint(new IndirectHint(grid.getType(),
                                                    getSolvingTechnique(),
                                                    cellsToModify,
                                                    excludedValues.toArray(new BitSet[0])));
        }
    }

    /**
     * Adds an elimination hint to remove all candidate values from the affected
     * cells (except the excluded ones) that are contained in the excludedValues bitset.
     *
     * @param affectedCells  the affected cells for this elimination hint
     * @param excludedValues the candidate value to remove
     */
    protected boolean eliminateValuesFromCells(Grid           grid,
                                               HintAggregator hintAggregator,
                                               BitSet         affectedCells,
                                               BitSet         excludedValues) {

        BitSet       cellsToModify       = new BitSet(grid.getCellCount());
        List<BitSet> valuesToExcludeList = new ArrayList<>();

        for (Cell cell : Grids.getCells(grid, affectedCells)) {
            if (!cell.isAssigned()) {
                BitSet valuesToExclude = valuesIncluding(cell.getPossibleValues(), excludedValues);

                if (valuesToExclude.cardinality() > 0) {
                    cellsToModify.set(cell.getCellIndex());
                    valuesToExcludeList.add(valuesToExclude);
                }
            }
        }

        if (cellsToModify.cardinality() > 0) {
            hintAggregator.addHint(new IndirectHint(grid.getType(),
                                                    getSolvingTechnique(),
                                                    cellsToModify,
                                                    valuesToExcludeList.toArray(new BitSet[0])));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a BitSet containing all values that have been set in the given bitset
     * excluding the values contained in the excludedValues array.
     */
    private static BitSet valuesExcluding(BitSet values, BitSet excludedValues) {
        BitSet result = (BitSet) values.clone();
        result.andNot(excludedValues);
        return result;
    }

    /**
     * Returns an array containing all values that have been set in the given bitset
     * only including the values contained in the includedValues bitset.
     */
    private static BitSet valuesIncluding(BitSet values, BitSet includedValues) {
        BitSet result = (BitSet) includedValues.clone();
        result.and(values);
        return result;
    }
}