package uk.ac.soton.comp1206.event;

import javafx.util.Pair;

import java.util.ArrayList;

public interface ScoresListListener {
    void updateScores(ArrayList<Pair<String, Integer>> scores);
}
