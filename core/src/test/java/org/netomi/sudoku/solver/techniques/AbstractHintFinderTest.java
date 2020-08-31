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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.netomi.sudoku.io.GridValueLoader;
import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.Grids;
import org.netomi.sudoku.model.PredefinedType;
import org.netomi.sudoku.solver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractHintFinderTest {

    private static final List<TechniqueTestCase> testCases = new ArrayList<>();

    @BeforeAll
    public static void loadTestSuite() throws IOException  {
        // only load the test cases once.
        if (!testCases.isEmpty()) {
            return;
        }

        try (InputStream is = AbstractHintFinder.class.getResourceAsStream("/reglib-1.4.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(":")) {
                    testCases.add(TechniqueTestCase.of(line));
                }
            }
        }
    }

    protected abstract HintFinder createHintFinder();

    protected abstract boolean matches(TechniqueTestCase testCase);

    @Test
    public void testRegressionTests() {
        HintFinder hintFinder = createHintFinder();
        HintSolver solver     = new HintSolver(hintFinder);

        System.out.print("Executing testcases for technique: " + hintFinder.getSolvingTechnique().getName() + " - ");

        int count = 0;
        for (TechniqueTestCase testCase : testCases) {
            if (matches(testCase)) {
                Grid grid = Grid.of(PredefinedType.CLASSIC_9x9);
                grid.accept(new GridValueLoader(testCase.getGivens()));

                for (Candidate c : testCase.getDeletedCandidates()) {
                    Cell cell = grid.getCell(c.getRow(), c.getCol());
                    cell.excludePossibleValues(false, c.getValue());
                }

                HintAggregator hints = solver.findAllHintsSingleStep(grid);

                boolean foundExpectedResult = false;
                if (testCase.expectsDirectHint()) {
                    // Some heuristic to detect if a HintFinder would suddenly find too many hints.
                    assertTrue(hints.size() <= 10, "found " + hints.size() + " hints");

                    for (Hint hint : hints) {
                        String result = ((DirectHint) hint).asString();
                        if (result.equals(testCase.getPlacement().asPlacement())) {
                            foundExpectedResult = true;
                            break;
                        }
                    }
                } else {
                    // Some heuristic to detect if a HintFinder would suddenly find too many hints.
                    assertTrue(hints.size() <= 10, "found " + hints.size() + " hints");

                    for (Hint hint : hints) {
                        Set<Candidate> candidateSet = new HashSet<>();
                        candidateSet.addAll(testCase.getEliminations());

                        IndirectHint indirectHint = (IndirectHint) hint;

                        int index = 0;
                        for (Cell cell : indirectHint.getCellIndices().allCells(grid)) {

                            for (int excludedValue : indirectHint.getExcludedValues()[index].allSetBits()) {
                                Candidate candidate =
                                        new Candidate(cell.getRow().getRowNumber(),
                                                      cell.getColumn().getColumnNumber(),
                                                      excludedValue);

                                candidateSet.remove(candidate);
                            }

                            index++;
                        }

                        if (candidateSet.isEmpty()) {
                            foundExpectedResult = true;
                            break;
                        }
                    }
                }

                if (foundExpectedResult) {
                    count++;
                } else {
                    if (testCase.expectsDirectHint()) {
                        fail("Failed to find expected result '" + testCase.getPlacement() + "' in " + hints);
                    } else {
                        fail("Failed to find expected result '" + testCase.getEliminations() + "' in " + hints);
                    }
                }
            }
        }

        System.out.println("passed " + count + " tests.");
    }

    protected static class TechniqueTestCase {
        private final String          technique;
        private final String          candidate;
        private final String          givens;
        private final List<Candidate> deletedCandidates;
        private final List<Candidate> eliminations;
        private final Candidate       placement;
        private final String          extra;

        private static TechniqueTestCase of(String line) {
            // # :<technique>:<candidate(s)>:<givens>:<deleted candidates>:<eliminations>:<placements>:<extra>

            String[] tokens = line.split(":");

            return new TechniqueTestCase(tokens[1],
                                         tokens[2],
                                         tokens[3],
                                         tokens[4],
                                         tokens.length > 5 ? tokens[5] : null,
                                         tokens.length > 6 ? tokens[6] : null,
                                         tokens.length > 7 ? tokens[7] : null);
        }

        private TechniqueTestCase(String technique,
                                  String candidate,
                                  String givens,
                                  String deletedCandidatesString,
                                  String eliminationString,
                                  String placementString,
                                  String extra) {

            this.technique    = technique;
            this.candidate    = candidate;
            this.givens       = givens;
            this.placement    = placementString != null && !placementString.isEmpty() ? Candidate.of(placementString) : null;
            this.extra        = extra;

            this.deletedCandidates = new ArrayList<>();
            for (String str : deletedCandidatesString.split(" ")) {
                if (!str.isEmpty()) {
                    this.deletedCandidates.add(Candidate.of(str));
                }
            }

            if (eliminationString != null) {
                this.eliminations = new ArrayList<>();
                for (String str : eliminationString.split(" ")) {
                    if (!str.isEmpty()) {
                        this.eliminations.add(Candidate.of(str));
                    }
                }
            } else {
                this.eliminations = Collections.EMPTY_LIST;
            }
        }

        public String getTechnique() {
            return technique;
        }

        public boolean expectsDirectHint() {
            return placement != null;
        }

        public String getGivens() {
            return givens;
        }

        public Candidate getPlacement() {
            return placement;
        }

        public Collection<Candidate> getDeletedCandidates() {
            return deletedCandidates;
        }

        public Collection<Candidate> getEliminations() {
            return eliminations;
        }

        @Override
        public String toString() {
            return String.format("%s:%s:%s:%s:%s:%s", technique, candidate, givens, deletedCandidates, eliminations, placement);
        }
    }

    public static class Candidate {
        private final int row;
        private final int col;
        private final int value;

        public static Candidate of(String str) {
            int row = Integer.parseInt("" + str.charAt(1));
            int col = Integer.parseInt("" + str.charAt(2));
            int val = Integer.parseInt("" + str.charAt(0));

            return new Candidate(row, col, val);
        }

        public Candidate(int row, int col, int value) {
            this.row   = row;
            this.col   = col;
            this.value = value;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Candidate candidate = (Candidate) o;
            return row   == candidate.row &&
                   col   == candidate.col &&
                   value == candidate.value;
        }

        public String asPlacement() {
            return String.format("r%dc%d=%d", row, col, value);
        }

        public String asElimination() {
            return toString();
        }

        @Override
        public String toString() {
            return String.format("r%dc%d<>%d", row, col, value);
        }
    }
}
