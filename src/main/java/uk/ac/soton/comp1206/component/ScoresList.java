package uk.ac.soton.comp1206.component;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.ScoresListListener;

import java.util.ArrayList;

public class ScoresList extends BorderPane implements ScoresListListener {

    private static final Logger logger = LogManager.getLogger(ScoresList.class);
    private final VBox NameScores = new VBox();
    private ArrayList<Pair<String, Integer>> scoreList = new ArrayList<>();
    private String title;

    public ScoresList() {
        super();
        this.setCenter(NameScores);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * method that updates the scoresList javaFX nodes
     **/
    public void draw() {
        logger.info("drawing scores");

        Platform.runLater(() -> {
            if (title != null) {
                Label title = new Label();
                title.setText(getTitle());
                title.getStyleClass().add("title");
                NameScores.getChildren().add(title);
            }

            for (Pair<String, Integer> score : scoreList) {
                Label label = new Label();
                label.setText(score.getKey() + " : " + score.getValue());
                label.getStyleClass().add("score");
                NameScores.getChildren().add(label);
            }
        });
    }

    @Override
    public void updateScores(ArrayList<Pair<String, Integer>> scores) {
        scoreList = scores;
        draw();
    }
}
