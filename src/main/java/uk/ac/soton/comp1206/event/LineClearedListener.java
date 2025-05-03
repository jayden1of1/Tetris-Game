package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

public interface LineClearedListener {

    /**
     * Provides the coordinates of the full lines to animate
     * @param coordinates
     */
    public void lineCleared(HashSet<GameBlockCoordinate> coordinates);
}
