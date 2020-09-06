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

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.util.Callback
import org.netomi.sudoku.solver.Hint
import org.netomi.sudoku.ui.Styles
import org.netomi.sudoku.ui.controller.GridController
import tornadofx.*

class MainView : View("Sudoku Trainer") {
    private val gridController: GridController by inject()

    private val gridView: GridView by inject()
    private lateinit var hintListView: ListView<Hint>

    override val root =
        vbox {
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
                        javafx.application.Platform.exit()
                    }
                }
                menu("Window") {
                    item("Close all").action {
                        //editorController.editorModelList.clear()
                        //workspace.dock(EmptyView(),true)
                    }
                    separator()
                }
                menu("Help") {
                    item("About...")
                }
            }

            borderpane {
                useMaxSize = true
                vgrow = Priority.ALWAYS

                center = gridView.root

                left = drawer(side = Side.LEFT) {
                    item("Solver", expanded = true) {
                        vbox {
                            button("Solve") {
                                action { gridController.findHints() }
                            }

                            anchorpane {
                                useMaxSize = true
                                vgrow = Priority.ALWAYS
                                hintListView = listview {
                                    addClass(Styles.listCell)

                                    anchorpaneConstraints {
                                        topAnchor = 0.0
                                        bottomAnchor = 0.0
                                        leftAnchor = 0.0
                                        rightAnchor = 0.0
                                    }

                                    minWidth = 250.0

                                    contextmenu {
                                        item("Apply").action {
                                            selectedItem?.apply {
                                                this.apply(gridController.modelProperty.get(), true)
                                                gridView.refreshView()
                                            }
                                        }
                                        item("Apply upto").action {
                                            selectedItem?.apply {
                                                val index = this@listview.selectionModel.selectedIndex
                                                for (i in 0..index) {
                                                    val hint = gridController.hintList[i]
                                                    hint.apply(gridController.modelProperty.get(), false)
                                                }
                                                gridController.modelProperty.get().updateState()
                                                gridView.refreshView()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    override fun onBeforeShow() {
        gridController.loadModel()
    }

    private fun initializeFontSizeManager() {
        // Cf. https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        root.sceneProperty().addListener(ChangeListener { _, oldScene: Scene?, newScene: Scene? ->
            if (oldScene == null && newScene != null) {
                val fontSize = SimpleDoubleProperty(0.0)
                fontSize.bind(newScene.widthProperty().add(newScene.heightProperty())
                        .divide(1400 + 900)
                        .multiply(100))
                gridView.root.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString("%.0f")).concat("%;"))
            }
        })
    }

    init {
        hintListView.items = gridController.hintList

        hintListView.selectionModel.selectedItemProperty().addListener { _: ObservableValue<out Hint>, _: Hint?, newValue: Hint? ->
            gridController.hintProperty.set(newValue)
            gridView.refreshView()
        }

        initializeFontSizeManager()
    }
}