module sudoku.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires ignite.guice;
    requires guice;
    requires javax.inject;
    requires sudoku.core;

    opens org.netomi.sudoku.ui            to javafx.fxml, javafx.graphics, guice;
    opens org.netomi.sudoku.ui.view       to javafx.fxml, javafx.graphics, guice;
    opens org.netomi.sudoku.ui.service    to guice;
    opens org.netomi.sudoku.ui.controller to javafx.fxml, javafx.graphics, guice;
}