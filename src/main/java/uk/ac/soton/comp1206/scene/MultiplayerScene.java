package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.utilty.Multimedia;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;


public class MultiplayerScene extends ChallengeScene {

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    private final Communicator communicator;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    protected ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
    protected ObservableList<Pair<String, Integer>> scoresList = FXCollections.observableArrayList(scores);
    protected SimpleListProperty<Pair<String, Integer>> scoreListWrapper = new SimpleListProperty<>(scoresList);

    protected ArrayList<Pair<String, Integer>> deadScores = new ArrayList<>();
    protected ObservableList<Pair<String, Integer>> deadScoresList = FXCollections.observableArrayList(deadScores);
    protected SimpleListProperty<Pair<String, Integer>> deadScoresListWrapper = new SimpleListProperty<>(deadScoresList);


    /**
     * Create a new Multiplayer challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        this.communicator = gameWindow.getCommunicator();
        enableMultiplayer = true;
    }

    /**
     * Build UI and set a listener for the chat field
     */
    @Override
    public void build() {
        super.build();
        msgField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                this.communicator.send("MSG  " + msgField.getText());
            }
        });
    }

    @Override
    public void setupGame() {
        game = new MultiplayerGame(5,5,communicator);
    }

    /**
     * Setup communicator
     */
    @Override
    public void initialise() {
        super.initialise();
        communicator.addListener(this::receiveCommunication);
        communicator.send("SCORES");
        communicator.send("NICK");
        Multimedia.setMusicPlayer("game_start.wav");
        game.start();
    }

    /**
     * Used to receive messages from the communicator
     * and then handle them
     *
     * @param message
     */
    private void receiveCommunication(String message) {
        Platform.runLater(() -> getCommand(message));
    }

    /**
     * Depending on the message a method is played
     *
     * @param message
     */
    private void getCommand(String message) {
        if (message.startsWith("MSG")) {
            this.showMSG(message);
        } else if (message.startsWith("SCORES")) {
            this.showScores(message);
        } else if (message.startsWith("NICK")) {
            this.showNick(message);
        }
    }

    /**
     * Sends message through the communicator
     * to process
     *
     * @param message
     */
    private void showMSG(String message) {
        String userMessage = message.replace("MSG ","");
        var currentTime = formatter.format(LocalDateTime.now());
        if (userMessage.length() > 30) {
            currentText.setText("[" + currentTime + "] " + userMessage.substring(0, 29));
        } else {
            currentText.setText("[" + currentTime + "] " + userMessage);
        }
        msgField.clear();
        msgField.setVisible(false);
        chatting = false;
    }

    /**
     * Shows players competing on the right hand side
     *
     * @param scores
     */
    private void showScores(String scores) {
        leaderBoard.getChildren().clear();
        scoreListWrapper.clear();
        deadScoresListWrapper.clear();

        for (String playerScore : scores.split("\n")) {
            playerScore = playerScore.replace("SCORES ","");

            Pair<String,Integer> pair;
            if (playerScore.split(":")[2].equals("DEAD")) {
                pair = new Pair<>("DEAD" + ":", Integer.valueOf(playerScore.split(":")[1]));
            } else {
                pair = new Pair<>(playerScore.split(":")[0] + ":", Integer.valueOf(playerScore.split(":")[1]));
            }
            deadScoresListWrapper.add(pair);


            pair = new Pair<>(playerScore.split(":")[0] + ":", Integer.valueOf(playerScore.split(":")[1]));
            scoreListWrapper.add(pair);
        }

        Comparator<Pair<String, Integer>> comparator = (o1, o2) -> o2.getValue().compareTo(o1.getValue());
        scoreListWrapper.sort(comparator);
        deadScoresListWrapper.sort(comparator);

        leaderBoard.setScoreList(deadScoresListWrapper);
        leaderBoard.createScores();
    }

    /**
     * Gets username and displays it on top of the score
     * @param message
     */
    public void showNick(String message) {
        this.scoreTitle.setText(message.replace("NICK ",""));
    }

    @Override
    protected void checkLives() {
        if (game.getLives().get() < 0) {
            Multimedia.musicPlayer.stop();
            gameWindow.startScores(game, scoreListWrapper, "online");
            communicator.send("DIE");
        }
    }

    @Override
    protected void keyActions(KeyEvent event) {
        if (event.getCode() == KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET) {
            logger.info("Rotating piece: " + game.getCurrentPiece().getName());
            game.rotateCurrentPiece(1);
            mainPieceBoard.showPiece(game.getCurrentPiece());
        } else if (event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.OPEN_BRACKET) {
            logger.info("Rotating piece: " + game.getCurrentPiece().getName());
            game.rotateCurrentPiece(3);
            mainPieceBoard.showPiece(game.getCurrentPiece());
        } else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
            logger.info("Swapping pieces");
            game.switchPieces();
            mainPieceBoard.showPiece(game.getCurrentPiece());
            nextPieceBoard.showPiece(game.getFollowingPiece());
        } else if (event.getCode() == KeyCode.ESCAPE) {
            logger.info("Going back to menu");
            Multimedia.stopMusic();
            communicator.send("DIE");
            gameWindow.startMenu();
            logger.info("Cancelling Timer");
            game.getTimer().cancel();
        } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.X) {
            if (!chatting) {
                logger.info("Placing piece " + game.getCurrentPiece().getName());
                game.blockClicked(board.getBlock(aim[0], aim[1]));
            }
        } else if ((event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) && aim[1] > 0) {
            if (!board.highlighted) {
                logger.info("Moving aim up");
                aim[1]--;
                board.getBlock(aim[0], aim[1] + 1).paint();
                board.getBlock(aim[0], aim[1]).setHighlight();
            }
        } else if ((event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) && aim[1] < 4) {
            if (!board.highlighted) {
                logger.info("Moving aim down");
                aim[1]++;
                board.getBlock(aim[0], aim[1] - 1).paint();
                board.getBlock(aim[0], aim[1]).setHighlight();
            }
        } else if ((event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) && aim[0] > 0) {
            if (!board.highlighted) {
                logger.info("Moving aim left");
                aim[0]--;
                board.getBlock(aim[0] + 1, aim[1]).paint();
                board.getBlock(aim[0], aim[1]).setHighlight();
            }
        } else if ((event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) && aim[0] < 4) {
            if (!board.highlighted) {
                logger.info("Moving aim right");
                aim[0]++;
                board.getBlock(aim[0] - 1, aim[1]).paint();
                board.getBlock(aim[0], aim[1]).setHighlight();
            }
        } else if (event.getCode() == KeyCode.T) {
            if (enableMultiplayer) {
                chatting = true;
                msgField.setVisible(true);
            }
        }
    }
}