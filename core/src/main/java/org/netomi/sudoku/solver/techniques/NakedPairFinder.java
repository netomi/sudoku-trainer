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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * A {@code HintFinder} implementation that looks for houses
 * where a pair of cells has the same two candidates left,
 * forming a naked pair. The same candidates in other cells
 * of the same house can be removed.
 */
public class NakedPairFinder extends AbstractHintFinder {

    @Override
    public SolvingTechnique getSolvingTechnique() {
        return SolvingTechnique.NAKED_PAIR;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {
        grid.acceptHouses(new HouseVisitor() {
            @Override
            public void visitAnyHouse(House house) {
                for (Cell cell : house.cells()) {
                    BitSet possibleValues = cell.getPossibleValues();

                    if (possibleValues.cardinality() != 2) {
                        continue;
                    }

                    for (Cell otherCell : house.cells(cell.getCellIndex() + 1)) {

                        BitSet otherPossibleValues = otherCell.getPossibleValues();

                        if (otherPossibleValues.cardinality() != 2) {
                            continue;
                        }

                        // If they two bitsets containing the possible candidate values
                        // have the same candidates, we have found a naked pair.
                        BitSet matching = new BitSet();
                        matching.or(possibleValues);
                        matching.xor(otherPossibleValues);

                        if (matching.cardinality() == 0) {
                            addIndirectHint(grid, hintAggregator, house, Arrays.asList(cell, otherCell), toIntArray(possibleValues));
                        }
                    }
                }
            }
        });
    }

    private void addIndirectHint(Grid           grid,
                                 HintAggregator hintAggregator,
                                 House          affectedHouse,
                                 List<Cell>     affectedCells,
                                 int[]          affectedValues) {

        List<Cell>  cellsToModify       = new ArrayList<>();
        List<int[]> valuesToExcludeList = new ArrayList<>();

        // All other cells in the same house shall have the given values
        // removed from their set of candidates.
        for (Cell cell : affectedHouse.cells()) {
            if (!affectedCells.contains(cell)) {
                int[] valuesToExclude = toIntArrayIncluding(cell.getPossibleValues(), affectedValues);

                if (valuesToExclude.length > 0) {
                    cellsToModify.add(cell);
                    valuesToExcludeList.add(valuesToExclude);
                }
            }
        }

        if (!cellsToModify.isEmpty()) {
            addIndirectHint(grid, hintAggregator, toCellIndexArray(cellsToModify), valuesToExcludeList.toArray(new int[0][]));
        }
    }
}
