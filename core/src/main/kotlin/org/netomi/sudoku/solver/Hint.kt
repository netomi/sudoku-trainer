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

import org.netomi.sudoku.model.CellSet
import org.netomi.sudoku.model.Grid

abstract class Hint protected constructor(val gridType:         Grid.Type,
                                          val solvingTechnique: SolvingTechnique,
                                          val relatedCells:     CellSet) {
    abstract val description: String

    abstract fun apply(targetGrid: Grid, updateGrid: Boolean)

    abstract fun revert(targetGrid: Grid, updateGrid: Boolean)

    abstract fun accept(visitor: HintVisitor)

    override fun hashCode(): Int {
        var result = gridType.hashCode()
        result = 31 * result + solvingTechnique.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Hint) return false

        if (gridType != other.gridType) return false
        if (solvingTechnique != other.solvingTechnique) return false

        return true
    }

    override fun toString(): String {
        return "%s: %s".format(solvingTechnique.techniqueName, description)
    }
}

fun interface HintVisitor
{
    fun visitAnyHint(hint: Hint)

    fun visitAssignmentHint(hint: AssignmentHint) {
        visitAnyHint(hint)
    }

    fun visitEliminationHint(hint: EliminationHint) {
        visitAnyHint(hint)
    }

    fun visitChainEliminationHint(hint: ChainEliminationHint) {
        visitAnyHint(hint)
    }
}