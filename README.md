# Deadlock Detection and Prevention Toolkit

A Java application that provides visualization and simulation of deadlock scenarios in resource allocation systems. The toolkit implements the Banker's Algorithm for deadlock prevention and provides a resource allocation graph visualization.

## Features

- Interactive setup of processes and resources
- Visual representation of resource allocation graph
- Implementation of Banker's Algorithm for deadlock prevention
- Real-time deadlock detection
- Deadlock resolution through process termination
- State history navigation
- Matrix view of system state

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- JavaFX 17 or higher

## Building and Running

1. Clone the repository
2. Navigate to the project directory
3. Build the project:
   ```
   mvn clean package
   ```
4. Run the application:
   ```
   mvn javafx:run
   ```

## Usage

1. Setup Tab:
   - Set the number of processes and resources
   - Initialize the system

2. Simulation Tab:
   - Request resources for processes
   - Release resources from processes
   - Set maximum resource demands for processes
   - View the resource allocation graph

3. Analysis Tab:
   - Detect deadlocks
   - Resolve deadlocks by terminating processes
   - Navigate through system states
   - View allocation matrices

## Implementation Details

The toolkit implements several key components:

- Banker's Algorithm for deadlock prevention
- Resource Allocation Graph for deadlock detection
- Process and resource state management
- Visual representation of system state
- History tracking for state navigation

## License

MIT License
