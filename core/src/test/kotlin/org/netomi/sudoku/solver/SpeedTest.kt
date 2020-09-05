package org.netomi.sudoku.solver

import org.netomi.sudoku.io.GridValueLoader
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.model.PredefinedType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object SpeedTest {
    private fun solveGrid(grid: Grid, solver: GridSolver): Grid {
        val solvedGrid: Grid = solver.solve(grid)
//        return if (!solvedGrid.isSolved) {
//            val bfSolver = BruteForceSolver()
//            bfSolver.solve(solvedGrid)
//        } else {
            return solvedGrid
//        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        //GridSolver solver = new BruteForceSolver();
        val solver: GridSolver = HintSolver()
        val `is` = SpeedTest::class.java.getResourceAsStream("/all_17_clue_sudokus.txt")
        val reader = BufferedReader(InputStreamReader(`is`))
        val tests = Integer.valueOf(reader.readLine())
        val grid: Grid = Grid.of(PredefinedType.CLASSIC_9x9)
        for (i in 0 until tests) {
            val input = reader.readLine()
            grid.clear()
            grid.accept(GridValueLoader(input))
            val start = System.nanoTime()
            val grid2: Grid = solveGrid(grid, solver)
            val end = System.nanoTime()

            if (!grid2.isSolved) {
                println(input)
                println("Solved sudoku in " + (end - start) / 1e6 + " ms " + (i + 1) + " valid = " + grid2.isValid + " solved = " + grid2.isSolved)
            }
        }
    }
}