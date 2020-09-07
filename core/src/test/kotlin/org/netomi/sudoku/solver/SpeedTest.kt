package org.netomi.sudoku.solver

import org.netomi.sudoku.io.GridValueLoader
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.model.PredefinedType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object SpeedTest {
    private fun solveGrid(grid: Grid, solver: GridSolver): Grid {
        return solver.solve(grid)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val `is` = SpeedTest::class.java.getResourceAsStream("/all_17_clue_sudokus.txt")
        //val `is` = SpeedTest::class.java.getResourceAsStream("/hard_sudokus.txt")

        val reader = BufferedReader(InputStreamReader(`is`))
        val tests = Integer.valueOf(reader.readLine())

        val grid: Grid = Grid.of(PredefinedType.CLASSIC_9x9)
        val solver = BruteForceSolver()
        //val solver = HintSolver()

        for (i in 0 until tests) {
            val input = reader.readLine()
            grid.clear()
            grid.accept(GridValueLoader(input))
            val start = System.nanoTime()
            val result: Grid = solveGrid(grid, solver)
            val end = System.nanoTime()

            if (!result.isSolved) {
                println(input)
                println("Solved " + (i + 1) + " sudoku in " + (end - start) / 1e6 + " ms, valid = " + result.isValid + ", solved = " + result.isSolved)
            }
        }
    }
}