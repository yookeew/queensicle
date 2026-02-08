import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 0=purple, 1=blue, 2=orange, 3=green, 4=gray, 5=dark blue, 6=red, 7=yellow
        //answer: (0,5), (1,2), (2,0), (3,3), (4,1), (5,4), (6,6)
        Puzzle test = createTest();
        Solver solver = new Solver(test);
        List<List<Integer>> solution = solver.solve();
        System.out.println(solution);
    }
    public static Puzzle createTest() {
        int[][] regions = {
                {0, 0, 0, 1, 3, 4, 4},
                {0, 1, 1, 1, 3, 4, 4},
                {0, 2, 2, 2, 3, 3, 4},
                {0, 2, 2, 3, 3, 4, 4},
                {0, 2, 2, 3, 3, 4, 4},
                {0, 0, 0, 3, 5, 4, 4},
                {5, 5, 5, 5, 5, 5, 6}
        };

        List<List<Integer>> board = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                row.add(regions[i][j]);
            }
            board.add(row);
        }

        return new Puzzle(7, board);
    }
}
