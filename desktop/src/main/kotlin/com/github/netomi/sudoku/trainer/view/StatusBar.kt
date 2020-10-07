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
package com.github.netomi.sudoku.trainer.view

import com.github.netomi.sudoku.model.Cell
import com.github.netomi.sudoku.model.Grid
import com.github.netomi.sudoku.solver.GridRater
import com.github.netomi.sudoku.trainer.Styles
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Label
import tornadofx.*

class StatusBar : View()
{
    val gridProperty: ObjectProperty<Grid> = SimpleObjectProperty()
    val selectedCellProperty: ObjectProperty<Cell?> = SimpleObjectProperty()

    private var ratingLabel       by singleAssign<Label>()
    private var selectedCellLabel by singleAssign<Label>()

    override val root = hbox {
        addClass(Styles.statusBar)
        ratingLabel = label("")

        label(" | ")

        selectedCellLabel = label("")
    }

    init {
        gridProperty.onChange { grid ->
            grid?.apply {
                runAsync {
                    GridRater.rate(this@apply)
                } ui {
                    ratingLabel.text = "%s (%d)".format(it.first.toString().toLowerCase().capitalize(), it.second)
                }
            }
        }

        selectedCellProperty.onChange { cell ->
            val text = cell?.let {
                val gridType = it.owner.type
                gridType.getCellName(it.cellIndex)
            } ?: ""

            selectedCellLabel.text = text
        }
    }
}