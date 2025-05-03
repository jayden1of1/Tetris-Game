package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Bloom;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.*;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utilty.Multimedia;

import java.io.IOException;
import java.util.HashSet;


/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;

    protected PieceBoard mainPieceBoard = new PieceBoard(3, 3, 120, 120);
    protected PieceBoard nextPieceBoard = new PieceBoard(3, 3, 70, 70);

    protected GameBoard board;

    /**
     * Variables used to be configured in multiplayer
     */
    protected VBox bottomVBox;
    protected VBox rightVBox;
    public Label scoreTitle;
    protected TextFlow msg;
    protected Text currentText;
    protected TextField msgField;
    protected Rectangle timeBar;
    protected Leaderboard leaderBoard;

    /**
     * Switch to enable multiplayer as well as to check if a message
     * is being sent so keyboard controls are disabled for the game
     */
    protected Boolean chatting;
    protected Boolean enableMultiplayer = false;

    protected Timeline timeLine;

    /**
     * Variable to control keyboard position and to highlight
     * keyboard movement
     */
    protected int[] aim = new int[]{0, 0};

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.getStyleClass().add("challenge-background");
        root.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        enableHighlight();
        mainPane.setCenter(board);

        //Multiplayer UI
        if (enableMultiplayer) {
            bottomVBox = new VBox();
            bottomVBox.setMinHeight(40);
            bottomVBox.setSpacing(5);
            bottomVBox.setAlignment(Pos.CENTER);
            mainPane.setBottom(bottomVBox);

            msg = new TextFlow();
            msg.setTextAlignment(TextAlignment.CENTER);
            msg.setPadding(new Insets(0, 0, 0, 0));
            msg.setMaxWidth(500);
            bottomVBox.getChildren().add(msg);

            currentText = new Text("Press 'T' to open chat");
            currentText.getStyleClass().add("chat");
            msg.getChildren().add(currentText);

            msgField = new TextField();
            msgField.setMaxWidth(230);
            msgField.setVisible(false);
            bottomVBox.getChildren().add(msgField);

            timeBar = new Rectangle(0, gameWindow.getHeight(), gameWindow.getWidth(), 12);
            timeBar.setEffect(new Bloom(0));
            bottomVBox.getChildren().add(timeBar);
        } else {
            timeBar = new Rectangle(0, gameWindow.getHeight(), gameWindow.getWidth(), 12);
            timeBar.setEffect(new Bloom(0));
            mainPane.setBottom(timeBar);
        }

        //Boxes
        var topHBox = new HBox();
        mainPane.setTop(topHBox);
        topHBox.setSpacing(110);


        var miniLeftVBox = new VBox();
        miniLeftVBox.setPadding(new Insets(11,0,0,0));
        topHBox.getChildren().add(miniLeftVBox);
        miniLeftVBox.setPrefWidth(200);
        miniLeftVBox.setAlignment(Pos.CENTER);


        var miniRightVBox = new VBox();
        topHBox.getChildren().add(miniRightVBox);
        miniRightVBox.setAlignment(Pos.CENTER);

        rightVBox = new VBox();
        mainPane.setRight(rightVBox);
        rightVBox.setPadding(new Insets(17, 10, 0, 0));
        rightVBox.setAlignment(Pos.TOP_CENTER);

        scoreTitle = new Label("Score");
        scoreTitle.getStyleClass().add("title");
        miniLeftVBox.getChildren().add(scoreTitle);

        var score = new Label();
        score.textProperty().bind(game.getScore().asString());
        score.getStyleClass().add("score");
        miniLeftVBox.getChildren().add(score);

        var livesTitle = new Label("Lives");
        livesTitle.getStyleClass().add("title");
        miniRightVBox.getChildren().add(livesTitle);
        livesTitle.setPadding(new Insets(9.2, 0, 0, 0));

        var lives = new Label();
        lives.textProperty().bind(game.getLives().asString());
        lives.getStyleClass().add("score");
        miniRightVBox.getChildren().add(lives);

        //Multiplayer UI
        if (enableMultiplayer) {
            var versus = new Label("Versus");
            versus.getStyleClass().add("score");
            rightVBox.getChildren().add(versus);

            leaderBoard = new Leaderboard();
            leaderBoard.setPrefWidth(220);
            leaderBoard.setPrefHeight(200);
            rightVBox.getChildren().add(leaderBoard);
        }

        if (!enableMultiplayer) {
            var highScoreTitle = new Label("HighScore");
            highScoreTitle.getStyleClass().add("score");
            rightVBox.getChildren().add(highScoreTitle);

            try {
                var highScore = new Label(getHighScore());
                highScore.getStyleClass().add("level");
                rightVBox.getChildren().add(highScore);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        var levelTitle = new Label("Level");
        levelTitle.getStyleClass().add("score");
        rightVBox.getChildren().add(levelTitle);

        var level = new Label();
        level.textProperty().bind(game.getLevel().asString());
        level.getStyleClass().add("level");
        rightVBox.getChildren().add(level);

        if (!enableMultiplayer) {
            var multiplierTitle = new Label("Multiplier");
            multiplierTitle.getStyleClass().add("score");
            rightVBox.getChildren().add(multiplierTitle);

            var multiplier = new Label();
            multiplier.textProperty().bind(game.getMultiplier().asString());
            multiplier.getStyleClass().add("level");
            rightVBox.getChildren().add(multiplier);
        }

        mainPieceBoard.setPadding(new Insets(10, 0, 0, 0));
        rightVBox.getChildren().add(mainPieceBoard);
        mainPieceBoard.getBlock(1, 1).setShouldDrawCircle();

        nextPieceBoard.setPadding(new Insets(10, 0, 0, 0));
        rightVBox.getChildren().add(nextPieceBoard);


        //Handle block on gameBoard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        //Gives the next pieces to the piece boards to show
        game.setNextPieceListener(this::nextPiece);

        //Handles line getting cleared so it can animate them
        game.setLineClearedListener(this::lineCleared);

        //Listens for time bar ending to reset it
        game.setGameLoopListener(this::gameLoop);

        //Checks if lives are 0
        game.setLivesListener(this::checkLives);

        //Rotates piece if main game board is clicked
        board.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                logger.info("Rotating piece: " + game.getCurrentPiece().getName());
                game.getCurrentPiece().rotate();
                mainPieceBoard.showPiece(game.getCurrentPiece());
            }
        });

        //Rotates piece if piece board is clicked
        mainPieceBoard.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                logger.info("Rotating piece: " + game.getCurrentPiece().getName());
                game.getCurrentPiece().rotate();
                mainPieceBoard.showPiece(game.getCurrentPiece());
            }
        });
    }

    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }


    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        scene.setOnKeyPressed(this::keyActions);
    }

    /**
     * Shows next pieces on piece board
     *
     * @param gamePiece
     * @param followingPiece
     */
    private void nextPiece(GamePiece gamePiece, GamePiece followingPiece) {
        mainPieceBoard.showPiece(game.getCurrentPiece());
        nextPieceBoard.showPiece(game.getFollowingPiece());
    }

    /**
     * Hands over the coordinates of the blocks to
     * be animated
     *
     * @param coordinates
     */
    private void lineCleared(HashSet<GameBlockCoordinate> coordinates) {
        board.fadeOut(coordinates);
    }

    /**
     * Animation for time bar on bottom
     *
     * @param time
     */
    private void gameLoop(int time) {
        logger.info("Begin TimerBar");
        if (timeLine != null)
            timeLine.stop();
        timeLine = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(this.timeBar.fillProperty(), Color.GREEN.brighter())),
                new KeyFrame(Duration.ZERO, new KeyValue(this.timeBar.widthProperty(), gameWindow.getWidth())),
                new KeyFrame(new Duration((double) time / 1.8), new KeyValue(this.timeBar.fillProperty(), Color.DARKGOLDENROD)),
                new KeyFrame(new Duration((double) time / 1.2), new KeyValue(this.timeBar.fillProperty(), Color.RED)),
                new KeyFrame(new Duration(time), new KeyValue(this.timeBar.widthProperty(), 0)));
        timeLine.play();
    }

    /**
     * Checks if lives are 0 and stops games
     */
    protected void checkLives() {
        if (game.getLives().get() < 0) {
            Multimedia.musicPlayer.stop();
            gameWindow.startScores(game);
        }
    }

    /**
     * Keyboard controls
     *
     * @param event
     */
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
            gameWindow.startMenu();
            logger.info("Cancelling Timer");
            game.getTimer().cancel();
        } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.X) {
            logger.info("Placing piece " + game.getCurrentPiece().getName());
            game.blockClicked(board.getBlock(aim[0], aim[1]));
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

    /**
     * Sets highlight for game board only
     */
    private void enableHighlight() {
        for (GameBlock[] blocks : board.getBlocks()) {
            for (GameBlock block : blocks) {
                block.setCanHighlight();
            }
        }
    }

    /**
     * Gets the local high score and displays
     *
     * @return local high score
     * @throws IOException
     */
    private String getHighScore() throws IOException {
        logger.info("Getting High Score");
        return ScoresScene.loadScores().get(0).getValue().toString();
    }

}