package application;


import controllers.Controller;
import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.DBConnector;

import java.sql.Connection;


public class Main extends Application {
	
	private static Stage stg;
	private Connection con;
	public Controller c;
	private static BorderlessScene bs;
	private Image icon = new Image(getClass().getResourceAsStream("/images/PngItem_348433.png"));

	@Override
	public void start(Stage primaryStage) throws Exception{
		Font.loadFont(getClass().getResourceAsStream("/fonts/KOverwatch/koverwatch.ttf"),26);
		Font.loadFont(getClass().getResourceAsStream("/fonts/KOverwatch/koverwatch.ttf"),45);
		Font.loadFont(getClass().getResourceAsStream("/fonts/KOverwatch/koverwatch.ttf"),20);
		stg = primaryStage;
		System.setProperty("prism.lcdtext", "false");
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXML/MainMenu.fxml"));
		Parent root = loader.load();
		c = loader.getController();
		c.setMain(this);
		con = DBConnector.conDB();
		c.setConnection(con);
		primaryStage.setTitle("Overwatch Account Tracker");
		bs = new BorderlessScene(primaryStage, StageStyle.UNDECORATED,root);
		bs.removeDefaultCSS();
		primaryStage.setScene(bs);
		primaryStage.getIcons().add(icon);
		primaryStage.show();
		bs.maximizeStage();
//		stg.setMinWidth(600);
		bs.setResizable(true);
		c.fill();
	}

	public static void main(String[] args) {
		launch();
	}

	public Stage getStg(){
		return stg;
	}
	public BorderlessScene getBs() {
		return bs;
	}
	public void setBs(BorderlessScene newScene) {
		bs=newScene;
	}
	
}
