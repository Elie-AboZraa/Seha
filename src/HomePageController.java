import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;;

public class HomePageController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Stage stage;
    private Scene scene;
    //private Parent root; 
    

    @FXML
    void CreateLeave(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/CreateLeavePage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);  // Add dimensions
        scene.getStylesheets().add(getClass().getResource("/Scene/SceneStyle/CreateLeavePage.css").toExternalForm());  // Add stylesheet
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void MedicalReport(ActionEvent event) {

    }

    @FXML
    void PatientEscort(ActionEvent event) {

    }

    @FXML
    void ReviewScene(ActionEvent event) {

    }

    @FXML
    void initialize() {
        
    }

}
