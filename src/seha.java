import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.net.URL;

public class seha extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Load the login FXML
        URL fxmlUrl = getClass().getResource("/Scene/Login.fxml");
        if (fxmlUrl == null) {
            throw new RuntimeException("FXML file not found: /Scene/Login.fxml");
        }
        Parent root = FXMLLoader.load(fxmlUrl);

        // 2) Create the scene and attach CSS
        Scene scene = new Scene(root, 800, 600);
        try {
                Image icon = new Image("file:Resorses/favicon-2.png");
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Could not load icon: " + e.getMessage());
                // Continue without icon if loading fails
            }

        // 3) Load the application icon from "Resorses/favicon-2.png"
        URL iconUrl = getClass().getResource("/Resorses/favicon-2.png");
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        } else {
            System.err.println("Icon not found at /Resorses/favicon-2.png");
        }

        // 4) Finalize and show the stage
        stage.setTitle("Seha - تسجيل الدخول");
        stage.setScene(scene);
        stage.show();
    }
}