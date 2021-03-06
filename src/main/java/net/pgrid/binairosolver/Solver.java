package net.pgrid.binairosolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import net.pgrid.binairosolver.Game.State;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static net.pgrid.binairosolver.Game.State.EMPTY;
import static net.pgrid.binairosolver.Game.State.ONE;
import static net.pgrid.binairosolver.Game.State.ZERO;

/**
 * Main class of the Binary puzzle solver.
 * @author Patrick Kramer
 */
public class Solver {
    
    public static final Path INPUT  = Paths.get("samples/binairo2.txt");
    public static final Path OUTPUT = Paths.get("samples/binairo2.solution.txt");
    
    /**
     * Entry point of the application.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        Solver solver = new Solver();
        try {
            Game game = Game.create(Files.lines(INPUT));
            System.out.println("Playing Game (from file " + INPUT + ')');
            System.out.println(game);
            System.out.println();
            Game result = solver.solve(game);
            System.out.println("End Result: ");
            System.out.println(result);
            List<String> output = Arrays.asList(result.toString().split("\n"));
            Files.write(OUTPUT, output, UTF_8, CREATE, WRITE);
            System.out.println("Written to " + OUTPUT);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
    }
    
    /**
     * Solves the provided Game instance.
     * 
     * If the Game is unsolvable, this method returns the argument Game instance.
     * 
     * @param game The Game instance.
     * @return     The solved Game.
     */
    public Game solve(Game game) {
        try {
            return solveImpl(game);
        } catch (SolverException ex) {
            debug(ex);
            return game;
        }
    }
    
    /**
     * Solves the provided Game instance, throwing a SolverException on failure.
     * @param game The Game instance, not null.
     * @return     The solved Game.
     * @throws     SolverException - If the Game could not be solved.
     */
    public Game solveImpl(Game game) throws SolverException {
        assert game != null;
        Game original, newGame = game;
        do {
            original = newGame;
            try {
                newGame = solveSimpleRules(original.copy());
            } catch (SolverException ex) {
                // The solve step got stuck. Return.
                System.err.println("Got Stuck in basic Rule: " + ex.getMessage());
                return original;
            }
            if (original.equals(newGame)) {
                try {
                    return findGuess(original);
                } catch (SolverException ex) {
                    // The guess step got stuck. Return.
                    System.err.println("Got Stuck in Guess: " + ex.getMessage());
                    return original;
                }
            }
        } while (!newGame.isComplete());
        return newGame;
    }
    
    /**
     * Tries to guess a field on the board.
     * @param game The Game instance, not null.
     * @return     The solved Game.
     * @throws     SolverException - If the Game could not be solved.
     */
    public Game findGuess(Game game) throws SolverException {
        int x = 0, y = 0;
LOOP:   for (; x < game.getWidth(); x++) {
            for (; y < game.getHeight(); y++) {
                if (game.get(x,y) == State.EMPTY) {
                    break LOOP;
                }
            }
        }
        if (!game.isValidCell(x, y)) {
            throw new SolverException("Cannot find an empty cell.");
        }
        try {
            return guessSolve(game, x, y, State.ZERO);
        } catch (SolverException ex1) {
            try {
                return guessSolve(game, x, y, State.ONE);
            } catch (SolverException ex2) {
                ex2.addSuppressed(ex1);
                throw ex2;
            }
        }
    }
    
    
    /**
     * Tries to solve the Game using simple Rules.
     * @param game The Game
     * @return     A (partial) solution to the Game.
     * @throws     SolverException - If the Game is unsolvable.
     */
    public Game solveSimpleRules(Game game) throws SolverException {
        Game result = game;
        
        result = solveDoubleRulePerColumn(result);
        result = solveDoubleRulePerRow(result);
        result = solveGapRulePerColumn(result);
        result = solveGapRulePerRow(result);
        result = solveValueCountPerColumn(result);
        result = solveValueCountPerRow(result);
        
        return result;
    }
    
