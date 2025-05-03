package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.utilty.Multimedia;
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

        var mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.getStyleClass().add("menu");
        root.getChildren().add(mainPane);

        var title = new Text("TetrECS");
        title.getStyleClass().add("bigtitle");
        mainPane.setCenter(title);
        beginAnimation(title, 1.5);

        var buttonBox = new VBox();
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        buttonBox.setSpacing(10);
        mainPane.setBottom(buttonBox);

        var play = new Button("Play");
        play.setPrefWidth(300);
        play.getStyleClass().add("menuItem");

        play.setOnAction(this::startGame);

        var multiplayer = new Button("Multiplayer");
        multiplayer.setPrefWidth(300);
        multiplayer.getStyleClass().add("menuItem");

        multiplayer.setOnAction(this::showLobby);

        var instructions = new Button("Instructions");
        instructions.setPrefWidth(300);
        instructions.getStyleClass().add("menuItem");

        instructions.setOnAction(this::showInstructions);



        var exit = new Button("Exit");
        exit.setPrefWidth(300);
        exit.getStyleClass().add("menuItem");

        exit.setOnAction(this::exitGame);

        buttonBox.getChildren().addAll(play, multiplayer, instructions, exit);
    }

    /**
     * Initialise the menu by playing the appropriate
     * song
     */
    @Override
    public void initialise() {
        Multimedia.setMusicPlayer("menu.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * and launch a single player game
     *
     * @param event event
     */
    private void startGame(ActionEvent event) {
        Multimedia.stopMusic();
        Multimedia.setAudioPlayer("gamestart.wav");
        gameWindow.startChallenge();
    }

    /**
     * Handle when the multiplayer button is pressed
     * and show lobby scene
     *
     * @param event event
     */
    private void showLobby(ActionEvent event) {
        gameWindow.startMultiplayerLobby();
    }

    /**
     * Handle when the instructions button is pressed
     * and show instructions scene
     *
     * @param event event
     */
    private void showInstructions(ActionEvent event) {
        gameWindow.instructionsPage();
    }


    /**
     * Handle when the exit button is pressed
     * and close the game
     *
     * @param event event
     */
    private void exitGame(ActionEvent event) {
        gameWindow.exit();
    }

    /**
     * Shutdown application method
     */
    private void shutdown() {
        App.getInstance().shutdown();
    }
}