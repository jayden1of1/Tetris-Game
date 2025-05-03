package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Interface for listening to the next piece event.
 */
public interface NextPieceListener {

    /**
     * Called when a new piece is provided.
     * @param nextPiece the next GamePiece
     */
    public void nextPiece(GamePiece nextPiece, GamePiece followingPiece);
}
