package deadlocktoolkit.ui;

import deadlocktoolkit.core.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced panel for testing and visualizing the Banker's Algorithm
 * with user input capabilities.
 */
public class BankersAlgorithmPanel {
    private DeadlockEngine engine;
    private VBox mainPanel;
    private TableView<ProcessRow> processTable;
    private TableView<ResourceRow> resourceTable;
    private ObservableList<ProcessRow> processData;
    private ObservableList<ResourceRow> resourceData;
    private Label safetyStatusLabel;
    
    public BankersAlgorithmPanel(DeadlockEngine engine) {
        this.engine = engine;
        initializeUI();
    }
    
    private void initializeUI() {
        mainPanel = new VBox(15);
        mainPanel.setPadding(new Insets(15));
        mainPanel.setStyle("-fx-background-color: #f8f8f8;");
        
        // Title
        Label titleLabel = new Label("Banker's Algorithm Simulator");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Description
        TextArea descriptionArea = new TextArea(
            "The Banker's Algorithm is a deadlock avoidance algorithm that tests for safety " +
            "by simulating the allocation of predetermined maximum possible amounts of all resources, " +
            "and then makes an \"s-state\" check to test for possible deadlock conditions.\n\n" +
            "Use this panel to configure processes, resources, and test the algorithm with your own inputs."
        );
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(80);
        descriptionArea.setStyle("-fx-control-inner-background: #f0f0f0; -fx-background-color: #f0f0f0;");
        
        // Setup controls
        HBox setupControls = createSetupControls();
        
        // Tables panel
        VBox tablesPanel = createTablesPanel();
        
        // Action buttons
        HBox actionButtons = createActionButtons();
        
        // Safety status panel
        HBox safetyPanel = createSafetyPanel();
        
        // Resource Allocation Graph Visualization
        TitledPane ragVisualizationPane = createRAGVisualizationPane();
        
        // Example button
        Button loadExampleButton = new Button("Load Example Data");
        loadExampleButton.setOnAction(_ -> loadExampleData());
        loadExampleButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        // Add all components to main panel
        mainPanel.getChildren().addAll(
            titleLabel,
            descriptionArea,
            setupControls,
            tablesPanel,
            actionButtons,
            safetyPanel,
            ragVisualizationPane,
            loadExampleButton
        );
    }
    
