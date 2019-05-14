import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Json;

import java.util.jar.JarException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("view/sample.fxml"));
        primaryStage.setTitle("Weather Dash");
        primaryStage.setScene(new Scene(root, 1200, 810));
        primaryStage.show();
    }


    public static void main(String[] args) throws JarException {
        System.out.println(Json.getJson());
        launch(args);

    }
}
