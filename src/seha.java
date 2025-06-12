import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class seha extends Application {  
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {  
        try {
            // Corrected FXML path with leading slash and forward slashes
            Parent root = FXMLLoader.load(getClass().getResource("/Scene/HomePage.fxml"));
            
            if (root == null) {
                throw new RuntimeException("FXML file could not be loaded. Check the file path.");
            }
            
            // Create scene with root node and dimensions
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/Scene/SceneStyle/HomePage.css").toExternalForm());
            
            // Set Icone for the App
            try {
                Image icon = new Image("file:Resorses/favicon-2.png");
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Could not load icon: " + e.getMessage());
                // Continue without icon if loading fails
            }
            
            // Set stage properties
            stage.setTitle("Seha");
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;  // Re-throw to let the JavaFX framework handle it
        }
    }
}
