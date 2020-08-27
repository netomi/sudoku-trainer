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

import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.House;
import org.netomi.sudoku.model.HouseVisitor;
import org.netomi.sudoku.solver.HintAggregator;
import org.netomi.sudoku.solver.SolvingTechnique;

import java.util.BitSet;

/**
 * A {@code HintFinder} implementation to look for houses which have a
 * single missing digit to place.
 */
public class FullHouseFinder extends AbstractHintFinder {

    @Override
    public SolvingTechnique getSolvingTechnique() {
        return SolvingTechnique.FULL_HOUSE;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {
        final int expectedCardinality = grid.getGridSize() - 1;

        grid.acceptHouses(house -> {
            BitSet assignedValues = house.getAssignedValues();
            if (assignedValues.cardinality() == expectedCardinality) {
                int value = assignedValues.nextClearBit(1);

                // Find the cell that is not assigned, and create a hint for it.
                for (Cell cell : house.cells()) {
                    if (!cell.isAssigned()) {
                        placeValueInCell(grid, hintAggregator, cell.getCellIndex(), value);
                    }
                }
            }
        });
    }
}
