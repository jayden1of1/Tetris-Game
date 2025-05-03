package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

public class ScoreList extends VBox {

    public SimpleListProperty<Pair<String, Integer>> scoreList = new SimpleListProperty<>();

    public ScoreList() {
        setAlignment(Pos.CENTER);
        setSpacing(5);
    }

    /**
     * Goes through a list of pairs that have username and score
     * and creates a UI element to display them
     *
     * @param type
     */
    public void createScores(String type) {
        int counter = 0;

        var title = new Text(type);
        title.getStyleClass().add("smallHiscore");
        getChildren().add(title);

        for (Pair<String, Integer> score : scoreList) {
            if (counter == 10) {
                break;
            }

            Label user;
            if (score.getKey().length() > 15) {
                user = new Label(score.getKey().substring(0, 14));
            } else {
                user = new Label(score.getKey());
            }
            user.getStyleClass().add("scorer");

            var userScore = new Label(score.getValue().toString());
            userScore.getStyleClass().add("scoreItem");

            HBox scoreBox = new HBox();
            scoreBox.setAlignment(Pos.CENTER);
            scoreBox.setSpacing(10);
            scoreBox.getChildren().addAll(user, userScore);
            getChildren().add(scoreBox);

            counter++;
        }
    }

    /**
     * Same method as the other createScores()
     * but the difference is there no Label on top
     */
    public void createScores() {
        int counter = 0;

        for (Pair<String, Integer> score : scoreList) {
            if (counter == 10) {
                break;
            }

            Label user;
            if (score.getKey().length() > 15) {
                user = new Label(score.getKey().substring(0, 14));
            } else {
                user = new Label(score.getKey());
            }
            user.getStyleClass().add("scorer");

            var userScore = new Label(score.getValue().toString());
            userScore.getStyleClass().add("scoreItem");


            HBox scoreBox = new HBox();
            scoreBox.setAlignment(Pos.CENTER);
            scoreBox.setSpacing(10);
            scoreBox.getChildren().addAll(user, userScore);

            getChildren().add(scoreBox);
            counter++;
        }
    }

    /**
     * Sets the list of usernames and scores to iterate through
     *
     * @param scoreList
     */
    public void setScoreList(SimpleListProperty<Pair<String, Integer>> scoreList) {
        this.scoreList = scoreList;
    }

    /**
     * Animation to reveal the scores UI through a
     * fade transition
     */
    public void revealScores() {
        for (Node hBox : getChildren()) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(1400), hBox);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.play();
        }
    }

}