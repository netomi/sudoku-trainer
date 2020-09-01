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

import org.netomi.sudoku.model.Grid
import java.util.*
import kotlin.collections.LinkedHashSet

open class HintAggregator : Sequence<Hint>
{
    val hints: MutableCollection<Hint> = LinkedHashSet()

    open fun addHint(hint: Hint) {
        if (!hints.contains(hint)) {
            hints.add(hint)
        }
    }

    fun applyHints(targetGrid: Grid) {
        hints.forEach { hint -> hint.apply(targetGrid, false) }
        targetGrid.updateState()
    }

    override fun iterator(): Iterator<Hint> {
        return hints.iterator()
    }

    fun size(): Int {
        return hints.size
    }

    override fun toString(): String {
        return hints.toString()
    }
}