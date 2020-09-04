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
import java.util.function.Supplier

enum class SolvingTechnique(val techniqueName: String, val supplier: Supplier<HintFinder>)
{
    // Singles.
    FULL_HOUSE("Full House", ::FullHouseFinder),
    HIDDEN_SINGLE("Hidden Single", ::HiddenSingleFinder),
    NAKED_SINGLE("Naked Single", ::NakedSingleFinder),

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

    // Locked subsets.
    LOCKED_PAIR("Locked Pair", ::LockedPairFinder),
    LOCKED_TRIPLE("Locked Triple", ::LockedTripleFinder),

    // Basic fish.
    X_WING("X-Wing", ::XWingHintFinder),
    SWORDFISH("Swordfish", ::SwordFishFinder),
    JELLYFISH("Jellyfish", ::JellyFishFinder),

    // chains.
    REMOTE_PAIR("Remote Pair", ::RemotePairFinder);
}