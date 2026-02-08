import java.util.List;
import java.util.Objects;

public class Puzzle {
    private int boardSize;
    private List<List<Square>> board; //inner list is row, outer list is column.

    public Puzzle(int boardSize, List<List<Square>> board){
        this.boardSize = boardSize;
        this.board = board;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public List<List<Square>> getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Puzzle puzzle = (Puzzle) o;
        return boardSize == puzzle.boardSize && Objects.equals(board, puzzle.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardSize, board);
    }
}
