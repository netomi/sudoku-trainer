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
package org.netomi.sudoku.ui;

import com.gluonhq.ignite.guice.GuiceContext;
import com.google.inject.AbstractModule;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.netomi.sudoku.ui.service.ModelService;
import org.netomi.sudoku.ui.service.ModelServiceImpl;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * A simple Conway's Game of Life application.
 * <p>
 * It allows to visualize and simulate a cellular automata.
 *
 * @author Thomas Neidhart
 */
public class SudokuApp extends Application {

    private GuiceContext context = new GuiceContext(this, () -> Arrays.asList(new AppModule()));

    @Inject
    private FXMLLoader fxmlLoader;

    @Override
    public void start(Stage primaryStage) throws Exception {
        context.init();

        Parent root = fxmlLoader.load(getClass().getResourceAsStream("/main.fxml"));

        Scene scene = new Scene(root, 800, 600);

        scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());

        primaryStage.setTitle("Sudoku");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class AppModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(ModelService.class).to(ModelServiceImpl.class);
        }

    }

}
