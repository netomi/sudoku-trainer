module sudoku.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.logging;

    requires tornadofx;
    requires kotlin.stdlib;

    requires sudoku.core;

    opens org.netomi.sudoku.ui            to javafx.fxml, javafx.graphics, tornadofx;
    opens org.netomi.sudoku.ui.view       to javafx.fxml, javafx.graphics, tornadofx;
    opens org.netomi.sudoku.ui.controller to javafx.fxml, javafx.graphics, tornadofx;
}