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
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.ButtonBar
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.netomi.sudoku.model.PredefinedType
import org.netomi.sudoku.solver.Hint
import org.netomi.sudoku.ui.controller.GridController
import org.netomi.sudoku.ui.model.DisplayOptions
import tornadofx.*

class MainView : View("Sudoku Trainer") {
    private val gridController: GridController by inject()

    private val gridView: GridView by inject()

    private lateinit var hintListView:     ListView<Hint>
    private lateinit var gridTypeComboBox: ComboBox<PredefinedType>

    override val root =
        vbox {
            menubar {
                menu("File") {
                    item("New sudoku").action {
                        log.info("Creating new sudoku")
                    }
                    separator()
                    item("Exit").action {
                        log.info("Exiting application")
                        Platform.exit()
                    }
                }
                menu("Edit") {
                    item("Paste values").action {
                        gridController.loadModelFromClipboard()
                    }
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
                    item("Layout", expanded = true) {
                        form {
                            fieldset("General settings") {
                                field("Grid Layout") {
                                    combobox<PredefinedType> {
                                        gridTypeComboBox = this

                                        items = FXCollections.observableArrayList(*PredefinedType.values())
                                        selectionModel.select(PredefinedType.CLASSIC_9x9)
                                    }
                                }

                                buttonbar {
                                    button("Reset") {
                                        ButtonBar.setButtonData(this@button, ButtonBar.ButtonData.LEFT)

                                        action {
                                            gridController.resetModel(gridTypeComboBox.selectedItem ?: PredefinedType.CLASSIC_9x9)
                                        }
                                    }
                                }
                            }

                            fieldset("Display settings") {
                                field("Display possible values") {
                                    checkbox {
                                        selectedProperty().set(true)
                                        selectedProperty().bindBidirectional(DisplayOptions.displayPossibleValuesProperty)
                                        action {
                                            gridView.refreshView()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item("Solver") {
                        vbox {
                            button("Solve") {
                                action { gridController.findHints() }
                            }

                            listview<Hint> {
                                hintListView = this

                                minWidth   = 250.0
                                useMaxSize = true
                                vgrow      = Priority.ALWAYS

                                contextmenu {
                                    item("Apply").action {
                                        selectedItem?.apply {
                                            gridController.applyHint(this)
                                            gridView.refreshView()
                                        }
                                    }
                                    item("Apply upto").action {
                                        selectedItem?.apply {
                                            val index = this@listview.selectionModel.selectedIndex
                                            gridController.applyHints(0, index)
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
                gridView.root.styleProperty()
                             .bind(Bindings.concat("-fx-font-size: ", fontSize.asString("%.0f")).concat("%;"))
            }
        })
    }

    init {
        hintListView.items = gridController.hintList

        hintListView.selectionModel.selectedItemProperty().addListener { _, _, newValue: Hint? ->
            gridController.hintProperty.set(newValue)
            gridView.refreshView()
        }

        hintListView.focusedProperty().onChange { focused -> if (!focused) hintListView.selectionModel.clearSelection() }

        initializeFontSizeManager()
    }
}