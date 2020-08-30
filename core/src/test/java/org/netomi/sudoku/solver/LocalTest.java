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

import org.netomi.sudoku.io.GridPrinter;
import org.netomi.sudoku.io.GridValueLoader;
import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.PredefinedType;

import java.util.Collection;

public class LocalTest {

    public static void main(String[] args) {
        String input = "000000010400000000020000000000050407008000300001090000300400200050100000000806000";

        // unsolvable
        //String input = "..9.7...5..21..9..1...28....7...5..1..851.....5....3.......3..68........21.....87";
        //String input = "..9.287..8.6..4..5..3.....46.........2.71345.........23.....5..9..4..8.7..125.3..";

        // multiple solutions
//        String input =
//                " |...|.7.|..3|\n" +
//                " |2.7|..6|.4.|\n" +
//                " |..3|...|7..|\n" +
//                " |---+---+---|\n" +
//                " |4.1|2..|53.|\n" +
//                " |..8|4.5|2..|\n" +
//                " |.52|..1|6.4|\n" +
//                " |---+---+---|\n" +
//                " |..5|...|3..|\n" +
//                " |.3.|6..|4.2|\n" +
//                " |6..|.2.|...|\n" +
//                " *-----------*";

//        String input = "3..1....5.2.7\n" +
//                "7.2.4.5.1....6\n" +
//                ".9..2.7.8.3..\n" +
//                "9.5.6.7.3...8.\n" +
//                "8.4....2.6..3\n" +
//                "1.2.3.6.8...4.\n" +
//                "4.6.9.8.5.7...\n" +
//                "...9.2.1.4..8\n" +
//                "2.1..3.4..9.7.";

        //String input = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......";

        //String input = "15.63...2.3.21.56.69258471372134598634987625158619237426.7531...73.61.25.15.28637";
        //String input = "000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        //String input = "85...24..72......9..4.........1.7..23.5...9...4...........8..7..17..........36.4.";
        //String input = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......";
        //String input = ".....6....59.....82....8....45........3........6..3.54...325..6..................";

        //String input = "627134598..1928763938675.2.3764........763........2637.63..794.7...4638..843...76";

        //String input = "300000004002060100010908020005000600020000010009000800080304060004010900500000007";

        Grid grid = Grid.of(PredefinedType.CLASSIC_9x9);

        grid.accept(new GridValueLoader(input));
        grid.accept(new GridPrinter(GridPrinter.STYLE.SIMPLE));

        System.out.println();

        Cell cell = grid.getCell(0);
        System.out.println(cell);

        cell.excludePossibleValues(false, 5);
        System.out.println(cell);

        cell.clearExcludedValues(false);
        System.out.println(cell);

//        HintSolver hintSolver = new HintSolver();
//
//        long start = System.nanoTime();
//
//        HintAggregator hints = hintSolver.findHints(grid);
//
//        long end = System.nanoTime();
//
//        hints.applyHints(grid);
//        grid.accept(new GridPrinter(GridPrinter.STYLE.SIMPLE));
//
//        System.out.println(hints);
//        System.out.println("valid = " + grid.isValid());
//        System.out.println("Solving sudoku logically took " + (end - start) / 1e6 + "ms");
//
//        BruteForceSolver solver = new BruteForceSolver();
//
//        start = System.nanoTime();
//        Grid resultGrid = solver.solve(grid, true);
//        end = System.nanoTime();
//        System.out.println("Solving sudoku with brute-force took " + (end - start) / 1e6 + "ms");
//        resultGrid.accept(new GridPrinter(GridPrinter.STYLE.SIMPLE));
//        System.out.println("grid valid = " + resultGrid.isValid());
    }
}
