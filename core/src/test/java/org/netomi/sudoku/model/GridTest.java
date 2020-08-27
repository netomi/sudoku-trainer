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

import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridTest {

    @Test
    public void cellIterator() {
        Grid grid = Grid.of(PredefinedType.CLASSIC_9x9);

        House.Row row = grid.getRow(0);

        assertEquals(grid.getGridSize(), countItems(row.cells()));

        grid.getCell(0).setValue(1);

        assertEquals(grid.getGridSize(), countItems(row.cells()));
        assertEquals(grid.getGridSize() - 1, countItems(row.unassignedCells()));
        assertEquals(1, countItems(row.unassignedCells(8)));


        for (int i = 0; i < grid.getGridSize(); i++) {
            grid.getCell(i).setValue(i + 1);
        }

        assertEquals(grid.getGridSize(), countItems(row.cells()));
        assertEquals(0, countItems(row.unassignedCells()));
        assertEquals(0, countItems(row.unassignedCells(8)));
    }

    private static <T> long countItems(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).count();
    }
}
