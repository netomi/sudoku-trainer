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
package com.github.netomi.sudoku.trainer.model

import com.github.netomi.sudoku.model.ValueSet
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty

import tornadofx.getValue
import tornadofx.setValue

object DisplayOptions
{
    val showPencilMarksProperty = SimpleBooleanProperty(true)
    var showPencilMarks by showPencilMarksProperty

    val showComputedValuesProperty = SimpleBooleanProperty(true)
    var showComputedValues by showComputedValuesProperty

    val pencilMarkFilterProperty = SimpleObjectProperty<(ValueSet) -> Boolean>(null)
    var pencilMarkFilter: ((ValueSet) -> Boolean)? by pencilMarkFilterProperty
}