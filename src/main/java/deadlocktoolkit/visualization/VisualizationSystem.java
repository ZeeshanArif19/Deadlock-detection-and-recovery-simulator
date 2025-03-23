package deadlocktoolkit.visualization;

import deadlocktoolkit.core.*;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;
import java.util.List;
import java.util.ArrayList;

public class VisualizationSystem {
    private DeadlockEngine engine;
    private Pane graphPane;
    private Pane matrixPane;
    private Pane statsPane;
    
    private static final double PROCESS_RADIUS = 30;
    private static final double RESOURCE_SIZE = 50;
    private static final Color PROCESS_COLOR = Color.LIGHTBLUE;
    private static final Color RESOURCE_COLOR = Color.LIGHTGREEN;
    private static final Color DEADLOCKED_COLOR = Color.RED;
    private static final Color ALLOCATION_COLOR = Color.GREEN;
    private static final Color REQUEST_COLOR = Color.RED;
    private static final Color SAFE_COLOR = Color.GREEN;
    private static final Color UNSAFE_COLOR = Color.ORANGE;
    
    public VisualizationSystem(DeadlockEngine engine) {
        this.engine = engine;
        this.graphPane = new Pane();
        this.matrixPane = new Pane();
        this.statsPane = new Pane();
    }
    
    public Pane getGraphPane() {
        return graphPane;
    }
    
    public Pane getMatrixPane() {
        return matrixPane;
    }
    
    public Pane getStatsPane() {
        return statsPane;
    }
    
    public void updateVisualization() {
        updateResourceAllocationGraph();
        updateMatrixVisualization();
        updateStatistics();
    }
    
    private void updateResourceAllocationGraph() {
        graphPane.getChildren().clear();
        
        ResourceAllocationGraph rag = engine.getResourceAllocationGraph();
        int[][] allocationEdges = rag.getAllocationEdges();
        int[][] requestEdges = rag.getRequestEdges();
        List<Integer> deadlockedProcesses = engine.getDeadlockedProcesses();
        
        // Calculate positions
        Point2D[] processPositions = calculateProcessPositions(allocationEdges.length);
        Point2D[] resourcePositions = calculateResourcePositions(requestEdges.length);
        
        // Draw processes
        for (int i = 0; i < allocationEdges.length; i++) {
            Point2D pos = processPositions[i];
            Circle process = new Circle(pos.getX(), pos.getY(), PROCESS_RADIUS);
            process.setFill(deadlockedProcesses.contains(i) ? DEADLOCKED_COLOR : PROCESS_COLOR);
            process.setStroke(Color.BLACK);
            
            Text text = new Text(pos.getX() - 10, pos.getY() + 5, "P" + i);
            graphPane.getChildren().addAll(process, text);
        }
        
        // Draw resources
        for (int i = 0; i < requestEdges.length; i++) {
            Point2D pos = resourcePositions[i];
            Rectangle resource = new Rectangle(pos.getX() - RESOURCE_SIZE/2, pos.getY() - RESOURCE_SIZE/2,
                                            RESOURCE_SIZE, RESOURCE_SIZE);
            resource.setFill(RESOURCE_COLOR);
            resource.setStroke(Color.BLACK);
            
            Text text = new Text(pos.getX() - 10, pos.getY() + 5, "R" + i);
            graphPane.getChildren().addAll(resource, text);
        }
        
        // Draw allocation edges
        for (int i = 0; i < allocationEdges.length; i++) {
            for (int j = 0; j < allocationEdges[i].length; j++) {
                if (allocationEdges[i][j] > 0) {
                    Point2D start = calculateEdgeStart(resourcePositions[j], processPositions[i], RESOURCE_SIZE/2);
                    Point2D end = calculateEdgeEnd(processPositions[i], resourcePositions[j], PROCESS_RADIUS);
                    Arrow arrow = new Arrow(start, end, ALLOCATION_COLOR);
                    graphPane.getChildren().add(arrow);
                }
            }
        }
        
        // Draw request edges
        for (int i = 0; i < requestEdges.length; i++) {
            for (int j = 0; j < requestEdges[i].length; j++) {
                if (requestEdges[i][j] > 0) {
                    Point2D start = calculateEdgeStart(processPositions[j], resourcePositions[i], PROCESS_RADIUS);
                    Point2D end = calculateEdgeEnd(resourcePositions[i], processPositions[j], RESOURCE_SIZE/2);
                    Arrow arrow = new Arrow(start, end, REQUEST_COLOR);
                    graphPane.getChildren().add(arrow);
                }
            }
        }
    }
    
