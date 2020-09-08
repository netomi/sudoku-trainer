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
package org.netomi.sudoku.ui

import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val sudokuGrid by cssclass()
        val sudokuCell by cssclass()

        val cellAssignedValue   by cssclass()
        val cellGivenValue      by cssclass()
        val cellPossibleValue   by cssclass()
        val cellFocus           by cssclass()
        val cellValueConflict   by cssclass()
        val cellAssigmentHint   by cssclass()
        val cellEliminationHint by cssclass()
        val cellHighlight       by cssclass()
    }

    init {
        sudokuGrid {
            borderColor += box(Color.BLACK)
            borderWidth += box(2.px)
            padding      = box(2.px)
        }

        sudokuCell {
            borderColor += box(Color.GRAY)
            borderWidth += box(1.px)
        }

        cellAssignedValue {
            textFill = Color.BLUE
            fontSize = 3.em
        }

        cellGivenValue {
            textFill = Color.BLACK
            fontSize = 3.em
        }

        cellPossibleValue {
            textFill  = Color.GRAY
            fontSize  = 1.5.em
            alignment = Pos.CENTER
        }

        cellValueConflict {
            textFill = Color.RED
        }

        cellFocus {
            backgroundColor += Color.YELLOW
        }

        cellAssigmentHint {
            backgroundRadius += box(2.em)
            backgroundColor  += Color.LIMEGREEN
            textFill = Color.BLACK
        }

        cellEliminationHint {
            backgroundRadius += box(2.em)
            backgroundColor  += Color.CORAL
            textFill = Color.BLACK
        }

        cellHighlight {
            backgroundColor += Color.LIGHTSTEELBLUE
        }

        listCell and empty {
            backgroundColor += Color.WHITE
            borderColor     += box(Color.WHITE)
        }
    }
}