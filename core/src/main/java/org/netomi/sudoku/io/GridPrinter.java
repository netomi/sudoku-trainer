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
package org.netomi.sudoku.io;

import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.GridVisitor;
import org.netomi.sudoku.model.House;

import java.io.PrintStream;

public class GridPrinter implements GridVisitor<Grid> {

    public enum STYLE {
        ONE_LINE,
        SIMPLE
    }

    private final STYLE       style;
    private final PrintStream ps;

    public GridPrinter(STYLE style) {
        this(style, System.out);
    }

    public GridPrinter(STYLE style, PrintStream ps) {
        this.style = style;
        this.ps    = ps;
    }

    @Override
    public Grid visitGrid(Grid grid) {

        switch (style) {
            case ONE_LINE:
                printOnelineGrid(grid);
                break;
            case SIMPLE:
                printSimpleGrid(grid);
                break;
        }
        return grid;
    }

    private void printOnelineGrid(Grid grid) {
        for (Cell cell : grid.cells()) {
            ps.print(cell.getValue());
        }
    }

    private void printSimpleGrid(Grid grid) {
        for (House row : grid.rows()) {
            for (Cell cell : row.cells()) {
                ps.print(cell.getValue());
            }
            ps.println();
        }
    }
}
