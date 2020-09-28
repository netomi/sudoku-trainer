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
package com.github.netomi.sudoku.trainer.controller

import com.github.netomi.sudoku.model.Cell
import com.github.netomi.sudoku.model.Grid
import com.github.netomi.sudoku.solver.Hint
import tornadofx.FXEvent

abstract class ModelInteractionEvent : FXEvent()
{
    abstract fun apply()

    abstract fun revert()
}

class AssignValueEvent(private val cell: Cell, private val value: Int) : ModelInteractionEvent()
{
    private val previousValue: Int = cell.value

    override fun apply() {
        cell.value = value
    }

    override fun revert() {
        cell.value = previousValue
    }
}

class ExcludePossibleValueEvent(private val cell: Cell, private val value: Int) : ModelInteractionEvent()
{
    override fun apply() {
        cell.excludePossibleValues(true, value)
    }

    override fun revert() {
        cell.removeExcludedPossibleValues(true, value)
    }
}

class RemoveExcludedPossibleValueEvent(private val cell: Cell, private val value: Int) : ModelInteractionEvent()
{
    override fun apply() {
        cell.removeExcludedPossibleValues(true, value)
    }

    override fun revert() {
        cell.excludePossibleValues(true, value)
    }
}

class ApplyHintsEvent(private val hints: List<Hint>) : ModelInteractionEvent()
{
    var targetGrid: Grid? = null

    override fun apply() {
        targetGrid?.apply {
            hints.forEach { it.apply(this, false) }
            updateState()
        }
    }

    override fun revert() {
        targetGrid?.apply {
            hints.forEach { it.revert(this, false) }
            updateState()
        }
    }
}
