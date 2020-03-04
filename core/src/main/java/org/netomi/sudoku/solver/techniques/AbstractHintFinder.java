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
import org.netomi.sudoku.model.House;
import org.netomi.sudoku.solver.*;

import java.util.*;

public abstract class AbstractHintFinder implements HintFinder {

    protected void addDirectHint(Grid grid,
                                 HintAggregator hintAggregator,
                                 int            cellIndex,
                                 int            value) {
        hintAggregator.addHint(new DirectHint(grid.getType(), getSolvingTechnique(), cellIndex, value));
    }

    protected void addIndirectHint(Grid           grid,
                                   HintAggregator hintAggregator,
                                   int[]          cellIndices,
                                   int[]          excludedValues) {
        hintAggregator.addHint(new IndirectHint(grid.getType(), getSolvingTechnique(), cellIndices, excludedValues));
    }

    protected void addIndirectHint(Grid           grid,
                                   HintAggregator hintAggregator,
                                   int[]          cellIndices,
                                   int[][]        excludedValues) {
        hintAggregator.addHint(new IndirectHint(grid.getType(), getSolvingTechnique(), cellIndices, excludedValues));
    }

    protected void addIndirectHint(Grid           grid,
                                   HintAggregator hintAggregator,
                                   House          constrainedHouse,
                                   int            constrainedValue,
                                   House          affectedHouse) {

        List<Cell> affectedCells = new ArrayList<>();
        for (Cell cell : affectedHouse.cellsExcluding(constrainedHouse)) {
            if (cell.getPossibleValues().get(constrainedValue)) {
                affectedCells.add(cell);
            }
        }

        if (!affectedCells.isEmpty()) {
            addIndirectHint(grid, hintAggregator, toCellIndexArray(affectedCells), new int[] { constrainedValue });
        }
    }

    protected static int[] toCellIndexArray(List<Cell> cells) {
        int[] result = new int[cells.size()];

        for (int i = 0; i < cells.size(); i++) {
            result[i] = cells.get(i).getCellIndex();
        }

        return result;
    }

    protected static int[] toIntArray(BitSet values, int[] excludedValues) {
        Set<Integer> allValues = new HashSet<>();
        for (int i = values.nextSetBit(1); i >= 0; i = values.nextSetBit(i + 1)) {
            allValues.add(i);
        }

        for (int excludedValue : excludedValues) {
            allValues.remove(excludedValue);
        }

        return toIntArray(allValues);
    }

    private static int[] toIntArray(Set<Integer> values) {
        int[] result = new int[values.size()];

        int i = 0;
        for (Integer v : values) {
            result[i++] = v;
        }

        return result;
    }

}