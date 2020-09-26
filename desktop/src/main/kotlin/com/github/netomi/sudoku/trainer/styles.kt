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

import javafx.css.PseudoClass
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.paint.Color.WHITE
import javafx.scene.text.FontSmoothingType
import javafx.scene.text.FontWeight
import kfoenix.JFXStylesheet
import tornadofx.*
import tornadofx.DrawerStyles.Companion.buttonArea
import tornadofx.DrawerStyles.Companion.contentArea
import tornadofx.DrawerStyles.Companion.drawer
import tornadofx.DrawerStyles.Companion.drawerItem

class Styles : JFXStylesheet() {
    companion object {
        val sudokuGrid by cssclass()
        val sudokuCell by cssclass()

        val cellValue      by cssid()
        val cellCandidate  by cssid()
        val cellSelectPane by cssid()

        val editValue             by cssid()
        val editCandidate         by cssid()

        val selectBox               by cssclass()
        val selectAssignedValue     by cssclass()
        val selectPossibleCandidate by cssclass()

        val chainLink               by cssclass()
        val chainLinkArrow          by cssclass()
        val weakChainLink           by cssclass()

        // pseudo classes

        val selected by csspseudoclass("selected")
        val assigned by csspseudoclass("assigned")
        val given    by csspseudoclass("given")
        val conflict by csspseudoclass("conflict")

        val matched     by csspseudoclass("matching")
        val eliminated  by csspseudoclass("eliminated")
        val highlighted by csspseudoclass("highlighted")

        val active   by csspseudoclass("active")
        val inactive by csspseudoclass("inactive")

        val medium  by csspseudoclass("medium")
        val hard    by csspseudoclass("hard")
        val unfair  by csspseudoclass("unfair")
        val extreme by csspseudoclass("extreme")

        // jfoenix overrides

        val jfxToolBar      by cssclass()
        val toolBarRightBox by cssclass()
        val toolBarLeftBox  by cssclass()
        val statusBar       by cssclass()

        val defaultColor: Color   = Color.web("#4059a9")
        val decoratorColor: Color = Color.web("#5264AE").derive(-0.2)
    }

    init {
        root {
            fontFamily = "Roboto"
            fontSmoothingType = FontSmoothingType.GRAY
            backgroundColor += WHITE
        }

        // overwrite styles for jfx components

        jfxDecoratorButtonsContainer {
            backgroundColor += decoratorColor
        }

        jfxDecorator {
            s(".resize-border") {
                borderColor += box(decoratorColor)
                borderWidth += box(0.0.px, 4.0.px, 4.0.px, 4.0.px)
            }
        }

        jfxDecoratorTitleContainer {
            s(".jfx-decorator-text") {
                fill = WHITE
                fontSize = 16.px
            }
        }

        jfxButton {
            backgroundColor += defaultColor
            textFill = WHITE
        }

        jfxToolBar {
            toolBarLeftBox {
                spacing = 10.px
            }
            toolBarRightBox {
                spacing = 10.px
            }
        }

        statusBar {
            backgroundColor += decoratorColor
            padding = box(4.px)
            label {
                textFill = WHITE
            }
        }

        // overwrite default style for drawer component

        contentArea {
            backgroundColor += Color.TRANSPARENT
        }

        buttonArea {
            backgroundColor += Color.TRANSPARENT
        }

        drawerItem {
            backgroundColor += Color.TRANSPARENT
        }

        drawer {
            contentArea {
                backgroundColor += Color.TRANSPARENT
            }
        }

        drawerItem child titledPane {
            textFill = WHITE
            fontSize = 1.5.em

            title {
                backgroundColor += defaultColor
            }

            content {
                backgroundColor += Color.TRANSPARENT
            }
        }

        // custom application styles

        sudokuGrid {
            borderColor += box(Color.BLACK)
            borderWidth += box(2.px)
            padding      = box(2.px)
        }

        sudokuCell {
            borderColor += box(Color.GRAY)
            borderWidth += box(1.px)

            and(active) {
                backgroundColor += Color.LIGHTGREEN
            }

            and(inactive) {
                backgroundColor += Color.LIGHTCORAL
            }

            and(highlighted) {
                backgroundColor += Color.LIGHTSTEELBLUE
            }
        }

        cellValue {
            fontSize = 3.em

            and(assigned) {
                textFill = Color.BLUE
            }

            and(given) {
                textFill = Color.BLACK
            }

            and(conflict) {
                textFill = Color.RED
            }
        }

        cellCandidate {
            textFill  = Color.GRAY
            fontSize  = 1.5.em
            alignment = Pos.CENTER

            and(matched) {
                backgroundRadius += box(1.5.em)
                backgroundColor  += Color.LIMEGREEN
                textFill = Color.BLACK
            }

            and(eliminated) {
                backgroundRadius += box(1.5.em)
                backgroundColor  += Color.CORAL
                textFill = Color.BLACK
            }

            and(active) {
                backgroundRadius += box(1.5.em)
                backgroundColor  += Color.LIMEGREEN
                textFill = Color.BLACK
            }

            and(inactive) {
                backgroundRadius += box(1.5.em)
                backgroundColor  += Color.CORNFLOWERBLUE
                textFill = Color.BLACK
            }
        }

        cellSelectPane and selected {
            borderColor += box(Color.YELLOW)
            borderWidth += box(4.px)
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

        editValue {
            textFill = Color.BLACK
            fontSize = 2.5.em
        }

        editCandidate {
            textFill = Color.BLACK
            fontSize = 1.5.em
        }

        listCell {
            fontWeight = FontWeight.BOLD
        }

        listCell and empty {
            backgroundColor += WHITE
            borderColor     += box(WHITE)
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

        treeCell {
            fontWeight = FontWeight.BOLD
        }
    }
}

val CssRule.pseudoClass: PseudoClass
    get() = PseudoClass.getPseudoClass(this.name)
