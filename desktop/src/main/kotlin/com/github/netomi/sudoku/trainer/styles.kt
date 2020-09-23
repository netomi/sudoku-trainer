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
package com.github.netomi.sudoku.trainer

import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val sudokuGrid by cssclass()
        val sudokuCell by cssclass()

        val cellAssignedValue       by cssclass()
        val cellGivenValue          by cssclass()
        val cellPossibleValue       by cssclass()
        val cellFocus               by cssclass()
        val cellValueConflict       by cssclass()
        val cellMatchingCandidate   by cssclass()
        val cellEliminatedCandidate by cssclass()
        val cellActiveCandidate     by cssclass()
        val cellInactiveCandidate   by cssclass()
        val cellHighlight           by cssclass()

        val cellActiveFilter        by cssclass()
        val cellInactiveFilter      by cssclass()

        val selectBox               by cssclass()
        val selectAssignedValue     by cssclass()
        val selectPossibleCandidate by cssclass()
        val selectValue             by cssclass()
        val selectCandidate         by cssclass()

        val chainLink               by cssclass()
        val chainLinkArrow          by cssclass()
        val weakChainLink           by cssclass()

        val medium  by csspseudoclass("medium")
        val hard    by csspseudoclass("hard")
        val unfair  by csspseudoclass("unfair")
        val extreme by csspseudoclass("extreme")
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
            borderColor += box(Color.YELLOW)
            borderWidth += box(4.px)
        }

        cellMatchingCandidate {
            backgroundRadius += box(2.em)
            backgroundColor  += Color.LIMEGREEN
            textFill = Color.BLACK
        }

        cellEliminatedCandidate {
            backgroundRadius += box(2.em)
            backgroundColor  += Color.CORAL
            textFill = Color.BLACK
        }

        cellActiveCandidate {
            backgroundRadius += box(2.em)
            backgroundColor  += Color.LIMEGREEN
            textFill = Color.BLACK
        }

        cellInactiveCandidate {
            backgroundRadius += box(2.em)
            backgroundColor  += Color.CORNFLOWERBLUE
            textFill = Color.BLACK
        }

        cellHighlight {
            backgroundColor += Color.LIGHTSTEELBLUE
        }

        cellActiveFilter {
            backgroundColor += Color.LIGHTGREEN
        }

        cellInactiveFilter {
            backgroundColor += Color.LIGHTCORAL
        }

        chainLink {
            stroke = Color.RED
            strokeWidth = 2.0.px
            fill = Color.TRANSPARENT
        }

        chainLinkArrow {
            stroke = Color.RED
            strokeWidth = 2.0.px
            fill = Color.RED
        }

        weakChainLink {
            strokeDashArray = listOf(10.px, 10.px)
        }

        selectBox {
            borderColor += box(Color.GRAY)
            borderWidth += box(1.px)
        }

        selectAssignedValue {
            backgroundColor += Color.CORNFLOWERBLUE
        }

        selectPossibleCandidate {
            backgroundColor += Color.CORNFLOWERBLUE
        }

        selectValue {
            textFill = Color.BLACK
            fontSize = 3.em
        }

        selectCandidate {
            textFill = Color.BLACK
            fontSize = 1.5.em
        }

        listCell and empty {
            backgroundColor += Color.WHITE
            borderColor     += box(Color.WHITE)
        }

        listCell and medium {
            backgroundColor += Color.LIGHTGREEN
        }

        listCell and hard {
            backgroundColor += Color.YELLOW
        }

        listCell and unfair {
            backgroundColor += Color.LIGHTCORAL
        }

        listCell and extreme {
            backgroundColor += Color.CORAL
        }
    }
}
