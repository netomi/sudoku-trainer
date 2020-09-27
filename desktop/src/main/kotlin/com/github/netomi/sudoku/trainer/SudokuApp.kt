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

import com.github.netomi.sudoku.trainer.controller.GridController
import com.github.netomi.sudoku.trainer.view.MainView
import com.jfoenix.assets.JFoenixResources
import javafx.application.Application
import javafx.stage.Stage
import kfoenix.jfxdecorator
import tornadofx.*


class SudokuApp : App(Main::class, Styles::class)
{
    class Main: View() {
        private val gridController: GridController by inject()

        override val root = jfxdecorator(MainView::class) {
            title = "Sudoku Trainer"
        }

        override fun onBeforeShow() {
            gridController.loadModel()
        }
    }

    override fun start(stage: Stage) {
        with(stage) {
            minWidth  = 600.0
            minHeight = 600.0
            super.start(this)
        }
    }

    init {
        importStylesheet(JFoenixResources.load("css/jfoenix-fonts.css").toExternalForm())
        importStylesheet(JFoenixResources.load("css/jfoenix-design.css").toExternalForm())
    }
}

fun main(args: Array<String>) {
    Application.launch(SudokuApp::class.java, *args)
}