    /**
     * Inverts the argument State.
     * @param s The State.
     * @return  The inverse of the State.
     */
    public State invert(State s) {
        switch (s) {
            case EMPTY: return EMPTY;
            case ZERO:  return ONE;
            case ONE:   return ZERO;
            default: throw new AssertionError("Unreachable state");
        }
    }
    
    /**
     * Logs the given message.
     * 
     * The message is converted to a String using the {@code String.valueOf} 
     * method.
     * @param msg The message.
     */
    public void debug(Object msg) {
        System.out.println(msg);
    }
    
    /**
     * Performs a safe update on the provided Game instance.
     * @param g The Game instance, not null.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param s The new State, nor null.
     * @return  The updated board.
     * @throws  SolverException - If a collision occurs. That is, if a 
     *          {@code ZERO} State would be overwritten by a {@code ONE} State 
     *          or the other way around.
     */
    public Game checkedUpdate(Game g, int x, int y, State s) throws SolverException {
        if (x < 0 || y < 0 || x >= g.getWidth() || y >= g.getHeight()) {
            // Cannot update, ignore.
            return g;
        }
        State current = g.get(x, y);
        if (current == s) {
            return g;
        } else if (current == EMPTY) {
            g.set(x, y, s);
        } else {
            // Collision! Throw a SolverException
            throw new SolverException("Collision (" + x + "," + y + "): " + 
                    current.name() + " => " + s.name());
        }
        return g;
    }
    
    /**
     * Fills any remaining empty cells in the given row with the provided 
     * value.
     * @param game  The Game instance, not null.
     * @param row   The row index, between 0 and {@code game.getHeight()}.
     * @param value The State value, not null.
     * @return      The Game with the filled states.
     * @throws      SolverException - If an update collision occurs.
     */
    public Game fillRemainingRow(Game game, int row, State value) throws SolverException {
        for (int x = 0; x < game.getWidth(); x++) {
            State s = game.get(x, row);
            if (s == EMPTY) {
                checkedUpdate(game, x, row, value);
            }
        }
        return game;
    }
    
    /**
     * Fills any remaining empty cells in the given column with the provided 
     * value.
     * @param game  The Game instance, not null.
     * @param col   The column index, between 0 and {@code game.getWidth()}.
     * @param value The State value, not null.
     * @return      The Game with the filled states.
     * @throws      SolverException - If an update collision occurs.
     */
    public Game fillRemainingColumn(Game game, int col, State value) throws SolverException {
        for (int y = 0; y < game.getHeight(); y++) {
            State s = game.get(col, y);
            if (s == EMPTY) {
                checkedUpdate(game, col, y, value);
            }
        }
        return game;
    }
    
