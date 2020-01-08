import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.jar.JarException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("view/WeatherDash.fxml"));
        primaryStage.setTitle("Weather Dash");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 1182, 798));
        primaryStage.show();
    }


    public static void main(String[] args) throws JarException {

        Locale.setDefault(Locale.UK);
        launch(args);

    }
}
