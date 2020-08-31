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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class CellSet extends BaseBitSet<CellSet>
{
    public static CellSet empty(Grid grid) {
        return new CellSet(grid.getCellCount());
    }

    public static CellSet of(Cell cell, Cell... otherCells) {
        CellSet cellSet = CellSet.empty(cell.getOwner());
        cellSet.set(cell.getCellIndex());
        for (Cell otherCell : otherCells) {
            cellSet.set(otherCell.getCellIndex());
        }
        return cellSet;
    }

    public static CellSet of(Grid grid, int... cellIndices) {
        CellSet cellSet = CellSet.empty(grid);
        for (int cellIndex : cellIndices) {
            cellSet.set(cellIndex);
        }
        return cellSet;
    }

    private CellSet(int size) {
        super(size);
    }

    private CellSet(CellSet other) {
        super(other.size);
        or(other);
    }

    public Iterable<Cell> allCells(Grid grid) {
        return () -> new CellIterator(grid);
    }

    Iterable<Cell> allCells(Grid grid, int startIndex) {
        return () -> new CellIterator(grid, startIndex);
    }

    Iterable<Cell> filteredCells(Grid grid, Predicate<Cell> predicate) {
        return () -> new CellIterator(grid, predicate);
    }

    Iterable<Cell> filteredCells(Grid grid, Predicate<Cell> predicate, int startIndex) {
        return () -> new CellIterator(grid, predicate, startIndex);
    }

    public CellSet copy() {
        return new CellSet(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    // inner helper classes.

    private class CellIterator implements Iterator<Cell>
    {
        private final Grid            grid;
        private final Predicate<Cell> predicate;
        private       int             nextOffset;
        private       Cell            nextCell;
        private       boolean         nextCellSet;

        CellIterator(Grid grid) {
            this(grid, (cell) -> true, 0);
        }

        CellIterator(Grid grid, int startIndex) {
            this(grid, (cell) -> true, startIndex);
        }

        CellIterator(Grid grid, Predicate<Cell> predicate) {
            this(grid, predicate, 0);
        }

        CellIterator(Grid grid, Predicate<Cell> predicate, int startIndex) {
            this.grid        = grid;
            this.predicate   = predicate;
            this.nextCell    = null;
            this.nextCellSet = false;
            advanceIterator(startIndex);
        }

        private boolean advanceIterator(int startIndex) {
            if (nextOffset < 0) {
                return false;
            }

            int startOffset = startIndex;
            while ((nextOffset = bits.nextSetBit(startOffset)) >= 0) {
                Cell cell = grid.getCell(nextOffset);
                if (predicate.test(cell)) {
                    nextCell    = cell;
                    nextCellSet = true;
                    return true;
                }
                startOffset = nextOffset + 1;
            }
            return false;
        }

        @Override
        public boolean hasNext() {
            return nextCellSet || advanceIterator(nextOffset + 1);
        }

        @Override
        public Cell next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            nextCellSet = false;
            return nextCell;
        }
    }
}
