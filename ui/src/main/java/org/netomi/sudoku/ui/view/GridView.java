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
package org.netomi.sudoku.ui.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Conflict;
import org.netomi.sudoku.model.ConflictDetector;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.solver.Hint;

import java.util.Collection;
import java.util.List;

/**
 * The main grid view to visualize the state of a sudoku grid.
 *
 * @author Thomas Neidhart
 */
public class GridView extends GridPane {

    private ObjectProperty<Grid> modelProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Hint> hintProperty  = new SimpleObjectProperty<>();


    public GridView() {
        getStyleClass().add("grid");
        setPadding(new Insets(2, 2, 2, 2));
        modelProperty.addListener((observable, oldValue, newValue) -> updateModel());
    }

    public ObjectProperty<Grid> modelProperty() {
        return modelProperty;
    }

    public ObjectProperty<Hint> hintProperty() {
        return hintProperty;
    }

    public Grid getModel() {
        return modelProperty.get();
    }

    public void resetGrid() {
        getModel().clear(true);
    }

    private void updateModel() {
        getChildren().clear();
        getRowConstraints().clear();
        getColumnConstraints().clear();

        Grid model = getModel();

        for (Cell cell : model.cells()) {
            CellView cellView = new CellView(cell);

            int column = cell.getColumnIndex();
            int row    = cell.getRowIndex();

            cellView.dirtyProperty().addListener((observable, oldValue, newValue) -> { if (newValue) refreshView(); });

            add(cellView, column, row);
        }

        for (int i = 0; i < model.getGridSize(); i++) {
            RowConstraints row = new RowConstraints(3, 100, Double.MAX_VALUE);
            row.setVgrow(Priority.ALWAYS);
            getRowConstraints().add(row);
        }

        for (int i = 0; i < model.getGridSize(); i++) {
            ColumnConstraints col = new ColumnConstraints(3, 100, Double.MAX_VALUE);
            col.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(col);
        }

        refreshView();
    }

    public void refreshView() {
        System.out.println("refresh");
        Conflict[] conflicts = getModel().accept(new ConflictDetector());

        for (Node child : getChildren()) {
            if (child instanceof CellView) {
                ((CellView) child).refreshView(conflicts, hintProperty.getValue());
            }
        }
    }
}
