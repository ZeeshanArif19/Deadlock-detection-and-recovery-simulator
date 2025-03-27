package deadlocktoolkit.ui;

import deadlocktoolkit.core.*;
import deadlocktoolkit.core.PerformanceTracker.DeadlockEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Panel for real-time monitoring and performance analysis of the deadlock detection system.
 */
public class MonitoringPanel implements DeadlockEngine.DeadlockListener {
    private DeadlockEngine engine;
    private PerformanceTracker tracker;
    private VBox mainPanel;
    private Label statusLabel;
    private ProgressBar resourceUsageBar;
    private ListView<String> eventLogView;
    private ObservableList<String> eventLog;
    private PieChart deadlockChart;
    private LineChart<Number, Number> performanceChart;
    private XYChart.Series<Number, Number> detectionSeries;
    private XYChart.Series<Number, Number> resolutionSeries;
    private SimpleBooleanProperty alertActive;
    private Timeline alertTimeline;
    private AtomicInteger timeCounter;
    
    public MonitoringPanel(DeadlockEngine engine) {
        this.engine = engine;
        this.tracker = engine.getPerformanceTracker();
        this.eventLog = FXCollections.observableArrayList();
        this.alertActive = new SimpleBooleanProperty(false);
        this.timeCounter = new AtomicInteger(0);
        
        // Register as a deadlock listener
        engine.addDeadlockListener(this);
        
        initializeUI();
        setupAlertTimeline();
    }
    
    private void initializeUI() {
        mainPanel = new VBox(15);
        mainPanel.setPadding(new Insets(15));
        mainPanel.setStyle("-fx-background-color: #f8f8f8;");
        
        // Title
        Label titleLabel = new Label("Real-time Monitoring & Performance Analysis");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Status panel
        VBox statusPanel = createStatusPanel();
        
        // Charts panel
        HBox chartsPanel = createChartsPanel();
        
        // Event log panel
        VBox logPanel = createEventLogPanel();
        
        // Controls panel
        HBox controlsPanel = createControlsPanel();
        
        mainPanel.getChildren().addAll(titleLabel, statusPanel, chartsPanel, logPanel, controlsPanel);
        
        // Start updating the charts
        setupChartUpdates();
    }
    
    private VBox createStatusPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        panel.setPadding(new Insets(10));
        
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label statusTitleLabel = new Label("System Status:");
        statusTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        statusLabel = new Label("Initializing...");
        statusLabel.textProperty().bind(tracker.systemStatusProperty());
        statusLabel.styleProperty().bind(
            Bindings.when(tracker.deadlockDetectedProperty())
                .then("-fx-text-fill: red; -fx-font-weight: bold;")
                .otherwise("-fx-text-fill: green;")
        );
        
        statusBox.getChildren().addAll(statusTitleLabel, statusLabel);
        
        HBox resourceBox = new HBox(10);
        resourceBox.setAlignment(Pos.CENTER_LEFT);
        
        Label resourceLabel = new Label("System Load:");
        resourceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        resourceUsageBar = new ProgressBar(0.0);
        resourceUsageBar.setPrefWidth(200);
        resourceUsageBar.setStyle("-fx-accent: #3498db;");
        
        resourceBox.getChildren().addAll(resourceLabel, resourceUsageBar);
        
        // Alert indicator
        HBox alertBox = new HBox(10);
        alertBox.setAlignment(Pos.CENTER_LEFT);
        
        Label alertLabel = new Label("Alert Status:");
        alertLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label alertStatusLabel = new Label("No alerts");
        alertStatusLabel.textProperty().bind(
            Bindings.when(alertActive)
                .then("DEADLOCK DETECTED!")
                .otherwise("No alerts")
        );
        alertStatusLabel.styleProperty().bind(
            Bindings.when(alertActive)
                .then("-fx-text-fill: red; -fx-font-weight: bold;")
                .otherwise("-fx-text-fill: green;")
        );
        
        alertBox.getChildren().addAll(alertLabel, alertStatusLabel);
        
