package uk.ac.soton.comp1206.scene;

import javafx.animation.ScaleTransition;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * A Base Scene used in the game. Handles common functionality between all scenes.
 */
public abstract class BaseScene {

    protected final GameWindow gameWindow;

    private static final Logger logger = LogManager.getLogger(BaseScene.class);

    protected GamePane root;
    protected Scene scene;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public BaseScene(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * Initialise this scene. Called after creation
     */
    public abstract void initialise();

    /**
     * Build the layout of the scene
     */
    public abstract void build();

    /**
     * Create a new JavaFX scene using the root contained within this scene
     * @return JavaFX scene
     */
    public Scene setScene() {
        var previous = gameWindow.getScene();
        Scene scene = new Scene(root, previous.getWidth(), previous.getHeight(), Color.BLACK);
        scene.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm());
        this.scene = scene;
        return scene;
    }

    /**
     * Get the JavaFX scene contained inside
     * @return JavaFX scene
     */
    public Scene getScene() {
        return this.scene;
    }

    /**
     * Animates the text provided by scaling it indefinitely
     *
     * @param text
     * @param scale
     */
    public void beginAnimation(Text text, double scale) {
        logger.info("Animating");
        ScaleTransition transition = new ScaleTransition(Duration.seconds(1.45), text);
        transition.setAutoReverse(true);
        transition.setCycleCount(ScaleTransition.INDEFINITE);

        transition.setToX(scale);
        transition.setToY(scale);

        transition.play();
    }

    /**
     * Sets a rectangles properties to be used as a background
     * element
     *
     * @param stackPane
     * @param background
     */
    public void backgroundRect(StackPane stackPane, Rectangle background) {
        logger.info("Creating Background");
        background.setFill(Color.rgb(30, 30, 30, 0.75));
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(75, 0, 130, 0.65));
        background.setEffect(dropShadow);
        background.setArcHeight(20);
        background.setArcWidth(20);
        stackPane.getChildren().add(background);
    }

}
