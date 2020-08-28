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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import org.netomi.sudoku.model.Cell;

/**
 * The view to display the state of an individual cell within a
 * sudoku grid.
 *
 * @author Thomas Neidhart
 */
public class CellView extends StackPane {

    private final Cell cell;

    private final ObjectProperty<Integer> value = new SimpleObjectProperty<>();

    private final GridPane numbers;
    private final Label label;

    public CellView(Cell cell) {
        this.cell = cell;

        getStyleClass().add("cell");

        setMinSize(30, 30);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        numbers = new GridPane();

        for (int i = 0; i < 3; i++) {
            RowConstraints row1 = new RowConstraints(10, 20, Double.MAX_VALUE);
            row1.setVgrow(Priority.ALWAYS);
            row1.setValignment(VPos.CENTER);
            numbers.getRowConstraints().add(row1);

            ColumnConstraints col1 = new ColumnConstraints(10, 20, Double.MAX_VALUE);
            col1.setHgrow(Priority.ALWAYS);
            col1.setHalignment(HPos.CENTER);
            numbers.getColumnConstraints().add(col1);
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Label label = new Label("" + (i * 3 + j));
                numbers.add(label, j, i);
            }
        }

        label = new Label("" + value.getValue());
        label.getStyleClass().add("cell-value");
        getChildren().addAll(numbers, label);

        setFocusTraversable(true);
        //requestFocus();

        label.textProperty().bind(Bindings.createStringBinding(() -> {
            if (value.get()==null) {
                return "";
            } else {
                return "" + value.getValue();
            }
        }, value));

        setOnMousePressed(event -> CellView.this.requestFocus());

        setOnKeyPressed(new EventHandler<>() {
            @Override
            public void handle(KeyEvent event) {
                try {
                    value.setValue(Integer.valueOf(event.getText()));

                    numbers.setVisible(false);
                    System.out.println(this.toString() + " set to " + value.getValue());
                } catch (NumberFormatException ex) {
                }
            }
        });

        focusedProperty().addListener((observable, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue)
            {
                getStyleClass().remove("cell-focus");
                getStyleClass().add("cell-focus");
                System.out.println("Textfield " + CellView.this.toString() + " on focus");
            }
            else
            {
                getStyleClass().remove("cell-focus");
                System.out.println("Textfield " + CellView.this.toString() + " out focus");
            }
        });
    }

    public int getRow() {
        return cell.getRowIndex();
    }

    public int getColumn() {
        return cell.getColumnIndex();
    }

    @Override
    public String toString() {
        return cell.toString();
    }
}
