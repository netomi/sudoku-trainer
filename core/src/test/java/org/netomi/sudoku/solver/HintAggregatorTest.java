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

import org.junit.Assert;
import org.junit.Test;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.PredefinedType;

public class HintAggregatorTest {

    @Test
    public void duplicateHints() {
        HintAggregator aggregator = new HintAggregator();

        Grid grid = Grid.of(PredefinedType.CLASSIC_9x9);

        Hint hint = new DirectHint(grid.getType(), SolvingTechnique.FULL_HOUSE, 0, 1);

        aggregator.addHint(hint);
        aggregator.addHint(hint);

        Assert.assertEquals(1, aggregator.hints.size());
    }

    @Test
    public void differentHints() {
        HintAggregator aggregator = new HintAggregator();

        Grid grid = Grid.of(PredefinedType.CLASSIC_9x9);

        Hint hint1 = new DirectHint(grid.getType(), SolvingTechnique.FULL_HOUSE, 0, 1);
        Hint hint2 = new DirectHint(grid.getType(), SolvingTechnique.FULL_HOUSE, 1, 2);

        aggregator.addHint(hint1);
        aggregator.addHint(hint2);

        Assert.assertEquals(2, aggregator.hints.size());
    }
}
