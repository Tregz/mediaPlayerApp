/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Application;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import static org.apache.log4j.helpers.Loader.getResource;

public class mediaPlayerApp extends Application {
	protected static Group root = new Group();
	protected static Scene scene = new Scene(root, 640, 720);
	protected static TextArea results = new TextArea();
	protected static StackPane stack = new StackPane();
        protected static MediaView mv = new MediaView();
        
	public static void main(String[] args) {
		launch(args);
	}

    	@Override
	public void start(Stage primaryStage) throws IOException, Exception, MediaException {
		primaryStage.setTitle("MediaPlayer");
                TabPane tabPane = new TabPane();
		BorderPane borderPane = new BorderPane();

		// Indexing tab
		Tab tab1 = new Tab();
		tab1.setText("Indexing");
		GridPane subroot = new GridPane();
		subroot.setHgap(8);
		subroot.setVgap(8);
		subroot.setPadding(new Insets(5));
		ColumnConstraints cons1 = new ColumnConstraints();
		ColumnConstraints cons2 = new ColumnConstraints();
		cons1.setHgrow(Priority.NEVER);
		cons2.setHgrow(Priority.ALWAYS);
		subroot.getColumnConstraints().addAll(cons1, cons2);
		RowConstraints row1 = new RowConstraints();
		RowConstraints row2 = new RowConstraints();
		row1.setVgrow(Priority.NEVER);
		row2.setVgrow(Priority.ALWAYS);
		subroot.getRowConstraints().addAll(row1, row2);
		Label lbl = new Label("Files:");
		TextField field = new TextField();
		Button createBtn = new Button("Create");
		Button updateBtn = new Button("Update");
		Button removeBtn = new Button("Remove");
		GridPane.setHalignment(createBtn, HPos.RIGHT);
		GridPane.setHalignment(updateBtn, HPos.RIGHT);
		GridPane.setHalignment(removeBtn, HPos.RIGHT);
		subroot.add(lbl, 0, 0);
		subroot.add(field, 1, 0, 1, 1);
		subroot.add(createBtn, 2, 0);
		subroot.add(updateBtn, 3, 0);
		subroot.add(removeBtn, 4, 0);
		tab1.setContent(subroot);
		tabPane.getTabs().add(tab1);

		// Searching tab
		Tab tab2 = new Tab();
		tab2.setText("Searching");
		GridPane playroot = new GridPane();
		playroot.setHgap(8);
		playroot.setVgap(8);
		playroot.setPadding(new Insets(5));
		cons1.setHgrow(Priority.NEVER);
		cons2.setHgrow(Priority.ALWAYS);
		playroot.getColumnConstraints().addAll(cons1, cons2);
		row1.setVgrow(Priority.NEVER);
		row2.setVgrow(Priority.ALWAYS);
		subroot.getRowConstraints().addAll(row1, row2);
		Label qry = new Label("Query:");
		TextField queryField = new TextField();
		ComboBox<String> metaNames = new ComboBox<>();
		metaNames.getItems().addAll("Title", "Artist", "Genre", "Year");
		metaNames.setPromptText("metaNames");
		Button searchBtn = new Button("Search");
		Button playAllBtn = new Button("Play");
		GridPane.setHalignment(createBtn, HPos.RIGHT);
		GridPane.setHalignment(updateBtn, HPos.RIGHT);
		GridPane.setHalignment(removeBtn, HPos.RIGHT);
		playroot.add(qry, 0, 0);
		playroot.add(queryField, 1, 0, 1, 1);
		playroot.add(metaNames, 2, 0);
		playroot.add(searchBtn, 3, 0);
		playroot.add(playAllBtn, 4, 0);
		tab2.setContent(playroot);
		tabPane.getTabs().add(tab2);
		borderPane.setTop(tabPane);

		// Mediaplayer
                
                //if (fpath == null)fpath = getClass().getResource("/logo.mp4").toURI().toString();
                //File f = new File(fpath);
                //Path fpath = Paths.get(f.toURI().toString());
                //InputStream stream = Files.newInputStream(fpath);
                //String mediaPath = fpath.toString();
                //File newPath = new File(mediaPath);
                //Media mTest = new Media(newPath.toURI().toString());
                
                //getClass().getResource("/logo.mp4").toURI().toString();
                //if (fpath == null)fpath = getClass().getResource("logo.mp4").toURI().toString();
                
                //String fpath = (mediaPlayerApp.class.getResource("logo.mp4").toURI().toString());
                
                //String fpath = (getClass().getClassLoader().getResource("resources/logo.mp4").toURI().toString());
                String mpath = "resources/logo.mp4";
                String fpath = (getClass().getResource(mpath).toURI().toString());
                
                //if (fpath == null)fpath = ("logo.mp4");
                //final InputStream stream;

                //stream = mediaPlayerApp.class.getResourceAsStream("logo.mp4");
                //File newStream = new File(stream).toString();
                Media m = new Media(new File(fpath).toString());
                MediaPlayer mp = new MediaPlayer(m);
                mp.setAutoPlay(true);
                mp.play();
                mv = new MediaView();
                mv.setMediaPlayer(mp);
                mv.setPreserveRatio(true);
		mv.fitWidthProperty().bind(scene.widthProperty());
                
		stack.getChildren().add(mv);
                borderPane.setCenter(stack);

		// Results
		borderPane.setBottom(results);
		root.getChildren().add(borderPane);

		// Display
		primaryStage.setScene(scene);
		primaryStage.show();
                
		// Events
		String folder = "Drag folder into query bar, than click Create, Update or Remove";
		createBtn.setOnAction((event) -> {
			if (field.getText().trim().equals(""))
				results.appendText(folder + "\n");
			else {
				results.appendText("results..." + "\n");
				try {
					mediaIndexer.IndexFiles("index", field.getText(), results, true, false);
				} catch (IOException e) {
				}
			}
		});
		updateBtn.setOnAction((event) -> {
			if (field.getText().trim().equals(""))
				results.appendText(folder + "\n");
			else {
				results.appendText("Updating..." + "\n");
				try {
					mediaIndexer.IndexFiles("index", field.getText(), results, false, false);
				} catch (IOException e) {
				}
			}
		});
		removeBtn.setOnAction((event) -> {
			if (field.getText().trim().equals(""))
				results.appendText(folder + "\n");
			else {
				results.appendText("Removing..." + "\n");
				try {
					mediaIndexer.IndexFiles("index", field.getText(), results, true, true);
				} catch (IOException e) {
				}
			}
		});
		String critera = "Write query, select metadata, than click Search";
		searchBtn.setOnAction((event) -> {
			String selected = "title";
			if (queryField.getText().trim().equals(""))
				results.appendText(critera + "\n");
			else if (metaNames.getValue() == null)
				results.appendText(critera + "\n");
			else {
				results.appendText("Searching..." + "\n");
				try {
                                    switch (metaNames.getValue()) {
                                        case "Title":
                                            selected = "title";
                                            break;
                                        case "Artist":
                                            selected = "xmpDM:artist";
                                            break;
                                        case "Genre":
                                            selected = "xmpDM:genre";
                                            break;
                                        case "Year":
                                            selected = "xmpDM:releaseDate";
                                            break;
                                        default:
                                            break;
                                    }
					mediaIndexer.SearchFiles("index", queryField.getText(), selected, results);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}
		});
		playAllBtn.setOnAction((event) -> {
			if (metaNames.getValue() == null)
				results.appendText(critera + "\n");
			else
				mediaTV.tv();
		});
	}
}