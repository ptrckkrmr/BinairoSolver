package net.pgrid.binairosolver;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.pgrid.binairosolver.Game.State.EMPTY;
import static net.pgrid.binairosolver.Game.State.ONE;
import static net.pgrid.binairosolver.Game.State.ZERO;

/**
 *
 * @author Patrick Kramer
 */
public class Game {
    
    public static enum State {
        EMPTY (' '), 
        ZERO  ('0'), 
        ONE   ('1');
        
        private final char c;
        State(char c) {
            this.c = c;
        }
        public char getSymbol() {
            return c;
        }
    }
    
    private final State[][] values;
    private final int width, height;
    
    public Game(int width, int height) {
        assert width > 0 && height > 0;
        this.width  = width;
        this.height = height;
        this.values = new State[width][height];
    }
    
    public Game(State[][] set) {
        assert set.length > 0 && set[0].length > 0;
        assert set != null;
        this.width  = set.length;
        this.height = set[0].length;
        this.values = new State[width][height];
        for (int i=0; i<set.length; i++) {
            assert set[i].length == height;
            System.arraycopy(set[i], 0, values[i], 0, height);
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
    
    public Game copy() {
        return new Game(values);
    }
    
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    protected State[][] values() {
        return values;
    }
    
    public void set(int x, int y, State value) {
        values[x][y] = value;
    }
    
    public State get(int x, int y) {
        return values[x][y];
    }
    
    public boolean isComplete() {
        return Arrays.stream(values)
                .flatMap(Arrays::stream)
                .noneMatch(s -> s == EMPTY);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ Arrays.deepHashCode(values);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            Game that = (Game) obj;
            return Arrays.deepEquals(this.values(), that.values());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(width * height + height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(values[x][y].getSymbol());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
    public static Game create(Stream<String> lines) {
        return create(lines.collect(Collectors.toList()));
    }
    
    public static Game create(List<String> lines) {
        int height = lines.size() - (int)lines.stream().filter(String::isEmpty).count();
        int width  = lines.stream().mapToInt(String::length).max().orElse(0);
        if (height == 0 || width == 0) {
            throw new IllegalArgumentException("Invalid board size: " + width + "x" + height);
        }
        State[][] values = new State[width][height];
        for (int y=0; y<height; y++) {
            String row = lines.get(y);
            for (int x=0; x<row.length(); x++) {
                switch (row.charAt(x)) {
                    case ' ': values[x][y] = EMPTY; break;
                    case '1': values[x][y] = ONE;   break;
                    case '0': values[x][y] = ZERO;  break;
                    default:  throw new IllegalArgumentException("Unexpected symbol at " + x + "," + y + ": " + row.charAt(x));
                }
            }
            for (int x=row.length(); x<width; x++) {
                values[x][y] = EMPTY;
            }
        }
        assert Arrays.stream(values)
                .flatMap(Arrays::stream)
                .noneMatch(Objects::isNull) : "Null State in result";
        
        return new Game(values);
    }
}
