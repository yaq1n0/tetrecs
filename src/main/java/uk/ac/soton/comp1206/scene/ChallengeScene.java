package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.FadeOutListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener, GameLoopListener, GameOverListener, FadeOutListener {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);

    protected Game game;

    protected BorderPane mainPane;

    protected Rectangle timer;

    private GameBoard board;

    private PieceBoard currentPieceBoard;

    private PieceBoard followingPieceBoard;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        // game text
        var scoreText = new Label();
        var levelText = new Label();
        var livesText = new Label();
        var multiplierText = new Label();

        // game values
        var scoreLabel = new Label();
        var levelLabel = new Label();
        var livesLabel = new Label();
        var multiplierLabel = new Label();

        // pieceboard labels
        var nextPieceText = new Label();
        var followingPieceText = new Label();

        // Setting game text
        scoreText.setText("Score: ");
        levelText.setText("Level: ");
        livesText.setText("Lives: ");
        multiplierText.setText("Multiplier: ");

        // setting pieceboard label text
        nextPieceText.setText("Current Piece");
        followingPieceText.setText("Following Piece");

        // binding game properties to top values
        scoreLabel.textProperty().bind(game.scoreProperty().asString());
        levelLabel.textProperty().bind(game.levelProperty().asString());
        livesLabel.textProperty().bind(game.livesProperty().asString());
        multiplierLabel.textProperty().bind(game.multiplierProperty().asString());

        // CSS for top text
        scoreText.getStyleClass().add("gameUI");
        levelText.getStyleClass().add("gameUI");
        livesText.getStyleClass().add("gameUI");
        multiplierText.getStyleClass().add("gameUI");

        // CSS for top values
        scoreLabel.getStyleClass().add("gameUI");
        levelLabel.getStyleClass().add("gameUI");
        livesLabel.getStyleClass().add("gameUI");
        multiplierLabel.getStyleClass().add("gameUI");

        // CSS for pieceboard text
        nextPieceText.getStyleClass().add("gameUI");
        followingPieceText.getStyleClass().add("gameUI");

        // left side organization
        var leftBox = new HBox();
        var leftSubBoxA = new VBox();
        var leftSubBoxB = new VBox();

        leftBox.getChildren().add(leftSubBoxA);
        leftBox.getChildren().add(leftSubBoxB);

        leftSubBoxA.getChildren().add(scoreText);
        leftSubBoxA.getChildren().add(levelText);
        leftSubBoxA.getChildren().add(livesText);
        leftSubBoxA.getChildren().add(multiplierText);

        leftSubBoxB.getChildren().add(scoreLabel);
        leftSubBoxB.getChildren().add(levelLabel);
        leftSubBoxB.getChildren().add(livesLabel);
        leftSubBoxB.getChildren().add(multiplierLabel);

        leftSubBoxA.setSpacing(12.0);
        leftSubBoxB.setSpacing(12.0);

        // bottom timer
        timer = new Rectangle();
        timer.setHeight(20.0);
        timer.setWidth(gameWindow.getWidth());
        timer.setFill(Color.RED);

        // main board
        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2.0, gameWindow.getWidth() / 2.0);
        board.setOnBlockClick(this::blockClicked);
        board.setEnableDrawHover(true);

        // current piece
        currentPieceBoard = new PieceBoard(gameWindow.getWidth() / 5.0, gameWindow.getWidth() / 5.0);
        currentPieceBoard.setOnBlockClick(this::blockClicked);
        currentPieceBoard.drawCenterDot(true);

        // following piece
        followingPieceBoard = new PieceBoard(gameWindow.getWidth() / 5.0, gameWindow.getWidth() / 5.0);
        followingPieceBoard.setOnBlockClick(this::blockClicked);
        currentPieceBoard.drawCenterDot(true);

        leftSubBoxA.getChildren().add(nextPieceText);
        leftSubBoxA.getChildren().add(currentPieceBoard);
        leftSubBoxA.getChildren().add(followingPieceText);
        leftSubBoxA.getChildren().add(followingPieceBoard);

        leftBox.setAlignment(Pos.CENTER_LEFT);
        mainPane.setLeft(leftBox);

        board.setAlignment(Pos.CENTER);
        mainPane.setCenter(board);

        mainPane.setBottom(timer);
    }

    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(MouseEvent event, GameBlock gameBlock) {
        if (gameBlock.getGameBoard().equals(board)) {
            // block on the main board is clicked
            game.blockClicked(event, gameBlock);
        } else if (gameBlock.getGameBoard().equals(currentPieceBoard)) {
            // block on the current piece board clicked
            game.currentPieceBlockClicked(event, gameBlock);
        } else if (gameBlock.getGameBoard().equals(followingPieceBoard)) {
            // block on the following piece board clicked
            game.followingPieceBlockClicked(event, gameBlock);
        }
    }

    @Override
    public void nextPiece(GamePiece currentPiece, GamePiece followingPiece) {
        currentPieceBoard.clear();
        followingPieceBoard.clear();

        currentPieceBoard.displayPiece(currentPiece);
        followingPieceBoard.displayPiece(followingPiece);
    }

    @Override
    public void gameLoop(int timerDelay) {
        // draw and start the timer in this.timer rectangle
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREEN)),
                new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), gameWindow.getWidth())),
                new KeyFrame(new Duration((double) timerDelay * 0.5D), new KeyValue(timer.fillProperty(), Color.YELLOW)),
                new KeyFrame(new Duration((double) timerDelay * 0.75D), new KeyValue(timer.fillProperty(), Color.RED)),
                new KeyFrame(new Duration(timerDelay), new KeyValue(timer.widthProperty(), 0))
        );
        timeline.play();
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);

        // setting listeners
        game.setNextPieceListener(this::nextPiece);
        game.setGameLoopListener(this::gameLoop);
        game.setGameOverListener(this::gameOver);
        game.setFadeOutListener(this::fadeOut);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        game.start();
    }

    @Override
    public void gameOver() {
        game.stop();
        gameWindow.startScores(game, true);
    }

    @Override
    public void fadeOut(int row, int col) {
        board.getGameBlocks()[row][col].fadeOut();
    }
}
