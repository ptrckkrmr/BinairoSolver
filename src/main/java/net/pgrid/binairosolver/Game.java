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
 * Represents a Game state.
 * @author Patrick Kramer
 */
public class Game {
    
    /**
     * Represents one of the three possible states of a cell on the board.
     */
    public static enum State {
        /**
         * The unknown State.
         */
        EMPTY (' '), 
        
        /**
         * The State with the '0' value. 
         */
        ZERO  ('0'), 
        
        /**
         * The State with the '1' value.
         */
        ONE   ('1');
        
        private final char c;
        
        /**
         * Creates a State.
         * @param c The character that represents the State.
         */
        private State(char c) {
            this.c = c;
        }
        
        /**
         * Returns the character that represents the State.
         * @return The character that represents the State.
         */
        public char getSymbol() {
            return c;
        }
    }
    
    private final State[][] values;
    private final int width, height;
    
    /**
     * Creates a new game with the given dimensions.
     * @param width  The width of the board, must be positive.
     * @param height The height of the board, must be positive.
     */
    public Game(int width, int height) {
        assert width > 0 && height > 0;
        this.width  = width;
        this.height = height;
        this.values = new State[width][height];
    }
    
    /**
     * Creates a new game from the given two-dimensional State array.
     * @param set The State array, not null and not empty.
     */
    public Game(State[][] set) {
        assert set != null;
        assert set.length > 0 && set[0].length > 0;
        this.width  = set.length;
        this.height = set[0].length;
        this.values = new State[width][height];
        for (int i=0; i<set.length; i++) {
            assert set[i].length == height;
            System.arraycopy(set[i], 0, values[i], 0, height);
        }
    }

    /**
     * Returns the height of the board.
     * @return the height of the board in cells.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width of the board.
     * @return The width of the board in cells.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Produces a copy of this Game instance.
     * @return The created copy.
     */
    public Game copy() {
        return new Game(values);
    }
    
    /**
     * Returns the internal two-dimensional State array.
     * 
     * The array is not copied, so changes to the returned State array are 
     * reflected in this Game instance.
     * 
     * @return The internal State array.
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    protected State[][] values() {
        return values;
    }
    
    /**
     * Sets the cell at the given coordinates to the provided State.
     * @param x     The x coordinate, must be between 0 and {@code width()}.
     * @param y     The y coordinate, must be between 0 and {@code height()}.
     * @param value The new value of the cell, not null.
     */
    public void set(int x, int y, State value) {
        assert value != null;
        assert isValidCell(x, y) : "Cell coordinates out of range";
        values[x][y] = value;
    }
    
    /**
     * Returns the State of the cell at the given coordinates.
     * @param x The x coordinate, must be between 0 and {@code width()}.
     * @param y The y coordinate, must be between 0 and {@code height()}.
     * @return  The State at the given coordinates.
     */
    public State get(int x, int y) {
        assert isValidCell(x, y) : "Cell coordinates out of range";
        return values[x][y];
    }
    
    /**
     * Returns whether the given coordinates correspond to a valid cell on the 
     * board.
     * 
     * This method returns true if (and only if) the x coordinate lies between 
     * 0 and {@code width()}, and the y coordinate lies between 0 and 
     * {@code height()}.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return  True if the cell coordinates are valid, false otherwise.
     */
    public boolean isValidCell(int x, int y) {
        return x >= 0 && x < width 
            && y >= 0 && y < height;
    }
    
    /**
     * Returns whether the board is completely filled.
     * @return True if the board is completely filled, false otherwise.
     */
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
    
    /**
     * Creates a game from the provided String representation.
     * @param lines The Stream of individual lines, not null.
     * @return      The Game encoded in the Stream.
     */
    public static Game create(Stream<String> lines) {
        assert lines != null;
        return create(lines.collect(Collectors.toList()));
    }
    
    /**
     * Creates a Game from the provided String representation.
     * @param lines The List of individual lines, not null.
     * @return      The Game encoded in the List of Strings.
     */
    public static Game create(List<String> lines) {
        assert lines != null;
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
