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
        try {
            // Load login page first
            URL fxmlUrl = getClass().getResource("/Scene/Login.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML file not found: /Scene/Login.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            
            // Create scene with root node and dimensions
            Scene scene = new Scene(root, 800, 600);
            
            // Load CSS for login page
            URL cssUrl = getClass().getResource("/Scene/SceneStyle/Login.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("CSS file not found: /Scene/SceneStyle/Login.css");
            }
            
            // Set Icon for the App
            try {
                Image icon = new Image(getClass().getResourceAsStream("/Resorses/favicon-2.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Could not load icon: " + e.getMessage());
            }
            
            // Set stage properties
            stage.setTitle("Seha - تسجيل الدخول");
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}