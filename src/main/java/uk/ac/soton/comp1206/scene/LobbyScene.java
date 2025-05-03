package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.utilty.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * LobbyScene is used to display game channels retrieved from the communicator
 * and gives the option to join a channel and chat or play
 */
public class LobbyScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    private final Communicator communicator;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    private Label errorLabel;

    private Timer channelTimer;
    private Timer usersTimer;
    private Timer hostTimer;


    protected TextFlow channelList;
    protected TextFlow userList;
    protected TextFlow chat;
    protected VBox rightVbox;
    protected Button startGame;

    protected boolean inChannel = false;
    protected boolean imHost = false;


    /**
     * Create a new Lobby scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Lobby Scene");
        this.communicator = gameWindow.getCommunicator();
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        BorderPane lobbyPane = new BorderPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu");
        root.getChildren().add(lobbyPane);

        var stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        lobbyPane.setCenter(stackPane);

        var topHBox = new HBox();
        topHBox.setAlignment(Pos.CENTER);
        lobbyPane.setTop(topHBox);

        var multiplayerTitle = new Label("Multiplayer");
        multiplayerTitle.getStyleClass().add("title");
        topHBox.getChildren().add(multiplayerTitle);

        var backGround = new Rectangle(gameWindow.getWidth() - 20, gameWindow.getHeight() - 75);
        backgroundRect(stackPane, backGround);

        var centreBox = new HBox();
        centreBox.setAlignment(Pos.CENTER_LEFT);
        centreBox.setSpacing(80);
        stackPane.getChildren().add(centreBox);

        var leftVbox = new VBox();
        leftVbox.setPadding(new Insets(10, 0, 0, 20));
        leftVbox.setMaxHeight(500);
        leftVbox.setMaxWidth(200);
        leftVbox.setAlignment(Pos.CENTER_LEFT);
        leftVbox.setSpacing(10);
        centreBox.getChildren().add(leftVbox);

        var channelTitle = new Label("Game Channels");
        channelTitle.getStyleClass().add("title");
        leftVbox.getChildren().add(channelTitle);

        channelList = new TextFlow();
        channelList.getStyleClass().add("textFlow");
        channelList.setMinWidth(260);
        channelList.setPrefHeight(370);
        channelList.setPadding(new Insets(0, 0, 0, 10));
        channelList.setTextAlignment(TextAlignment.LEFT);

        ScrollPane scroller = new ScrollPane();
        scroller.getStyleClass().add("scroll-pane");
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setMinWidth(270);
        scroller.setFitToWidth(true);
        scroller.setContent(channelList);
        leftVbox.getChildren().add(scroller);

        var createGame = new Button("Create Game");
        createGame.getStyleClass().add("menuItem");
        leftVbox.getChildren().add(createGame);
        createGame.setOnMouseClicked(mouseEvent -> {
            if (!inChannel) {
                Multimedia.setAudioPlayer("buttonclick1.wav");
                createChannel();
            } else {
                Multimedia.setAudioPlayer("incorrect.wav");
            }
        });

        rightVbox = new VBox();
        rightVbox.setPadding(new Insets(18, 0, 0, 0));
        rightVbox.setMaxWidth(350);
        rightVbox.setMaxHeight(500);
        rightVbox.setAlignment(Pos.TOP_CENTER);
        rightVbox.setSpacing(11);
        centreBox.getChildren().add(rightVbox);
    }

    @Override
    public void initialise() {
        logger.info("Initializing Lobby");

        communicator.addListener(this::receiveCommunication);

        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                if (inChannel) communicator.send("PART");
                channelTimer.cancel();
                if (usersTimer != null) {
                    usersTimer.cancel();
                }
                if (hostTimer != null) {
                    hostTimer.cancel();
                }
                gameWindow.startMenu();
            }
        });
        startChannelTimer();
    }

    /**
     * Method to handle incoming communicator messages
     *
     * @param message
     */
    private void receiveCommunication(String message) {
        Platform.runLater(() -> getCommand(message));
    }

    /**
     * Methods depending on the communicator message
     *
     * @param message
     */
    private void getCommand(String message) {
        if (message.startsWith("CHANNELS")) {
            this.showChannels(message);
        } else if (message.startsWith("USERS")) {
            this.showUsers(message);
        } else if (message.startsWith("MSG")) {
            this.showMsg(message);
        } else if (message.startsWith("START")) {
            this.showGame();
        }
    }

    /**
     * Timer that constantly refreshes the channel list
     */
    private void startChannelTimer() {
        channelTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("Getting Channel List");
                communicator.send("LIST");
            }
        };
        channelTimer.scheduleAtFixedRate(timerTask, 1000, 2000);
    }

    /**
     * Builds the UI needed to display the game channels
     *
     * @param message
     */
    private void showChannels(String message) {
        logger.info("Showing Channel List");
        String[] channels = message.replace("CHANNELS ", "").split("\n");
        channelList.getChildren().clear();
        for (String channel : channels) {
            Text channelName = new Text(channel + "\n");
            channelName.getStyleClass().add("channelItem");

            channelName.setOnMouseEntered(mouseEvent -> channelName.setEffect(new Glow(3)));

            channelName.setOnMouseExited(mouseEvent -> channelName.setEffect(null));

            channelName.setOnMouseClicked(mouseEvent -> {
                if (!inChannel) {
                    Multimedia.setAudioPlayer("buttonclick1.wav");
                    communicator.send("JOIN " + channel);
                    showChannelChat(channel);
                } else {
                    Multimedia.setAudioPlayer("incorrect.wav");
                }
            });
            channelList.getChildren().add(channelName);
        }
    }

    /**
     * Checks if a string is alphanumerical
     * or if a channel already exists with that name
     *
     * @param name
     * @return true or false
     */
    private boolean isValid(String name) {
        String acceptableChars = "^[0-9A-Za-z]*$";
        if (name.matches(acceptableChars)) {
            for (Node node : channelList.getChildren())
                return (node instanceof Text) && !((Text) node).getText().equals(name + "\n");
        }
        return false;
    }

    /**
     * Used when create game is pressed in order
     * to create a channel
     */
    private void createChannel() {
        if (!inChannel) {
            logger.info("Creating New Channel");
            inChannel = true;
            var textField = new TextField();
            textField.setPrefWidth(500);
            textField.setPromptText("Enter New Channel name and then press 'ENTER'");
            rightVbox.getChildren().add(textField);
            textField.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER && !textField.getText().isEmpty()) {
                    rightVbox.getChildren().remove(errorLabel);
                    if (!isValid(textField.getText())) {
                        showError();
                    } else {
                        String name = textField.getText();
                        communicator.send("CREATE " + name);
                        imHost = true;
                        showChannelChat(name);
                    }
                }
            });
        }
    }

    /**
     * Shows the channel chat
     *
     * @param name
     */
    private void showChannelChat(String name) {
        logger.info("Showing Channel Chat");
        inChannel = true;

        Label channelName;
        if (name.length() > 16) {
            channelName = new Label(name.substring(0, 16));
        } else {
            channelName = new Label(name);
        }
        channelName.getStyleClass().add("title");
        rightVbox.getChildren().clear();
        rightVbox.getChildren().add(channelName);

        chat = new TextFlow();
        chat.getStyleClass().add("textFlow");
        chat.setMinWidth(400);
        chat.setPrefHeight(390);


        ScrollPane chatScroller = new ScrollPane();
        chatScroller.getStyleClass().add("scroll-pane");
        chatScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroller.setMinWidth(405);
        chatScroller.setPrefHeight(600);
        chatScroller.setFitToHeight(true);
        chatScroller.setFitToWidth(true);
        chatScroller.setContent(chat);
        rightVbox.getChildren().add(chatScroller);


        Text text = new Text("Welcome to the channel '" + name + "'\n" + "Type /nick to change your nickname" +
                "\n\n" + "Current players are: " + "\n");

        text.getStyleClass().add("chat");
        chat.getChildren().add(text);

        userList = new TextFlow();
        userList.setMinWidth(400);
        chat.getChildren().add(userList);

        TextField chatField = new TextField();
        chatField.setPromptText("Send a message - Press 'ENTER' to send");
        chatField.setPrefWidth(40);
        rightVbox.getChildren().add(chatField);

        //Listener to check if the message is a /nick command to change username or to just send the message
        chatField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER && !chatField.getText().isEmpty()) {
                if (chatField.getText().startsWith("/nick")) {
                    String newNickname = chatField.getText().replace("/nick ", "");
                    if (newNickname.length() > 10) {
                        communicator.send("NICK " + newNickname.substring(0,9));
                    } else {
                        communicator.send("NICK " + newNickname);
                    }
                } else {
                    String msg = chatField.getText();
                    communicator.send("MSG  " + msg);
                }
                chatField.clear();
            }
        });

        var buttonBox = new HBox();
        buttonBox.setSpacing(150);
        rightVbox.getChildren().add(buttonBox);

        startGame = new Button("Start Game");
        startGame.setVisible(false);
        startGame.getStyleClass().add("chatButton");
        buttonBox.getChildren().add(startGame);

        startGame.setOnMouseClicked(mouseEvent -> {
            communicator.send("START");
        });

        var leaveChannel = new Button("Leave Channel");
        leaveChannel.getStyleClass().add("chatButton");
        buttonBox.getChildren().add(leaveChannel);

        //Listener to leave channel and stop timers and remove chat window
        leaveChannel.setOnMouseClicked(mouseEvent -> {
            communicator.send("PART");
            Multimedia.setAudioPlayer("buttonclick1.wav");
            rightVbox.getChildren().removeAll(rightVbox.getChildren());
            usersTimer.cancel();
            hostTimer.cancel();
            inChannel = false;
            imHost = false;
        });

        startHostTimer();
        startUsersTimer();
    }

    /**
     * When a server name isn't valid this pops up to want the user
     */
    private void showError() {
        logger.info("Showing Error");

        errorLabel = new Label("NOT VALID NAME\nTRY AGAIN");
        errorLabel.setPadding(new Insets(120, 0, 0, 0));
        errorLabel.getStyleClass().add("title");
        rightVbox.getChildren().add(errorLabel);
    }

    /**
     * Checks to see if the player is host or not in order to
     * gain access to the start game button
     */
    private void startHostTimer() {
        hostTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("Checking if i am host");
                startGame.setVisible(imHost);
            }
        };
        hostTimer.scheduleAtFixedRate(timerTask, 100, 2000);

    }

    /**
     * Timer that refreshes the user list in the chat
     * window
     */
    private void startUsersTimer() {
        usersTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("Getting Users List");
                communicator.send("USERS");
            }
        };
        usersTimer.scheduleAtFixedRate(timerTask, 1000, 2000);
    }

    /**
     * UI that gets refreshed each time the users are retrieved
     * to keep track of the players in the channels
     *
     * @param message
     */
    private void showUsers(String message) {
        logger.info("Showing Users");
        userList.getChildren().clear();

        String[] users = message.replace("USERS ", "").split("\n");
        for (String user : users) {
            Text userName = new Text(user + ", ");
            userName.getStyleClass().add("chat");
            userList.getChildren().add(userName);
        }

        Text lineSpace = new Text("\n");
        userList.getChildren().add(lineSpace);

        if (users.length == 1) {
            imHost = true;
        }
    }

    /**
     * When a message is sent it get handled and shown
     * with a appropriate style
     *
     * @param message
     */
    private void showMsg(String message) {
        logger.info("Showing Message");
        var currentTime = formatter.format(LocalDateTime.now());
        Text msg = new Text("[" + currentTime + "] " + message.replace("MSG ", "") + "\n");
        msg.getStyleClass().add("chat");
        chat.getChildren().add(msg);
    }

    /**
     * When start game button is pressed the game starts and
     * channel timers are cancelled
     */
    private void showGame() {
        Multimedia.setAudioPlayer("gamestart.wav");
        usersTimer.cancel();
        channelTimer.cancel();
        hostTimer.cancel();
        gameWindow.startMultiplayer();
    }
}