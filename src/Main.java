import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Json;

import java.sql.Time;
import java.util.jar.JarException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("view/sample.fxml"));
        primaryStage.setTitle("Weather Dash");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 1191, 801));
        primaryStage.show();
    }


    public static void main(String[] args) throws JarException {
        Json.getJson();
        launch(args);

    }
}
