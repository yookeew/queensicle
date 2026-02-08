import java.util.Objects;

public class Square {
    private int region;
    private boolean blocked;

    public Square(int region, boolean blocked){
        this.region = region;
        this.blocked = blocked;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Square square = (Square) o;
        return region == square.region && blocked == square.blocked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, blocked);
    }
}
