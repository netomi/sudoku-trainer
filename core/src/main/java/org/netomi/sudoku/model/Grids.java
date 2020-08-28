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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Grids {

    // hide constructor of utility class.
    private Grids() {}

    public static BitSet toRowSet(Grid grid, BitSet cells) {
        BitSet rows = new BitSet(grid.getGridSize());

        for (Cell cell : grid.getCells(cells)) {
            rows.set(cell.getRowIndex());
        }

        return rows;
    }

    /**
     * Returns the row in which the cells are contained if all cells
     * are contained in the same row.
     *
     * @return the row all cells are contained in, or {@code null} if the
     * cells are contained in different rows.
     */
    public static House.Row getSingleRow(Grid grid, BitSet cells) {
        BitSet rows = toRowSet(grid, cells);
        if (rows.cardinality() == 1) {
            return grid.getRow(rows.nextSetBit(0));
        } else {
            return null;
        }
    }

    public static BitSet toColumnSet(Grid grid, BitSet cells) {
        BitSet columns = new BitSet(grid.getGridSize());

        for (Cell cell : grid.getCells(cells)) {
            columns.set(cell.getColumnIndex());
        }

        return columns;
    }

    /**
     * Returns the column in which the cells are contained if all cells
     * are contained in the same column.
     *
     * @return the column all cells are contained in, or {@code null} if the
     * cells are contained in different columns.
     */
    public static House.Column getSingleColumn(Grid grid, BitSet cells) {
        BitSet columns = toColumnSet(grid, cells);
        if (columns.cardinality() == 1) {
            return grid.getColumn(columns.nextSetBit(0));
        } else {
            return null;
        }
    }

    public static BitSet toBlockSet(Grid grid, BitSet cells) {
        BitSet blocks = new BitSet(grid.getGridSize());

        for (Cell cell : grid.getCells(cells)) {
            blocks.set(cell.getBlockIndex());
        }

        return blocks;
    }

    /**
     * Returns the block in which the cells are contained if all cells
     * are contained in the same block.
     *
     * @return the block all cells are contained in, or {@code null} if the
     * cells are contained in different blocks.
     */
    public static House.Block getSingleBlock(Grid grid, BitSet cells) {
        BitSet blocks = toBlockSet(grid, cells);
        if (blocks.cardinality() == 1) {
            return grid.getBlock(blocks.nextSetBit(0));
        } else {
            return null;
        }
    }

    public static Iterable<Cell> getCells(Grid grid, BitSet cells) {
        return grid.getCells(cells);
    }

    public static BitSet toBitSet(Cell... cells) {
        BitSet bitSet = new BitSet();

        for (Cell cell : cells) {
            bitSet.set(cell.getCellIndex());
        }

        return bitSet;
    }

    public static BitSet toBitSet(Iterable<Cell> cells) {
        BitSet bitSet = new BitSet();

        for (Cell cell : cells) {
            bitSet.set(cell.getCellIndex());
        }

        return bitSet;
    }

    public static Iterable<Integer> getValues(Grid grid, BitSet values) {
        return () -> new Grid.ValueIterator(values, 1, grid.getGridSize(), false);
    }

    public static List<Cell> toCellList(Grid grid, BitSet cells) {
        return StreamSupport.stream(grid.getCells(cells).spliterator(), false)
                            .collect(Collectors.toList());
    }

    public static Collection<Integer> toIntCollection(BitSet values) {
        List<Integer> result = new ArrayList<>(values.cardinality());
        for (int value = values.nextSetBit(1); value >= 0; value = values.nextSetBit(value + 1)) {
            result.add(value);
        }
        return result;
    }

    public static int[] toIntArray(BitSet values) {
        int[] result = new int[values.cardinality()];
        for (int value = values.nextSetBit(1), idx = 0; value >= 0; value = values.nextSetBit(value + 1)) {
            result[idx++] = value;
        }
        return result;
    }

    public static BitSet getCells(House... houses) {
        BitSet result = new BitSet();

        for (House house : houses) {
            result.or(house.getCells());
        }

        return result;
    }
}
