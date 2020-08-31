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

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Grids
{
    // hide constructor of utility class.
    private Grids() {}

    public static CellSet toRowSet(Grid grid, CellSet cells) {
        CellSet rows = CellSet.empty(grid);
        cells.allCells(grid).forEach(cell -> rows.set(cell.getRowIndex()));
        return rows;
    }

    /**
     * Returns the row in which the cells are contained if all cells
     * are contained in the same row.
     *
     * @return the row all cells are contained in, or {@code null} if the
     * cells are contained in different rows.
     */
    public static House.Row getSingleRow(Grid grid, CellSet cells) {
        CellSet rows = toRowSet(grid, cells);
        return rows.cardinality() == 1 ?
            grid.getRow(rows.firstSetBit()) :
            null;
    }

    public static CellSet toColumnSet(Grid grid, CellSet cells) {
        CellSet columns = CellSet.empty(grid);
        cells.allCells(grid).forEach(cell -> columns.set(cell.getColumnIndex()));
        return columns;
    }

    /**
     * Returns the column in which the cells are contained if all cells
     * are contained in the same column.
     *
     * @return the column all cells are contained in, or {@code null} if the
     * cells are contained in different columns.
     */
    public static House.Column getSingleColumn(Grid grid, CellSet cells) {
        CellSet columns = toColumnSet(grid, cells);
        return columns.cardinality() == 1 ?
            grid.getColumn(columns.firstSetBit()) :
            null;
    }

    public static CellSet toBlockSet(Grid grid, CellSet cells) {
        CellSet blocks = CellSet.empty(grid);
        cells.allCells(grid).forEach(cell -> blocks.set(cell.getBlockIndex()));
        return blocks;
    }

    /**
     * Returns the block in which the cells are contained if all cells
     * are contained in the same block.
     *
     * @return the block all cells are contained in, or {@code null} if the
     * cells are contained in different blocks.
     */
    public static House.Block getSingleBlock(Grid grid, CellSet cells) {
        CellSet blocks = toBlockSet(grid, cells);
        return blocks.cardinality() == 1 ?
            grid.getBlock(blocks.firstSetBit()) :
            null;
    }

    public static CellSet toCellSet(Grid grid, Iterable<Cell> cells) {
        CellSet cellSet = CellSet.empty(grid);
        cells.forEach(cell -> cellSet.set(cell.getCellIndex()));
        return cellSet;
    }

    public static List<Cell> toCellList(Grid grid, CellSet cells) {
        return StreamSupport.stream(cells.allCells(grid).spliterator(), false)
                            .collect(Collectors.toList());
    }

    public static CellSet getCells(House house, House... otherHouses) {
        CellSet result = house.getCells().copy();
        for (House otherHouse : otherHouses) {
            result.or(otherHouse.getCells());
        }
        return result;
    }
}
