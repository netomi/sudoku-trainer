package org.netomi.sudoku.solver;

import org.netomi.sudoku.io.GridPrinter;
import org.netomi.sudoku.io.GridValueLoader;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.PredefinedType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SpeedTest {

    private static Grid solveGrid(Grid grid, GridSolver solver) {
        Grid solvedGrid = solver.solve(grid);

        if (!solvedGrid.isSolved()) {
            BruteForceSolver bfSolver = new BruteForceSolver();

            return bfSolver.solve(solvedGrid);
        } else {
            return solvedGrid;
        }
    }

    public static void main(String[] args) throws IOException  {

        //GridSolver solver = new BruteForceSolver();
        GridSolver solver = new HintSolver();

        InputStream is = SpeedTest.class.getResourceAsStream("/all_17_clue_sudokus.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        int tests = Integer.valueOf(reader.readLine());

        Grid grid = Grid.of(PredefinedType.CLASSIC_9x9);

        for (int i = 0; i < tests; i++) {
            String input = reader.readLine();

            grid.clear();
            grid.accept(new GridValueLoader(input));

            long start = System.nanoTime();
            Grid grid2 = solveGrid(grid, solver);
            long end = System.nanoTime();

            System.out.println("Solved sudoku in " + ((end - start) / 1e6) + " ms " + (i + 1) + " valid = " + grid2.isValid() + " solved = " + grid2.isSolved());
        }
    }
}
