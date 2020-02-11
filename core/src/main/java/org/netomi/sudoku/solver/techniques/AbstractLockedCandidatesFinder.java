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
import org.netomi.sudoku.solver.HintAggregator;
import org.netomi.sudoku.solver.HintFinder;
import org.netomi.sudoku.solver.IndirectHint;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractLockedCandidatesFinder implements HintFinder {

    protected void addHint(Grid grid, HintAggregator hintAggregator, House house, House other, int value) {
        List<Cell> affectedCells = new ArrayList<>();
        for (Cell cell : other.cells()) {
            if (!house.containsCell(cell) &&
                cell.getPossibleValues().get(value)) {
                affectedCells.add(cell);
            }
        }

        if (!affectedCells.isEmpty()) {
            hintAggregator.addHint(new IndirectHint(grid.getType(),
                                                    getSolvingTechnique(),
                                                    toCellIndexList(affectedCells),
                                                    new int[] { value }));
        }
    }

    private static int[] toCellIndexList(List<Cell> cells) {
        int[] result = new int[cells.size()];

        for (int i = 0; i < cells.size(); i++) {
            result[i] = cells.get(i).getCellIndex();
        }

        return result;
    }
}
