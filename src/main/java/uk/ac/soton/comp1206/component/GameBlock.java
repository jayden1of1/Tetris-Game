package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Used to determine if a circle should be drawn at
     * the center of the block
     */
    private boolean shouldDrawCircle = false;

    /**
     * Determines if a block can be highlighted
     */
    private boolean canHighlight = false;

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);

        //Highlights when mouse moves on block
        this.setOnMouseEntered(mouseEvent -> {
            if (canHighlight && !gameBoard.highlighted) {
                gameBoard.highlighted = true;
                var gc = getGraphicsContext2D();
                gc.setFill(Color.rgb(255, 255, 255, 0.2));
                gc.fillRect(0, 0, width, height);
            }
        });

        //Removes the highlight on mouse exit
        this.setOnMouseExited(mouseEvent -> {
            gameBoard.highlighted = false;
            paint();
        });
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);

        //Checks if circle needs to be drawn
        if (shouldDrawCircle) {
            gc.setFill(Color.rgb(254, 254, 254, 0.6));
            gc.fillOval(width / 4, height / 4, width / 2, height / 2);
            gc.strokeOval(width / 4, height / 4, width / 2, height / 2);
        }
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Sets the circle highlight on a specific gameblock
     *
     */
    public void setShouldDrawCircle() {
        this.shouldDrawCircle = true;
    }

    /**
     * Sets highlight option for specific blocks
     * (Since instructions and piece boards shouldn't be highlighted)
     *
     */
    public void setCanHighlight() {
        this.canHighlight = true;
    }

    /**
     * Highlights the specific block
     * Used for keyboard movement
     */
    public void setHighlight() {
        logger.info("Highlighting Keyboard Movement");
        var gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(255, 255, 255, 0.2));
        gc.fillRect(0, 0, width, height);
    }

    /**
     * Animation for line cleared
     */
    public void fadeOut() {
        logger.info("Fading Line");
        var gc = getGraphicsContext2D();

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200));
        fadeTransition.setFromValue(gc.getCanvas().getOpacity());
        fadeTransition.setToValue(0);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200));
        scaleTransition.setToX(2);
        scaleTransition.setToY(2);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);

        ParallelTransition parallelTransition = new ParallelTransition(gc.getCanvas(), scaleTransition, fadeTransition);
        parallelTransition.play();
    }

}
