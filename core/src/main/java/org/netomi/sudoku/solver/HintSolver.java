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

import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.solver.techniques.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HintSolver implements GridSolver {

    private final List<HintFinder> finderList = new ArrayList<>();

    public HintSolver() {
        finderList.add(new FullHouseFinder());
        finderList.add(new NakedSingleFinder());
        finderList.add(new HiddenSingleFinder());
        finderList.add(new LockedCandidatesType1Finder());
        finderList.add(new LockedCandidatesType2Finder());
        finderList.add(new HiddenPairFinder());
        finderList.add(new HiddenTripleFinder());
        finderList.add(new HiddenQuadrupleFinder());
        finderList.add(new NakedPairFinder());
        finderList.add(new NakedTripleFinder());
        finderList.add(new NakedQuadrupleFinder());
        finderList.add(new XWingHintFinder());
        finderList.add(new SwordFishFinder());
        finderList.add(new JellyFishFinder());
    }

    public HintSolver(HintFinder finder) {
        finderList.add(finder);
    }

    public HintSolver(HintFinder... finder) {
        finderList.addAll(Arrays.asList(finder));
    }

    public Grid solve(Grid grid) {
        Grid searchGrid = grid.copy();

        while (!searchGrid.isSolved()) {
            HintAggregator hintAggregator = new SingleHintAggregator();

            try {
                for (HintFinder hintFinder : finderList) {
                    hintFinder.findHints(searchGrid, hintAggregator);
                }
            } catch (RuntimeException ex) {}

            if (hintAggregator.size() == 0) {
                break;
            }

            hintAggregator.applyHints(searchGrid);
        }

        return searchGrid;
    }

    public HintAggregator findAllHintsSingleStep(Grid grid) {
        Grid searchGrid = grid.copy();
        searchGrid.updateState();

        HintAggregator hintAggregator = new HintAggregator();

        try {
            for (HintFinder hintFinder : finderList) {
                hintFinder.findHints(searchGrid, hintAggregator);
            }
        } catch (RuntimeException ex) {}

        return hintAggregator;
    }

    public HintAggregator findHint(Grid grid) {
        Grid searchGrid = grid.copy();
        searchGrid.updateState();

        HintAggregator hintAggregator = new SingleHintAggregator();

        try {
            for (HintFinder hintFinder : finderList) {
                hintFinder.findHints(searchGrid, hintAggregator);
            }
        } catch (RuntimeException ex) {}

        return hintAggregator;
    }

    public HintAggregator findDirectHint(Grid grid) {
        HintAggregator hintAggregator = new SingleHintAggregator();

        try {
            for (HintFinder hintFinder : finderList) {
                hintFinder.findHints(grid, hintAggregator);
            }
        } catch (RuntimeException ex) {}

        return hintAggregator;
    }

    public HintAggregator findHints(Grid grid) {
        Grid searchGrid = grid.copy();

        HintAggregator allHints = new HintAggregator();

        while (!searchGrid.isSolved()) {
            //searchGrid.updateState();

            HintAggregator hintAggregator = new SingleHintAggregator();

            try {
                for (HintFinder hintFinder : finderList) {
                    hintFinder.findHints(searchGrid, hintAggregator);
                }
            } catch (RuntimeException ex) {}

            if (hintAggregator.hints.isEmpty()) {
                break;
            }

            hintAggregator.applyHints(searchGrid);

            allHints.hints.addAll(hintAggregator.hints);
        }

        return allHints;
    }
}
