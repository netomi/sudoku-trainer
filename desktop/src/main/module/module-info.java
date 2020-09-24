module sudoku.desktop {
    requires javafx.controls;
    requires javafx.graphics;

    requires java.logging;

    requires tornadofx;
    requires com.jfoenix;
    requires kfoenix;

    requires kotlin.stdlib;

    requires sudoku.solver.jvm;

    opens com.github.netomi.sudoku.trainer            to javafx.graphics, tornadofx;
    opens com.github.netomi.sudoku.trainer.view       to javafx.graphics, tornadofx;
    opens com.github.netomi.sudoku.trainer.controller to javafx.graphics, tornadofx;
}