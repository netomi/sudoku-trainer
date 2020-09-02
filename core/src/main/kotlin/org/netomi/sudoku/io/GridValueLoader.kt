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
package org.netomi.sudoku.io

import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.model.GridVisitor
import java.io.IOException
import java.io.Reader
import java.io.StringReader

class GridValueLoader(private val reader: Reader) : GridVisitor<Grid>
{
    constructor(input: String) : this(StringReader(input))

    override fun visitGrid(grid: Grid): Grid {
        for (cell in grid.cells()) {
            var isGiven: Boolean
            while (true) {
                var ch = nextChar()
                if (ch == '+') {
                    ch = nextChar()
                    isGiven = false
                } else {
                    isGiven = true
                }
                if (isGiven(ch)) {
                    cell.setValue(("" + ch).toInt(), false)
                    cell.isGiven = isGiven
                    break
                } else if (isUnknownValue(ch)) {
                    cell.isGiven = false
                    break
                }
            }
        }
        grid.updateState()
        return grid
    }

    private fun nextChar(): Char {
        return try {
            val ch = reader.read()
            if (ch == -1) {
                throw RuntimeException("reader exhausted")
            }
            ch.toChar()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    private fun isGiven(character: Char): Boolean {
        return character in '1'..'9'
    }

    private fun isUnknownValue(character: Char): Boolean {
        return character == '0' || character == '.' || character == '-'
    }
}