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
package org.netomi.sudoku.solver.techniques

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.netomi.sudoku.io.GridValueLoader
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.model.PredefinedType
import org.netomi.sudoku.solver.DirectHint
import org.netomi.sudoku.solver.HintFinder
import org.netomi.sudoku.solver.HintSolver
import org.netomi.sudoku.solver.IndirectHint
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

abstract class BaseHintFinderTest
{
    protected abstract fun createHintFinder(): HintFinder

    protected abstract fun matches(testCase: TechniqueTestCase): Boolean

    @Test
    fun testRegressionTests() {
        val hintFinder = createHintFinder()
        val solver = HintSolver(hintFinder)

        print("Executing testcases for technique: " + hintFinder.solvingTechnique.techniqueName + " - ")

        var count = 0
        for (testCase in testCases) {
            if (matches(testCase)) {
                val grid = Grid.of(PredefinedType.CLASSIC_9x9)
                grid.accept(GridValueLoader(testCase.givens))
                for (c in testCase.getDeletedCandidates()) {
                    val cell = grid.getCell(c.row, c.col)
                    cell.excludePossibleValues(false, c.value)
                }
                val hints = solver.findAllHintsSingleStep(grid)
                var foundExpectedResult = false
                if (testCase.expectsDirectHint()) {
                    // Some heuristic to detect if a HintFinder would suddenly find too many hints.
                    assertTrue(hints.size() <= 10, "found " + hints.size() + " hints")
                    for (hint in hints) {
                        val result = (hint as DirectHint).asString()
                        if (result == testCase.placement!!.asPlacement()) {
                            foundExpectedResult = true
                            break
                        }
                    }
                } else {
                    // Some heuristic to detect if a HintFinder would suddenly find too many hints.
                    assertTrue(hints.size() <= 10, "found " + hints.size() + " hints")
                    for (hint in hints) {
                        val candidateSet: MutableSet<Candidate> = HashSet()
                        candidateSet.addAll(testCase.getEliminations())
                        val indirectHint = hint as IndirectHint
                        var index = 0
                        for (cell in indirectHint.cellIndices.allCells(grid)) {
                            for (excludedValue in indirectHint.excludedValues[index].allSetBits()) {
                                val candidate = Candidate(cell.row.rowNumber,
                                                          cell.column.columnNumber,
                                                          excludedValue)
                                candidateSet.remove(candidate)
                            }
                            index++
                        }
                        if (candidateSet.isEmpty()) {
                            foundExpectedResult = true
                            break
                        }
                    }
                }
                if (foundExpectedResult) {
                    count++
                } else {
                    if (testCase.expectsDirectHint()) {
                        fail("Failed to find expected result '" + testCase.placement + "' in " + hints)
                    } else {
                        fail("Failed to find expected result '" + testCase.getEliminations() + "' in " + hints)
                    }
                }
            }
        }
        println("passed $count tests.")
    }

    protected class TechniqueTestCase private constructor(val technique: String,
                                                          private val candidate: String,
                                                          val givens: String,
                                                          deletedCandidatesString: String,
                                                          eliminationString: String?,
                                                          placementString: String?,
                                                          val extra: String?) {
        private val deletedCandidates: MutableList<Candidate>
        private var eliminations:      MutableList<Candidate> = mutableListOf()

        val placement: Candidate?

        fun expectsDirectHint(): Boolean {
            return placement != null
        }

        fun getDeletedCandidates(): Collection<Candidate> {
            return deletedCandidates
        }

        fun getEliminations(): Collection<Candidate> {
            return eliminations
        }

        override fun toString(): String {
            return "%s:%s:%s:%s:%s:%s".format(technique, candidate, givens, deletedCandidates, eliminations, placement)
        }

        companion object {
            fun of(line: String?): TechniqueTestCase { // # :<technique>:<candidate(s)>:<givens>:<deleted candidates>:<eliminations>:<placements>:<extra>
                val tokens = line!!.split(":").toTypedArray()
                return TechniqueTestCase(tokens[1],
                        tokens[2],
                        tokens[3],
                        tokens[4],
                        if (tokens.size > 5) tokens[5] else null,
                        if (tokens.size > 6) tokens[6] else null,
                        if (tokens.size > 7) tokens[7] else null)
            }
        }

        init {
            placement = if (placementString != null && placementString.isNotEmpty()) Candidate.of(placementString) else null

            deletedCandidates = mutableListOf()
            for (str in deletedCandidatesString.split(" ").toTypedArray()) {
                if (str.isNotEmpty()) {
                    deletedCandidates.add(Candidate.of(str))
                }
            }

            eliminationString?.let {
                for (str in it.split(" ").toTypedArray()) {
                    if (str.isNotEmpty()) {
                        eliminations.add(Candidate.of(str))
                    }
                }
            }
        }
    }

    class Candidate(val row: Int, val col: Int, val value: Int) {

        override fun hashCode(): Int {
            return Objects.hash(row, col, value)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val candidate = other as Candidate
            return row == candidate.row && col == candidate.col && value == candidate.value
        }

        fun asPlacement(): String {
            return "r%dc%d=%d".format(row, col, value)
        }

        fun asElimination(): String {
            return toString()
        }

        override fun toString(): String {
            return "r%dc%d<>%d".format(row, col, value)
        }

        companion object {
            fun of(str: String): Candidate {
                val row = ("" + str[1]).toInt()
                val col = ("" + str[2]).toInt()
                val `val` = ("" + str[0]).toInt()
                return Candidate(row, col, `val`)
            }
        }

    }

    companion object {
        private val testCases: MutableList<TechniqueTestCase> = ArrayList()
        @BeforeAll
        @JvmStatic
        @Throws(IOException::class)
        fun loadTestSuite() {
            // only load the test cases once.
            if (testCases.isNotEmpty()) {
                return
            }
            HintFinder::class.java.getResourceAsStream("/reglib-1.4.txt").use { `is` ->
                BufferedReader(InputStreamReader(`is`)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.startsWith(":")) {
                            testCases.add(TechniqueTestCase.of(line))
                        }
                    }
                }
            }
        }
    }
}