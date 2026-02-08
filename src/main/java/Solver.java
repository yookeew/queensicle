import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Solver {
    private Model model;
    private Puzzle puzzle;
    private IntVar[][] vars;

    public Solver(Puzzle puzzle){
        this.model = new Model("solver");
        this.puzzle = puzzle;
        int n = puzzle.getBoardSize();
        this.vars = new IntVar[n][n];
    }

    public List<List<Integer>> solve(){
        int n = puzzle.getBoardSize();

        //load variables
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                vars[i][j] = model.intVar("Q_" + i + "_" + j, 0, 1);
            }
        }

        //row
        for(int i = 0; i < n; i++){
            model.sum(vars[i], "=", 1).post();
        }

        //column
        for(int j = 0; j < n; j++){
            IntVar[] column = new IntVar[n];
            for(int i = 0; i < n; i++){
                column[i] = vars[i][j];
            }
            model.sum(column, "=", 1).post();
        }

        //no touchy
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                List<IntVar> neighbors = new ArrayList<>();
                for(int di = -1; di <= 1; di++){
                    for(int dj = -1; dj <= 1; dj++){
                        if(di == 0 && dj == 0) continue;
                        int ni = i + di;
                        int nj = j + dj;
                        if(ni >= 0 && ni < n && nj >= 0 && nj < n){
                            neighbors.add(vars[ni][nj]);
                        }
                    }
                }
                model.ifThen(
                        model.arithm(vars[i][j], "=", 1),
                        model.sum(neighbors.toArray(new IntVar[0]), "=", 0)
                );
            }
        }

        //1 per region
        int maxRegion = 0;
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                maxRegion = Math.max(maxRegion, puzzle.getBoard().get(i).get(j).getRegion());
            }
        }

        for(int regionId = 0; regionId <= maxRegion; regionId++){
            List<IntVar> regionVars = new ArrayList<>();
            for(int i = 0; i < n; i++){
                for(int j = 0; j < n; j++){
                    if(puzzle.getBoard().get(i).get(j).getRegion() == regionId){
                        regionVars.add(vars[i][j]);
                    }
                }
            }
            model.sum(regionVars.toArray(new IntVar[0]), "=", 1).post();
        }

        //solve it
        Solution solution = model.getSolver().findSolution();
        if(solution != null){
            List<List<Integer>> coordinates = new ArrayList<>();
            for(int i = 0; i < n; i++){
                for(int j = 0; j < n; j++){
                    if(solution.getIntVal(vars[i][j]) == 1){
                        coordinates.add(Arrays.asList(i, j));
                    }
                }
            }
            return coordinates;
        } else {
            return null;
        }
    }
}