package org.netomi.sudoku.solver;

import org.netomi.sudoku.io.GridValueLoader;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.PredefinedType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SpeedTest {

    private static Grid solveGrid(Grid grid, BruteForceSolver solver) {
        return solver.solve(grid, true);
    }

    public static void main(String[] args) throws IOException  {

        BruteForceSolver solver = new BruteForceSolver();

        InputStream is = SpeedTest.class.getResourceAsStream("/all_17_clue_sudokus.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        int tests = Integer.valueOf(reader.readLine());

        Grid grid = Grid.of(PredefinedType.CLASSIC_9x9);

        for (int i = 0; i < tests; i++) {
            String input = reader.readLine();

            grid.clear();
            grid.accept(new GridValueLoader(input));

            Grid grid2 = solveGrid(grid, solver);

            System.out.println("Solved sudoku " + (i+1) + " valid = " + grid2.isValid() + " solved = " + grid2.isSolved());

            grid2.getCell(1).reset();

            System.out.println("valid = " + grid2.isValid() + " solved = " + grid2.isSolved());

            break;
        }
    }
}
