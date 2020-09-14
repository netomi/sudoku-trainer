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
package org.netomi.sudoku.solver

import org.netomi.sudoku.solver.techniques.*

enum class SolvingTechnique(val techniqueName: String, val supplier: () -> HintFinder)
{
    // Singles.
    FULL_HOUSE("Full House", ::FullHouseFinder),
    NAKED_SINGLE("Naked Single", ::NakedSingleFinder),
    HIDDEN_SINGLE("Hidden Single", ::HiddenSingleFinder),

    // Locked subsets.
    LOCKED_PAIR("Locked Pair", ::LockedPairFinder),
    LOCKED_TRIPLE("Locked Triple", ::LockedTripleFinder),

    // Intersections.
    LOCKED_CANDIDATES_TYPE_1("Locked Candidates Type 1 (Pointing)", ::LockedCandidatesType1Finder),
    LOCKED_CANDIDATES_TYPE_2("Locked Candidates Type 2 (Claiming)", ::LockedCandidatesType2Finder),

    // Hidden subsets.
    HIDDEN_PAIR("Hidden Pair", ::HiddenPairFinder),
    HIDDEN_TRIPLE("Hidden Triple", ::HiddenTripleFinder),
    HIDDEN_QUADRUPLE("Hidden Quadruple", ::HiddenQuadrupleFinder),

    // Naked subsets.
    NAKED_PAIR("Naked Pair", ::NakedPairFinder),
    NAKED_TRIPLE("Naked Triple", ::NakedTripleFinder),
    NAKED_QUADRUPLE("Naked Quadruple", ::NakedQuadrupleFinder),

    // Basic fish.
    X_WING("X-Wing", ::XWingHintFinder),
    SWORDFISH("Swordfish", ::SwordFishFinder),
    JELLYFISH("Jellyfish", ::JellyFishFinder),

    // Single digit patterns.
    SKYSCRAPER("Skyscraper", ::SkyscraperFinder),
    TWO_STRING_KITE("2-String Kite", ::TwoStringKiteFinder),

    // Uniqueness tests.
    UNIQUE_RECTANGLE_TYPE_1("Unique Rectangle Type 1", ::UniqueRectangleType1Finder),
    UNIQUE_RECTANGLE_TYPE_2("Unique Rectangle Type 2", ::UniqueRectangleType2Finder),
    // UNIQUE_RECTANGLE_TYPE_3("Unique Rectangle Type 3", ::UniqueRectangleType3Finder),
    UNIQUE_RECTANGLE_TYPE_4("Unique Rectangle Type 4", ::UniqueRectangleType4Finder),

    // Chains.
    REMOTE_PAIR("Remote Pair", ::RemotePairFinder),
    X_CHAIN("X-Chain", ::XChainFinder),
    XY_CHAIN("XY-Chain", ::XYChainFinder);
}