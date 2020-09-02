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
package org.netomi.sudoku.ui.view

import javafx.application.Platform
import org.netomi.sudoku.ui.controller.GridController
import tornadofx.*

class MainView : View("Sudoku Trainer") {
    private val gridController: GridController by inject()

    override val root = vbox {
        menubar {
            menu("File") {
                item("New").action {
                    //workspace.dock(mainView, true)
                    log.info("Opening new sudoku grid")
                    //workspace.dock(gridController.newModel(), true)
                }
                separator()
                item("Exit").action {
                    log.info("Leaving workspace")
                    Platform.exit()
                }
            }
            menu("Window") {
                item("Close all").action {
                    //editorController.editorModelList.clear()
                    //workspace.dock(EmptyView(),true)
                }
                separator()
                //openWindowMenuItemsAtfer()
            }
            menu("Help") {
                item("About...")
            }
        }

        buttonbar {
            button("Test")
        }

        add<GridView>()
    }

    override fun onBeforeShow() {
        gridController.loadModel()
    }
}