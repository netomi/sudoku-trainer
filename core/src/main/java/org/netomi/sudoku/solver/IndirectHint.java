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

import org.netomi.sudoku.model.Grid;

import java.util.Arrays;

public class IndirectHint extends Hint {

    private final int[]   cellIndices;
    private final int[][] excludedValues;

    public IndirectHint(Grid.Type type, SolvingTechnique solvingTechnique, int[] cellIndices, int[] excludedValues) {
        this(type, solvingTechnique, cellIndices, expandArray(excludedValues, cellIndices.length));
    }

    public IndirectHint(Grid.Type type, SolvingTechnique solvingTechnique, int[] cellIndices, int[][] excludedValues) {
        super(type, solvingTechnique);
        this.cellIndices    = cellIndices;
        this.excludedValues = excludedValues;
    }

    private static int[][] expandArray(int[] array, int copies) {
        int[][] result = new int[copies][];

        for (int i = 0; i < copies; i++) {
            result[i] = array;
        }

        return result;
    }

    public int[] getCellIndices() {
        return cellIndices;
    }

    public int[][] getExcludedValues() {
        return excludedValues;
    }

    @Override
    public void apply(Grid targetGrid, boolean updateGrid) {
        int index = 0;
        for (int cellIndex : cellIndices) {
            targetGrid.getCell(cellIndex).excludePossibleValues(excludedValues[index++]);
        }
    }

    @Override
    public String toString() {
        StringBuilder eliminations = new StringBuilder();

        int index = 0;
        for (int cellIndex : cellIndices) {
            eliminations.append(getGridType().getCellName(cellIndex));
            eliminations.append(" <> ");
            eliminations.append(Arrays.toString(excludedValues[index++]));
            eliminations.append(", ");
        }
        eliminations.deleteCharAt(eliminations.length() - 2);

        return String.format("%s: %s", getSolvingTechnique().getName(), eliminations.toString());
    }
}
