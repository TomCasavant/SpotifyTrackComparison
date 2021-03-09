package casavant.tom.trackcomparison;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.special.SearchResult;
import com.wrapper.spotify.model_objects.specification.Track;

public class MainDisplay implements ActionListener{
	
	static JFrame main;
	static JPanel song1, song2, features1, features2, artistInfo1, artistInfo2;;
	private static JButton songSelect1, songSelect2;
	private static SpotifyApi spotifyApi;
	private static final File configFile = new File("config.properties"); // File used for getting ClientId and ClientSecrety
	private static String clientId, clientSecret;
	public static final int WIDTH=375;
	public static final int HEIGHT=400;
	Song track1, track2;

	
	/*
	 * Gets an access token for the Spotify API
	 */
	private static void clientCredentials() {
		try {
			ClientCredentials client = spotifyApi.clientCredentials().build().execute();
			spotifyApi.setAccessToken(client.getAccessToken());
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	/*
	 * Sets up an displays the main display
	 */
	private static void display() {
		MainDisplay display = new MainDisplay(); // Used as an Actionlistener
		
		// Main menu bar (Settings to setup clientId and ClientSecret
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Settings");
		JMenuItem menuItem = new JMenuItem("Config");
		menu.add(menuItem);
		menuBar.add(menu);
		menuItem.addActionListener(display);
		
		// Create the main frame
		main = new JFrame("Spotify Track Comparisons");
		main.setSize(WIDTH,HEIGHT);
		main.setLayout(new BorderLayout());
		
		main.setJMenuBar(menuBar); // Assign the menu to the new frame
		
		// Initialize all the panels on the frame to display individual Song information
		song1 = new JPanel();
		song2 = new JPanel();
		features1 = new JPanel();
		features2 = new JPanel();
		artistInfo1 = new JPanel();
		artistInfo2 = new JPanel();
		songSelect1 = new JButton("Select Song");
		songSelect2 = new JButton("Select Song");
		
		song1.setPreferredSize(new Dimension(175, 10));
		song2.setPreferredSize(new Dimension(175, 10));
		
		// Align elements on the song info panels to be vertically stacked
		song1.setLayout(new BoxLayout(song1, BoxLayout.PAGE_AXIS));
		song2.setLayout(new BoxLayout(song2, BoxLayout.PAGE_AXIS));
		song1.add(songSelect1);
		song2.add(songSelect2);
		main.add(song1, BorderLayout.WEST);
		main.add(song2, BorderLayout.EAST);
		main.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.CENTER); // Split the 2 songs down the middle
		
		// Listen for button presses
		songSelect1.addActionListener(display);
		songSelect2.addActionListener(display);
		
		features1.setLayout(new BoxLayout(features1, BoxLayout.PAGE_AXIS));
		features2.setLayout(new BoxLayout(features2, BoxLayout.PAGE_AXIS));
		main.setVisible(true); // Display the frame
	}
	
	/*
	 * When a new song is found add it to the panel
	 */
	public void applyTrackToDisplay(JPanel panel, JPanel features, JPanel artistInfo, Song track) {
		if (panel.isAncestorOf(artistInfo)) {
			// If song currently exists in position, delete it
			panel.remove(artistInfo);
			panel.revalidate();
			panel.repaint();
			artistInfo.removeAll();
		}
		panel.add(artistInfo);
		try {
			// Get and resize the Album Cover
			URL album_url = new URL(track.album_cover);
			BufferedImage album_cover = resize(ImageIO.read(album_url), 100, 100);
			artistInfo.add(new JLabel(new ImageIcon(album_cover)));
		} catch (IOException e) {
			System.out.println("Error" + e.getMessage());
		}
		
		// Add info about the song to the panel
		artistInfo.add(new JLabel(track.name));
		artistInfo.add(new JLabel(track.album));
		artistInfo.add(new JLabel(track.artist));
		
		if (!(track1 == null || track2 == null)){
			// When both songs are present, perform a comparison
			compareSongs();
		} else {
			// If one of the songs is not present, just display the features with no assigned color
			artistInfo.add(features);
			for (int i=0; i<8; i++) {
				JLabel feature = new JLabel(String.format("%s: %f", track.FEATURE_NAMES[i], track.all_features[i]));
				features.add(feature);
			}
		}
	}
	/*
	 * Compares the audio features of each track and assigns corresponding colors to each feature
	 */
	public void compareSongs() {
		// Remove all the currently displayed features
		artistInfo1.remove(features1);
		artistInfo2.remove(features2);
		artistInfo1.revalidate();
		artistInfo2.revalidate();
		artistInfo1.repaint();
		artistInfo2.repaint();
		features1.removeAll();
		features2.removeAll();
		
		// Default colors
		Color song1Color = Color.GREEN;
		Color song2Color = Color.RED;
		
		for (int i=0; i<8; i++) {
			
			if (track1.all_features[i] > track2.all_features[i]) {
				// If track1 feature is bigger, make track 1 green
				song1Color = Color.GREEN;
				song2Color = Color.RED;
			} else if (track1.all_features[i] < track2.all_features[i]) {
				// If track1 feature is smaller, make track 1 red
				song1Color = Color.RED;
				song2Color = Color.GREEN;
			} else {
				// Otherwise they're equal so assign them both to be the default black
				song1Color = Color.BLACK;
				song2Color = Color.BLACK;
				
			}
			
			// Apply the new label to the frame
			JLabel song1Feature = new JLabel(String.format("%s: %f", track1.FEATURE_NAMES[i], track1.all_features[i]));
			JLabel song2Feature = new JLabel(String.format("%s: %f", track2.FEATURE_NAMES[i], track2.all_features[i]));
			song1Feature.setForeground(song1Color);
			song2Feature.setForeground(song2Color);
			features1.add(song1Feature);
			features2.add(song2Feature);
			
		}
		artistInfo1.add(features1);
		artistInfo2.add(features2);
	}
	
	/*
	 * Monitors click actions on each button
	 */
	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		if (s == "Config") {
			// Config pressed, display inputs for adding clientId and ClientSecret
			JPanel settings = new JPanel();
			JTextField clientIdField = new JTextField(20);
			JTextField clientSecretField = new JTextField(20);
			settings.add(new JLabel("Client ID:"));
			settings.add(clientIdField);
			settings.add(Box.createHorizontalStrut(15));
			settings.add(new JLabel("Client Secret:"));
			settings.add(clientSecretField);
			int result = JOptionPane.showConfirmDialog(null, settings, "Settings", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				try {
					// Save the provided info to the config file
					FileWriter writer = new FileWriter(configFile, false);
					writer.write(String.format("clientId=%s\nclientSecret=%s", clientIdField.getText(), clientSecretField.getText()));
					writer.close();
					clientId = clientIdField.getText();
					clientSecret = clientSecretField.getText();
					setupSpotify(); // Reset spotify API
				} catch (IOException e1) {
					System.out.println("Error: " + e1.getMessage());
				}
			}
			
		}
		else {
			// Otherwise this is a song search
			String result = JOptionPane.showInputDialog(null, "Search", "Enter a song name here");
			try {
				// Attempt to get track, or display warning that credentials may be incorrect
				Track track = search(result);
				if (e.getSource() == songSelect1) {
					// Apply song to panel 1
					track1 = new Song(track, spotifyApi);
					applyTrackToDisplay(song1, features1, artistInfo1, track1);
				} else if (e.getSource() == songSelect2){
					// Apply song to panel 2
					track2 = new Song(track, spotifyApi);
					applyTrackToDisplay(song2, features2, artistInfo2, track2);
				}
			} catch (Exception err) {
				System.out.println("Error: " + err.getMessage());
				JOptionPane.showConfirmDialog(null, null, "Check your Spotify credentials", JOptionPane.OK_OPTION);
			}
		}
		main.setVisible(true);
	}
	
