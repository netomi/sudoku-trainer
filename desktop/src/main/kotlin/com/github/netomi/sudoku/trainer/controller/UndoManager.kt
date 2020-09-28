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

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*
import kotlin.properties.Delegates

object UndoManager
{
    private val changes: ArrayList<Change> = ArrayList()
    private var currentPosition: Int by Delegates.observable(-1) { _, _, _ ->
        updateProperties()
    }

    val undoAvailableProperty = SimpleBooleanProperty(false)
    private var undoAvailable by undoAvailableProperty

    val redoAvailableProperty = SimpleBooleanProperty(false)
    private var redoAvailable by redoAvailableProperty

    fun push(event: ModelInteractionEvent) {
        changes.subList(currentPosition + 1, changes.size).clear()
        currentPosition++
        changes.add(currentPosition, Change(event))
    }

    fun undo() {
        if (undoAvailable) {
            val lastChange = changes[currentPosition]
            lastChange.revert()
            currentPosition--
        }
    }

    fun redo() {
        if (redoAvailable) {
            currentPosition++
            val nextChange = changes[currentPosition]
            nextChange.apply()
        }
    }

    fun clear() {
        changes.clear()
        currentPosition = -1
    }

    internal fun updateProperties() {
        undoAvailable = currentPosition >= 0
        redoAvailable = currentPosition < changes.lastIndex
    }
}

class Change(val event: ModelInteractionEvent)
{
    fun apply() {
        event.apply()
    }

    fun revert() {
        event.revert()
    }
}