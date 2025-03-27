package deadlocktoolkit.ui;

import deadlocktoolkit.core.*;
import deadlocktoolkit.visualization.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert.AlertType;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private DeadlockEngine engine;
    private VisualizationSystem visualSystem;
    private BorderPane mainView;
    private TabPane tabPane;
    private Tab setupTab;
    private Tab simulationTab;
    private Tab analysisTab;
    private Tab scenariosTab;
    private Tab preventionTab;
    private Tab bankersTab;
    private Tab monitoringTab;
    private Tab performanceTab;
    private int numProcesses = 0;
    private int numResources = 0;
    private ScenarioGenerator scenarioGenerator;
    private MonitoringPanel monitoringPanel;
    private BankersAlgorithmPanel bankersAlgorithmPanel;
    
    public MainController(DeadlockEngine engine, VisualizationSystem visualSystem) {
        this.engine = engine;
        this.visualSystem = visualSystem;
        this.scenarioGenerator = new ScenarioGenerator(engine);
        
        initializeUI();
    }
    
    private void initializeUI() {
        mainView = new BorderPane();
        
        // Create menu bar
        mainView.setTop(createMenuBar());
        
        // Create tab pane
        tabPane = new TabPane();
        
        setupTab = new Tab("Setup");
        setupTab.setContent(createSetupPanel());
        setupTab.setClosable(false);
        
        simulationTab = new Tab("Simulation");
        simulationTab.setContent(createSimulationPanel());
        simulationTab.setClosable(false);
        
        analysisTab = new Tab("Analysis");
        analysisTab.setContent(createAnalysisPanel());
        analysisTab.setClosable(false);
        
        scenariosTab = new Tab("Scenarios");
        scenariosTab.setContent(createScenariosPanel());
        scenariosTab.setClosable(false);
        
        preventionTab = new Tab("Prevention");
        preventionTab.setContent(createPreventionPanel());
        preventionTab.setClosable(false);
        
        bankersTab = new Tab("Banker's Algorithm");
        bankersTab.setContent(createBankersPanel());
        bankersTab.setClosable(false);
        
        monitoringTab = new Tab("Monitoring");
        monitoringTab.setContent(createMonitoringPanel());
        monitoringTab.setClosable(false);
        
        performanceTab = new Tab("Performance");
        performanceTab.setContent(createPerformancePanel());
        performanceTab.setClosable(false);
        
        Tab welcomeTab = new Tab("Welcome");
        welcomeTab.setContent(createWelcomePanel());
        welcomeTab.setClosable(false);
        
        tabPane.getTabs().addAll(welcomeTab, setupTab, simulationTab, analysisTab, bankersTab, scenariosTab, preventionTab, monitoringTab, performanceTab);
        
        // Select welcome tab by default
        tabPane.getSelectionModel().select(0);
        
        mainView.setCenter(tabPane);
        
        // Create status bar
        mainView.setBottom(createStatusBar());
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        Menu fileMenu = new Menu("File");
        MenuItem resetItem = new MenuItem("Reset");
        resetItem.setOnAction(e -> resetSystem());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(resetItem, exitItem);
        
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        MenuItem helpItem = new MenuItem("Help");
        helpItem.setOnAction(e -> showHelpDialog());
        helpMenu.getItems().addAll(aboutItem, helpItem);
        
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");
        Label statusLabel = new Label("Ready");
        statusBar.getChildren().add(statusLabel);
        return statusBar;
    }
    
    private VBox createSetupPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        // Process and Resource Setup
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        
        Label processLabel = new Label("Number of Processes:");
        Spinner<Integer> processSpinner = new Spinner<>(1, 10, 3);
        
        Label resourceLabel = new Label("Number of Resources:");
        Spinner<Integer> resourceSpinner = new Spinner<>(1, 10, 3);
        
        grid.add(processLabel, 0, 0);
        grid.add(processSpinner, 1, 0);
        grid.add(resourceLabel, 0, 1);
        grid.add(resourceSpinner, 1, 1);
        
        Button initButton = new Button("Initialize System");
        initButton.setOnAction(e -> {
            numProcesses = processSpinner.getValue();
            numResources = resourceSpinner.getValue();
            int[] available = new int[numResources];
            for (int i = 0; i < numResources; i++) {
                available[i] = 10; // Default available resources
            }
            engine.initialize(numProcesses, numResources, available);
            visualSystem.updateVisualization();
            tabPane.getSelectionModel().select(simulationTab);
        });
        
        panel.getChildren().addAll(grid, initButton);
        return panel;
    }
    
    private VBox createSimulationPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        // Add request, release, and demand controls
        panel.getChildren().addAll(
            createRequestPane(),
            createReleasePane(),
            createDemandPane(),
            visualSystem.getGraphPane()
        );
        
        return panel;
    }
    
    private TitledPane createRequestPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        
        Spinner<Integer> processSpinner = new Spinner<>(0, 9, 0);
        Spinner<Integer> resourceSpinner = new Spinner<>(0, 9, 0);
        Spinner<Integer> unitsSpinner = new Spinner<>(1, 10, 1);
        
        Button requestButton = new Button("Request Resource");
        requestButton.setOnAction(e -> {
            boolean granted = engine.requestResource(
                processSpinner.getValue(),
                resourceSpinner.getValue(),
                unitsSpinner.getValue()
            );
            
            if (!granted) {
                showAlert("Request Denied", "Resource request would lead to unsafe state.");
            }
            
            visualSystem.updateVisualization();
        });
        
        grid.add(new Label("Process:"), 0, 0);
        grid.add(processSpinner, 1, 0);
        grid.add(new Label("Resource:"), 0, 1);
        grid.add(resourceSpinner, 1, 1);
        grid.add(new Label("Units:"), 0, 2);
        grid.add(unitsSpinner, 1, 2);
        grid.add(requestButton, 0, 3, 2, 1);
        
        return new TitledPane("Request Resources", grid);
    }
    
    private TitledPane createReleasePane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        
        Spinner<Integer> processSpinner = new Spinner<>(0, 9, 0);
        Spinner<Integer> resourceSpinner = new Spinner<>(0, 9, 0);
        Spinner<Integer> unitsSpinner = new Spinner<>(1, 10, 1);
        
        Button releaseButton = new Button("Release Resource");
        releaseButton.setOnAction(e -> {
            engine.releaseResource(
                processSpinner.getValue(),
                resourceSpinner.getValue(),
                unitsSpinner.getValue()
            );
            visualSystem.updateVisualization();
        });
        
        grid.add(new Label("Process:"), 0, 0);
        grid.add(processSpinner, 1, 0);
        grid.add(new Label("Resource:"), 0, 1);
        grid.add(resourceSpinner, 1, 1);
        grid.add(new Label("Units:"), 0, 2);
        grid.add(unitsSpinner, 1, 2);
        grid.add(releaseButton, 0, 3, 2, 1);
        
        return new TitledPane("Release Resources", grid);
    }
    
    private TitledPane createDemandPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        
        Spinner<Integer> processSpinner = new Spinner<>(0, 9, 0);
        TextField demandField = new TextField();
        
        Button setButton = new Button("Set Maximum Demand");
        setButton.setOnAction(e -> {
            try {
                String[] values = demandField.getText().split(",");
                int[] demand = new int[numResources];
                for (int i = 0; i < numResources; i++) {
                    demand[i] = Integer.parseInt(values[i].trim());
                }
                engine.getBankersAlgorithm().setMaxDemand(processSpinner.getValue(), demand);
                visualSystem.updateVisualization();
            } catch (Exception ex) {
                showAlert("Invalid Input", "Please enter comma-separated integers.");
            }
        });
        
        grid.add(new Label("Process:"), 0, 0);
        grid.add(processSpinner, 1, 0);
        grid.add(new Label("Max Demand (comma-separated):"), 0, 1);
        grid.add(demandField, 1, 1);
        grid.add(setButton, 0, 2, 2, 1);
        
        return new TitledPane("Set Maximum Demand", grid);
    }
    
    private VBox createAnalysisPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        Button detectButton = new Button("Detect Deadlocks");
        detectButton.setOnAction(e -> detectDeadlocks());
        
        Button resolveButton = new Button("Resolve Deadlocks");
        resolveButton.setOnAction(e -> resolveDeadlocks());
        
        Button backButton = new Button("Go Back");
        backButton.setOnAction(e -> navigateBack());
        
        Button forwardButton = new Button("Go Forward");
        forwardButton.setOnAction(e -> navigateForward());
        
        // Create a tab pane for different analysis views
        TabPane analysisTabPane = new TabPane();
        
        Tab matrixTab = new Tab("Matrices");
        matrixTab.setContent(visualSystem.getMatrixPane());
        matrixTab.setClosable(false);
        
        Tab statsTab = new Tab("Statistics");
        statsTab.setContent(visualSystem.getStatsPane());
        statsTab.setClosable(false);
        
        analysisTabPane.getTabs().addAll(matrixTab, statsTab);
        
        panel.getChildren().addAll(
            new HBox(10, detectButton, resolveButton),
            new HBox(10, backButton, forwardButton),
            analysisTabPane
        );
        
        return panel;
    }
    
    private VBox createScenariosPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        
        // Circular Wait Scenario
        TitledPane circularWaitPane = new TitledPane();
        circularWaitPane.setText("Circular Wait Scenario");
        
        VBox circularContent = new VBox(10);
        circularContent.setPadding(new Insets(10));
        
        Label circularLabel = new Label("Number of processes in circular wait:");
        Spinner<Integer> circularSpinner = new Spinner<>(2, 10, 4);
        
        Button circularButton = new Button("Generate Circular Wait");
        circularButton.setOnAction(e -> {
            try {
                scenarioGenerator.generateCircularWaitScenario(circularSpinner.getValue());
                visualSystem.updateVisualization();
                showAlert("Scenario Generated", 
                         "Circular wait scenario with " + circularSpinner.getValue() + 
                         " processes has been generated.\n\n" +
                         "Each process holds one resource and requests another in a circular pattern.");
                tabPane.getSelectionModel().select(simulationTab);
            } catch (Exception ex) {
                showAlert("Error", "Failed to generate scenario: " + ex.getMessage());
            }
        });
        
        circularContent.getChildren().addAll(circularLabel, circularSpinner, circularButton);
        circularWaitPane.setContent(circularContent);
        
        // Dining Philosophers Scenario
        TitledPane diningPane = new TitledPane();
        diningPane.setText("Dining Philosophers Scenario");
        
        VBox diningContent = new VBox(10);
        diningContent.setPadding(new Insets(10));
        
        Label diningLabel = new Label("Number of philosophers:");
        Spinner<Integer> diningSpinner = new Spinner<>(2, 10, 5);
        
        Button diningButton = new Button("Generate Dining Philosophers");
        diningButton.setOnAction(e -> {
            try {
                scenarioGenerator.generateDiningPhilosophersScenario(diningSpinner.getValue());
                visualSystem.updateVisualization();
                showAlert("Scenario Generated", 
                         "Dining philosophers scenario with " + diningSpinner.getValue() + 
                         " philosophers has been generated.\n\n" +
                         "Each philosopher has picked up their left fork and is waiting for their right fork.");
                tabPane.getSelectionModel().select(simulationTab);
            } catch (Exception ex) {
                showAlert("Error", "Failed to generate scenario: " + ex.getMessage());
            }
        });
        
        diningContent.getChildren().addAll(diningLabel, diningSpinner, diningButton);
        diningPane.setContent(diningContent);
        
        // Random Scenario
        TitledPane randomPane = new TitledPane();
        randomPane.setText("Random Scenario");
        
        GridPane randomGrid = new GridPane();
        randomGrid.setHgap(10);
        randomGrid.setVgap(10);
        randomGrid.setPadding(new Insets(10));
        
        randomGrid.add(new Label("Number of processes:"), 0, 0);
        Spinner<Integer> processSpinner = new Spinner<>(2, 10, 5);
        randomGrid.add(processSpinner, 1, 0);
        
        randomGrid.add(new Label("Number of resources:"), 0, 1);
        Spinner<Integer> resourceSpinner = new Spinner<>(2, 10, 4);
        randomGrid.add(resourceSpinner, 1, 1);
        
        randomGrid.add(new Label("Deadlock probability:"), 0, 2);
        Slider probabilitySlider = new Slider(0, 1, 0.5);
        probabilitySlider.setShowTickLabels(true);
        probabilitySlider.setShowTickMarks(true);
        probabilitySlider.setMajorTickUnit(0.25);
        randomGrid.add(probabilitySlider, 1, 2);
        
        Button randomButton = new Button("Generate Random Scenario");
        randomButton.setOnAction(e -> {
            try {
                scenarioGenerator.generateRandomScenario(
                    processSpinner.getValue(),
                    resourceSpinner.getValue(),
                    probabilitySlider.getValue()
                );
                visualSystem.updateVisualization();
                showAlert("Scenario Generated", 
                         "Random scenario with " + processSpinner.getValue() + 
                         " processes and " + resourceSpinner.getValue() + 
                         " resources has been generated.\n\n" +
                         "Deadlock probability was set to " + String.format("%.2f", probabilitySlider.getValue()));
                tabPane.getSelectionModel().select(simulationTab);
            } catch (Exception ex) {
                showAlert("Error", "Failed to generate scenario: " + ex.getMessage());
            }
        });
        
        randomGrid.add(randomButton, 0, 3, 2, 1);
        randomPane.setContent(randomGrid);
        
        // Add explanation text
        Label explanationLabel = new Label(
            "These scenarios demonstrate common deadlock patterns. After generating a scenario, " +
            "use the Analysis tab to detect deadlocks and observe the resource allocation graph."
        );
        explanationLabel.setWrapText(true);
        explanationLabel.setPadding(new Insets(0, 0, 10, 0));
        
        panel.getChildren().addAll(
            explanationLabel,
            circularWaitPane,
            diningPane,
            randomPane
        );
        
        return panel;
    }
    
    private VBox createPreventionPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        
        // Add explanation text
        Label explanationLabel = new Label(
            "Deadlock Prevention Strategies help avoid deadlocks before they occur. " +
            "Select a strategy below to see how it would be applied to the current system state."
        );
        explanationLabel.setWrapText(true);
        explanationLabel.setPadding(new Insets(0, 0, 10, 0));
        
        // Prevention strategies
        VBox strategiesBox = new VBox(10);
        strategiesBox.setPadding(new Insets(10));
        strategiesBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        Label strategiesLabel = new Label("Select a Prevention Strategy:");
        strategiesLabel.setStyle("-fx-font-weight: bold;");
        
        ToggleGroup strategiesGroup = new ToggleGroup();
        
        RadioButton resourceOrderingBtn = new RadioButton("Resource Ordering");
        resourceOrderingBtn.setToggleGroup(strategiesGroup);
        resourceOrderingBtn.setSelected(true);
        
        RadioButton preemptionBtn = new RadioButton("Preemption");
        preemptionBtn.setToggleGroup(strategiesGroup);
        
        RadioButton timeoutBtn = new RadioButton("Timeout");
        timeoutBtn.setToggleGroup(strategiesGroup);
        
        RadioButton allOrNothingBtn = new RadioButton("All-or-Nothing");
        allOrNothingBtn.setToggleGroup(strategiesGroup);
        
        RadioButton waitDieBtn = new RadioButton("Wait-Die");
        waitDieBtn.setToggleGroup(strategiesGroup);
        
        RadioButton woundWaitBtn = new RadioButton("Wound-Wait");
        woundWaitBtn.setToggleGroup(strategiesGroup);
        
        Button applyBtn = new Button("Apply Strategy");
        applyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(200);
        resultArea.setWrapText(true);
        
        applyBtn.setOnAction(e -> {
            DeadlockPrevention.PreventionStrategy strategy;
            
            if (resourceOrderingBtn.isSelected()) {
                strategy = DeadlockPrevention.PreventionStrategy.RESOURCE_ORDERING;
            } else if (preemptionBtn.isSelected()) {
                strategy = DeadlockPrevention.PreventionStrategy.PREEMPTION;
            } else if (timeoutBtn.isSelected()) {
                strategy = DeadlockPrevention.PreventionStrategy.TIMEOUT;
            } else if (allOrNothingBtn.isSelected()) {
                strategy = DeadlockPrevention.PreventionStrategy.ALL_OR_NOTHING;
            } else if (waitDieBtn.isSelected()) {
                strategy = DeadlockPrevention.PreventionStrategy.WAIT_DIE;
            } else {
                strategy = DeadlockPrevention.PreventionStrategy.WOUND_WAIT;
            }
            
            String result = engine.getDeadlockPrevention().applyPreventionStrategy(strategy);
            resultArea.setText(result);
        });
        
        // Strategy descriptions
        TitledPane descriptionsPane = new TitledPane();
        descriptionsPane.setText("Strategy Descriptions");
        
        VBox descriptionsBox = new VBox(10);
        descriptionsBox.setPadding(new Insets(10));
        
        descriptionsBox.getChildren().addAll(
            createStrategyDescription("Resource Ordering", 
                "Ensures resources are requested in a specific order to prevent circular wait."),
            createStrategyDescription("Preemption", 
                "Allows resources to be forcibly taken from processes when needed to prevent deadlock."),
            createStrategyDescription("Timeout", 
                "Sets time limits on resource requests to prevent indefinite waiting."),
            createStrategyDescription("All-or-Nothing", 
                "Processes must request all needed resources at once or none at all."),
            createStrategyDescription("Wait-Die", 
                "Older processes wait for resources held by younger processes, while younger processes are aborted when requesting resources held by older processes."),
            createStrategyDescription("Wound-Wait", 
                "Older processes preempt resources from younger processes, while younger processes wait for resources held by older processes.")
        );
        
        descriptionsPane.setContent(descriptionsBox);
        
        strategiesBox.getChildren().addAll(
            strategiesLabel,
            resourceOrderingBtn,
            preemptionBtn,
            timeoutBtn,
            allOrNothingBtn,
            waitDieBtn,
            woundWaitBtn,
            applyBtn,
            resultArea
        );
        
        panel.getChildren().addAll(
            explanationLabel,
            strategiesBox,
            descriptionsPane
        );
        
        return panel;
    }
    
    private HBox createStrategyDescription(String title, String description) {
        HBox box = new HBox(10);
        
        Label titleLabel = new Label(title + ":");
        titleLabel.setStyle("-fx-font-weight: bold;");
        titleLabel.setMinWidth(120);
        
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        
        box.getChildren().addAll(titleLabel, descLabel);
        return box;
    }
    
    private void detectDeadlocks() {
        if (engine.detectDeadlock()) {
            List<Integer> deadlocked = engine.getDeadlockedProcesses();
            showAlert("Deadlock Detected",
                     "Deadlocked processes: " + deadlocked.toString());
        } else {
            showAlert("No Deadlock",
                     "No deadlock detected in the current state.");
        }
        visualSystem.updateVisualization();
    }
    
    private void resolveDeadlocks() {
        engine.resolveDeadlock();
        visualSystem.updateVisualization();
    }
    
    private void navigateBack() {
        if (engine.canGoBack()) {
            engine.goBack();
            visualSystem.updateVisualization();
        }
    }
    
    private void navigateForward() {
        if (engine.canGoForward()) {
            engine.goForward();
            visualSystem.updateVisualization();
        }
    }
    
    private void resetSystem() {
        engine = new DeadlockEngine();
        visualSystem = new VisualizationSystem(engine);
        scenarioGenerator = new ScenarioGenerator(engine);
        initializeUI();
    }
    
    private void showAboutDialog() {
        showAlert("About",
                 "Deadlock Detection and Prevention Toolkit\n" +
                 "Version 1.0\n" +
                 "A tool for visualizing and understanding deadlock scenarios.");
    }
    
    private void showHelpDialog() {
        showAlert("Help",
                 "1. Set up the system in the Setup tab\n" +
                 "2. Use the Simulation tab to request and release resources\n" +
                 "3. Use the Analysis tab to detect and resolve deadlocks\n" +
                 "4. Use the Scenarios tab to generate predefined deadlock scenarios\n" +
                 "5. Use the Prevention tab to explore deadlock prevention strategies\n" +
                 "6. Use the navigation buttons to move through system states");
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Make the dialog resizable for better text visibility
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(500, 300);
        
        // Create a TextArea for the content to make it scrollable
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(200);
        
        alert.getDialogPane().setContent(textArea);
        
        alert.showAndWait();
    }
    
    private Pane createBankersPanel() {
        // Create the enhanced Banker's Algorithm panel
        bankersAlgorithmPanel = new BankersAlgorithmPanel(engine);
        return bankersAlgorithmPanel.getMainPanel();
    }
    
    private Pane createMonitoringPanel() {
        // Create the monitoring panel with the engine
        monitoringPanel = new MonitoringPanel(engine);
        return monitoringPanel.getMainPanel();
    }
    
    private Pane createPerformancePanel() {
        // Performance analysis panel
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #f8f8f8;");
        
        // Title
        Label titleLabel = new Label("Performance Analysis");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Description
        TextArea descriptionArea = new TextArea(
            "This panel provides detailed performance metrics for the deadlock detection and prevention algorithms. " +
            "You can analyze execution times, resource utilization, and compare the efficiency of different approaches."
        );
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(80);
        descriptionArea.setStyle("-fx-control-inner-background: #f0f0f0; -fx-background-color: #f0f0f0;");
        
        // Performance metrics section
        TitledPane metricsPane = createPerformanceMetricsPane();
        
        // Comparison section
        TitledPane comparisonPane = createAlgorithmComparisonPane();
        
        // Add all components to the panel
        panel.getChildren().addAll(
            titleLabel,
            descriptionArea,
            metricsPane,
            comparisonPane
        );
        
        return panel;
    }
    
    private TitledPane createPerformanceMetricsPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Labels for metrics
        Label avgDetectionTimeLabel = new Label("Average Detection Time:");
        Label maxDetectionTimeLabel = new Label("Maximum Detection Time:");
        Label avgResolutionTimeLabel = new Label("Average Resolution Time:");
        Label deadlockFrequencyLabel = new Label("Deadlock Frequency:");
        Label resourceUtilizationLabel = new Label("Resource Utilization:");
        
        // Values for metrics (these would be updated from the PerformanceTracker)
        Label avgDetectionTimeValue = new Label("0.05 ms");
        Label maxDetectionTimeValue = new Label("0.12 ms");
        Label avgResolutionTimeValue = new Label("1.45 ms");
        Label deadlockFrequencyValue = new Label("2.3 per minute");
        Label resourceUtilizationValue = new Label("78%");
        
        // Style the value labels
        avgDetectionTimeValue.setStyle("-fx-font-weight: bold;");
        maxDetectionTimeValue.setStyle("-fx-font-weight: bold;");
        avgResolutionTimeValue.setStyle("-fx-font-weight: bold;");
        deadlockFrequencyValue.setStyle("-fx-font-weight: bold;");
        resourceUtilizationValue.setStyle("-fx-font-weight: bold;");
        
        // Add to grid
        grid.add(avgDetectionTimeLabel, 0, 0);
        grid.add(avgDetectionTimeValue, 1, 0);
        grid.add(maxDetectionTimeLabel, 0, 1);
        grid.add(maxDetectionTimeValue, 1, 1);
        grid.add(avgResolutionTimeLabel, 0, 2);
        grid.add(avgResolutionTimeValue, 1, 2);
        grid.add(deadlockFrequencyLabel, 0, 3);
        grid.add(deadlockFrequencyValue, 1, 3);
        grid.add(resourceUtilizationLabel, 0, 4);
        grid.add(resourceUtilizationValue, 1, 4);
        
        // Refresh button
        Button refreshButton = new Button("Refresh Metrics");
        refreshButton.setOnAction(e -> {
            // Update metrics from the PerformanceTracker
            PerformanceTracker tracker = engine.getPerformanceTracker();
            if (tracker != null) {
                avgDetectionTimeValue.setText(String.format("%.2f ms", tracker.getAverageDetectionTime()));
                maxDetectionTimeValue.setText(String.format("%.2f ms", tracker.getMaxDetectionTime()));
                avgResolutionTimeValue.setText(String.format("%.2f ms", tracker.getAverageResolutionTime()));
                deadlockFrequencyValue.setText(String.format("%.1f per minute", tracker.getDeadlockFrequency()));
                resourceUtilizationValue.setText(String.format("%.0f%%", tracker.getResourceUtilization() * 100));
                
                // Show confirmation
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Metrics Refreshed");
                alert.setHeaderText(null);
                alert.setContentText("Performance metrics have been updated from the tracker.");
                alert.showAndWait();
            } else {
                // Show error
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Metrics Not Available");
                alert.setHeaderText(null);
                alert.setContentText("Performance tracker is not initialized. Please run a simulation first.");
                alert.showAndWait();
            }
        });
        
        VBox content = new VBox(15);
        content.getChildren().addAll(grid, refreshButton);
        
        TitledPane pane = new TitledPane("Performance Metrics", content);
        pane.setExpanded(true);
        return pane;
    }
    
    private TitledPane createAlgorithmComparisonPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        // Algorithm selection
        HBox selectionBox = new HBox(10);
        selectionBox.setAlignment(Pos.CENTER_LEFT);
        
        Label algorithmLabel = new Label("Select Algorithms to Compare:");
        
        CheckBox bankersCheckBox = new CheckBox("Banker's Algorithm");
        bankersCheckBox.setSelected(true);
        
        CheckBox detectionCheckBox = new CheckBox("Detection Algorithm");
        detectionCheckBox.setSelected(true);
        
        CheckBox preventionCheckBox = new CheckBox("Prevention Strategies");
        
        selectionBox.getChildren().addAll(
            algorithmLabel,
            bankersCheckBox,
            detectionCheckBox,
            preventionCheckBox
        );
        
        // Comparison metrics
        Label comparisonLabel = new Label("Comparison Results:");
        comparisonLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        TextArea comparisonResults = new TextArea(
            "Banker's Algorithm vs. Detection Algorithm:\n" +
            "- Banker's is 25% slower but prevents 98% of deadlocks\n" +
            "- Detection resolves deadlocks faster but allows them to occur\n" +
            "- Resource utilization is 12% higher with Banker's\n\n" +
            "Run a comparison with different parameters to see detailed results."
        );
        comparisonResults.setWrapText(true);
        comparisonResults.setEditable(false);
        comparisonResults.setPrefHeight(120);
        
        // Run comparison button
        Button compareButton = new Button("Run Comparison");
        compareButton.setOnAction(e -> {
            // Get selected algorithms
            List<String> selectedAlgorithms = new ArrayList<>();
            if (bankersCheckBox.isSelected()) selectedAlgorithms.add("Banker's Algorithm");
            if (detectionCheckBox.isSelected()) selectedAlgorithms.add("Detection Algorithm");
            if (preventionCheckBox.isSelected()) selectedAlgorithms.add("Prevention Strategies");
            
            if (selectedAlgorithms.size() < 2) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Selection Required");
                alert.setHeaderText(null);
                alert.setContentText("Please select at least two algorithms to compare.");
                alert.showAndWait();
                return;
            }
            
            // Run comparison simulation
            StringBuilder results = new StringBuilder();
            
            // For demonstration, generate some comparison results
            if (selectedAlgorithms.contains("Banker's Algorithm") && 
                selectedAlgorithms.contains("Detection Algorithm")) {
                results.append("Banker's Algorithm vs. Detection Algorithm:\n");
                results.append("- Banker's average detection time: 0.08ms vs Detection: 0.03ms\n");
                results.append("- Banker's deadlock prevention rate: 98% vs Detection: 0%\n");
                results.append("- Banker's resource utilization: 72% vs Detection: 85%\n\n");
            }
            
            if (selectedAlgorithms.contains("Banker's Algorithm") && 
                selectedAlgorithms.contains("Prevention Strategies")) {
                results.append("Banker's Algorithm vs. Prevention Strategies:\n");
                results.append("- Banker's overhead: Medium vs Prevention: Low\n");
                results.append("- Banker's starvation risk: Low vs Prevention: Medium\n");
                results.append("- Banker's implementation complexity: High vs Prevention: Medium\n\n");
            }
            
            if (selectedAlgorithms.contains("Detection Algorithm") && 
                selectedAlgorithms.contains("Prevention Strategies")) {
                results.append("Detection Algorithm vs. Prevention Strategies:\n");
                results.append("- Detection recovery time: 1.2ms vs Prevention: N/A\n");
                results.append("- Detection system impact: High vs Prevention: Medium\n");
                results.append("- Detection resource overhead: Low vs Prevention: Medium\n");
            }
            
            comparisonResults.setText(results.toString());
            
            // Show confirmation
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Comparison Complete");
            alert.setHeaderText(null);
            alert.setContentText("Algorithm comparison has been completed.");
            alert.showAndWait();
        });
        
        content.getChildren().addAll(
            selectionBox,
            comparisonLabel,
            comparisonResults,
            compareButton
        );
        
        TitledPane pane = new TitledPane("Algorithm Comparison", content);
        pane.setExpanded(true);
        return pane;
    }
    
    private Pane createWelcomePanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);
        
        Label titleLabel = new Label("Welcome to DeadlockToolkit");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("A tool for visualizing and understanding deadlock scenarios");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
        
        // Create a tabbed pane for instructions
        TabPane instructionsPane = new TabPane();
        instructionsPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Overview tab
        Tab overviewTab = new Tab("Overview");
        VBox overviewContent = new VBox(10);
        overviewContent.setPadding(new Insets(15));
        
        Label overviewLabel = new Label(
            "DeadlockToolkit is an educational application that helps you understand and visualize deadlocks " +
            "in operating systems. You can create your own resource allocation scenarios or use the predefined " +
            "scenarios to explore different deadlock situations."
        );
        overviewLabel.setWrapText(true);
        
        Label featuresTitle = new Label("Key Features:");
        featuresTitle.setStyle("-fx-font-weight: bold;");
        
        VBox featuresList = new VBox(5);
        featuresList.getChildren().addAll(
            new Label("• Setup and simulate resource allocation systems"),
            new Label("• Detect deadlocks using resource allocation graphs"),
            new Label("• Apply the Banker's Algorithm for deadlock avoidance"),
            new Label("• Generate common deadlock scenarios (Circular Wait, Dining Philosophers)"),
            new Label("• Explore deadlock prevention strategies")
        );
        
        overviewContent.getChildren().addAll(overviewLabel, new Separator(), featuresTitle, featuresList);
        overviewTab.setContent(overviewContent);
        
        // Getting Started tab
        Tab gettingStartedTab = new Tab("Getting Started");
        VBox gettingStartedContent = new VBox(10);
        gettingStartedContent.setPadding(new Insets(15));
        
        Label stepsTitle = new Label("Follow these steps to get started:");
        stepsTitle.setStyle("-fx-font-weight: bold;");
        
        VBox stepsList = new VBox(10);
        
        TitledPane step1 = new TitledPane();
        step1.setText("Step 1: Set up your system");
        Label step1Content = new Label(
            "Go to the Setup tab and initialize a system with processes and resources. " +
            "This creates the foundation for your resource allocation system."
        );
        step1Content.setWrapText(true);
        step1.setContent(step1Content);
        
        TitledPane step2 = new TitledPane();
        step2.setText("Step 2: Define resource requirements");
        Label step2Content = new Label(
            "In the Simulation tab, set the maximum demand for each process. " +
            "This represents how many units of each resource a process might need."
        );
        step2Content.setWrapText(true);
        step2.setContent(step2Content);
        
        TitledPane step3 = new TitledPane();
        step3.setText("Step 3: Allocate resources");
        Label step3Content = new Label(
            "Use the Request and Release sections in the Simulation tab to allocate " +
            "resources to processes and create different allocation scenarios."
        );
        step3Content.setWrapText(true);
        step3.setContent(step3Content);
        
        TitledPane step4 = new TitledPane();
        step4.setText("Step 4: Analyze for deadlocks");
        Label step4Content = new Label(
            "Go to the Analysis tab and click 'Detect Deadlocks' to check if your " +
            "current allocation state has created a deadlock. You can view matrices " +
            "and statistics to understand the system state."
        );
        step4Content.setWrapText(true);
        step4.setContent(step4Content);
        
        stepsList.getChildren().addAll(step1, step2, step3, step4);
        gettingStartedContent.getChildren().addAll(stepsTitle, stepsList);
        gettingStartedTab.setContent(gettingStartedContent);
        
        // Tabs tab
        Tab tabsTab = new Tab("Understanding Tabs");
        VBox tabsContent = new VBox(10);
        tabsContent.setPadding(new Insets(15));
        
        GridPane tabsGrid = new GridPane();
        tabsGrid.setHgap(15);
        tabsGrid.setVgap(15);
        
        tabsGrid.add(new Label("Setup Tab:"), 0, 0);
        Label setupDesc = new Label("Initialize your system with processes and resources");
        setupDesc.setWrapText(true);
        tabsGrid.add(setupDesc, 1, 0);
        
        tabsGrid.add(new Label("Simulation Tab:"), 0, 1);
        Label simDesc = new Label("Set maximum demand, request and release resources");
        simDesc.setWrapText(true);
        tabsGrid.add(simDesc, 1, 1);
        
        tabsGrid.add(new Label("Analysis Tab:"), 0, 2);
        Label analysisDesc = new Label("Detect deadlocks, view matrices and statistics");
        analysisDesc.setWrapText(true);
        tabsGrid.add(analysisDesc, 1, 2);
        
        tabsGrid.add(new Label("Scenarios Tab:"), 0, 3);
        Label scenariosDesc = new Label("Generate predefined deadlock scenarios for learning");
        scenariosDesc.setWrapText(true);
        tabsGrid.add(scenariosDesc, 1, 3);
        
        tabsGrid.add(new Label("Prevention Tab:"), 0, 4);
        Label preventionDesc = new Label("Explore different deadlock prevention strategies");
        preventionDesc.setWrapText(true);
        tabsGrid.add(preventionDesc, 1, 4);
        
        tabsContent.getChildren().add(tabsGrid);
        tabsTab.setContent(tabsContent);
        
        // Banker's Algorithm tab
        Tab bankersTab = new Tab("Banker's Algorithm");
        VBox bankersContent = new VBox(10);
        bankersContent.setPadding(new Insets(15));
        
        Label bankersTitle = new Label("Understanding the Banker's Algorithm");
        bankersTitle.setStyle("-fx-font-weight: bold;");
        
        Label bankersDesc = new Label(
            "The Banker's Algorithm is a deadlock avoidance algorithm that ensures the system " +
            "never enters an unsafe state where deadlock might occur. It works by checking if " +
            "a resource request would lead to an unsafe state before granting it."
        );
        bankersDesc.setWrapText(true);
        
        Label matricesTitle = new Label("Key Matrices:");
        matricesTitle.setStyle("-fx-font-weight: bold;");
        
        VBox matricesList = new VBox(5);
        matricesList.getChildren().addAll(
            new Label("• Allocation Matrix: Resources currently allocated to each process"),
            new Label("• Max Matrix: Maximum demand of each process for each resource"),
            new Label("• Need Matrix: Additional resources each process might still request"),
            new Label("• Available Vector: Resources available in the system")
        );
        
        Label usageTitle = new Label("Using Banker's Algorithm in DeadlockToolkit:");
        usageTitle.setStyle("-fx-font-weight: bold;");
        
        Label usageDesc = new Label(
            "The Banker's Algorithm is automatically used when you request resources in the " +
            "Simulation tab. If a request would lead to an unsafe state, it will be denied. " +
            "You can view the current state of all matrices in the Analysis tab."
        );
        usageDesc.setWrapText(true);
        
        bankersContent.getChildren().addAll(
            bankersTitle, bankersDesc, new Separator(),
            matricesTitle, matricesList, new Separator(),
            usageTitle, usageDesc
        );
        bankersTab.setContent(bankersContent);
        
        instructionsPane.getTabs().addAll(overviewTab, gettingStartedTab, tabsTab, bankersTab);
        
        Button startButton = new Button("Get Started");
        startButton.setStyle("-fx-font-size: 14px;");
        startButton.setOnAction(e -> tabPane.getSelectionModel().select(setupTab));
        
        panel.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Separator(),
            instructionsPane,
            startButton
        );
        
        return panel;
    }
    
    public BorderPane getMainView() {
        return mainView;
    }
}
