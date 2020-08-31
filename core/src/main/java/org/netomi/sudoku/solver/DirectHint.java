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

import java.util.Objects;

public class DirectHint extends Hint
{
    private final int cellIndex;
    private final int value;

    public DirectHint(Grid.Type type, SolvingTechnique solvingTechnique, int cellIndex, int value) {
        super(type, solvingTechnique);
        this.cellIndex = cellIndex;
        this.value     = value;
    }

    public int getCellIndex() {
        return cellIndex;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void apply(Grid targetGrid, boolean updateGrid) {
        targetGrid.getCell(cellIndex).setValue(value, updateGrid);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(cellIndex, value);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirectHint that = (DirectHint) o;
        return super.equals(o)             &&
               cellIndex == that.cellIndex &&
               value     == that.value;
    }

    public String asString() {
        return String.format("%s=%d", getGridType().getCellName(cellIndex), value);
    }

    @Override
    public String toString() {
        return String.format("%s: %s=%d", getSolvingTechnique().getName(), getGridType().getCellName(cellIndex), value);
    }
}
