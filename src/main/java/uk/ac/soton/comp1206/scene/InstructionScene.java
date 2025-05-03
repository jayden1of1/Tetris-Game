package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utilty.Multimedia;

public class InstructionScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionScene.class);

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instruction Scene");

    }

    @Override
    public void initialise() {
        logger.info("Initialise");
        Scene scene = gameWindow.getScene();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenu();
            }
        });
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var instructionPane = new StackPane();
        instructionPane.setPrefWidth(gameWindow.getWidth() * 0.);
        instructionPane.setPrefHeight(gameWindow.getHeight() * 0.4);
        instructionPane.setAlignment(Pos.CENTER);
        instructionPane.getStyleClass().add("instructions");
        root.getChildren().add(instructionPane);

        var mainPane = new BorderPane();
        instructionPane.getChildren().add(mainPane);

        var pieces = new VBox();
        pieces.setAlignment(Pos.CENTER);
        pieces.setSpacing(7);
        mainPane.setBottom(pieces);

        var instructionsTitle = new Label("Instructions");
        instructionsTitle.getStyleClass().add("title");
        pieces.getChildren().add(instructionsTitle);

        var image = new Image(InstructionScene.class.getResource("/images/Instructions.png").toExternalForm());
        var instructions = new ImageView(image);
        instructions.setFitHeight(300);
        instructions.setFitWidth(500);
        pieces.getChildren().add(instructions);

        var piecesLabel = new Label("Game Pieces");
        piecesLabel.getStyleClass().add("title");
        pieces.getChildren().add(piecesLabel);

        createLines(pieces);

    }
    /**
     * Used to create dynamically generated pieces
     *
     * @param pieces
     */
    public void createLines(VBox pieces) {
        int pieceCounter = 0;
        for (int line = 0; line < 3; line++) {
            var piecesLine = new HBox();
            piecesLine.setAlignment(Pos.CENTER);
            piecesLine.setSpacing(10);
            pieces.getChildren().add(piecesLine);
            for (int piece = 0; piece < 5; piece++) {
                var pieceBoard = new PieceBoard(3, 3, 50, 50);
                pieceBoard.showPiece(GamePiece.createPiece(pieceCounter));
                piecesLine.getChildren().add(pieceBoard);
                pieceCounter++;
            }
        }
    }

}
