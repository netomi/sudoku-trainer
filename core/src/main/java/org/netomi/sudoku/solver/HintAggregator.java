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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class HintAggregator implements Iterable<Hint>
{
    protected final Collection<Hint> hints;

    public HintAggregator() {
        hints = new LinkedHashSet<>();
    }

    public void addHint(Hint hint) {
        if (!hints.contains(hint)) {
            hints.add(hint);
        }
    }

    public void applyHints(Grid targetGrid) {
        for (Hint hint : hints) {
            hint.apply(targetGrid, false);
        }
        targetGrid.updateState();
    }

    public Collection<Hint> getHints() {
        return hints;
    }

    @Override
    public Iterator<Hint> iterator() {
        return hints.iterator();
    }

    public int size() {
        return hints.size();
    }

    @Override
    public String toString() {
        return hints.toString();
    }
}
