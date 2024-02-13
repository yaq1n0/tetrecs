package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class MultiplayerGame extends AbstractGame {

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

    protected GamePiece currentPiece;

    protected GamePiece followingPiece;

    protected IntegerProperty score = new SimpleIntegerProperty();

    protected IntegerProperty level = new SimpleIntegerProperty();

    protected IntegerProperty lives = new SimpleIntegerProperty();

    protected IntegerProperty multiplier = new SimpleIntegerProperty();

    protected ScheduledExecutorService executor;

    protected ScheduledFuture<?> nextLoop;

    protected NextPieceListener nextPieceListener;
    protected GameLoopListener gameLoopListener;
    protected GameOverListener gameOverListener;
    protected FadeOutListener fadeOutListener;
    protected RequestNewPieceListener requestNewPieceListener;
    protected RequestScoresListener requestScoresListener;
    protected LivesLostListener livesLostListener;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols, rows);

        // initialize executorService
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        setScore(0);
        setLevel(0);
        setLives(3);
        setMultiplier(1);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");

        initialiseGame();
        initializeGameLoop();
    }

    public void stop() {
        logger.info("Stopping Game");

        executor.shutdownNow();
    }

    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    public void setGameLoopListener(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    public void setGameOverListener(GameOverListener gameOverListener) {
        this.gameOverListener = gameOverListener;
    }

    public void setFadeOutListener(FadeOutListener fadeOutListener) {
        this.fadeOutListener = fadeOutListener;
    }

    public void setRequestNewPieceListener(RequestNewPieceListener requestNewPieceListener) {
        this.requestNewPieceListener = requestNewPieceListener;
    }

    public void setRequestScoresListener(RequestScoresListener requestScoresListener) {
        this.requestScoresListener = requestScoresListener;
    }

    public void setLivesLostListener(LivesLostListener livesLostListener) {
        this.livesLostListener = livesLostListener;
    }

    /**
     * Handle what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(MouseEvent event, GameBlock gameBlock) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (grid.canPlayPiece(getCurrentPiece(), gameBlock.getX(), gameBlock.getY())) {
                grid.playPiece(getCurrentPiece(), gameBlock.getX(), gameBlock.getY());

                afterPiece();
            } else {
                // can't play piece
                // TODO: add error sound?
            }
        } else if (event.getButton() == MouseButton.SECONDARY) {
            // right clicking on the game board is equivalent to left clicking on the current piece block
            currentPieceBlockClicked(event, gameBlock);
        }
    }

    public void currentPieceBlockClicked(MouseEvent event, GameBlock gameBlock) {
        if (event.getButton() == MouseButton.PRIMARY) {
            rotateCurrentPiece(false);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            rotateCurrentPiece(true);
        } else if (event.getButton() == MouseButton.MIDDLE) {
            swapCurrentPiece();
        }
    }

    public void followingPieceBlockClicked(MouseEvent event, GameBlock gameBlock) {
        if (event.getButton() == MouseButton.PRIMARY) {
            rotateFollowingPiece(false);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            rotateFollowingPiece(true);
        } else if (event.getButton() == MouseButton.MIDDLE) {
            swapCurrentPiece();
        }
    }

    public void requestNewPiece() {
        requestNewPieceListener.requestNewPiece();
    }

    public void rotateCurrentPiece(boolean clockwise) {
        if (clockwise) {
            getCurrentPiece().rotate();
        } else {
            getCurrentPiece().rotate(3);
        }

        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(getCurrentPiece(), getFollowingPiece());
        }
    }

    public void rotateFollowingPiece(boolean clockwise) {
        if (clockwise) {
            getFollowingPiece().rotate();
        } else {
            getFollowingPiece().rotate(3);
        }

        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(getCurrentPiece(), getFollowingPiece());
        }
    }

    public void swapCurrentPiece() {
        GamePiece temp = getCurrentPiece();

        setCurrentPiece(getFollowingPiece());
        setFollowingPiece(temp);

        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(getCurrentPiece(), getFollowingPiece());
        }
    }

    public void nextPiece() {
        setCurrentPiece(getFollowingPiece());
        requestNewPiece();

        if (nextPieceListener != null && getCurrentPiece() != null && getFollowingPiece() != null) {
            nextPieceListener.nextPiece(getCurrentPiece(), getFollowingPiece());
        }
    }

    public void afterPiece() {
        List<Integer> rowsToClear = new ArrayList<>();
        List<Integer> colsToClear = new ArrayList<>();
        Set<List<Integer>> clearedCells = new HashSet<>();

        // checking for rows to clear
        for (int row = 0; row < rows; row++) {
            boolean rowclear = true;
            for (int col = 0; col < cols; col++) {
                if (getGrid().getGridProperty(row, col).getValue() == 0) {
                    rowclear = false;
                }
            }
            if (rowclear) {
                rowsToClear.add(row);
            }
        }

        // checking for columns to clear
        for (int col = 0; col < cols; col++) {
            boolean colclear = true;
            for (int row = 0; row < rows; row++) {
                if (getGrid().getGridProperty(row, col).getValue() == 0) {
                    colclear = false;
                }
            }
            if (colclear) {
                colsToClear.add(col);
            }
        }

        // clearing rows and adding cleared cells to cleared list
        for (int row : rowsToClear) {
            for (int col = 0; col < cols; col++) {
                getGrid().getGridProperty(row, col).set(0);
                List<Integer> clearedCell = new ArrayList<>();
                clearedCell.add(row);
                clearedCell.add(col);
                clearedCells.add(clearedCell);
            }
        }

        // clearing columns and adding cleared cells to cleared list
        for (int col : colsToClear) {
            for (int row = 0; row < rows; row++) {
                getGrid().getGridProperty(row, col).set(0);
                List<Integer> clearedCell = new ArrayList<>();
                clearedCell.add(row);
                clearedCell.add(col);
                clearedCells.add(clearedCell);
            }
        }

        // fading out cleared cells
        for (List<Integer> clearedCell : clearedCells) {
            fadeOutListener.fadeOut(clearedCell.get(0), clearedCell.get(1));
        }

        // setting score based on number of cleared lines and number of cleared cells
        setScore(getScore() + score(rowsToClear.size() + colsToClear.size(), clearedCells.size()));

        // setting multiplier based on line clearing streak
        if (rowsToClear.size() + colsToClear.size() > 0) {
            setMultiplier(getMultiplier() + 1);
        } else {
            setMultiplier(1);
        }

        // checking for level up
        setLevel(Math.floorDiv(getScore(), 1000));

        nextPiece();

        restartGameLoop();

        if (requestScoresListener != null) {
            requestScoresListener.requestScores();
        }
    }

    public int getTimerDelay() {
        return Math.max(12000 - 500 * getLevel(), 2500);
    }

    public void gameOver() {
        logger.info("game over");

        if (gameOverListener != null) {
            gameOverListener.gameOver();
        }
    }

    public void resetChallengeSceneTimer() {
        if (gameLoopListener != null) {
            gameLoopListener.gameLoop(getTimerDelay());
        }
    }

    public void initializeGameLoop() {
        logger.info("initializing game loop");

        // setup the next loop to run gameLoop after getTimerDelay int value of milliseconds
        nextLoop = executor.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);

        // tell listener (challengescene) to re-draw timer
        resetChallengeSceneTimer();
    }

    public void restartGameLoop() {
        logger.info("restarting game loop");

        // cancel the current nextLoop
        nextLoop.cancel(false);

        initializeGameLoop();
    }

    public void gameLoop() {
        logger.info("game loop");

        Platform.runLater(this::gameLoopLogic);

        resetChallengeSceneTimer();

        nextLoop = executor.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
    }

    public void gameLoopLogic() {
        if (getLives() > 0) {
            logger.info("lives reduced to {}", getLives() - 1);

            setLives(getLives() - 1);
            setMultiplier(1);
            nextPiece();
        } else {
            gameOver();
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

    public int score(int lines, int blocks) {
        return lines * blocks * 10 * getMultiplier();
    }

    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    public void setCurrentPiece(GamePiece currentPiece) {
        this.currentPiece = currentPiece;
    }

    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    public void setFollowingPiece(GamePiece followingPiece) {
        this.followingPiece = followingPiece;
    }

    @Override
    public int getScore() {
        return score.get();
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    public int getLevel() {
        return level.get();
    }

    public void setLevel(int level) {
        this.level.set(level);
    }

    public int getLives() {
        return lives.get();
    }

    public void setLives(int lives) {
        this.lives.set(lives);
        if (this.livesLostListener != null) {
            this.livesLostListener.updateLives(getLives());
        }
    }

    public int getMultiplier() {
        return multiplier.get();
    }

    public void setMultiplier(int multiplier) {
        this.multiplier.set(multiplier);
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public IntegerProperty livesProperty() {
        return lives;
    }

    public IntegerProperty multiplierProperty() {
        return multiplier;
    }
}
