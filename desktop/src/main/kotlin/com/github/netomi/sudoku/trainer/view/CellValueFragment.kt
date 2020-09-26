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

import com.github.netomi.sudoku.trainer.Styles
import javafx.beans.property.*
import javafx.scene.control.Label
import tornadofx.*

class CellValueFragment(valueArg: Int = 0) : Fragment()
{
    val valueProperty: IntegerProperty = SimpleIntegerProperty(valueArg)
    var value: Int by valueProperty

    private val textProperty: StringProperty = SimpleStringProperty()

    val labelIdProperty: ObjectProperty<String?> = SimpleObjectProperty()
    var labelId: String? by labelIdProperty

    private var valueLabel by singleAssign<Label>()

    override val root = stackpane {
        addClass(Styles.selectBox)

        label {
            valueLabel = this
            textProperty().bind(textProperty)
        }
    }

    init {
        textProperty.bind(valueProperty.asString())

        labelIdProperty.onChange { newId -> valueLabel.id = newId }
    }
}