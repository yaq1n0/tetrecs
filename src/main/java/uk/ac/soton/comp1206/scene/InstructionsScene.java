package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InstructionsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var instructionsPane = new StackPane();
        instructionsPane.setMaxWidth(gameWindow.getWidth());
        instructionsPane.setMaxHeight(gameWindow.getHeight());
        instructionsPane.getStyleClass().add("menu-background"); // subject to change
        root.getChildren().add(instructionsPane);

        var mainPane = new BorderPane();
        instructionsPane.getChildren().add(mainPane);

        // loading instructions image
        try {
            InputStream stream = new FileInputStream("src" + File.separator + "main" + File.separator + "resources" + File.separator + "images" + File.separator + "Instructions.png");
            Image image = new Image(stream);
            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setFitHeight(gameWindow.getHeight() * 0.6);
            imageView.setPreserveRatio(true);
            mainPane.setTop(imageView);
        } catch (IOException exception) {
            logger.info(exception.getMessage());
        }

        int[] cols = {0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6};
        int[] rows = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1};

        // loading all the pieceboards
        var boardGrid = new GridPane();

        for (int i = 0; i < GamePiece.PIECES; i++) {
            PieceBoard pieceBoard = new PieceBoard(gameWindow.getWidth() / 10.0, gameWindow.getWidth() / 10.0);
            pieceBoard.displayPiece(GamePiece.createPiece(i));
            pieceBoard.setAlignment(Pos.CENTER);
            boardGrid.add(pieceBoard, cols[i], rows[i], 1, 1);
        }

        boardGrid.setHgap(10.0);
        boardGrid.setVgap(10.0);

        boardGrid.setAlignment(Pos.CENTER);
        mainPane.setCenter(boardGrid);
    }

    @Override
    public void initialise() {

    }
}
