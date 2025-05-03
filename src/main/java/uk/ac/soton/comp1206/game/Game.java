package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.LivesListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.utilty.Multimedia;


import java.util.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The Current piece
     */
    protected GamePiece currentPiece;

    /**
     * The Following piece
     */
    protected GamePiece followingPiece;

    /**
     *The Score property
     */
    protected SimpleIntegerProperty score = new SimpleIntegerProperty(0);

    /**
     *The Level property
     */
    protected SimpleIntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * The Lives property
     */
    protected SimpleIntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * The Multiplier property
     */
    protected SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * The Next piece listener
     */
    private NextPieceListener nextPieceListener;

    /**
     * The Line cleared listener
     */
    private LineClearedListener lineClearedListener;

    /**
     * The Game loop listener
     */
    private GameLoopListener gameLoopListener;

    /**
     * The Lives listener
     */
    private LivesListener livesListener;

    /**
     * The Game timer
     */
    private Timer timer;

    /**
     * The Timer task
     */
    private TimerTask timerTask;

    /**
     * The Multimedia
     */
    private Multimedia multimedia;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        //Create a new grid model to represent the game state
        this.grid = new Grid(cols, rows);
    }

    /**
     * Start the game
     */
    public void start() {
        Multimedia.stopMusic();
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initializing game");

        // Start background music
        multimedia.setMusicPlayer("game.wav");

        // Spawn the current and following pieces
        currentPiece = spawnPiece();
        followingPiece = spawnPiece();

        // Set up listener for displaying the next piece
        nextPieceListen();

        // Set up the game timer
        setTimer();

        // Start the game loop listener with the initial timer delay
        loopListener(getTimerDelay());
    }

    /**
     * Handle what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        logger.info("Block clicked at (" + gameBlock.getX() + "," + gameBlock.getY() + ")");

        // Get the coordinates of the clicked block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        // Check if the current piece can be played at the clicked position
        if (grid.canPlayPiece(currentPiece, x, y)) {
            logger.info("Playing piece at (" + x + "," + y + ")");
            grid.playPiece(currentPiece, x, y);
            Multimedia.setAudioPlayer("place.wav"); // Play sound for successful placement

            // Cancel existing timer and task, then set a new timer with appropriate delay
            timer.cancel();
            timerTask.cancel();
            setTimer();
            loopListener(getTimerDelay());

            // Handle actions after placing the piece (e.g., check for line clears)
            afterPiece();

            // Move to the next piece
            nextPiece();
        } else {
            logger.info("Failed to play piece at (" + x + "," + y + ")");
            Multimedia.setAudioPlayer("fail.wav"); // Play sound for failed placement
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Spawns a new piece
     *
     * @return new piece
     */
    public GamePiece spawnPiece() {
        logger.info("Spawning Piece");
        Random random = new Random();
        // Generate random integer between 0 and 14 (inclusive)
        return GamePiece.createPiece(random.nextInt(15));
    }

    /**
     * Spawns next pieces
     */
    public void nextPiece() {
        logger.info("Getting next piece");
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        nextPieceListen();
    }

    /**
     * Cleans up after a piece is played by
     * checking if there are lines to be cleared
     * and the score needs to be updated
     */
    public void afterPiece() {
        logger.info("Starting Cleanup");
        ArrayList<Integer> rows = checkRow();
        ArrayList<Integer> cols = checkColumns();
        HashSet<GameBlockCoordinate> coordinates = getCoordinates(cols, rows);
        int lines = cols.size() + rows.size();
        int blocks = coordinates.size();

        lineCleared(coordinates);

        for (GameBlockCoordinate gameBlockCoordinate : coordinates) {
            logger.info("Cleaning");
            int X = gameBlockCoordinate.getX();
            int Y = gameBlockCoordinate.getY();
            grid.set(X, Y, 0);
        }
        int oldScore = getScore().get();
        score(lines, blocks);
        int newScore = getScore().get();

        if (oldScore != newScore) {
          Multimedia.setAudioPlayer("lineclear.mp3");
        }
    }

    /**
     * Checks if any row is filled
     *
     * @return fullRows
     */
    public ArrayList<Integer> checkRow() {
        ArrayList<Integer> fullRows = new ArrayList<>();
        for (int rows = 0; rows < getRows(); rows++) {
            int rowCounter = 0;
            for (int cols = 0; cols < getCols(); cols++) {
                if (getGrid().get(cols, rows) != 0) {
                    if (rowCounter + 1 == 5) {
                        logger.info("Adding row to clean: " + rows);
                        fullRows.add(rows);
                        rowCounter = 0;
                    }
                    rowCounter++;
                }
            }
        }
        return fullRows;
    }

    /**
     * Checks if any columns are filled
     *
     * @return fullColumns
     */
    public ArrayList<Integer> checkColumns() {
        ArrayList<Integer> fullColumns = new ArrayList<>();
        for (int cols = 0; cols < getCols(); cols++) {
            int colCounter = 0;
            for (int rows = 0; rows < getRows(); rows++) {
                if (getGrid().get(cols, rows) != 0) {
                    if (colCounter + 1 == 5) {
                        logger.info("Adding col to clean: " + cols);
                        fullColumns.add(cols);
                        colCounter = 0;
                    }
                    colCounter++;
                }
            }
        }
        return fullColumns;
    }

    /**
     * Gets the coordinates for all the rows that
     * are filled
     *
     * @param Columns
     * @param Rows
     * @return coordinates
     */
    public HashSet<GameBlockCoordinate> getCoordinates(ArrayList<Integer> Columns, ArrayList<Integer> Rows) {
        HashSet<GameBlockCoordinate> coordinates = new HashSet<>();
        for (int row : Rows) {
            for (int col = 0; col < getCols(); col++) {
                GameBlockCoordinate gameBlockCoordinate = new GameBlockCoordinate(col, row);
                coordinates.add(gameBlockCoordinate);
            }
        }
        for (int col : Columns) {
            for (int row = 0; row < getRows(); row++) {
                GameBlockCoordinate gameBlockCoordinate = new GameBlockCoordinate(col, row);
                coordinates.add(gameBlockCoordinate);
            }
        }
        return coordinates;
    }

    /**
     * Getter for score
     *
     * @return score
     */
    public SimpleIntegerProperty getScore() {
        return score;
    }

    /**
     * Getter for level
     *
     * @return level
     */
    public SimpleIntegerProperty getLevel() {
        return level;
    }

    /**
     * Getter for lives
     *
     * @return lives
     */
    public SimpleIntegerProperty getLives() {
        return lives;
    }

    /**
     * Getter for multiplier
     *
     * @return multiplier
     */
    public SimpleIntegerProperty getMultiplier() {
        return multiplier;
    }

    /**
     * Getter for current piece
     *
     * @return current piece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     * Getter for followingPiece
     *
     * @return followingPiece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    /**
     * Getter for timer
     *
     * @return timer
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Updates score and checks if the level
     * needs updating as well as the multiplier
     *
     * @param lines
     * @param blocks
     */
    public void score(int lines, int blocks) {
        var oldScore = score.getValue();
        score.setValue(score.getValue() + lines * blocks * 10 * multiplier.getValue());
        checkLevel();
        checkMultiplier(oldScore);
    }

    /**
     * Updates level based on score
     */
    protected void checkLevel() {
        logger.info("Checking Level");
        level.setValue(getScore().getValue() / 1000);
    }

    /**
     * Checks the multiplier to see if it needs
     * updating
     *
     * @param oldScore
     */
    public void checkMultiplier(int oldScore) {
        if (score.getValue() != oldScore) {
            multiplier.setValue(getMultiplier().getValue() + 1);
            logger.info("Multiplier increased to: " + multiplier.getValue());
        } else {
            logger.info("Resetting Multiplier");
            multiplier.setValue(1);
        }
    }

    /**
     * Rotates the current piece
     *
     * @param amount
     */
    public void rotateCurrentPiece(int amount) {
       Multimedia.setAudioPlayer("rotate.wav");
        currentPiece.rotate(amount);
    }

    /**
     * Swaps pieces
     */
    public void switchPieces() {
        Multimedia.setAudioPlayer("transition.wav");
        var piece = currentPiece;
        currentPiece = followingPiece;
        followingPiece = piece;

    }

    /**
     * Sets next piece listener
     *
     * @param listener
     */
    public void setNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }

    /**
     * Shows next pieces on the piece boards
     */
    public void nextPieceListen() {
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
    }

    /**
     * Sets line cleared listener
     *
     * @param listener
     */
    public void setLineClearedListener(LineClearedListener listener) {
        this.lineClearedListener = listener;
    }

    /**
     * Sends coordinates for the game blocks
     * to be animated
     *
     * @param coordinates
     */
    public void lineCleared(HashSet<GameBlockCoordinate> coordinates) {
        if (lineClearedListener != null) {
            lineClearedListener.lineCleared(coordinates);
        }
    }

    /**
     * Sets lives cleared listener
     *
     * @param listener
     */
    public void setLivesListener(LivesListener listener) {
        this.livesListener = listener;
    }

    /**
     * Checks to see if there are any lives left
     */
    public void livesListener() {
        if (livesListener != null) {
            livesListener.checkLives();
        }
    }

    /**
     * Sets game loop listener
     * @param listener
     */
    public void setGameLoopListener(GameLoopListener listener) {
        this.gameLoopListener = listener;
    }

    /**
     * Restarts the game loop for the time bar
     *
     * @param time
     */
    public void loopListener(int time) {
        if (gameLoopListener != null) {
            gameLoopListener.gameLoop(time);
        }
    }

    /**
     * Sets game timer
     */
    public void setTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> gameLoop());
            }
        };
        timer.schedule(timerTask, getTimerDelay());
    }

    /**
     * Sets the timers delay
     *
     * @return timer delay
     */
    public int getTimerDelay() {
        return Math.max(2500, (12000 - 500 * level.getValue()));
    }

    /**
     * Restarts the game loop, reduces the lives
     * and checks if there are any lives left
     */
    public void gameLoop() {
        logger.info("Restarting Timer");
        multiplier.setValue(1);
        lives.setValue(lives.get() - 1);
        livesListener();
        if (lives.get() >= 0) {
            uk.ac.soton.comp1206.utilty.Multimedia.setAudioPlayer("lifelose.wav");
            nextPiece();
            timer.cancel();
            timerTask.cancel();
            setTimer();
            loopListener(getTimerDelay());
        } else {
            logger.info("Closing Loop");
        }
    }
}