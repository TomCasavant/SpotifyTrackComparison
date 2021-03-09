package casavant.tom.trackcomparison;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Track;

public class Song {
	String name, artist, album, album_cover;
	AudioFeatures features;
	String[] FEATURE_NAMES = {"Acousticness", "Danceability", "Energy", "Instrumentalness", "Liveness", "Loudness", "Tempo", "Valence"};
	float[] all_features = new float[8];
	public Song(Track songdata, SpotifyApi spotifyapi) {
		// Create a new Song object and assign all necessary information about the song
		name = songdata.getName();
		album = songdata.getAlbum().getName();
		artist = songdata.getArtists()[0].getName();
		album_cover = songdata.getAlbum().getImages()[0].getUrl();
		try {
			// Get all the audio features from the track
			features = spotifyapi.getAudioFeaturesForTrack(songdata.getId()).build().execute();
			all_features[0] = features.getAcousticness();
			all_features[1] = features.getDanceability();
			all_features[2] = features.getEnergy();
			all_features[3] = features.getInstrumentalness();
			all_features[4] = features.getLiveness();
			all_features[5] = features.getLoudness();
			all_features[6] = features.getTempo();
			all_features[7] = features.getValence();
		} catch (Exception e) {
			System.out.print("Error: " + e.getMessage());
		}

		
	}
	
	public String getInfo() {
		return String.format("%s\n%s\n%s\n%s", name, album, artist, album_cover);
	}
}
