package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
            HelloApplication.class.getResource("timer-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);

        Image icon = new Image("file:///D:/eclipse-workspace/Timer/src/application/melofocus-logo.png");
        stage.getIcons().add(icon);

        stage.setTitle("MeloFocus");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}