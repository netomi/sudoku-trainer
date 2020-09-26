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

// Workaround for https://youtrack.jetbrains.com/issue/KT-35343
@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")

package com.github.netomi.sudoku.trainer.view

import com.github.netomi.sudoku.model.GridType
import com.github.netomi.sudoku.model.ValueSet
import com.github.netomi.sudoku.solver.DifficultyLevel
import com.github.netomi.sudoku.solver.GridRater
import com.github.netomi.sudoku.solver.Hint
import com.github.netomi.sudoku.trainer.Styles
import com.github.netomi.sudoku.trainer.controller.GridController
import com.github.netomi.sudoku.trainer.model.DisplayOptions
import com.github.netomi.sudoku.trainer.model.SudokuLibrary
import com.github.netomi.sudoku.trainer.model.TechniqueCategory
import com.github.netomi.sudoku.trainer.model.TechniqueCategoryOrLibraryEntry
import com.jfoenix.controls.JFXButton.ButtonType.RAISED
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.beans.binding.When
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.css.PseudoClass
import javafx.geometry.Insets
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import kfoenix.*
import tornadofx.*


class MainView : View("Sudoku Trainer") {
    private val gridController: GridController by inject()

    private val gridView: GridView by inject()

    private val filterToggleGroup = ToggleGroup()

    private lateinit var hintListView:     ListView<Hint>
    private lateinit var gridTypeComboBox: ComboBox<GridType>
    private lateinit var statusBar:        Label

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

            jfxtoolbar {
                vboxConstraints { margin = Insets(5.0) }
                leftSide {
                    button {
                        isFocusTraversable = false
                        setMinSize(40.0, 40.0)

                        contentDisplay = ContentDisplay.GRAPHIC_ONLY
                        graphic = FontAwesomeIconView(FontAwesomeIcon.UNDO)
                    }

                    button {
                        isFocusTraversable = false
                        setMinSize(40.0, 40.0)

                        contentDisplay = ContentDisplay.GRAPHIC_ONLY
                        graphic = FontAwesomeIconView(FontAwesomeIcon.REPEAT)
                    }

                    (1..9).forEach { value ->
                        togglebutton(
                            value.toString(),
                            filterToggleGroup,
                            false,
                            { valueSet: ValueSet -> valueSet[value] }) {
                            isFocusTraversable = false
                            setMinSize(40.0, 40.0)
                        }
                    }

                    togglebutton("x/y", filterToggleGroup, false, ValueSet::isBiValue) {
                        isFocusTraversable = false
                        setMinSize(40.0, 40.0)
                    }
                }
            }

            borderpane {
                useMaxSize = true

                center = gridView.root

                right = drawer(side = Side.RIGHT, multiselect = true) {
                    item("Layout", expanded = false) {
                        form {
                            fieldset("Layout settings") {
                                field("Grid Layout") {
                                    jfxcombobox<GridType> {
                                        gridTypeComboBox = this

                                        items = FXCollections.observableArrayList(*GridType.values())
                                        selectionModel.select(GridType.CLASSIC_9x9)
                                    }
                                }

                                jfxbutton("Reset", RAISED) {
                                    action {
                                        gridController.resetModel(gridTypeComboBox.selectedItem ?: GridType.CLASSIC_9x9)
                                    }
                                }
                            }

                            fieldset("Display settings") {
                                field("Show pencil marks") {
                                    jfxcheckbox(DisplayOptions.showPencilMarksProperty) {
                                        action { gridView.refreshView() }
                                    }
                                }
                                field("Show computed values") {
                                    disableProperty().bind(DisplayOptions.showPencilMarksProperty.not())

                                    jfxcheckbox(DisplayOptions.showComputedValuesProperty) {
                                        action { gridView.refreshView() }
                                    }
                                }
                            }
                        }
                    }
                    item("Solver", expanded = true) {
                        vbox {
                            minWidth  = 150.0
                            prefWidth = 350.0

                            jfxtoolbar {
                                vboxConstraints { margin = Insets(5.0) }
                                leftSide {
                                    jfxbutton("Full", RAISED) {
                                        action { gridController.findHints() }
                                    }
                                    jfxbutton("Single Step", RAISED) {
                                        action { gridController.findHintsSingleStep() }
                                    }
                                }
                            }

                            listview<Hint> {
                                hintListView = this

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

                    item("Library", expanded = true) {
                        treeview<TechniqueCategoryOrLibraryEntry> {
                            minWidth  = 150.0
                            prefWidth = 350.0

                            root = TreeItem(TechniqueCategory.All)

                            cellFormat { text = it.toDisplayString() }

                            populate { parent ->
                                val node = parent.value
                                return@populate if (node.isCategory()) {
                                    val category = node as TechniqueCategory

                                    if (category.hasSubCategories()) {
                                        category.subCategories()
                                    } else {
                                        SudokuLibrary.entries[category]
                                    }
                                } else {
                                    null
                                }
                            }

                            onUserSelect {
                                it.getLibraryEntry()?.apply {
                                    gridController.loadModel(this)
                                }
                            }
                        }
                    }
                }

                bottom = hbox {
                    addClass(Styles.statusBar)
                    statusBar = label("") {}
                }
            }
        }

    private fun initializeFontSizeManager() {
        // Cf. https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        root.sceneProperty().addListener(ChangeListener { _, oldScene: Scene?, newScene: Scene? ->
            if (oldScene == null && newScene != null) {
                val fontSize = SimpleDoubleProperty(0.0)
                fontSize.bind(
                    newScene.widthProperty().add(newScene.heightProperty())
                        .divide(1400 + 900)
                        .multiply(100)
                )
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

        hintListView.setCellFactory { lv ->
            val selectionModel = lv.selectionModel
            val cell = ListCell<Hint>()

            cell.textProperty().bind(
                When(cell.itemProperty().isNotNull).then(cell.itemProperty().asString()).otherwise(
                    ""
                )
            )
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
                if (event.isPrimaryButtonDown && !cell.isEmpty) {
                    val index = cell.index
                    if (selectionModel.selectedIndices.contains(index) && lv.isFocused) {
                        selectionModel.clearSelection(index)
                    } else {
                        selectionModel.select(index)
                    }
                    lv.requestFocus()
                    event.consume()
                }
            }

            val difficultyListener = InvalidationListener {
                for (difficultyLevel in DifficultyLevel.values()) {
                    // use pseudo classes to style list cells based on hint difficulty
                    val pseudoClass = PseudoClass.getPseudoClass(difficultyLevel.name.toLowerCase())

                    val active = cell.item != null &&
                                 cell.item.solvingTechnique.difficultyLevel == difficultyLevel

                    cell.pseudoClassStateChanged(pseudoClass, active)
                }
            }

            cell.itemProperty().addListener(difficultyListener)

            cell
        }

        DisplayOptions.pencilMarkFilterProperty.bind(filterToggleGroup.selectedValueProperty<(ValueSet) -> Boolean>())
        filterToggleGroup.selectedToggleProperty().onChange {
            gridView.refreshView()
        }

        gridController.modelProperty.onChange { grid ->
            grid?.apply {
                runAsync {
                    GridRater.rate(this@apply)
                } ui {
                    statusBar.text = "%s (%d)".format(it.first.toString().toLowerCase().capitalize(), it.second)
                }
            }
        }

        initializeFontSizeManager()
    }
}