package org.netomi.sudoku.solver;

import org.junit.Test;
import org.netomi.sudoku.io.GridPrinter;
import org.netomi.sudoku.io.GridValueLoader;
import org.netomi.sudoku.model.Cell;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.model.PredefinedType;
import org.netomi.sudoku.solver.techniques.*;

public class SolverTest {

    @Test
    public void tstFullHouse() {
        //String input = ":0000:1:+6+3+42+8+9...8+7531+6+2+9+42+91+7+5+4+6+38+349+16+58+7+2+5+8+79+42..+6+1+62+83+754+97+5+8+4+2+39+61+9.+6+5784+23+4+2+3+6+91...:::182:";

        //String input = ":0100:1:.+92....+365...3+697..3+6.+94...+2.58.1.+9+3+3.96.28.....9.32..+9+5+34+6..2..87+32+9..56+2....3.+9:811 515 715 538 761 595 795:193 198::";

        //String input = ":0101:1:..7+1+2+3+5..3+5+16497..6+295+78+3+4+1..+6+4.2.35.+32...4..59+43..+2.....9.61.4.+65214..3......6..:818 819 758 768 791 792 798 799:151::";

        //String input = ":0210:29:.6...1..4.48....9+1....+4...+2+4..2.87+5+658+2967+143..65.4+9+2+8+6.....+4+1.+85+4+1.+326.1..4.+6+38.:913 315 325 932 933:311 515 711 715 815::";
        //String input = ":0210:68:...+5.+1......24+9.1..1.3.+69.297+36+5+482+15+21+93+86+74+486+1+27+3951+68+4+95+23+7.4..63+1......+1+2...::321 329 721::";

        //String input = ":0002:1:74..+2+9..6+3+9.8..2..+28.3+7.+945.+2..651......+9+3.+2...328....63...7.9+2.+79+6.2...5+1+2+9+3+8+764:118 123 125 126 185::175:";

        String input = ":0210:29:.6...1..4.48....9+1....+4...+2+4..2.87+5+658+2967+143..65.4+9+2+8+6.....+4+1.+85+4+1.+326.1..4.+6+38.:913 315 325 932 933:311 515 711 715 815::";

        String[] tokens = input.split(":");

        Grid grid = Grid.of(PredefinedType.CLASSIC_9x9);

        grid.accept(new GridValueLoader(tokens[3]));

        String[] dels = tokens[4].split(" ");
        for (String del : dels) {
            if (!del.isEmpty()) {
                int value = Integer.parseInt("" + del.charAt(0));
                int row = Integer.parseInt("" + del.charAt(1));
                int col = Integer.parseInt("" + del.charAt(2));

                Cell cell = grid.getCell(row, col);

                cell.excludePossibleValues(value);
            }
        }

        grid.updateState();
        grid.accept(new GridPrinter(GridPrinter.STYLE.SIMPLE));

        HintSolver solver = new HintSolver(new HiddenPairFinder());

        HintAggregator agg = solver.findHints(grid);
        System.out.println(agg.hints);
    }
}
