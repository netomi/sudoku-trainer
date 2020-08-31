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
package org.netomi.sudoku.model;

public class ValueSet extends BaseBitSet<ValueSet>
{
    public static ValueSet empty(Grid grid) {
        return new ValueSet(grid.getGridSize());
    }

    public static ValueSet of(Grid grid, int... values) {
        ValueSet valueSet = ValueSet.empty(grid);
        for (int value : values) {
            valueSet.set(value);
        }
        return valueSet;
    }

    public static ValueSet fullySet(Grid grid) {
        ValueSet valueSet = ValueSet.empty(grid);
        valueSet.setAll();
        return valueSet;
    }

    private ValueSet(int valueRange) {
        super(valueRange + 1);
    }

    private ValueSet(ValueSet other) {
        super(other.size);
        or(other);
    }

    @Override
    protected int getOffset() {
        return 1;
    }

    @Override
    protected void checkInput(int value) {
        if (value < 1 || value >= size) {
            throw new IllegalArgumentException("illegal value " + value);
        }
    }

    public ValueSet copy() {
        return new ValueSet(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }
}
