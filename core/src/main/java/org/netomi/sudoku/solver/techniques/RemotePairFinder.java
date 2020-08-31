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

import java.util.*;

/**
 * A {@code HintFinder} implementation ...
 */
public class RemotePairFinder extends AbstractHintFinder
{
    @Override
    public SolvingTechnique getSolvingTechnique() {
        return SolvingTechnique.REMOTE_PAIR;
    }

    @Override
    public void findHints(Grid grid, HintAggregator hintAggregator) {
        Set<CellSet> visitedChains = new HashSet<>();

        grid.acceptCells(cell -> {
            ValueSet possibleValues = cell.getPossibleValues();
            if (possibleValues.cardinality() != 2) {
                return;
            }

            findChain(grid, hintAggregator, cell, new Chain(grid, cell), visitedChains, 1);
        });
    }

    private void findChain(Grid           grid,
                           HintAggregator hintAggregator,
                           Cell           currentCell,
                           Chain          currentChain,
                           Set<CellSet>   visitedChains,
                           int            length) {

        currentChain.addLink(currentCell);
        ValueSet possibleValues = currentCell.getPossibleValues();

        // make sure we do not add chains twice: in forward and reverse order.
        if (visitedChains.contains(currentChain.cells)) {
            return;
        }

        if (length > 3 && length % 2 == 0) {
            CellSet affectedCells = Grids.toCellSet(grid, currentCell.peers());
            affectedCells.andNot(currentChain.cells);

            for (Cell affectedCell : affectedCells.allCells(grid)) {
                CellSet peers     = Grids.toCellSet(grid, affectedCell.peers());
                CellSet endPoints = CellSet.of(currentChain.startCell, currentCell);
                peers.and(endPoints);
                if (peers.cardinality() < 2) {
                    affectedCells.clear(affectedCell.getCellIndex());
                }
            }

            if (eliminateValuesFromCells(grid, hintAggregator, affectedCells, possibleValues)) {
                visitedChains.add(currentChain.cells.copy());
            }
        }

        for (Cell nextCell : currentCell.peers()) {
            if (currentChain.contains(nextCell)) {
                continue;
            }

            ValueSet possibleValuesOfNextCell = nextCell.getPossibleValues();
            if (possibleValuesOfNextCell.cardinality() != 2 ||
                !possibleValues.equals(possibleValuesOfNextCell)) {
                continue;
            }

            findChain(grid, hintAggregator, nextCell, currentChain, visitedChains, length + 1);
        }

        currentChain.removeLink(currentCell);
    }

    private static class Chain {
        private final Cell    startCell;
        private final CellSet cells;

        Chain(Grid grid, Cell startCell) {
            this.startCell = startCell;
            this.cells     = CellSet.empty(grid);
            this.cells.set(startCell.getCellIndex());
        }

        public void addLink(Cell cell) {
            cells.set(cell.getCellIndex());
        }

        public void removeLink(Cell cell) {
            cells.clear(cell.getCellIndex());
        }

        public boolean contains(Cell cell) {
            return cells.get(cell.getCellIndex());
        }
    }
}
