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
package org.netomi.sudoku.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.PredefinedType;
import org.netomi.sudoku.ui.service.ModelService;
import org.netomi.sudoku.ui.view.GridView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for the simple life app.
 *
 * @author Thomas Neidhart
 */
public class MainController implements Initializable {

    @FXML
    private Button loadButton;

    @FXML
    private Button saveButton;

    @FXML
    private ToggleButton controlToggleButton;

    @FXML
    private Slider rowSlider;

    @FXML
    private Slider colSlider;

    @FXML
    private GridPane divider;

    @FXML
    private GridView modelGrid;

    @FXML
    private Label statusLabel;

    @Inject
    private ModelService modelService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rowSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                updateModel();
            }
        });

        colSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                updateModel();
            }
        });

        modelGrid.modelProperty().bind(modelService.modelProperty());
        updateModel();

        //statusLabel.textProperty().bind(evolutionStats.textProperty());
    }

    public void toggleControls(ActionEvent actionEvent) {
        if (controlToggleButton.isSelected()) {
            divider.getColumnConstraints().get(1).setPrefWidth(Control.USE_COMPUTED_SIZE);
        }
        else {
            divider.getColumnConstraints().get(1).setPrefWidth(0.0);
        }
    }

    public void resetGrid(ActionEvent actionEvent) {
        modelGrid.resetGrid();
    }

    private void updateModel() {
        modelService.setModel(Grid.of(PredefinedType.CLASSIC_9x9));
    }

    public void saveModel(ActionEvent actionEvent) {
//        try {
//            FileChooser chooser = new FileChooser();
//
//            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("GOL files (*.gol)", "*.gol");
//            chooser.getExtensionFilters().add(extFilter);
//
//            chooser.setTitle("Save Model");
//            File file = chooser.showSaveDialog(saveButton.getScene().getWindow());
//
//            if (file != null) {
//                SaveModelAction action = new SaveModelAction();
//                action.setFile(file);
//                action.execute(modelService.getModel());
//            }
//        } catch (IOException ex) {
//            Alert alert = new Alert(Alert.AlertType.ERROR, "Saving model to file failed!", ButtonType.OK);
//            alert.showAndWait();
//        }
    }

    public void loadModel(ActionEvent actionEvent) {
//        try {
//            FileChooser chooser = new FileChooser();
//
//            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("GOL files (*.gol)", "*.gol");
//            chooser.getExtensionFilters().add(extFilter);
//
//            chooser.setTitle("Load Model");
//            File file = chooser.showOpenDialog(loadButton.getScene().getWindow());
//
//            if (file != null) {
//                LoadModelAction action = new LoadModelAction();
//                action.setFile(file);
//                CellularAutomaton model = action.execute();
//                modelService.setModel(model);
//
//                rowSlider.setValue(model.getRows());
//                colSlider.setValue(model.getCols());
//            }
//        } catch (IOException ex) {
//            Alert alert = new Alert(Alert.AlertType.ERROR, "Loading model from file failed!", ButtonType.OK);
//            alert.showAndWait();
//        }
    }
}
