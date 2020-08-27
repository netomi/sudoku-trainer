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

public enum SolvingTechnique {
    // Singles.
    FULL_HOUSE("Full House"),
    HIDDEN_SINGLE("Hidden Single"),
    NAKED_SINGLE("Naked Single"),

    // Intersections.
    LOCKED_CANDIDATES_TYPE_1("Locked Candidates Type 1 (Pointing)"),
    LOCKED_CANDIDATES_TYPE_2("Locked Candidates Type 2 (Claiming)"),

    // Hidden subsets.
    HIDDEN_PAIR("Hidden Pair"),
    HIDDEN_TRIPLE("Hidden Triple"),
    HIDDEN_QUADRUPLE("Hidden Quadruple"),

    // Naked subsets.
    NAKED_PAIR("Naked Pair"),
    NAKED_TRIPLE("Naked Triple"),
    NAKED_QUADRUPLE("Naked Quadruple"),

    // Locked subsets.
    LOCKED_PAIR("Locked Pair"),
    //LOCKED_TRIPLE("Locked Triple"),

    // Basic fish.
    X_WING("X-Wing"),
    SWORDFISH("Swordfish"),
    JELLYFISH("Jellyfish");

    private final String name;

    SolvingTechnique(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
