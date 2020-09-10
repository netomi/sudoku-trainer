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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.netomi.sudoku.model.Cell
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.model.PredefinedType
import org.netomi.sudoku.model.ValueSet
import java.lang.IllegalArgumentException

class ChainTest
{
    companion object {
        val grid: Grid = Grid.of(PredefinedType.CLASSIC_9x9)
    }

    @Test
    fun add() {
        val chain = Chain(grid, 0, 1)

        chain.addLink(LinkType.STRONG, 0, 5)
        chain.addLink(LinkType.WEAK, 8, 5)

        assertEquals(3, chain.length)
    }

    @Test
    fun remove() {
        val chain = Chain(grid, 0, 1)

        assertThrows<IllegalArgumentException> {
            chain.removeLastLink()
        }

        chain.addLink(LinkType.WEAK, 1, 2)
        chain.removeLastLink()

        assertEquals(1, chain.length)
    }

    @Test
    fun copy() {
        val chain = Chain(grid, 0, 1)
        chain.addLink(LinkType.WEAK, 1, 2)

        assertEquals(2, chain.length)

        val copy = chain.copy()
        assertEquals(2, copy.length)

        chain.addLink(LinkType.WEAK, 2, 3)
        assertEquals(2, copy.length)
    }

    @Test
    fun length() {
        val chain = Chain(grid, 0, 1)
        assertEquals(1, chain.length)

        chain.addLink(LinkType.WEAK, 1, 2)
        assertEquals(2, chain.length)

        chain.addLink(LinkType.STRONG, 2, 3)
        assertEquals(3, chain.length)

        chain.removeLastLink()
        assertEquals(2, chain.length)
    }

    @Test
    fun accept() {
        val chain = Chain(grid, 0, 1)

        chain.addLink(LinkType.STRONG, 0, 2)
        chain.addLink(LinkType.WEAK, 1, 2)
        chain.addLink(LinkType.STRONG, 1, 1)

        chain.accept(grid, object: ChainVisitor {
            override fun visitCell(grid: Grid, chain: Chain, cell: Cell, activeValues: ValueSet, inactiveValues: ValueSet) {
                println("%s %s %s".format(cell.name, activeValues, inactiveValues))
            }

            override fun visitCellLink(grid: Grid, chain: Chain, fromCell: Cell, fromCandidate: Int, toCell: Cell, toCandidate: Int, linkType: LinkType) {
                println("%s %s %s".format(fromCell.name, linkType, toCell.name))
            }
        })
    }
}