    /**
     * Makes a guess and tries to solve recursively from there. 
     * @param game  The Game 
     * @param x     The x coordinate
     * @param y     The y coordinate
     * @param guess The initial guess
     * @return      A full solution
     * @throws      SolverException - If the Game has no solution.
     */
    public Game guessSolve(Game game, int x, int y, State guess) throws SolverException {
        Game copy = game.copy();
        try {
            return solveImpl(checkedUpdate(copy, x, y, guess));
        } catch (SolverException e1) {
            try {
                return solveImpl(checkedUpdate(copy, x, y, invert(guess)));
            } catch (SolverException e2) {
                SolverException e = new SolverException("No valid move for (" + x + "," + y + ")", e2);
                e.addSuppressed(e1);
                throw e;
            }
        }
    }
    /**
     * Fills {@code _11_} and {@code _00_} patterns in rows.
     * @param game The Game
     * @return     A (partial) solution
     * @throws     SolverException - If the Game is unsolvable.
     */
    public Game solveDoubleRulePerRow(Game game) throws SolverException {
        for (int y=0; y<game.getHeight(); y++) {
            for (int x=0; x<game.getWidth()-1; x++) {
                State s = game.get(x, y);
                if (s != EMPTY && s == game.get(x+1, y)) {
                    State inverse = invert(s);
                    checkedUpdate(game, x-1, y, inverse);
                    checkedUpdate(game, x+2, y, inverse);
                }
            }
        }
        return game;
    }
    /**
     * Fills {@code 1_1} and {@code 0_0} patterns in rows.
     * @param game The Game
     * @return     A (partial) solution
     * @throws     SolverException - If the Game is unsolvable.
     */
    public Game solveGapRulePerRow(Game game) throws SolverException {
        for (int y=0; y<game.getHeight(); y++) {
            for (int x=1; x<game.getWidth()-1; x++) {
                State s = game.get(x, y);
                if (s == EMPTY && game.get(x-1, y) == game.get(x+1, y)) {
                    State inverse = invert(game.get(x-1, y));
                    checkedUpdate(game, x, y, inverse);
                }
            }
        }
        return game;
    }
    /**
     * Fills {@code _11_} and {@code _00_} patterns in columns.
     * @param game The Game
     * @return     A (partial) solution
     * @throws     SolverException - If the Game is unsolvable.
     */
    public Game solveDoubleRulePerColumn(Game game) throws SolverException {
        for (int y=0; y<game.getHeight()-1; y++) {
            for (int x=0; x<game.getWidth(); x++) {
                State s = game.get(x, y);
                if (s != EMPTY && s == game.get(x, y+1)) {
                    State inverse = invert(s);
                    checkedUpdate(game, x, y-1, inverse);
                    checkedUpdate(game, x, y+2, inverse);
                }
            }
        }
        return game;
    }
    /**
     * Fills {@code 1_1} and {@code 0_0} patterns in columns.
     * @param game The Game
     * @return     A (partial) solution
     * @throws     SolverException - If the Game is unsolvable.
     */
    public Game solveGapRulePerColumn(Game game) throws SolverException {
        for (int y=1; y<game.getHeight()-1; y++) {
            for (int x=0; x<game.getWidth(); x++) {
                State s = game.get(x, y);
                if (s == EMPTY && game.get(x, y-1) == game.get(x, y+1)) {
                    State inverse = invert(game.get(x, y+1));
                    checkedUpdate(game, x, y, inverse);
                }
            }
        }
        return game;
    }
    /**
     * Fills the 0 and 1 counting patterns in rows.
     * @param game The Game
     * @return     A (partial) solution
     * @throws     SolverException - If the Game is unsolvable.
     */
    public Game solveValueCountPerRow(Game game) throws SolverException {
        Game result = game;
        int w = game.getWidth(), h = game.getHeight();
        for (int y=0; y<h; y++) {
            int c0 = 0, c1 = 0;
            for (int x=0; x<w; x++) {
                State s = result.get(x, y);
                if (s == ZERO) {
                    c0++;
                } else if (s == ONE) {
                    c1++;
                }
            }
            if (2*c0 >= w) {
                result = fillRemainingRow(result, y, ONE);
            } else if (2*c1 >= w) {
                result = fillRemainingRow(result, y, ZERO);
            }
        }
        return result;
    }
    /**
     * Fills the 0 and 1 counting patterns in columns.
     * @param game The Game
     * @return     A (partial) solution
     * @throws     SolverException - If the Game is unsolvable.
     */
    public Game solveValueCountPerColumn(Game game) throws SolverException {
        Game result = game;
        int w = game.getWidth(), h = game.getHeight();
        for (int x=0; x<w; x++) {
            int c0 = 0, c1 = 0;
            for (int y=0; y<h; y++) {
                State s = result.get(x, y);
                if (s == ZERO) {
                    c0++;
                } else if (s == ONE) {
                    c1++;
                }
            }
            if (2*c0 >= h) {
                result = fillRemainingColumn(result, x, ONE);
            } else if (2*c1 >= h) {
                result = fillRemainingColumn(result, x, ZERO);
            }
        }
        return result;
    }
    
}
