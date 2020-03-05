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

import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.House;
import org.netomi.sudoku.model.HouseVisitor;
import org.netomi.sudoku.solver.HintAggregator;
import org.netomi.sudoku.solver.SolvingTechnique;

import java.util.BitSet;

/**
 * A {@code HintFinder} implementation that looks for houses
 * where a certain digit can only be placed in a single cell
 * anymore.
 */
public class HiddenSingleFinder extends AbstractHintFinder {

    @Override
    public SolvingTechnique getSolvingTechnique() {
        return SolvingTechnique.HIDDEN_SINGLE;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {
        grid.acceptHouses(new HouseVisitor() {
            @Override
            public void visitAnyHouse(House house) {
                for (int value : house.unassignedValues()) {
                    BitSet possiblePositions = house.getPotentialPositions(value);
                    if (possiblePositions.cardinality() == 1) {
                        int cellIndex = possiblePositions.nextSetBit(0);
                        addPlacementHint(grid, hintAggregator, cellIndex, value);
                    }
                }
            }
        });
    }
}
