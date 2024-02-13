package uk.ac.soton.comp1206.utility;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Multimedia {
    public MediaPlayer audioPlayer;
    public MediaPlayer musicPlayer;

    public void playAudio(String path) {
        Media audio = new Media(path);
        audioPlayer = new MediaPlayer(audio);
        audioPlayer.play();
    }

    public void playMusic(String path) {
        Media music = new Media(path);
        musicPlayer = new MediaPlayer(music);
        musicPlayer.play();
    }
}
