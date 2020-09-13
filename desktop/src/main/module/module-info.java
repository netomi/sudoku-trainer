module sudoku.desktop {
    requires javafx.controls;
    requires javafx.graphics;

    requires java.logging;

    requires tornadofx;
    requires kotlin.stdlib;

    opens org.netomi.sudoku.ui            to javafx.graphics, tornadofx;
    opens org.netomi.sudoku.ui.view       to javafx.graphics, tornadofx;
    opens org.netomi.sudoku.ui.controller to javafx.graphics, tornadofx;
}