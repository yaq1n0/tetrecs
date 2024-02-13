package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.event.ScoresListListener;
import uk.ac.soton.comp1206.game.AbstractGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ScoresScene extends BaseScene implements CommunicationsListener {

    public static final String scoresFilePath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "localscores.txt";
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    private final AbstractGame game;
    private final int gameScore;
    private final boolean checkHighScore;

    private final ArrayList<Pair<String, Integer>> localScores = new ArrayList<>();
    private ArrayList<Pair<String, Integer>> onlineScores = new ArrayList<>();

    private ScoresListListener localScoresListener;
    private ScoresListListener onlineScoresListener;

    private BorderPane mainPane;
    private HBox scoresHBox;
    private ScoresList localScoresList;
    private ScoresList onlineScoresList;

    public ScoresScene(GameWindow gameWindow, AbstractGame game, boolean checkHighScore) {
        super(gameWindow);
        logger.info("Creating Scores Scene after game over");
        this.game = game;
        this.gameScore = game.getScore();
        this.checkHighScore = checkHighScore;
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var scoresPane = new StackPane();
        scoresPane.setMaxWidth(gameWindow.getWidth());
        scoresPane.setMaxHeight(gameWindow.getHeight());
        scoresPane.getStyleClass().add("menu-background"); // subject to change
        root.getChildren().add(scoresPane);

        mainPane = new BorderPane();
        scoresPane.getChildren().add(mainPane);
        scoresHBox = new HBox();
        localScoresList = new ScoresList();
        onlineScoresList = new ScoresList();
        scoresHBox.getChildren().add(localScoresList);
        scoresHBox.getChildren().add(onlineScoresList);
        scoresHBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(scoresHBox);
    }

    @Override
    public void initialise() {
        logger.info("initalizing scores scene");

        gameWindow.getCommunicator().addListener(this::receiveCommunication);

        setLocalScoresListener(localScoresList::updateScores);
        setOnlineScoresListener(onlineScoresList::updateScores);

        localScoresList.setTitle("Local Scores");
        onlineScoresList.setTitle("Online Scores");

        loadScoresFromFile();
        loadScoresFromCommunicator();

        if (this.checkHighScore) {
            checkNewHighScore();
        }
    }

    public void checkNewHighScore() {
        logger.info("checking for new HighScore");

        int caseNumber = 0;

        for (Pair<String, Integer> score : localScores) {
            if (gameScore > score.getValue()) {
                // new high score
                caseNumber = 1; // beaten local highscore
                gameWindow.startNewHighScoreScene(game, localScores, onlineScores, caseNumber);
            }
        }

        for (Pair<String, Integer> score : onlineScores) {
            if (gameScore > score.getValue()) {
                // new high score
                if (caseNumber == 1) {
                    // beaten highscore on both, update both
                    caseNumber = 3;
                } else {
                    // only beaten online highscore, update online only
                    caseNumber = 2;
                }
                gameWindow.startNewHighScoreScene(game, localScores, onlineScores, caseNumber);
            }
        }
    }

    public void setLocalScoresListener(ScoresListListener localScoresListener) {
        this.localScoresListener = localScoresListener;
    }

    public void setOnlineScoresListener(ScoresListListener onlineScoresListener) {
        this.onlineScoresListener = onlineScoresListener;
    }

    private void loadScoresFromFile() {
        try {
            logger.info("attempting to load scores from file: " + scoresFilePath);
            BufferedReader br = new BufferedReader(new FileReader(scoresFilePath));

            String line = br.readLine();

            while (line != null) {
                String[] lineArray = line.split(":");
                localScores.add(new Pair<>(lineArray[0], Integer.parseInt(lineArray[1])));
                line = br.readLine();
            }

            logger.info("loaded scoreList as: " + localScores);
            localScoresListener.updateScores(localScores);
        } catch (IOException exception) {
            logger.info(exception.getMessage());
        }
    }

    private ArrayList<Pair<String, Integer>> highscoresMessageToPairList(String message) {
        logger.info("converting HISCORES message into PairList");

        ArrayList<Pair<String, Integer>> returnList = new ArrayList<>();
        String[] lines = message.split(" ")[1].split("\n");

        for (String line : lines) {
            String[] nameScore = line.split(":");
            returnList.add(new Pair<>(nameScore[0], Integer.parseInt(nameScore[1])));
        }

        return returnList;
    }

    private void loadScoresFromCommunicator() {
        logger.info("requesting communicator for HISCORES");

        gameWindow.getCommunicator().send("HISCORES");
    }

    @Override
    public void receiveCommunication(String communication) {
        if (communication.contains("HISCORES")) {
            logger.info("received communication from communicator that contains HISCORE");
            onlineScores = highscoresMessageToPairList(communication);
            onlineScoresListener.updateScores(onlineScores);
        }
    }
}
