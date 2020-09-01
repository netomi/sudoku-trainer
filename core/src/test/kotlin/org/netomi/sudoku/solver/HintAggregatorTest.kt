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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.model.PredefinedType

class HintAggregatorTest {
    @Test
    fun duplicateHints() {
        val aggregator = HintAggregator()
        val grid: Grid = Grid.of(PredefinedType.CLASSIC_9x9)
        val hint: Hint = DirectHint(grid.type, SolvingTechnique.FULL_HOUSE, 0, 1)
        aggregator.addHint(hint)
        aggregator.addHint(hint)
        Assertions.assertEquals(1, aggregator.hints.size)
    }

    @Test
    fun differentHints() {
        val aggregator = HintAggregator()
        val grid: Grid = Grid.of(PredefinedType.CLASSIC_9x9)
        val hint1: Hint = DirectHint(grid.type, SolvingTechnique.FULL_HOUSE, 0, 1)
        val hint2: Hint = DirectHint(grid.type, SolvingTechnique.FULL_HOUSE, 1, 2)
        aggregator.addHint(hint1)
        aggregator.addHint(hint2)
        Assertions.assertEquals(2, aggregator.hints.size)
    }
}