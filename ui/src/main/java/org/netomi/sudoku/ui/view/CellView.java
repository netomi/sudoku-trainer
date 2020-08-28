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

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.*;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.Grids;

import java.util.Collection;

/**
 * The view to display the state of an individual cell within a
 * sudoku grid.
 *
 * @author Thomas Neidhart
 */
public class CellView extends StackPane {

    private final Cell cell;

    private final IntegerProperty        value          = new SimpleIntegerProperty(0);
    private final ObservableIntegerArray possibleValues = FXCollections.observableIntegerArray();
    private final BooleanProperty        dirty          = new SimpleBooleanProperty(false);

    private final GridPane possibleValuesPane;
    private final Label    assignedValueLabel;

    public CellView(Cell cell) {
        this.cell = cell;

        getStyleClass().add("cell");

        setBorderStyle(cell);

        setMinSize(30, 30);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        possibleValuesPane = new GridPane();
        for (int i = 0; i < 3; i++) {
            RowConstraints row1 = new RowConstraints(10, 20, Double.MAX_VALUE);
            row1.setVgrow(Priority.ALWAYS);
            row1.setValignment(VPos.CENTER);
            possibleValuesPane.getRowConstraints().add(row1);

            ColumnConstraints col1 = new ColumnConstraints(10, 20, Double.MAX_VALUE);
            col1.setHgrow(Priority.ALWAYS);
            col1.setHalignment(HPos.CENTER);
            possibleValuesPane.getColumnConstraints().add(col1);
        }

        int tmpValue = 1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Label label = new Label(Integer.toString(tmpValue++));
                label.getStyleClass().add("cell-possible-value");
                possibleValuesPane.add(label, j, i);
            }
        }

        assignedValueLabel = new Label();
        if (cell.isGiven()) {
            assignedValueLabel.getStyleClass().add("cell-given-value");
        } else {
            assignedValueLabel.getStyleClass().add("cell-assigned-value");
        }
        getChildren().addAll(possibleValuesPane, assignedValueLabel);
        assignedValueLabel.setVisible(false);

        setFocusTraversable(true);

        assignedValueLabel.textProperty().bind(Bindings.createStringBinding(() -> Integer.toString(value.getValue()), value));

        value.addListener((observable, oldValue, newValue) -> {
            possibleValuesPane.setVisible(newValue.intValue() == 0);
            assignedValueLabel.setVisible(newValue.intValue() != 0);
        });

        possibleValues.addListener((observableArray, sizeChanged, from, to) -> {
            ObservableList<Node> children = possibleValuesPane.getChildren();

            for (Node node : children) {
                node.setVisible(false);
            }

            for (int index = from; index < to; index++) {
                int value = observableArray.get(index);
                children.get(value - 1).setVisible(true);
            }
        });

        setupEventListeners();
    }

    private void setBorderStyle(Cell cell) {
        StringBuilder borderStyle = new StringBuilder();
        StringBuilder borderColor = new StringBuilder();
        StringBuilder borderWidth = new StringBuilder();

        borderStyle.append("-fx-border-style: ");
        borderColor.append("-fx-border-color: ");
        borderWidth.append("-fx-border-width: ");

        selectBorder(getAdjacentCell(cell, cell.getCellIndex() - cell.getOwner().getGridSize()),
                     borderStyle, borderColor, borderWidth);

        int nextCellIndex = (cell.getCellIndex() + 1) % cell.getOwner().getGridSize() == 0 ? -1 : cell.getCellIndex() + 1;
        selectBorder(getAdjacentCell(cell, nextCellIndex), borderStyle, borderColor, borderWidth);

        selectBorder(getAdjacentCell(cell, cell.getCellIndex() + cell.getOwner().getGridSize()),
                     borderStyle, borderColor, borderWidth);

        int previousCellIndex = cell.getCellIndex() % cell.getOwner().getGridSize() == 0 ? -1 : cell.getCellIndex() - 1;
        selectBorder(getAdjacentCell(cell, previousCellIndex), borderStyle, borderColor, borderWidth);

        borderStyle.append(";");
        borderColor.append(";");
        borderWidth.append(";");

        setStyle(borderStyle.toString() + borderColor.toString() + borderWidth.toString());
    }

    private void selectBorder(Cell neighbour, StringBuilder borderStyle, StringBuilder borderColor, StringBuilder borderWidth) {
        if (neighbour != null) {
            if (neighbour.getBlockIndex() == cell.getBlockIndex()) {
                borderStyle.append(" solid");
                borderColor.append(" grey");
                borderWidth.append(" 1px");
            } else {
                borderStyle.append(" solid");
                borderColor.append(" black");
                borderWidth.append(" 2px");
            }
        } else {
            borderStyle.append(" solid");
            borderColor.append(" black");
            borderWidth.append(" 4px");
        }
    }

    private Cell getAdjacentCell(Cell cell, int adjacentCellIndex) {
        if (adjacentCellIndex >= 0 &&
            adjacentCellIndex < cell.getOwner().getCellCount()) {
            return cell.getOwner().getCell(adjacentCellIndex);
        }
        return null;
    }

    private void setupEventListeners() {
        setOnMousePressed(event -> CellView.this.requestFocus());

        setOnKeyPressed(event -> {
            if (cell.isGiven()) {
                return;
            }

            if (event.getCode() == KeyCode.DELETE) {
                cell.setValue(0);
                value.setValue(0);
                dirty.set(true);
            } else {
                try {
                    int newValue = Integer.parseInt(event.getText());
                    cell.setValue(newValue);
                    value.setValue(newValue);
                    dirty.set(true);
                } catch (NumberFormatException ex) {}
            }
        });

        focusedProperty().addListener((observable, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue)
            {
                getStyleClass().remove("cell-focus");
                getStyleClass().add("cell-focus");
            }
            else
            {
                getStyleClass().remove("cell-focus");
            }
        });
    }

    public int getRow() {
        return cell.getRowIndex();
    }

    public int getColumn() {
        return cell.getColumnIndex();
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public void refreshView(Collection<Grid.Conflict> conflicts) {
        boolean foundConflict = false;
        for (Grid.Conflict conflict : conflicts) {
            if (conflict.contains(cell)) {
                foundConflict = true;
                break;
            }
        }

        if (foundConflict) {
            assignedValueLabel.getStyleClass().remove("cell-value-conflict");
            assignedValueLabel.getStyleClass().add("cell-value-conflict");
        } else {
            assignedValueLabel.getStyleClass().remove("cell-value-conflict");
        }

        value.set(cell.getValue());
        possibleValues.setAll(Grids.toIntArray(cell.getPossibleValues()));
        dirty.set(false);
    }

    @Override
    public String toString() {
        return cell.toString();
    }
}
