package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoreList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.utilty.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * ScoresScene displays a game over animation
 * and then presents the score lists
 */
public class ScoresScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    private final Communicator communicator;

    protected Game game;

    protected String type;

    protected ArrayList<Pair<String, Integer>> scores;

    protected ObservableList<Pair<String, Integer>> scoresList;

    protected SimpleListProperty<Pair<String, Integer>> scoreListWrapper;

    protected ObservableList<Pair<String, Integer>> remoteScores;

    protected SimpleListProperty<Pair<String, Integer>> remoteScoresWrapper;

    /**
     * Create a new single player scores scene
     *
     * @param gameWindow the Game Window
     * @param finalGame  the final state of Game
     */
    public ScoresScene(GameWindow gameWindow, Game finalGame) {
        super(gameWindow);
        logger.info("Creating Scores Scene");
        this.game = finalGame;
        this.communicator = gameWindow.getCommunicator();
    }

    /**
     * Create a new multiplayer scores scene
     *
     * @param gameWindow the Game Window
     * @param finalGame  the final state of Game
     */
    public ScoresScene(GameWindow gameWindow, Game finalGame, SimpleListProperty<Pair<String, Integer>> loadedScores, String type) {
        super(gameWindow);
        logger.info("Creating Scores Scene");
        this.game = finalGame;
        this.communicator = gameWindow.getCommunicator();
        this.type = type;
        scoreListWrapper = loadedScores;
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        //Listener to check for high scores coming in
        communicator.addListener((message) -> {
            if (message.startsWith("HISCORES"))
                Platform.runLater(() -> this.loadOnlineScores(message));

        });

        //Sends message to retrieve high scores
        communicator.send("HISCORES");

        //Delay to make sure scores are loaded in
        try {
            Thread.sleep(110);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        //Setups and load scores
        setupScores();
        if (type == null) {
            try {
                scoreListWrapper.addAll(loadScores());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        var scorePane = new BorderPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add("challenge-background");
        root.getChildren().add(scorePane);

        //Show animation the show appropriate UI
        gameOverAnimation(scorePane);
    }

    /**
     * Shows game over animation and when it finishes
     * it builds appropriate UI
     * @param scorePane
     */
    private void gameOverAnimation(BorderPane scorePane) {
        logger.info("Game Over");

        Multimedia.setAudioPlayer("end.wav");

        var gameOverTitle = new Text("Game Over");
        scorePane.setCenter(gameOverTitle);
        gameOverTitle.setFont(Font.font("Monospaced Bold", 40)); // Base font size
        gameOverTitle.getStyleClass().add("largeGameOver"); // Apply CSS rule for additional styling

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(0.5), gameOverTitle);
        rotateTransition.setCycleCount(1);
        rotateTransition.setByAngle(360);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.9), gameOverTitle);
        fadeTransition.setCycleCount(4);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setRate(1.5);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0.1);

        SequentialTransition sequentialTransition = new SequentialTransition(gameOverTitle, rotateTransition, fadeTransition);
        sequentialTransition.play();
        sequentialTransition.setOnFinished(event -> buildAllUi(scorePane));
    }

    /**
     * Checks to see if the score passed any local
     * or online scores in order to submit it
     *
     * (If the game is online it does not check to submit local scores)
     *
     * @param scorePane
     */
    private void buildAllUi(BorderPane scorePane) {
        //Checks only online scores for multiplayer
        if (type != null) {
            if (checkScore(remoteScoresWrapper) != -1) {
                submitScoreUI(scorePane, "online");
            } else buildScoresUI(scorePane);
            //Checks if score passed any globally
        } else {
            if (checkScore(scoreListWrapper) != -1 && checkScore(remoteScoresWrapper) != -1) {
                submitScoreUI(scorePane, "both");
            } else if (checkScore(remoteScoresWrapper) != -1) {
                submitScoreUI(scorePane, "online");
            } else if (checkScore(scoreListWrapper) != -1) {
                submitScoreUI(scorePane, "local");
            } else {
                buildScoresUI(scorePane);
            }
        }
    }

    /**
     * Shows appropriate UI in order to submit a score
     *
     * @param scorePane
     * @param type
     */
    private void submitScoreUI(BorderPane scorePane, String type) {
        Integer localScore = game.getScore().get();

        var stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        scorePane.setCenter(stackPane);

        var scoreBox = new VBox();

        var background = new Rectangle(600, 250);
        backgroundRect(stackPane, background);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setSpacing(10);
        stackPane.getChildren().add(scoreBox);

        var enterTitle = new Text();
        if (type.equals("local")) {
            enterTitle.setText("New Local HighScore\n" + "Enter Your Name:");
        } else if (type.equals("online")) {
            enterTitle.setText("New Online HighScore\n" + "Enter Your Name:");
        } else if (type.equals("both")) {
            enterTitle.setText("New Global HighScore\n" + "Enter Your Name:");
        }
        enterTitle.setTextAlignment(TextAlignment.CENTER);
        enterTitle.getStyleClass().add("hiscore");
        scoreBox.getChildren().add(enterTitle);

        var inputField = new TextField();
        inputField.setMaxWidth(200);
        scoreBox.getChildren().add(inputField);

        var submit = new Button("Submit");
        submit.getStyleClass().add("menuItem");
        scoreBox.getChildren().add(submit);

        //When the button is pressed the score is also added to the scores list and is submitted
        submit.setOnAction((event) -> {
            logger.info("Submitting New Highscore");
            String userName;
            if (inputField.getText().isEmpty()) {
                userName = "LocalGuest:";
            } else {
                userName = inputField.getText() + ":";
            };
            var pair = new Pair<>(userName, localScore);
            switch (type) {
                case "local" -> {
                    scoreListWrapper.set(checkScore(scoreListWrapper), pair);
                    try {
                        submitLocalHighScore(pair);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case "online" -> writeOnlineScore(pair);
                case "both" -> {
                    scoreListWrapper.set(checkScore(scoreListWrapper), pair);
                    try {
                        submitLocalHighScore(pair);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    writeOnlineScore(pair);
                }
            }
            scoreBox.getChildren().removeAll(scoreBox, enterTitle, inputField, submit);
            buildScoresUI(scorePane);
        });
    }

    /**
     * Builds UI lists to display scores
     *
     * @param scorePane
     */
    private void buildScoresUI(BorderPane scorePane) {
        logger.info("Creating Score Lists");

        var stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        scorePane.setCenter(stackPane);

        var background = new Rectangle(580, 530);
        backgroundRect(stackPane, background);

        VBox mainScoreBox = new VBox();
        mainScoreBox.setAlignment(Pos.TOP_CENTER);
        mainScoreBox.setPadding(new Insets(40, 0, 0, 0));
        mainScoreBox.setSpacing(40);
        stackPane.getChildren().add(mainScoreBox);

        var highScoresTitle = new Text("HighScores");
        highScoresTitle.getStyleClass().add("hiscore");
        beginAnimation(highScoresTitle, 1.3);
        mainScoreBox.getChildren().add(highScoresTitle);

        HBox scoreListBox = new HBox();
        scoreListBox.setAlignment(Pos.CENTER);
        scoreListBox.setSpacing(20);
        mainScoreBox.getChildren().add(scoreListBox);

        var localScoreListUI = new ScoreList();
        localScoreListUI.setScoreList(scoreListWrapper);
        localScoreListUI.setMaxWidth(450);
        localScoreListUI.createScores("Local");
        localScoreListUI.revealScores();
        scoreListBox.getChildren().add(localScoreListUI);

        var onlineScoreListUI = new ScoreList();
        onlineScoreListUI.setScoreList(remoteScoresWrapper);
        onlineScoreListUI.setMaxWidth(450);
        onlineScoreListUI.createScores("Online");
        onlineScoreListUI.revealScores();
        scoreListBox.getChildren().add(onlineScoreListUI);
    }


    @Override
    public void initialise() {
        logger.info("Initialising Scores");
    }

    /**
     * Checks to see if the score passed any in the list
     *
     * @param scoreWrapper
     * @return the score it passed or -1 to indicate none
     */
    private int checkScore(SimpleListProperty<Pair<String, Integer>> scoreWrapper) {
        int counter = 1;
        for (Pair<String, Integer> pair : scoreWrapper) {
            if (counter > 10) {
                break;
            }
            if (game.getScore().get() >= pair.getValue()) {
                return scoreWrapper.indexOf(pair);
            }
            counter++;
        }
        return -1;
    }

    /**
     * Sets up the class fields to monitor the scores
     */
    private void setupScores() {
        scores = new ArrayList<>();

        scoresList = FXCollections.observableArrayList(scores);

        if (type == null)
            scoreListWrapper = new SimpleListProperty<>(scoresList);
    }

    /**
     * Reads the scores file or creates a default one
     * and then orders the list and loads the scores
     *
     * @return list
     */
    protected static ArrayList<Pair<String, Integer>> loadScores() throws IOException {
        logger.info("Loading Scores");
        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
        File file = new File("scores.txt");
        logger.info("FILES PATH IS " + file.getAbsolutePath());
        if (!file.exists()) {
            writeScores();
            file = new File("scores.txt");
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            String[] lineArray = {line.split(":")[0], line.split(":")[1]};
            list.add(new Pair<>(lineArray[0] + ":", Integer.valueOf(lineArray[1])));
            line = reader.readLine();
        }
        reader.close();

        Comparator<Pair<String, Integer>> comparator = (o1, o2) -> o2.getValue().compareTo(o1.getValue());
        list.sort(comparator);

        return list;
    }

    /**
     * Creates a default scores file
     */
    protected static void writeScores() throws IOException {
        logger.info("Writing Default Scores");
        File scoresFile = new File("scores.txt");
        logger.info(scoresFile.getAbsolutePath());
        scoresFile.createNewFile();
        Writer writer = new FileWriter(scoresFile);
        int defaultScore = 10000;
        for (int i = 0; i < 10; i++) {
            if (i == 9) {
                writer.write("Player" + i + ":" + defaultScore);
            } else {
                writer.write("Player" + i + ":" + defaultScore + "\n");
            }
            defaultScore -= 1000;
        }
        writer.close();
    }

    /**
     * Retrieves online scores
     *
     * @param message
     */
    private void loadOnlineScores(String message) {
        ArrayList<Pair<String, Integer>> onlineScores = new ArrayList<>();
        remoteScores = FXCollections.observableArrayList(onlineScores);
        remoteScoresWrapper = new SimpleListProperty<>(remoteScores);

        for (String line : message.split("\n")) {
            String user = line.split(":")[0].replace("HISCORES ", "") + ":";
            Integer score = Integer.valueOf(line.split(":")[1]);

            Pair<String, Integer> pair = new Pair(user, score);
            remoteScoresWrapper.add(pair);
        }
    }

    /**
     * Send high score to online server
     * @param score
     */
    private void writeOnlineScore(Pair<String, Integer> score) {
        communicator.send("HISCORE " + score.toString().replace("=", ""));

        remoteScoresWrapper.add(score);
        Comparator<Pair<String, Integer>> comparator = (o1, o2) -> o2.getValue().compareTo(o1.getValue());
        remoteScoresWrapper.sort(comparator);

    }

    /**
     * Writes high score to local file
     *
     * @param score
     * @throws IOException
     */
    private void submitLocalHighScore(Pair<String, Integer> score) throws IOException {
        logger.info("Writing New HighScore To File");
        File scoresFile = new File("scores.txt");
        Writer writer = new FileWriter(scoresFile, true);
        writer.write("\n" + score.toString().replace("=", ""));
        writer.close();
    }

    /**
     * Exits the ScoresScene and restarts the game.
     */
    public void restartGame() {
        logger.info("Restarting Game");
        gameWindow.startChallenge();// Create a new game
        game.initialiseGame(); // Reinitialize the game state
    }
}