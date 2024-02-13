package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.AbstractGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class NewHighScoreScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(NewHighScoreScene.class);

    private final AbstractGame game;
    private final int gameScore;
    private final ArrayList<Pair<String, Integer>> localScores;
    private final ArrayList<Pair<String, Integer>> onlineScores;
    private final int caseNumber;

    private TextField nameField;

    public NewHighScoreScene(GameWindow gameWindow, AbstractGame game, ArrayList<Pair<String, Integer>> localScores, ArrayList<Pair<String, Integer>> onlineScores, int caseNumber) {
        super(gameWindow);
        logger.info("creating New HighScore scene");

        this.game = game;
        this.gameScore = game.getScore();
        this.localScores = localScores;
        this.onlineScores = onlineScores;
        this.caseNumber = caseNumber;
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var hsPane = new StackPane();
        hsPane.setMaxWidth(gameWindow.getWidth());
        hsPane.setMaxHeight(gameWindow.getHeight());
        hsPane.getStyleClass().add("menu-background"); // subject to change
        root.getChildren().add(hsPane);

        var mainPane = new BorderPane();
        hsPane.getChildren().add(mainPane);

        var mainVBox = new VBox();
        var title = new Label();
        var subtitle = new Label();

        nameField = new TextField();
        nameField.setOnKeyPressed(this::saveNameScoreKeyboard);

        var saveButton = new Button();
        saveButton.setOnAction(this::saveNameScoreMouse);

        title.setText("New High Score!");
        subtitle.setText("enter your name below: ");
        saveButton.setText("save");

        title.getStyleClass().add("bigTitle");
        subtitle.getStyleClass().add("title");
        nameField.getStyleClass().add("TextField");
        saveButton.getStyleClass().add("button");

        mainVBox.getChildren().add(title);
        mainVBox.getChildren().add(subtitle);
        mainVBox.getChildren().add(nameField);
        mainVBox.getChildren().add(saveButton);

        mainVBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(mainVBox);
    }

    @Override
    public void initialise() {
        logger.info("initalizing new high score scene");
    }

    private void saveNameScoreKeyboard(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            saveNameScoreAux();
        }
    }

    public void saveNameScoreMouse(ActionEvent event) {
        saveNameScoreAux();
    }

    public void saveNameScoreAux() {
        logger.info("saving name and score");

        String nameFieldText = nameField.getText();

        if (nameFieldText != null) {
            if (caseNumber == 3) {
                // update both
                logger.info("case number 3");
                localScores.add(new Pair<>(nameFieldText, gameScore));
                onlineScores.add(new Pair<>(nameFieldText, gameScore));
                localScores.sort(Comparator.comparing(pair -> -pair.getValue()));
                onlineScores.sort(Comparator.comparing(pair -> -pair.getValue()));
                updateLocalScores();
                updateOnlineScores(nameFieldText);
            } else if (caseNumber == 1) {
                // update only local scores
                logger.info("case number 1");
                localScores.add(new Pair<>(nameFieldText, gameScore));
                localScores.sort(Comparator.comparing(pair -> -pair.getValue()));
                updateLocalScores();
            } else {
                // update only online scores
                logger.info("case number 2");
                onlineScores.add(new Pair<>(nameFieldText, gameScore));
                onlineScores.sort(Comparator.comparing(pair -> -pair.getValue()));
                updateOnlineScores(nameFieldText);
            }

            gameWindow.startScores(this.game, false);
        }
    }

    private void updateLocalScores() {
        logger.info("updating local scores");

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ScoresScene.scoresFilePath));

            for (Pair<String, Integer> score : localScores) {
                bw.write(score.getKey() + ":" + score.getValue() + "\n");
            }

            bw.close();
        } catch (IOException exception) {
            logger.info(exception.getMessage());
        }
    }

    private void updateOnlineScores(String name) {
        logger.info("updating online scores");
        gameWindow.getCommunicator().send("HISCORE " + name + ":" + gameScore);
    }
}
