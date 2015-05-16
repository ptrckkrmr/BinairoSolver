package net.pgrid.binairosolver;

import java.util.Arrays;
import net.pgrid.binairosolver.Game.State;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the Game class.
 * @author Patrick Kramer
 */
public class GameTest {
    
    /**
     * Creates a Game instance from the specified lines.
     * @param lines The lines.
     * @return      The created Game instance.
     */
    public static Game createGame(String... lines) {
        return Game.create(Arrays.asList(lines));
    }
    
    /**
     * Tests if the {@code createGame} method creates the Game correctly.
     */
    @Test
    public void testCreateGame() {
        Game game = createGame(
                "01",
                "  "
        );
        assertEquals(State.ZERO,  game.get(0, 0));
        assertEquals(State.ONE,   game.get(1, 0));
        assertEquals(State.EMPTY, game.get(0, 1));
        assertEquals(State.EMPTY, game.get(1, 1));
    }
    
    /**
     * Tests if the {@code set} method updates the Game instance.
     */
    @Test
    public void testGetSetState() {
        Game game = createGame(
                "10",
                "  "
        );
        assertEquals(State.EMPTY, game.get(0, 1));
        
        game.set(0, 1, State.ZERO);
        assertEquals(State.ZERO, game.get(0,1));
    }
    
    /**
     * Tests if the {@code isValidCell(int,int)} method changes its result on
     * the correct boundaries.
     */
    @Test
    public void testIsValidCell() {
        Game game = new Game(3, 5);
        
        // Boundary testing of x coordinate.
        assertFalse(game.isValidCell(-1, 0));
        assertTrue(game.isValidCell(0, 0));
        assertTrue(game.isValidCell(2, 0));
        assertFalse(game.isValidCell(3, 0));
        
        // Boundary testing of y coordinate.
        assertFalse(game.isValidCell(0, -1));
        assertTrue(game.isValidCell(0, 0));
        assertTrue(game.isValidCell(0, 4));
        assertFalse(game.isValidCell(0, 5));
    }
    
    /**
     * Tests if a completed Game returns true for {@code isComplete()}.
     */
    @Test
    public void testIsCompleteTrueCase() {
        Game game = createGame(
                "10",
                "01"
        );
        assertTrue(game.isComplete());
    }
    
    /**
     * Tests if an incomplete Game returns false for {@code isComplete()}.
     */
    @Test
    public void testIsCompleteFalseCase() {
        Game game = createGame(
                " 0",
                "01"
        );
        assertFalse(game.isComplete());
    }
    
    /**
     * Tests if creating a copy makes the Game behave independent from the 
     * original Game instance.
     */
    @Test
    public void testCopy() {
        Game game = createGame(
                "10",
                "0 "
        );
        Game copy = game.copy();
        
        assertEquals(game, game);
        assertEquals(game, copy);
        
        copy.set(1, 1, State.ONE);
        assertNotEquals(game, copy);
    }
    
    /**
     * Tests if the {@code toString()} method formats the Game as expected.
     */
    @Test
    public void testToString() {
        Game game = createGame(
                "0101",
                "10 1",
                "   0",
                " 1  "
        );
        String expected = "0101\n10 1\n   0\n 1  \n";
        String actual = game.toString();
        assertEquals(expected, actual);
    }
}
