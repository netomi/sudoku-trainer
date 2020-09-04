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
package org.netomi.sudoku.model

import java.util.ArrayList
import java.util.HashSet

class ConflictDetector : GridVisitor<Array<Conflict>>
{
    override fun visitGrid(grid: Grid): Array<Conflict> {
        val foundConflicts = HashSet<CellSet>()
        val conflicts      = ArrayList<Conflict>()

        for (house in grid.houses()) {
            for (cell in house.assignedCells()) {
                val value = cell.value

                val conflictPeers = cell.peerSet.filteredCells(grid, { c -> c.isAssigned && c.value == value })
                val conflictCells = MutableCellSet.of(grid, conflictPeers)

                if (conflictCells.cardinality() > 0) {
                    conflictCells.set(cell.cellIndex)
                    if (!foundConflicts.contains(conflictCells)) {
                        foundConflicts.add(conflictCells)
                        conflicts.add(Conflict(conflictCells))
                    }
                }
            }
        }
        return conflicts.toArray(emptyArray())
    }
}