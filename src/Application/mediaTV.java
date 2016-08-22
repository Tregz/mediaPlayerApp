/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Application;



import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class mediaTV extends mediaPlayerApp {

	public static void tv() {
		mv.getMediaPlayer().stop();

		// Create a player for each media file.
		List<MediaPlayer> players = new ArrayList<>();
		for (File mediaFile : mediaIndexer.playlist) {
			Media media = new Media(mediaFile.toURI().toString());
			MediaPlayer mediaPlayer = new MediaPlayer(media);
			players.add(mediaPlayer);
		}

		// Play first media from list.
		mv = new MediaView(players.get(0));
		mv.setMediaPlayer(players.get(0));
		mv.getMediaPlayer().play();
		mv.fitWidthProperty().bind(scene.widthProperty());
		stack.getChildren().add(mv);

		// Play each media file in turn.
		for (int i = 0; i < players.size(); i++) {
			final MediaPlayer player = players.get(i);
			final MediaPlayer nextPlayer = players.get((i + 1) % players.size());
			player.setOnEndOfMedia(new Runnable() {
				@Override
				public void run() {
					player.stop();
					mv.setMediaPlayer(nextPlayer);
					nextPlayer.play();
					stack.getChildren().add(mv);
				}
			});
		}
	}
}