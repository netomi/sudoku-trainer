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
import org.netomi.sudoku.model.Grids;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public class IndirectHint extends Hint {

    private final BitSet   cellIndices;
    private final BitSet[] excludedValues;

    public IndirectHint(Grid.Type        type,
                        SolvingTechnique solvingTechnique,
                        BitSet           cellIndices,
                        BitSet           excludedValues) {
        this(type, solvingTechnique, cellIndices, expand(excludedValues, cellIndices.cardinality()));
    }

    public IndirectHint(Grid.Type        type,
                        SolvingTechnique solvingTechnique,
                        BitSet           cellIndices,
                        BitSet[]         excludedValues) {
        super(type, solvingTechnique);
        this.cellIndices    = cellIndices;
        this.excludedValues = excludedValues;
    }

    private static BitSet[] expand(BitSet values, int copies) {
        BitSet[] result = new BitSet[copies];

        for (int i = 0; i < copies; i++) {
            result[i] = values;
        }

        return result;
    }

    public BitSet getCellIndices() {
        return cellIndices;
    }

    public BitSet[] getExcludedValues() {
        return excludedValues;
    }

    @Override
    public void apply(Grid targetGrid, boolean updateGrid) {
        int index = 0;
        for (Cell cell : Grids.getCells(targetGrid, cellIndices)) {
            cell.excludePossibleValues(excludedValues[index++], updateGrid);
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(cellIndices);
        result = 31 * result + Arrays.hashCode(excludedValues);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndirectHint that = (IndirectHint) o;
        return super.equals(o)                               &&
               Objects.equals(cellIndices, that.cellIndices) &&
               Arrays.equals(excludedValues, that.excludedValues);
    }

    @Override
    public String toString() {
        StringBuilder eliminations = new StringBuilder();

        int index = 0;
        for (int cellIndex = cellIndices.nextSetBit(0); cellIndex >= 0; cellIndex = cellIndices.nextSetBit(cellIndex + 1)) {
            eliminations.append(getGridType().getCellName(cellIndex));
            eliminations.append("<>");
            eliminations.append(Grids.toIntCollection(excludedValues[index++]));
            eliminations.append(", ");
        }

        eliminations.delete(eliminations.length() - 2, eliminations.length());

        return String.format("%s: => %s", getSolvingTechnique().getName(), eliminations.toString());
    }
}