	/*
	 * Search for a track and return a Track Object
	 */
	public Track search(String songname) {
		try {
			SearchResult search = spotifyApi.searchItem(songname, "track").build().execute();
			System.out.println("Total Tracks: " + search.getTracks().getItems()[0]); // Return first discovered track
			return search.getTracks().getItems()[0];
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
		return null; // No track found
	}
	
	/*
	 * Resize image function, used to resize album covers
	 */
	public static BufferedImage resize(BufferedImage img, int width, int height) {
		Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		return dimg;
	}
	
	/*
	 * Builds and applies new client information to the SpotifyApi
	 */
	public static void setupSpotify(){
		spotifyApi = new SpotifyApi.Builder()
				  .setClientId(clientId)
				  .setClientSecret(clientSecret).build();
		clientCredentials();
	}
	
	public static void main(String args[]) {
		try {
			// Attempt to get clientId and ClientSecret from ConfigFile
			Properties prop = new Properties();
			FileInputStream inputStream = new FileInputStream(configFile);
			try {
				prop.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			clientId = prop.getProperty("clientId");
			clientSecret = prop.getProperty("clientSecret");
			setupSpotify(); // Create the SpotifyApi instance
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			try {
				// Config file doesn't exist, so we'll create it now
				configFile.createNewFile();
				FileWriter writer = new FileWriter(configFile);
				writer.write("clientId=None\nclientSecret=None");
				writer.close();
			} catch (IOException e1) {
				System.out.println("Error " + e1.getMessage());
			}
		} catch (Exception e) {
			System.out.println("Error" + e.getMessage());
		}
		display(); // Create the main display
	}
}
