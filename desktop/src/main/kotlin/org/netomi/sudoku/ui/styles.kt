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

import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val grid by cssclass()
        val cell by cssclass()

        val cellAssignedValue by cssclass()
        val cellGivenValue    by cssclass()
        val cellPossibleValue by cssclass()
        val cellFocus         by cssclass()
        val cellValueConflict by cssclass()
    }

    init {
        root {
            prefHeight = 600.px
            prefWidth  = 800.px
        }

        grid {
            borderColor += box(Color.BLACK)
            borderWidth += box(2.px)
            padding      = box(2.px)
        }

        cell {
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
            prefWidth = 2.em
            padding   = box(0.px, 0.px, 0.px, 5.px)
        }

        cellValueConflict {
            textFill = Color.RED
        }

        cellFocus {
            backgroundColor += Color.YELLOW
        }

//                .cell-direct-hint {
//            -fx-background-radius: 15 15 15 15;
//            -fx-background-color: lightgreen;
//        }
//
//                .cell-indirect-hint {
//            -fx-background-radius: 15 15 15 15;
//            -fx-background-color: lightcoral;
//        }
    }
}