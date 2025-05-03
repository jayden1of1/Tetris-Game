package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard {

    private static final Logger logger = LogManager.getLogger(PieceBoard.class);


    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    /**
     * Translates the piece coordinates inside the piece board grid
     * and shows the piece
     * @param piece
     */
    public void showPiece(GamePiece piece) {
        logger.info("Showing piece: " + piece.getName());
        int gridX = 0;
        for (int[] line : piece.getBlocks()) {
            int gridY = 0;
            for (int value : line) {
                grid.set(gridX, gridY, value);
                gridY++;
            }
            gridX++;
        }
    }
}
