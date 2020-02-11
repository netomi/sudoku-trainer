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

import org.netomi.sudoku.model.*;
import org.netomi.sudoku.solver.HintAggregator;
import org.netomi.sudoku.solver.SolvingTechnique;

import java.util.BitSet;

public class LockedCandidatesType1Finder extends AbstractLockedCandidatesFinder {

    @Override
    public SolvingTechnique getSolvingTechnique() {
        return SolvingTechnique.LOCKED_CANDIDATES_TYPE_1;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {

        grid.acceptBlocks(new HouseVisitor() {
            @Override
            public void visitAnyHouse(House house) {

                for (int value = 1; value <= grid.getGridSize(); value++) {
                    BitSet possiblePositions = house.getPotentialPositions(value);

                    if (possiblePositions.cardinality() == 0) {
                        continue;
                    }

                    // Check if all possible cells are in the same row.
                    for (House row : grid.rows()) {
                        if (row.containsAllCells(possiblePositions)) {
                            addHint(grid, hintAggregator, house, row, value);
                        }
                    }

                    // Check if all possible cells are in the same column.
                    for (House column : grid.columns()) {
                        if (column.containsAllCells(possiblePositions)) {
                            addHint(grid, hintAggregator, house, column, value);
                        }
                    }
                }
            }
        });
    }
}
