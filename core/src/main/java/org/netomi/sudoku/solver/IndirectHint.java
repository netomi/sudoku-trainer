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

    private final int[] cellIndices;
    private final int[] excludedValues;

    public IndirectHint(Grid.Type type, SolvingTechnique solvingTechnique, int[] cellIndices, int[] excludedValues) {
        super(type, solvingTechnique);
        this.cellIndices    = cellIndices;
        this.excludedValues = excludedValues;
    }

    public int[] getCellIndices() {
        return cellIndices;
    }

    public int[] getExcludedValues() {
        return excludedValues;
    }

    @Override
    public void apply(Grid targetGrid, boolean updateGrid) {
        for (int cellIndex : cellIndices) {
            targetGrid.getCell(cellIndex).excludePossibleValues(excludedValues);
        }
    }

    @Override
    public String toString() {
        StringBuilder cellNames = new StringBuilder();

        for (int cellIndex : cellIndices) {
            cellNames.append(getGridType().getCellName(cellIndex));
            cellNames.append("/");
        }
        cellNames.deleteCharAt(cellNames.length() - 1);

        return String.format("%s: %s <> %s", getSolvingTechnique().getName(), cellNames.toString(), Arrays.toString(excludedValues));
    }
}
