package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LobbyScene extends BaseScene implements CommunicationsListener {
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    private TextField newGameField;

    private List<String> channelList;

    private VBox channelVBox;

    private ScrollPane chat;
    private TextFlow messages;
    private TextField textField;
    private Button sendButton;

    private boolean isHost = false;

    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Lobby Scene");
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background"); // subject to change
        root.getChildren().add(lobbyPane);

        var mainPane = new BorderPane();
        lobbyPane.getChildren().add(mainPane);

        // creating main center Vbox
        var centerVBox = new VBox();
        centerVBox.setSpacing(12.0);

        // creating new game label
        var newGameText = new Label();
        newGameText.setText("create new game");
        newGameText.getStyleClass().add("label");

        centerVBox.getChildren().add(newGameText);

        // creating new game field HBox for text field and button
        var newGameFieldHBox = new HBox();

        newGameField = new TextField();
        newGameField.getStyleClass().add("TextField");

        var newGameButton = new Button();
        newGameButton.setText("submit");
        newGameButton.getStyleClass().add("button");

        newGameFieldHBox.getChildren().add(newGameField);
        newGameFieldHBox.getChildren().add(newGameButton);

        newGameField.setMinWidth(200.0);
        newGameButton.setMinWidth(80.0);

        newGameField.setOnKeyPressed(this::submitNewChannelKeyboard);
        newGameButton.setOnAction(this::submitNewChannelMouse);

        newGameFieldHBox.setAlignment(Pos.CENTER);
        newGameFieldHBox.setMinWidth(300);

        centerVBox.getChildren().add(newGameFieldHBox);

        // creating channel vbox
        channelVBox = new VBox();
        channelList = new ArrayList<>();

        var requestChannelsButton = new Button();
        requestChannelsButton.setText("refresh");
        requestChannelsButton.setOnAction(this::requestCurrentChannels);

        centerVBox.getChildren().add(channelVBox);
        centerVBox.getChildren().add(requestChannelsButton);

        centerVBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(centerVBox);

        // creating right VBox for chat stuff
        var rightVBox = new VBox();
        rightVBox.setMaxWidth(250.0);
        rightVBox.setAlignment(Pos.TOP_RIGHT);
        mainPane.setRight(rightVBox);

        var chatPane = new BorderPane();
        rightVBox.getChildren().add(chatPane);

        chat = new ScrollPane();
        messages = new TextFlow();
        chat.setContent(messages);
        chat.setMinHeight(400.0);
        chat.setFitToWidth(true);

        chatPane.setCenter(chat);

        var chatHBox = new HBox();
        textField = new TextField();
        textField.setMaxWidth(130.0);
        textField.getStyleClass().add("TextField");

        sendButton = new Button();
        sendButton.setText("send");
        sendButton.setMinWidth(60.0);
        sendButton.getStyleClass().add("button");

        chatHBox.getChildren().add(textField);
        chatHBox.getChildren().add(sendButton);
        chatHBox.setAlignment(Pos.CENTER);
        chatHBox.setMinWidth(10);

        chatPane.setBottom(chatHBox);

        sendButton.setOnAction((e) -> {
            sendCurrentMessage();
        });

        textField.setOnKeyPressed((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendCurrentMessage();
            }
        });
    }

    private void submitNewChannelKeyboard(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            submitNewChannelAux();
        }
    }

    private void submitNewChannelMouse(ActionEvent event) {
        submitNewChannelAux();
    }

    private void submitNewChannelAux() {
        if (newGameField.getText() != null) {
            gameWindow.getCommunicator().send("CREATE " + newGameField.getText());
        }
    }

    public void sendCurrentMessage() {
        if (textField.getText() != null) {
            if (textField.getText().equals("/start")) {
                if (isHost) {
                    gameWindow.getCommunicator().send("START");
                }
            } else if (textField.getText().equals("/part")) {
                gameWindow.getCommunicator().send("PART");
            } else if (textField.getText().contains("/nick")) {
                gameWindow.getCommunicator().send("NICK " + textField.getText().split(" ")[1]);
            } else {
                gameWindow.getCommunicator().send("MSG " + textField.getText());
                textField.clear();
            }
        }
    }

    private void requestCurrentChannels(ActionEvent event) {
        gameWindow.getCommunicator().send("LIST");
    }

    private void drawCurrentChannels() {
        logger.info("drawing channels in lobby");

        Platform.runLater(() -> {
            channelVBox.getChildren().clear();

            for (String channel : channelList) {
                var channelHBox = new HBox();
                channelHBox.setSpacing(20.0);
                channelHBox.setAlignment(Pos.CENTER);

                var channelLabel = new Label();
                channelLabel.setText(channel);
                channelLabel.getStyleClass().add("label");

                var joinButton = new Button();
                joinButton.setText("join");
                joinButton.setOnAction((e) -> {
                    gameWindow.getCommunicator().send("JOIN " + channel);
                });

                channelHBox.getChildren().add(channelLabel);
                channelHBox.getChildren().add(joinButton);

                channelVBox.getChildren().add(channelHBox);
            }
        });
    }

    @Override
    public void initialise() {
        logger.info("initializing lobby scene");

        gameWindow.getCommunicator().addListener(this::receiveCommunication);
    }

    @Override
    public void receiveCommunication(String communication) {
        if (communication.contains("CHANNELS")) {
            channelList = parseChannels(communication);
            drawCurrentChannels();
        } else if (communication.contains("MSG")) {
            String rawMessage = communication.split(" ")[1];
            String[] nameMessage = rawMessage.split(":");

            var text = new Text(nameMessage[0] + ": " + nameMessage[1] + "\n");
            messages.getChildren().add(text);
            chat.setVvalue(1);
        } else if (communication.contains("HOST")) {
            isHost = true;
        } else if (communication.contains("PARTED")) {
            isHost = false;
        } else if (communication.contains("START")) {
            Platform.runLater(gameWindow::startMultiplayer);
        } else if (communication.contains("NICK")) {
            logger.info("nickname set to: " + communication.split(" ")[1]);
        }
    }

    private ArrayList<String> parseChannels(String com) {
        logger.info("parsing channels message");
        return new ArrayList<>(Arrays.asList(com.split(" ")[1].split("\n")));
    }
}