    private Point2D[] calculateProcessPositions(int numProcesses) {
        Point2D[] positions = new Point2D[numProcesses];
        double centerX = graphPane.getWidth() / 2;
        double centerY = graphPane.getHeight() / 2;
        double radius = Math.min(centerX, centerY) * 0.8;
        
        for (int i = 0; i < numProcesses; i++) {
            double angle = 2 * Math.PI * i / numProcesses;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            positions[i] = new Point2D(x, y);
        }
        
        return positions;
    }
    
    private Point2D[] calculateResourcePositions(int numResources) {
        Point2D[] positions = new Point2D[numResources];
        double centerX = graphPane.getWidth() / 2;
        double centerY = graphPane.getHeight() / 2;
        double radius = Math.min(centerX, centerY) * 0.4;
        
        for (int i = 0; i < numResources; i++) {
            double angle = 2 * Math.PI * i / numResources;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            positions[i] = new Point2D(x, y);
        }
        
        return positions;
    }
    
    private Point2D calculateEdgeStart(Point2D from, Point2D to, double radius) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double length = Math.sqrt(dx * dx + dy * dy);
        return new Point2D(
            from.getX() + dx * radius / length,
            from.getY() + dy * radius / length
        );
    }
    
    private Point2D calculateEdgeEnd(Point2D to, Point2D from, double radius) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double length = Math.sqrt(dx * dx + dy * dy);
        return new Point2D(
            to.getX() - dx * radius / length,
            to.getY() - dy * radius / length
        );
    }
    
    private void updateMatrixVisualization() {
        matrixPane.getChildren().clear();
        
        BankersAlgorithm bankersAlg = engine.getBankersAlgorithm();
        if (bankersAlg == null) {
            Label noDataLabel = new Label("No data available. Please initialize the system first.");
            matrixPane.getChildren().add(noDataLabel);
            return;
        }
        
        int[][] allocation = bankersAlg.getAllocationMatrix();
        int[][] max = bankersAlg.getMaxMatrix();
        int[][] need = bankersAlg.getNeedMatrix();
        int[] available = bankersAlg.getAvailableResources();
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        
        // Headers
        grid.add(new Label("Process"), 0, 0);
        for (int i = 0; i < available.length; i++) {
            grid.add(new Label("R" + i), i + 1, 0);
        }
        
        // Allocation Matrix
        grid.add(new Label("Allocation:"), 0, 1);
        for (int i = 0; i < allocation.length; i++) {
            grid.add(new Label("P" + i), 0, i + 2);
            for (int j = 0; j < allocation[i].length; j++) {
                Label valueLabel = new Label(String.valueOf(allocation[i][j]));
                if (allocation[i][j] > 0) {
                    valueLabel.setStyle("-fx-font-weight: bold;");
                }
                grid.add(valueLabel, j + 1, i + 2);
            }
        }
        
        // Max Matrix
        int rowOffset = allocation.length + 3;
        grid.add(new Label("Max:"), 0, rowOffset - 1);
        for (int i = 0; i < max.length; i++) {
            grid.add(new Label("P" + i), 0, i + rowOffset);
            for (int j = 0; j < max[i].length; j++) {
                grid.add(new Label(String.valueOf(max[i][j])), j + 1, i + rowOffset);
            }
        }
        
        // Need Matrix
        rowOffset = rowOffset + max.length + 1;
        grid.add(new Label("Need:"), 0, rowOffset - 1);
        for (int i = 0; i < need.length; i++) {
            grid.add(new Label("P" + i), 0, i + rowOffset);
            for (int j = 0; j < need[i].length; j++) {
                Label valueLabel = new Label(String.valueOf(need[i][j]));
                if (need[i][j] > 0) {
                    valueLabel.setStyle("-fx-text-fill: #cc0000;");
                }
                grid.add(valueLabel, j + 1, i + rowOffset);
            }
        }
        
        // Available Resources
        rowOffset = rowOffset + need.length + 1;
        grid.add(new Label("Available:"), 0, rowOffset);
        for (int i = 0; i < available.length; i++) {
            Label valueLabel = new Label(String.valueOf(available[i]));
            if (available[i] == 0) {
                valueLabel.setStyle("-fx-text-fill: #cc0000;");
            } else {
                valueLabel.setStyle("-fx-text-fill: #008800;");
            }
            grid.add(valueLabel, i + 1, rowOffset);
        }
        
        matrixPane.getChildren().add(grid);
    }
    
    private void updateStatistics() {
        statsPane.getChildren().clear();
        
        if (engine.getBankersAlgorithm() == null) {
            Label noDataLabel = new Label("No data available. Please initialize the system first.");
            statsPane.getChildren().add(noDataLabel);
            return;
        }
        
        VBox statsBox = new VBox(15);
        statsBox.setPadding(new Insets(10));
        
        // System status
        HBox statusBox = new HBox(10);
        Label statusLabel = new Label("System Status:");
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        boolean hasDeadlock = engine.detectDeadlock();
        Label deadlockStatus = new Label(hasDeadlock ? "DEADLOCK DETECTED" : "NO DEADLOCK");
        deadlockStatus.setStyle(hasDeadlock ? 
                               "-fx-text-fill: red; -fx-font-weight: bold;" : 
                               "-fx-text-fill: green; -fx-font-weight: bold;");
        
        statusBox.getChildren().addAll(statusLabel, deadlockStatus);
        
        // Resource utilization chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> resourceChart = new BarChart<>(xAxis, yAxis);
        resourceChart.setTitle("Resource Utilization");
        resourceChart.setLegendVisible(true);
        resourceChart.setPrefHeight(250);
        
        XYChart.Series<String, Number> allocatedSeries = new XYChart.Series<>();
        allocatedSeries.setName("Allocated");
        
        XYChart.Series<String, Number> availableSeries = new XYChart.Series<>();
        availableSeries.setName("Available");
        
        int[] available = engine.getBankersAlgorithm().getAvailableResources();
        int[][] allocation = engine.getBankersAlgorithm().getAllocationMatrix();
        
        for (int i = 0; i < available.length; i++) {
            int totalAllocated = 0;
            for (int j = 0; j < allocation.length; j++) {
                totalAllocated += allocation[j][i];
            }
            
            allocatedSeries.getData().add(new XYChart.Data<>("R" + i, totalAllocated));
            availableSeries.getData().add(new XYChart.Data<>("R" + i, available[i]));
        }
        
        resourceChart.getData().addAll(allocatedSeries, availableSeries);
        
        // Process status
        GridPane processGrid = new GridPane();
        processGrid.setHgap(10);
        processGrid.setVgap(5);
        processGrid.setPadding(new Insets(10));
        
        processGrid.add(new Label("Process"), 0, 0);
        processGrid.add(new Label("Status"), 1, 0);
        processGrid.add(new Label("Resources Held"), 2, 0);
        processGrid.add(new Label("Resources Needed"), 3, 0);
        
        List<Integer> deadlockedProcesses = engine.getDeadlockedProcesses();
        
        for (int i = 0; i < allocation.length; i++) {
            processGrid.add(new Label("P" + i), 0, i + 1);
            
            String status = deadlockedProcesses.contains(i) ? "Deadlocked" : "Running";
            Label statusValueLabel = new Label(status);
            statusValueLabel.setStyle(deadlockedProcesses.contains(i) ? 
                                    "-fx-text-fill: red;" : "-fx-text-fill: green;");
            processGrid.add(statusValueLabel, 1, i + 1);
            
            int resourcesHeld = 0;
            for (int j = 0; j < allocation[i].length; j++) {
                resourcesHeld += allocation[i][j];
            }
            processGrid.add(new Label(String.valueOf(resourcesHeld)), 2, i + 1);
            
            int resourcesNeeded = 0;
            for (int j = 0; j < engine.getBankersAlgorithm().getNeedMatrix()[i].length; j++) {
                resourcesNeeded += engine.getBankersAlgorithm().getNeedMatrix()[i][j];
            }
            processGrid.add(new Label(String.valueOf(resourcesNeeded)), 3, i + 1);
        }
        
        Label statsTitle = new Label("System Statistics");
        statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        statsBox.getChildren().addAll(
            statsTitle,
            statusBox,
            resourceChart,
            new Label("Process Status:"),
            processGrid
        );
        
        statsPane.getChildren().add(statsBox);
    }
    
    public Node createMatrixView(int[][] matrix, String title) {
        VBox view = new VBox(10);
        view.setPadding(new Insets(10));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        if (matrix == null || matrix.length == 0) {
            Label emptyLabel = new Label("No data available");
            view.getChildren().addAll(titleLabel, emptyLabel);
            return view;
        }
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        
        int numRows = matrix.length;
        int numCols = matrix[0].length;
        
        // Add column headers (Resource 0, Resource 1, etc.)
        for (int j = 0; j < numCols; j++) {
            Label header = new Label("R" + j);
            header.setStyle("-fx-font-weight: bold;");
            grid.add(header, j + 1, 0);
        }
        
        // Add row headers (Process 0, Process 1, etc.)
        for (int i = 0; i < numRows; i++) {
            Label header = new Label("P" + i);
            header.setStyle("-fx-font-weight: bold;");
            grid.add(header, 0, i + 1);
        }
        
        // Add matrix values with color coding
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Label valueLabel = new Label(String.valueOf(matrix[i][j]));
                
                // Color code the values
                if (matrix[i][j] > 0) {
                    valueLabel.setStyle("-fx-text-fill: #006400;"); // Dark green for positive values
                } else {
                    valueLabel.setStyle("-fx-text-fill: #000000;"); // Black for zero values
                }
                
                grid.add(valueLabel, j + 1, i + 1);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        
        view.getChildren().addAll(titleLabel, scrollPane);
        return view;
    }
    
    public Node createVectorView(int[] vector, String title) {
        VBox view = new VBox(10);
        view.setPadding(new Insets(10));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        if (vector == null || vector.length == 0) {
            Label emptyLabel = new Label("No data available");
            view.getChildren().addAll(titleLabel, emptyLabel);
            return view;
        }
        
        HBox vectorBox = new HBox(15);
        vectorBox.setPadding(new Insets(10));
        vectorBox.setAlignment(Pos.CENTER_LEFT);
        
        // Create a grid for the vector
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        
        // Add column headers (Resource 0, Resource 1, etc.)
        for (int j = 0; j < vector.length; j++) {
            Label header = new Label("R" + j);
            header.setStyle("-fx-font-weight: bold;");
            grid.add(header, j, 0);
        }
        
        // Add vector values with color coding
        for (int j = 0; j < vector.length; j++) {
            Label valueLabel = new Label(String.valueOf(vector[j]));
            
            // Color code the values
            if (vector[j] > 0) {
                valueLabel.setStyle("-fx-text-fill: #006400;"); // Dark green for positive values
            } else {
                valueLabel.setStyle("-fx-text-fill: #000000;"); // Black for zero values
            }
            
            grid.add(valueLabel, j, 1);
        }
        
        vectorBox.getChildren().add(grid);
        
        view.getChildren().addAll(titleLabel, vectorBox);
        return view;
    }
}
