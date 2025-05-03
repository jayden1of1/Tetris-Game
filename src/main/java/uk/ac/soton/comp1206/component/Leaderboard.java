package uk.ac.soton.comp1206.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

public class Leaderboard extends ScoreList {

    public Leaderboard() {
        setAlignment(Pos.CENTER);
    }

    @Override
    public void createScores() {
        int counter = 0;

        for (Pair<String, Integer> score : scoreList) {
            if (counter == 3) {
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

}
