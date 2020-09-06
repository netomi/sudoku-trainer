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

package org.netomi.sudoku.ui.controller

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import org.netomi.sudoku.io.GridValueLoader
import org.netomi.sudoku.model.Grid
import org.netomi.sudoku.model.Grid.Companion.of
import org.netomi.sudoku.model.PredefinedType
import org.netomi.sudoku.solver.Hint
import org.netomi.sudoku.solver.HintSolver
import tornadofx.Controller

class GridController : Controller()
{
    val modelProperty: ObjectProperty<Grid> = SimpleObjectProperty()
    val hintProperty:  ObjectProperty<Hint> = SimpleObjectProperty()

    val hintList = FXCollections.observableArrayList<Hint>()

    fun loadModel() {
        //Grid grid = Grid.of(PredefinedType.JIGSAW_1);
        val grid = of(PredefinedType.CLASSIC_9x9)
        //String input = "3.......4..2.6.1...1.9.8.2...5...6...2.....1...9...8...8.3.4.6...4.1.9..5.......7"; // jigsaw
        val input = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......" // 9x9
        //String input = "000000010400000000020000000000050407008000300001090000300400200050100000000806000"; // 9x9
        grid.accept(GridValueLoader(input))

        modelProperty.set(grid)
    }

    fun findHints() {
        val hintSolver = HintSolver()
        val hints = hintSolver.findAllHints(modelProperty.get())
        hintList.setAll(hints.hints)
    }

}