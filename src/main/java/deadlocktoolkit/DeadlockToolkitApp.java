package deadlocktoolkit;

import deadlocktoolkit.core.*;
import deadlocktoolkit.visualization.*;
import deadlocktoolkit.ui.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DeadlockToolkitApp extends Application {
    
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize the core engine
        DeadlockEngine engine = new DeadlockEngine();
        
        // Initialize visualization system
        VisualizationSystem visualSystem = new VisualizationSystem(engine);
        
        // Initialize the main UI controller
        MainController controller = new MainController(engine, visualSystem);
        
        // Set up the primary scene
        Scene scene = new Scene(controller.getMainView(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        
        // Configure and show the primary stage
        primaryStage.setTitle("Deadlock Detection and Prevention Toolkit");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
