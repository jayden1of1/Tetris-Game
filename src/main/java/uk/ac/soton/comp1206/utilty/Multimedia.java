package uk.ac.soton.comp1206.utilty;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/**
 * The Multimedia class handles the playback of audio files and background music in the game.
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    // MediaPlayer fields

    public static MediaPlayer audioPlayer;

    public static MediaPlayer musicPlayer;

    private static boolean audioEnabled = true;

    /**
     * Plays an audio file.
     *
     * @param audioFile the path to the audio file
     */
    public static void setAudioPlayer(String audioFile) {
        if (audioFile == null) {
            logger.warn("Audio file path is null. Unable to play audio.");
            return;
        }

        String resourcePath = "/sounds/" + audioFile;
        URL resourceUrl = Multimedia.class.getResource(resourcePath);

        if (resourceUrl == null) {
            logger.warn("Resource not found: {}", resourcePath);
            return;
        }

        String toPlay = resourceUrl.toExternalForm();
        try {
            Media audioMedia = new Media(toPlay);
            audioPlayer = new MediaPlayer(audioMedia);
            audioPlayer.play();

            // Log audio file playback
            logger.info("Playing audio file: {}", audioFile);
        } catch (Exception e) {
            logger.error("Error playing audio file {}: {}", audioFile, e.getMessage());
        }
    }

    /**
     * Plays background music with looping.
     *
     * @param musicFile the path to the background music file
     */
    public static void setMusicPlayer(String musicFile) {

        if (musicFile == null) {
            logger.warn("Audio file path is null. Unable to play audio.");
            return;
        }

        String resourcePath = "/music/" + musicFile;
        URL resourceUrl = Multimedia.class.getResource(resourcePath);

        if (resourceUrl == null) {
            logger.warn("Resource not found: {}", resourcePath);
            return;
        }

        String toPlay = resourceUrl.toExternalForm();
        logger.info("Playing audio: {}", toPlay);

        try {
            Media audioMedia = new Media(toPlay);
            musicPlayer = new MediaPlayer(audioMedia);
            musicPlayer.play();
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }

    }

    /**
     * Method to stop any music if playing
     */
    public static void stopMusic() {
        if (audioEnabled = true) {
            logger.info("Music stopping");
            musicPlayer.stop();
        }
    }
}
