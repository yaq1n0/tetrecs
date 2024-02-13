package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        // title
        var title = new Text("TetrECS");
        title.getStyleClass().add("title");

        // SinglePlayer Challenge
        var playButton = new Button("SinglePlayer");
        playButton.getStyleClass().add("bigButton");
        playButton.setMinWidth(200);
        playButton.setOnAction(this::startGame);

        // lobby button
        var lobbyButton = new Button("MultiPlayer");
        lobbyButton.getStyleClass().add("bigButton");
        lobbyButton.setMinWidth(200);
        lobbyButton.setOnAction(this::startLobby);

        // instructions button
        var instructionsButton = new Button("Instructions");
        instructionsButton.getStyleClass().add("bigButton");
        instructionsButton.setMinWidth(200);
        instructionsButton.setOnAction(this::startInstructions);

        // options box for organizing menu options
        VBox options = new VBox();

        options.getChildren().add(title);
        options.getChildren().add(playButton);
        options.getChildren().add(lobbyButton);
        options.getChildren().add(instructionsButton);

        options.setSpacing(20.0);

        // somehow aligns the options in the center, don't change this
        mainPane.setCenter(options);
        options.setAlignment(Pos.CENTER);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

    }

    /**
     * Handle when the Start Game button is pressed
     *
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    /**
     * Handle when the Multiplayer button is pressed
     *
     * @param event event
     */
    private void startMultiplayer(ActionEvent event) {
        gameWindow.startMultiplayer();
    }

    /**
     * Handle when the Lobby button is pressed
     *
     * @param event event
     */
    private void startLobby(ActionEvent event) {
        gameWindow.startLobby();
    }

    /**
     * Handle when the Instructions button is pressed
     *
     * @param event event
     */
    private void startInstructions(ActionEvent event) {
        gameWindow.startInstructions();
    }
}
