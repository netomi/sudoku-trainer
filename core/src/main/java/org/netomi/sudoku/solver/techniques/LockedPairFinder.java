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

import org.netomi.sudoku.solver.SolvingTechnique;

/**
 * A {@code HintFinder} implementation that looks for houses, where a pair
 * of cells has the same two candidates left, forming a locked pair if they
 * are on the same row or column. The same candidates in other cells of the
 * same house and row / column can be removed.
 */
public class LockedPairFinder extends NakedPairFinder
{
    public LockedPairFinder() {
        super(true);
    }

    @Override
    public SolvingTechnique getSolvingTechnique() {
        return SolvingTechnique.LOCKED_PAIR;
    }
}
