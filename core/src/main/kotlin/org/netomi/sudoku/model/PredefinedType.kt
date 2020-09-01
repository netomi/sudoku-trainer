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

/**
 * Some predefined sudoku grids.
 */
enum class PredefinedType(val gridSize: Int)
{
    CLASSIC_9x9(9) {
        override val blockMapping = intArrayOf(
            0, 0, 0, 1, 1, 1, 2, 2, 2,
            0, 0, 0, 1, 1, 1, 2, 2, 2,
            0, 0, 0, 1, 1, 1, 2, 2, 2,
            3, 3, 3, 4, 4, 4, 5, 5, 5,
            3, 3, 3, 4, 4, 4, 5, 5, 5,
            3, 3, 3, 4, 4, 4, 5, 5, 5,
            6, 6, 6, 7, 7, 7, 8, 8, 8,
            6, 6, 6, 7, 7, 7, 8, 8, 8,
            6, 6, 6, 7, 7, 7, 8, 8, 8
        )
    },
    CLASSIC_6x6(6) {
        override val blockMapping = intArrayOf(
            0, 0, 0, 1, 1, 1,
            0, 0, 0, 1, 1, 1,
            2, 2, 2, 3, 3, 3,
            2, 2, 2, 3, 3, 3,
            4, 4, 4, 5, 5, 5,
            4, 4, 4, 5, 5, 5
        )
    },
    CLASSIC_4x4(4) {
        override val blockMapping = intArrayOf(
            0, 0, 1, 1,
            0, 0, 1, 1,
            2, 2, 3, 3,
            2, 2, 3, 3
        )
    },
    JIGSAW_1(9) {
        override val blockMapping = intArrayOf(
            0, 0, 0, 1, 2, 2, 2, 2, 2,
            0, 0, 0, 1, 1, 1, 2, 2, 2,
            0, 3, 3, 3, 3, 1, 1, 1, 2,
            0, 0, 3, 4, 4, 4, 4, 1, 1,
            3, 3, 3, 3, 4, 5, 5, 5, 5,
            6, 6, 4, 4, 4, 4, 5, 7, 7,
            8, 6, 6, 6, 5, 5, 5, 5, 7,
            8, 8, 8, 6, 6, 6, 7, 7, 7,
            8, 8, 8, 8, 8, 6, 7, 7, 7
        )
    };

    internal abstract val blockMapping: IntArray

    val blockFunction = object : Grid.BlockFunction {
        override fun getBlockIndex(cellIndex: Int): Int {
            return blockMapping[cellIndex]
        }
    }
}