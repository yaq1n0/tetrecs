package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.*;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class MultiplayerScene extends BaseScene implements
        NextPieceListener,
        GameLoopListener,
        GameOverListener,
        FadeOutListener,
        CommunicationsListener,
        RequestNewPieceListener,
        RequestScoresListener,
        LivesLostListener {

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    protected MultiplayerGame game;

    protected BorderPane mainPane;

    protected Rectangle timer;

    private GameBoard board;

    private PieceBoard currentPieceBoard;

    private PieceBoard followingPieceBoard;

    private ScrollPane chat;

    private TextFlow messages;

    private TextField textField;

    private Button sendButton;

    private VBox scoresVBox;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Multiplayer Scene");
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

        // stuff to the right
        var rightVBox = new VBox();
        rightVBox.setAlignment(Pos.TOP_RIGHT);
        rightVBox.setMaxWidth(200.0);
        mainPane.setRight(rightVBox);

        // creating leaderboard
        scoresVBox = new VBox();
        scoresVBox.setAlignment(Pos.CENTER);
        rightVBox.getChildren().add(scoresVBox);

        // creating chat stuff
        chat = new ScrollPane();
        messages = new TextFlow();
        chat.setContent(messages);
        chat.setMinHeight(200.0);
        chat.setFitToWidth(true);

        rightVBox.getChildren().add(chat);

        var chatHBox = new HBox();
        textField = new TextField();
        textField.setMaxWidth(100.0);
        textField.getStyleClass().add("TextField");

        sendButton = new Button();
        sendButton.setText("send");
        sendButton.setMinWidth(60.0);
        sendButton.getStyleClass().add("button");

        chatHBox.getChildren().add(textField);
        chatHBox.getChildren().add(sendButton);
        chatHBox.setAlignment(Pos.CENTER);
        chatHBox.setMinWidth(10);

        rightVBox.getChildren().add(chatHBox);

        sendButton.setOnAction((e) -> {
            sendCurrentMessage();
        });

        textField.setOnKeyPressed((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendCurrentMessage();
            }
        });
    }

    @Override
    public void receiveCommunication(String communication) {
        if (communication.contains("MSG")) {
            var text = new Text(communication + "\n");
            messages.getChildren().add(text);
            chat.setVvalue(1);
        } else if (communication.contains("PIECE")) {
            if (game.getCurrentPiece() == null) {
                game.setCurrentPiece(GamePiece.createPiece(Integer.parseInt(communication.split(" ")[1])));
                currentPieceBoard.clear();
                currentPieceBoard.displayPiece(game.getCurrentPiece());
            } else {
                game.setFollowingPiece(GamePiece.createPiece(Integer.parseInt(communication.split(" ")[1])));
                followingPieceBoard.clear();
                followingPieceBoard.displayPiece(game.getFollowingPiece());
            }
        } else if (communication.contains("SCORES")) {
            Platform.runLater(() -> updateScores(communication.split(" ")[1]));
        }
    }

    public void updateScores(String message) {
        logger.info("updating player scores");

        scoresVBox.getChildren().clear();

        for (String playerScores : message.split("\n")) {
            Label label = new Label();
            label.setText(playerScores);
            label.getStyleClass().add("label");
            label.setAlignment(Pos.CENTER);
            scoresVBox.getChildren().add(label);
        }
    }

    public void sendCurrentMessage() {
        if (textField.getText() != null) {
            gameWindow.getCommunicator().send("MSG " + textField.getText());
            textField.clear();
        }
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
        game = new MultiplayerGame(5, 5);

        // setting listeners
        game.setNextPieceListener(this::nextPiece);
        game.setGameLoopListener(this::gameLoop);
        game.setGameOverListener(this::gameOver);
        game.setFadeOutListener(this::fadeOut);
        game.setRequestNewPieceListener(this::requestNewPiece);
        game.setRequestScoresListener(this::requestScores);
        game.setLivesLostListener(this::updateLives);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        gameWindow.getCommunicator().addListener(this::receiveCommunication);

        // requesting first pieces
        requestNewPiece();
        requestNewPiece();

        game.start();
    }

    @Override
    public void gameOver() {
        game.stop();
        gameWindow.startScores(game, true);
        gameWindow.getCommunicator().send("DIE");
        gameWindow.getCommunicator().send("PART");
    }

    @Override
    public void fadeOut(int row, int col) {
        board.getGameBlocks()[row][col].fadeOut();
    }

    @Override
    public void requestNewPiece() {
        gameWindow.getCommunicator().send("PIECE");
    }

    @Override
    public void requestScores() {
        gameWindow.getCommunicator().send("SCORES");
    }

    @Override
    public void updateLives(int lives) {
        gameWindow.getCommunicator().send("LIVES " + lives);
    }
}
