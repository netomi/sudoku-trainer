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
package org.netomi.sudoku.solver

import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.solver.techniques.*
import java.util.*

class HintSolver : GridSolver
{
    private val finderList: MutableList<HintFinder> = ArrayList()

    constructor() {
        for (technique in SolvingTechnique.values()) {
            finderList.add(technique.supplier.get())
        }
    }

    constructor(finder: HintFinder) {
        finderList.add(finder)
    }

    constructor(vararg finder: HintFinder) {
        finderList.addAll(finder)
    }

    override fun solve(grid: Grid): Grid {
        val searchGrid = grid.copy()

        while (!searchGrid.isSolved) {
            val hintAggregator = SingleHintAggregator()

            try {
                for (hintFinder in finderList) {
                    hintFinder.findHints(searchGrid, hintAggregator)
                }
            } catch (ex: HintAggregatorExhaustedException) {
                // do nothing
            } catch (ex: java.lang.RuntimeException) {
                ex.printStackTrace()
            }

            if (hintAggregator.size() == 0) {
                break
            }

            hintAggregator.applyHints(searchGrid)
        }
        return searchGrid
    }

    fun findAllHintsSingleStep(grid: Grid): HintAggregator {
        val searchGrid     = grid.copy()
        val hintAggregator = HintAggregator()

        try {
            for (hintFinder in finderList) {
                hintFinder.findHints(searchGrid, hintAggregator)
            }
        } catch (ex: HintAggregatorExhaustedException) {
            // do nothing
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        }

        return hintAggregator
    }

    fun findAllHints(grid: Grid): HintAggregator {
        val searchGrid = grid.copy()
        val allHints   = HintAggregator()

        while (!searchGrid.isSolved) {
            val hintAggregator = SingleHintAggregator()
            try {
                for (hintFinder in finderList) {
                    hintFinder.findHints(searchGrid, hintAggregator)
                }
            } catch (ex: HintAggregatorExhaustedException) {
                // do nothing
            } catch (ex: RuntimeException) {
                ex.printStackTrace()
            }

            if (hintAggregator.hints.isEmpty()) {
                break
            }

            hintAggregator.applyHints(searchGrid)
            allHints.hints.addAll(hintAggregator.hints)
        }

        return allHints
    }

    fun findNextHint(grid: Grid): HintAggregator {
        val hintAggregator = SingleHintAggregator()

        try {
            for (hintFinder in finderList) {
                hintFinder.findHints(grid, hintAggregator)
            }
        } catch (ex: HintAggregatorExhaustedException) {
            // do nothing
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        }

        return hintAggregator
    }
}