    private HBox createSetupControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));
        controls.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        // Process count spinner
        Label processLabel = new Label("Number of Processes:");
        Spinner<Integer> processSpinner = new Spinner<>(1, 10, 3);
        processSpinner.setEditable(true);
        processSpinner.setPrefWidth(80);
        
        // Resource count spinner
        Label resourceLabel = new Label("Number of Resources:");
        Spinner<Integer> resourceSpinner = new Spinner<>(1, 10, 3);
        resourceSpinner.setEditable(true);
        resourceSpinner.setPrefWidth(80);
        
        // Initialize button
        Button initButton = new Button("Initialize Tables");
        initButton.setOnAction(_ -> {
            int numProcesses = processSpinner.getValue();
            int numResources = resourceSpinner.getValue();
            initializeTables(numProcesses, numResources);
        });
        
        controls.getChildren().addAll(
            processLabel, processSpinner,
            resourceLabel, resourceSpinner,
            initButton
        );
        
        return controls;
    }
    
    private VBox createTablesPanel() {
        VBox panel = new VBox(15);
        
        // Process table (Allocation, Max, Need)
        Label processTableLabel = new Label("Process Resource Tables");
        processTableLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Add explanation for Need matrix
        Label needExplanationLabel = new Label("Note: Need = Max - Allocation (calculated automatically)");
        needExplanationLabel.setStyle("-fx-text-fill: #3498db; -fx-font-style: italic;");
        
        processTable = new TableView<>();
        processTable.setEditable(true);
        processTable.setPrefHeight(200);
        processData = FXCollections.observableArrayList();
        processTable.setItems(processData);
        
        // Resource table (Available, Total)
        Label resourceTableLabel = new Label("Resource Tables");
        resourceTableLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        resourceTable = new TableView<>();
        resourceTable.setEditable(true);
        resourceTable.setPrefHeight(100);
        resourceData = FXCollections.observableArrayList();
        resourceTable.setItems(resourceData);
        
        panel.getChildren().addAll(
            processTableLabel, needExplanationLabel, processTable,
            resourceTableLabel, resourceTable
        );
        
        return panel;
    }
    
    private HBox createActionButtons() {
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        
        Button checkSafetyButton = new Button("Check System Safety");
        checkSafetyButton.setOnAction(e -> checkSystemSafety());
        
        Button requestResourceButton = new Button("Request Resource");
        requestResourceButton.setOnAction(e -> showRequestResourceDialog());
        
        Button resetButton = new Button("Reset Tables");
        resetButton.setOnAction(e -> resetTables());
        
        buttons.getChildren().addAll(
            checkSafetyButton,
            requestResourceButton,
            resetButton
        );
        
        return buttons;
    }
    
    private HBox createSafetyPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label safetyLabel = new Label("System Safety Status:");
        safetyLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        safetyStatusLabel = new Label("Not checked");
        safetyStatusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        panel.getChildren().addAll(safetyLabel, safetyStatusLabel);
        
        return panel;
    }
    
    private TitledPane createRAGVisualizationPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        Label infoLabel = new Label("Resource Allocation Graph Visualization");
        infoLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Placeholder for the graph visualization
        Pane graphPane = new Pane();
        graphPane.setPrefSize(500, 300);
        graphPane.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        Label placeholderLabel = new Label("Graph will be displayed here");
        placeholderLabel.setLayoutX(200);
        placeholderLabel.setLayoutY(140);
        graphPane.getChildren().add(placeholderLabel);
        
        Button visualizeButton = new Button("Visualize Current State");
        visualizeButton.setOnAction(e -> visualizeResourceAllocationGraph(graphPane));
        
        content.getChildren().addAll(infoLabel, graphPane, visualizeButton);
        
        TitledPane pane = new TitledPane("Resource Allocation Graph", content);
        pane.setExpanded(false); // Collapsed by default
        return pane;
    }
    
    private void visualizeResourceAllocationGraph(Pane graphPane) {
        // Clear existing visualization
        graphPane.getChildren().clear();
        
        int numProcesses = processData.size();
        int numResources = resourceData.size();
        
        // Create temporary matrices for visualization
        int[][] allocation = new int[numProcesses][numResources];
        
        // Fill matrices from table data
        for (int i = 0; i < numProcesses; i++) {
            ProcessRow row = processData.get(i);
            for (int j = 0; j < numResources; j++) {
                allocation[i][j] = row.getAllocation(j);
            }
        }
        
        // Create process nodes (circles)
        double processRadius = 25;
        double resourceRadius = 20;
        double startX = 100;
        double startY = 50;
        double processSpacing = 80;
        double resourceSpacing = 80;
        
        // Create process nodes
        for (int i = 0; i < numProcesses; i++) {
            Circle processCircle = new Circle(startX, startY + i * processSpacing, processRadius);
            processCircle.setFill(Color.LIGHTBLUE);
            processCircle.setStroke(Color.BLUE);
            
            Text processText = new Text(startX - 10, startY + i * processSpacing + 5, "P" + i);
            processText.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            graphPane.getChildren().addAll(processCircle, processText);
        }
        
        // Create resource nodes (squares)
        for (int j = 0; j < numResources; j++) {
            Rectangle resourceRect = new Rectangle(
                startX + 250, 
                startY + j * resourceSpacing - resourceRadius, 
                resourceRadius * 2, 
                resourceRadius * 2
            );
            resourceRect.setFill(Color.LIGHTGREEN);
            resourceRect.setStroke(Color.GREEN);
            
            Text resourceText = new Text(
                startX + 250 + resourceRadius - 10, 
                startY + j * resourceSpacing + 5, 
                "R" + j
            );
            resourceText.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            graphPane.getChildren().addAll(resourceRect, resourceText);
        }
        
        // Draw allocation edges
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numResources; j++) {
                if (allocation[i][j] > 0) {
                    // Draw arrow from resource to process
                    Arrow arrow = new Arrow(
                        startX + 250 + resourceRadius, 
                        startY + j * resourceSpacing,
                        startX + processRadius, 
                        startY + i * processSpacing
                    );
                    arrow.setStroke(Color.RED);
                    arrow.setStrokeWidth(2);
                    
                    // Add allocation count
                    Text allocationText = new Text(
                        (startX + 250 + startX) / 2, 
                        (startY + j * resourceSpacing + startY + i * processSpacing) / 2,
                        String.valueOf(allocation[i][j])
                    );
                    allocationText.setFont(Font.font("System", FontWeight.BOLD, 12));
                    allocationText.setFill(Color.RED);
                    
                    graphPane.getChildren().addAll(arrow, allocationText);
                }
            }
        }
    }
    
    // Helper class to draw arrows for the graph
    private class Arrow extends Line {
        public Arrow(double startX, double startY, double endX, double endY) {
            super(startX, startY, endX, endY);
            
            // Calculate the direction of the arrow
            double dx = endX - startX;
            double dy = endY - startY;
            double length = Math.sqrt(dx * dx + dy * dy);
            
            // Normalize direction
            dx /= length;
            dy /= length;
            
            // Calculate the points of the arrow head
            double arrowHeadSize = 10;
            double arrowAngle = Math.toRadians(20);
            
            double x1 = endX - arrowHeadSize * (dx * Math.cos(arrowAngle) + dy * Math.sin(arrowAngle));
            double y1 = endY - arrowHeadSize * (dy * Math.cos(arrowAngle) - dx * Math.sin(arrowAngle));
            double x2 = endX - arrowHeadSize * (dx * Math.cos(arrowAngle) - dy * Math.sin(arrowAngle));
            double y2 = endY - arrowHeadSize * (dy * Math.cos(arrowAngle) + dx * Math.sin(arrowAngle));
            
            // Create and add the arrow head
            Polygon arrowHead = new Polygon();
            arrowHead.getPoints().addAll(
                endX, endY,
                x1, y1,
                x2, y2
            );
            arrowHead.setFill(Color.RED);
            
            // Add the arrow head to the parent when this line is added
            parentProperty().addListener((_, __, newParent) -> {
                if (newParent instanceof Pane) {
                    ((Pane) newParent).getChildren().add(arrowHead);
                }
            });
        }
    }
    
    private void loadExampleData() {
        // First initialize with 5 processes and 3 resources
        initializeTables(5, 3);
        
        // Set allocation values
        processData.get(0).setAllocation(0, 0);
        processData.get(0).setAllocation(1, 1);
        processData.get(0).setAllocation(2, 0);
        
        processData.get(1).setAllocation(0, 2);
        processData.get(1).setAllocation(1, 0);
        processData.get(1).setAllocation(2, 0);
        
        processData.get(2).setAllocation(0, 3);
        processData.get(2).setAllocation(1, 0);
        processData.get(2).setAllocation(2, 2);
        
        processData.get(3).setAllocation(0, 2);
        processData.get(3).setAllocation(1, 1);
        processData.get(3).setAllocation(2, 1);
        
        processData.get(4).setAllocation(0, 0);
        processData.get(4).setAllocation(1, 0);
        processData.get(4).setAllocation(2, 2);
        
        // Set max values
        processData.get(0).setMax(0, 7);
        processData.get(0).setMax(1, 5);
        processData.get(0).setMax(2, 3);
        
        processData.get(1).setMax(0, 3);
        processData.get(1).setMax(1, 2);
        processData.get(1).setMax(2, 2);
        
        processData.get(2).setMax(0, 9);
        processData.get(2).setMax(1, 0);
        processData.get(2).setMax(2, 2);
        
        processData.get(3).setMax(0, 2);
        processData.get(3).setMax(1, 2);
        processData.get(3).setMax(2, 2);
        
        processData.get(4).setMax(0, 4);
        processData.get(4).setMax(1, 3);
        processData.get(4).setMax(2, 3);
        
        // Update need values
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                processData.get(i).updateNeed(j);
            }
        }
        
        // Set available resources
        resourceData.get(0).setAvailable(3);
        resourceData.get(1).setAvailable(3);
        resourceData.get(2).setAvailable(2);
        
        // Set total resources
        resourceData.get(0).setTotal(10);
        resourceData.get(1).setTotal(5);
        resourceData.get(2).setTotal(7);
        
        // Check safety
        checkSystemSafety();
        
        // Show confirmation
        showAlert("Example Loaded", "A sample scenario has been loaded with 5 processes and 3 resources.\n\n" +
                 "This is a classic example used to demonstrate the Banker's Algorithm. " +
                 "The current state is safe, but you can experiment by requesting additional resources.");
    }
    
    private void initializeTables(int numProcesses, int numResources) {
        // Clear existing data
        processData.clear();
        resourceData.clear();
        processTable.getColumns().clear();
        resourceTable.getColumns().clear();
        
        // Create process table columns
        TableColumn<ProcessRow, String> processIdCol = new TableColumn<>("Process");
        processIdCol.setCellValueFactory(cellData -> cellData.getValue().processIdProperty());
        processIdCol.setPrefWidth(80);
        processTable.getColumns().add(processIdCol);
        
        // Add columns for allocation, max, and need for each resource
        for (int i = 0; i < numResources; i++) {
            final int resourceIndex = i;
            
            // Allocation columns
            TableColumn<ProcessRow, String> allocCol = new TableColumn<>("Alloc R" + i);
            allocCol.setCellValueFactory(cellData -> 
                cellData.getValue().getAllocationProperty(resourceIndex));
            allocCol.setCellFactory(col -> createEditableCell(resourceIndex, "allocation"));
            allocCol.setPrefWidth(70);
            
            // Max columns
            TableColumn<ProcessRow, String> maxCol = new TableColumn<>("Max R" + i);
            maxCol.setCellValueFactory(cellData -> 
                cellData.getValue().getMaxProperty(resourceIndex));
            maxCol.setCellFactory(col -> createEditableCell(resourceIndex, "max"));
            maxCol.setPrefWidth(70);
            
            // Need columns
            TableColumn<ProcessRow, String> needCol = new TableColumn<>("Need R" + i);
            needCol.setCellValueFactory(cellData -> 
                cellData.getValue().getNeedProperty(resourceIndex));
            needCol.setEditable(false);
            needCol.setPrefWidth(70);
            needCol.setStyle("-fx-background-color: #f0f0f0;");
            
            processTable.getColumns().addAll(allocCol, maxCol, needCol);
        }
        
        // Create resource table columns
        TableColumn<ResourceRow, String> resourceIdCol = new TableColumn<>("Resource");
        resourceIdCol.setCellValueFactory(cellData -> cellData.getValue().resourceIdProperty());
        resourceIdCol.setPrefWidth(80);
        resourceTable.getColumns().add(resourceIdCol);
        
        // Available column
        TableColumn<ResourceRow, String> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(cellData -> cellData.getValue().availableProperty());
        availableCol.setCellFactory(col -> createEditableResourceCell("available"));
        availableCol.setPrefWidth(100);
        
        // Total column
        TableColumn<ResourceRow, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData -> cellData.getValue().totalProperty());
        totalCol.setCellFactory(col -> createEditableResourceCell("total"));
        totalCol.setPrefWidth(100);
        
        resourceTable.getColumns().addAll(availableCol, totalCol);
        
        // Initialize data rows
        for (int i = 0; i < numProcesses; i++) {
            processData.add(new ProcessRow("P" + i, numResources));
        }
        
        for (int i = 0; i < numResources; i++) {
            resourceData.add(new ResourceRow("R" + i));
        }
        
        // Reset safety status
        safetyStatusLabel.setText("Not checked");
        safetyStatusLabel.setTextFill(Color.BLACK);
    }
    
    private TableCell<ProcessRow, String> createEditableCell(int resourceIndex, String type) {
        return new TableCell<ProcessRow, String>() {
            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                textField.selectAll();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(item);
                        }
                        setGraphic(textField);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    } else {
                        setText(item);
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                    }
                }
            }

            private void createTextField() {
                textField = new TextField(getItem());
                textField.setOnAction(e -> commitEdit(textField.getText()));
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        commitEdit(textField.getText());
                    }
                });
                // Only allow numeric input and max 10
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.matches("\\d*")) {
                        textField.setText(newVal.replaceAll("[^\\d]", ""));
                    } else if (!newVal.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newVal);
                            if (v > 10) {
                                textField.setText("10");
                            }
                        } catch (NumberFormatException ex) {
                            textField.setText(oldVal);
                        }
                    }
                });
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                ProcessRow row = getTableView().getItems().get(getIndex());
                int val = 0;
                try {
                    val = Integer.parseInt(newValue);
                } catch (Exception ignored) {}
                if (val > 10) val = 10;
                if (type.equals("allocation")) {
                    row.setAllocation(resourceIndex, val);
                } else if (type.equals("max")) {
                    row.setMax(resourceIndex, val);
                }
                row.updateNeed(resourceIndex);
                getTableView().refresh();
                setText(String.valueOf(val));
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        };
    }

    private TableCell<ResourceRow, String> createEditableResourceCell(String type) {
        return new TableCell<ResourceRow, String>() {
            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                textField.selectAll();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(item);
                        }
                        setGraphic(textField);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    } else {
                        setText(item);
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                    }
                }
            }

            private void createTextField() {
                textField = new TextField(getItem());
                textField.setOnAction(e -> commitEdit(textField.getText()));
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        commitEdit(textField.getText());
                    }
                });
                // Only allow numeric input and max 10
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.matches("\\d*")) {
                        textField.setText(newVal.replaceAll("[^\\d]", ""));
                    } else if (!newVal.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newVal);
                            if (v > 10) {
                                textField.setText("10");
                            }
                        } catch (NumberFormatException ex) {
                            textField.setText(oldVal);
                        }
                    }
                });
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                ResourceRow row = getTableView().getItems().get(getIndex());
                int val = 0;
                try {
                    val = Integer.parseInt(newValue);
                } catch (Exception ignored) {}
                if (val > 10) val = 10;
                if (type.equals("available")) {
                    row.setAvailable(val);
                } else if (type.equals("total")) {
                    row.setTotal(val);
                }
                setText(String.valueOf(val));
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        };
    }
    
    private void checkSystemSafety() {
        // Create temporary matrices for checking
        int numProcesses = processData.size();
        int numResources = resourceData.size();
        
        int[][] allocation = new int[numProcesses][numResources];
        int[][] max = new int[numProcesses][numResources];
        int[] available = new int[numResources];
        
        // Fill matrices from table data
        for (int i = 0; i < numProcesses; i++) {
            ProcessRow row = processData.get(i);
            for (int j = 0; j < numResources; j++) {
                allocation[i][j] = row.getAllocation(j);
                max[i][j] = row.getMax(j);
            }
        }
        
        for (int i = 0; i < numResources; i++) {
            available[i] = resourceData.get(i).getAvailable();
        }
        
        // Create temporary BankersAlgorithm instance for safety check
        BankersAlgorithm tempBankers = new BankersAlgorithm(numProcesses, numResources, available);
        tempBankers.setAllocationMatrix(allocation);
        tempBankers.setMaxMatrix(max);
        
        boolean isSafe = tempBankers.checkSystemSafety();
        
        // Update safety status
        if (isSafe) {
            safetyStatusLabel.setText("SAFE");
            safetyStatusLabel.setTextFill(Color.GREEN);
        } else {
            safetyStatusLabel.setText("UNSAFE");
            safetyStatusLabel.setTextFill(Color.RED);
        }
    }
    
    private void showRequestResourceDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Request Resource");
        dialog.setHeaderText("Enter resource request details");
        
        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Process selector
        ComboBox<String> processCombo = new ComboBox<>();
        for (ProcessRow row : processData) {
            processCombo.getItems().add(row.getProcessId());
        }
        processCombo.getSelectionModel().selectFirst();
        
        // Resource selector
        ComboBox<String> resourceCombo = new ComboBox<>();
        for (ResourceRow row : resourceData) {
            resourceCombo.getItems().add(row.getResourceId());
        }
        resourceCombo.getSelectionModel().selectFirst();
        
        // Units spinner
        Spinner<Integer> unitsSpinner = new Spinner<>(1, 100, 1);
        unitsSpinner.setEditable(true);
        
        grid.add(new Label("Process:"), 0, 0);
        grid.add(processCombo, 1, 0);
        grid.add(new Label("Resource:"), 0, 1);
        grid.add(resourceCombo, 1, 1);
        grid.add(new Label("Units:"), 0, 2);
        grid.add(unitsSpinner, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                int processIndex = processCombo.getSelectionModel().getSelectedIndex();
                int resourceIndex = resourceCombo.getSelectionModel().getSelectedIndex();
                int units = unitsSpinner.getValue();
                
                // Create temporary matrices for checking
                int numProcesses = processData.size();
                int numResources = resourceData.size();
                
                int[][] allocation = new int[numProcesses][numResources];
                int[][] max = new int[numProcesses][numResources];
                int[] available = new int[numResources];
                
                // Fill matrices from table data
                for (int i = 0; i < numProcesses; i++) {
                    ProcessRow row = processData.get(i);
                    for (int j = 0; j < numResources; j++) {
                        allocation[i][j] = row.getAllocation(j);
                        max[i][j] = row.getMax(j);
                    }
                }
                
                for (int i = 0; i < numResources; i++) {
                    available[i] = resourceData.get(i).getAvailable();
                }
                
                // Create temporary BankersAlgorithm instance for safety check
                BankersAlgorithm tempBankers = new BankersAlgorithm(numProcesses, numResources, available);
                tempBankers.setAllocationMatrix(allocation);
                tempBankers.setMaxMatrix(max);
                
                // Check if request is valid
                if (units > available[resourceIndex]) {
                    showAlert("Invalid Request", "Not enough available resources.");
                    return;
                }
                
                if (units > tempBankers.getNeedMatrix()[processIndex][resourceIndex]) {
                    showAlert("Invalid Request", "Request exceeds maximum need.");
                    return;
                }
                
                // Check if request would be safe
                boolean isSafe = tempBankers.isSafeState(processIndex, resourceIndex, units);
                
                if (isSafe) {
                    // Update allocation and available resources
                    ProcessRow processRow = processData.get(processIndex);
                    ResourceRow resourceRow = resourceData.get(resourceIndex);
                    
                    processRow.setAllocation(resourceIndex, 
                        processRow.getAllocation(resourceIndex) + units);
                    processRow.updateNeed(resourceIndex);
                    
                    resourceRow.setAvailable(resourceRow.getAvailable() - units);
                    
                    showAlert("Request Granted", "Resource request was granted.");
                    
                    // Update safety status
                    safetyStatusLabel.setText("SAFE");
                    safetyStatusLabel.setTextFill(Color.GREEN);
                } else {
                    showAlert("Request Denied", "Resource request would lead to unsafe state.");
                    
                    // Update safety status
                    safetyStatusLabel.setText("UNSAFE if granted");
                    safetyStatusLabel.setTextFill(Color.RED);
                }
            }
        });
    }
    
    private void resetTables() {
        int numProcesses = processData.size();
        int numResources = resourceData.size();
        
        // Reset process data
        for (ProcessRow row : processData) {
            for (int i = 0; i < numResources; i++) {
                row.setAllocation(i, 0);
                row.setMax(i, 0);
                row.updateNeed(i);
            }
        }
        
        // Reset resource data
        for (ResourceRow row : resourceData) {
            row.setAvailable(0);
            row.setTotal(0);
        }
        
        // Reset safety status
        safetyStatusLabel.setText("Not checked");
        safetyStatusLabel.setTextFill(Color.BLACK);
        
        processTable.refresh();
        resourceTable.refresh();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public Pane getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Data model for process rows in the table.
     */
    public static class ProcessRow {
        private String processId;
        private List<Integer> allocation;
        private List<Integer> max;
        private List<Integer> need;
        
        public ProcessRow(String processId, int numResources) {
            this.processId = processId;
            this.allocation = new ArrayList<>(numResources);
            this.max = new ArrayList<>(numResources);
            this.need = new ArrayList<>(numResources);
            
            for (int i = 0; i < numResources; i++) {
                allocation.add(0);
                max.add(0);
                need.add(0);
            }
        }
        
        public String getProcessId() {
            return processId;
        }
        
        public javafx.beans.property.StringProperty processIdProperty() {
            return new javafx.beans.property.SimpleStringProperty(processId);
        }
        
        public int getAllocation(int resourceIndex) {
            return allocation.get(resourceIndex);
        }
        
        public void setAllocation(int resourceIndex, int value) {
            allocation.set(resourceIndex, value);
        }
        
        public javafx.beans.property.StringProperty getAllocationProperty(int resourceIndex) {
            return new javafx.beans.property.SimpleStringProperty(
                String.valueOf(allocation.get(resourceIndex)));
        }
        
        public int getMax(int resourceIndex) {
            return max.get(resourceIndex);
        }
        
        public void setMax(int resourceIndex, int value) {
            max.set(resourceIndex, value);
        }
        
        public javafx.beans.property.StringProperty getMaxProperty(int resourceIndex) {
            return new javafx.beans.property.SimpleStringProperty(
                String.valueOf(max.get(resourceIndex)));
        }
        
        public int getNeed(int resourceIndex) {
            return need.get(resourceIndex);
        }
        
        public javafx.beans.property.StringProperty getNeedProperty(int resourceIndex) {
            return new javafx.beans.property.SimpleStringProperty(
                String.valueOf(need.get(resourceIndex)));
        }
        
        public void updateNeed(int resourceIndex) {
            int needValue = Math.max(0, max.get(resourceIndex) - allocation.get(resourceIndex));
            need.set(resourceIndex, needValue);
        }
    }
    
    /**
     * Data model for resource rows in the table.
     */
    public static class ResourceRow {
        private String resourceId;
        private int available;
        private int total;
        
        public ResourceRow(String resourceId) {
            this.resourceId = resourceId;
            this.available = 0;
            this.total = 0;
        }
        
        public String getResourceId() {
            return resourceId;
        }
        
        public javafx.beans.property.StringProperty resourceIdProperty() {
            return new javafx.beans.property.SimpleStringProperty(resourceId);
        }
        
        public int getAvailable() {
            return available;
        }
        
        public void setAvailable(int available) {
            this.available = available;
        }
        
        public javafx.beans.property.StringProperty availableProperty() {
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(available));
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public javafx.beans.property.StringProperty totalProperty() {
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(total));
        }
    }
}