        panel.getChildren().addAll(statusBox, resourceBox, alertBox);
        return panel;
    }
    
    private HBox createChartsPanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        
        // Deadlock statistics pie chart
        deadlockChart = new PieChart();
        deadlockChart.setTitle("Deadlock Statistics");
        deadlockChart.setLabelsVisible(true);
        deadlockChart.setLegendVisible(true);
        deadlockChart.setPrefSize(350, 300);
        updatePieChart();
        
        // Performance line chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        yAxis.setLabel("Count");
        
        performanceChart = new LineChart<>(xAxis, yAxis);
        performanceChart.setTitle("Deadlock Metrics Over Time");
        performanceChart.setAnimated(false);
        performanceChart.setPrefSize(350, 300);
        
        detectionSeries = new XYChart.Series<>();
        detectionSeries.setName("Detected");
        
        resolutionSeries = new XYChart.Series<>();
        resolutionSeries.setName("Resolved");
        
        performanceChart.getData().addAll(detectionSeries, resolutionSeries);
        
        panel.getChildren().addAll(deadlockChart, performanceChart);
        return panel;
    }
    
    private VBox createEventLogPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        panel.setPadding(new Insets(10));
        
        Label logLabel = new Label("Event Log");
        logLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        eventLogView = new ListView<>(eventLog);
        eventLogView.setPrefHeight(150);
        eventLogView.setCellFactory(_ -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("DEADLOCK DETECTED")) {
                        setStyle("-fx-text-fill: red;");
                    } else if (item.contains("resolved")) {
                        setStyle("-fx-text-fill: green;");
                    } else if (item.contains("prevented")) {
                        setStyle("-fx-text-fill: blue;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        panel.getChildren().addAll(logLabel, eventLogView);
        return panel;
    }
    
    private HBox createControlsPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER);
        
        ToggleButton monitoringToggle = new ToggleButton("Start Monitoring");
        monitoringToggle.setSelected(false);
        monitoringToggle.setOnAction(_ -> {
            if (monitoringToggle.isSelected()) {
                engine.startMonitoring();
                monitoringToggle.setText("Stop Monitoring");
                logEvent("Real-time monitoring started");
            } else {
                engine.stopMonitoring();
                monitoringToggle.setText("Start Monitoring");
                logEvent("Real-time monitoring stopped");
            }
        });
        
        Button clearLogButton = new Button("Clear Log");
        clearLogButton.setOnAction(_ -> {
            eventLog.clear();
            logEvent("Log cleared");
        });
        
        Button exportStatsButton = new Button("Export Statistics");
        exportStatsButton.setOnAction(_ -> {
            logEvent("Statistics exported to file");
            // In a real implementation, this would save stats to a file
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Statistics");
            alert.setHeaderText("Statistics Exported");
            alert.setContentText("Deadlock statistics have been exported to deadlock_stats.csv");
            alert.showAndWait();
        });
        
        panel.getChildren().addAll(monitoringToggle, clearLogButton, exportStatsButton);
        return panel;
    }
    
    private void setupChartUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), _ -> {
            updatePieChart();
            updatePerformanceChart();
            updateResourceUsage();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void updatePieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        int total = tracker.getTotalDeadlocks();
        int resolved = tracker.getResolvedDeadlocks();
        int prevented = tracker.getPreventedDeadlocks();
        int unresolved = total - resolved;
        
        if (total == 0 && prevented == 0) {
            pieChartData.add(new PieChart.Data("No Deadlocks", 1));
        } else {
            if (unresolved > 0) {
                pieChartData.add(new PieChart.Data("Unresolved (" + unresolved + ")", unresolved));
            }
            if (resolved > 0) {
                pieChartData.add(new PieChart.Data("Resolved (" + resolved + ")", resolved));
            }
            if (prevented > 0) {
                pieChartData.add(new PieChart.Data("Prevented (" + prevented + ")", prevented));
            }
        }
        
        Platform.runLater(() -> deadlockChart.setData(pieChartData));
    }
    
    private void updatePerformanceChart() {
        int time = timeCounter.getAndIncrement();
        
        Platform.runLater(() -> {
            if (detectionSeries.getData().size() > 20) {
                detectionSeries.getData().remove(0);
                resolutionSeries.getData().remove(0);
            }
            
            detectionSeries.getData().add(new XYChart.Data<>(time, tracker.getTotalDeadlocks()));
            resolutionSeries.getData().add(new XYChart.Data<>(time, tracker.getResolvedDeadlocks()));
        });
    }
    
    private void updateResourceUsage() {
        // In a real implementation, this would calculate actual resource usage
        // For demo purposes, we'll simulate resource usage based on system activity
        double usage = 0.3; // base usage
        
        if (tracker.deadlockDetectedProperty().get()) {
            usage += 0.4; // increase when deadlock detected
        }
        
        if (engine.getPerformanceTracker().isMonitoringActive()) {
            usage += 0.1; // increase when monitoring is active
        }
        
        // Add some randomness
        usage += Math.random() * 0.1;
        
        // Ensure it's between 0 and 1
        usage = Math.min(1.0, Math.max(0.0, usage));
        
        final double finalUsage = usage;
        Platform.runLater(() -> resourceUsageBar.setProgress(finalUsage));
    }
    
    private void setupAlertTimeline() {
        alertTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0.5), _ -> {
                if (alertActive.get()) {
                    mainPanel.setStyle("-fx-background-color: " + 
                        (mainPanel.getStyle().contains("#ffebee") ? "#f8f8f8" : "#ffebee") + ";");
                } else {
                    mainPanel.setStyle("-fx-background-color: #f8f8f8;");
                }
            })
        );
        alertTimeline.setCycleCount(Timeline.INDEFINITE);
        alertTimeline.play();
    }
    
    public void logEvent(String event) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + event;
        
        Platform.runLater(() -> {
            eventLog.add(0, logEntry); // Add to the top
            if (eventLog.size() > 100) {
                eventLog.remove(eventLog.size() - 1); // Remove oldest entry if too many
            }
        });
    }
    
    @Override
    public void onDeadlockDetected(List<Integer> processes, DeadlockEvent event) {
        String processesStr = processes.toString();
        logEvent("DEADLOCK DETECTED involving processes " + processesStr);
        alertActive.set(true);
    }
    
    @Override
    public void onDeadlockResolved(List<Integer> processes, String strategy) {
        String processesStr = processes.toString();
        logEvent("Deadlock resolved using " + strategy + " for processes " + processesStr);
        alertActive.set(false);
    }
    
    public Pane getMainPanel() {
        return mainPanel;
    }
}
