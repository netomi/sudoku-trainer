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
package org.netomi.sudoku.solver.techniques;

import org.netomi.sudoku.model.CellSet;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.ValueSet;
import org.netomi.sudoku.solver.HintAggregator;
import org.netomi.sudoku.solver.SolvingTechnique;

/**
 * A {@code HintFinder} implementation that looks for houses
 * where a pair of candidates is constrained to the same two
 * cells, forming a hidden pair. Any other candidate in these
 * cells can be removed.
 */
public class HiddenPairFinder extends AbstractHintFinder
{
    @Override
    public SolvingTechnique getSolvingTechnique() {
        return SolvingTechnique.HIDDEN_PAIR;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {
        grid.acceptHouses(house -> {
            for (int value : house.unassignedValues()) {
                CellSet potentialPositions = house.getPotentialPositions(value);
                if (potentialPositions.cardinality() != 2) {
                    continue;
                }

                for (int otherValue : house.unassignedValues(value + 1)) {
                    CellSet otherPotentialPositions = house.getPotentialPositions(otherValue);
                    if (otherPotentialPositions.cardinality() != 2) {
                        continue;
                    }

                    // If the two bitsets, containing the possible positions for some values,
                    // share the exact same positions, we have found a hidden pair.
                    if (potentialPositions.equals(otherPotentialPositions)) {
                        ValueSet allowedValues = ValueSet.of(grid, value, otherValue);
                        eliminateNotAllowedValuesFromCells(grid, hintAggregator, potentialPositions, allowedValues);
                    }
                }
            }
        });
    }
}
