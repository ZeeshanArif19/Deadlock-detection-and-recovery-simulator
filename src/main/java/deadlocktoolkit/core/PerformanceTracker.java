package deadlocktoolkit.core;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Tracks performance metrics and deadlock statistics for the DeadlockToolkit.
 */
public class PerformanceTracker {
    // Performance metrics
    private int totalDeadlocks;
    private int resolvedDeadlocks;
    private int preventedDeadlocks;
    private Map<String, Integer> resolutionStrategyCounts;
    private List<DeadlockEvent> deadlockEvents;
    private long totalResolutionTime; // in milliseconds
    
    // Real-time monitoring
    private SimpleBooleanProperty deadlockDetected;
    private SimpleIntegerProperty deadlockedProcessCount;
    private SimpleStringProperty systemStatus;
    private boolean monitoringActive;
    
    // Additional performance metrics for the Performance tab
    private List<Double> detectionTimeHistory = new ArrayList<>();
    private List<Double> utilizationHistory = new ArrayList<>();
    private double maxDetectionTime = 0.0;
    private double averageDetectionTime = 0.0;
    private double deadlockFrequency = 0.0; // deadlocks per minute
    private double resourceUtilization = 0.75; // default 75%
    
    public PerformanceTracker() {
        totalDeadlocks = 0;
        resolvedDeadlocks = 0;
        preventedDeadlocks = 0;
        resolutionStrategyCounts = new HashMap<>();
        deadlockEvents = new ArrayList<>();
        totalResolutionTime = 0;
        
        // Initialize observable properties for real-time monitoring
        deadlockDetected = new SimpleBooleanProperty(false);
        deadlockedProcessCount = new SimpleIntegerProperty(0);
        systemStatus = new SimpleStringProperty("System initialized");
        monitoringActive = false;
    }
    
    /**
     * Records a deadlock detection event.
     * 
     * @param deadlockedProcesses The list of processes involved in the deadlock
     * @return The created DeadlockEvent
     */
    public DeadlockEvent recordDeadlockDetection(List<Integer> deadlockedProcesses) {
        totalDeadlocks++;
        DeadlockEvent event = new DeadlockEvent(deadlockedProcesses);
        deadlockEvents.add(event);
        
        // Update real-time monitoring properties
        deadlockDetected.set(true);
        deadlockedProcessCount.set(deadlockedProcesses.size());
        systemStatus.set("Deadlock detected involving " + deadlockedProcesses.size() + " processes");
        
        return event;
    }
    
    /**
     * Records a deadlock resolution event.
     * 
     * @param event The deadlock event that was resolved
     * @param strategy The strategy used to resolve the deadlock
     */
    public void recordDeadlockResolution(DeadlockEvent event, String strategy) {
        if (event != null) {
            event.setResolved(true);
            event.setResolutionStrategy(strategy);
            event.setResolutionTime(LocalDateTime.now());
            
            // Calculate resolution time
            Duration duration = Duration.between(event.getDetectionTime(), event.getResolutionTime());
            long resolutionTimeMs = duration.toMillis();
            totalResolutionTime += resolutionTimeMs;
            event.setResolutionDurationMs(resolutionTimeMs);
            
            // Update counts
            resolvedDeadlocks++;
            resolutionStrategyCounts.put(strategy, resolutionStrategyCounts.getOrDefault(strategy, 0) + 1);
            
            // Update real-time monitoring properties
            deadlockDetected.set(false);
            deadlockedProcessCount.set(0);
            systemStatus.set("Deadlock resolved using " + strategy);
        }
    }
    
    /**
     * Records a deadlock prevention event.
     * 
     * @param strategy The strategy used to prevent the deadlock
     */
    public void recordDeadlockPrevention(String strategy) {
        preventedDeadlocks++;
        resolutionStrategyCounts.put(strategy, resolutionStrategyCounts.getOrDefault(strategy, 0) + 1);
        
        // Update real-time monitoring properties
        systemStatus.set("Deadlock prevented using " + strategy);
    }
    
    /**
     * Updates the system status message.
     * 
     * @param status The new status message
     */
    public void updateSystemStatus(String status) {
        systemStatus.set(status);
    }
    
    /**
     * Starts real-time monitoring of the system.
     */
    public void startMonitoring() {
        monitoringActive = true;
        systemStatus.set("Real-time monitoring active");
    }
    
    /**
     * Stops real-time monitoring of the system.
     */
    public void stopMonitoring() {
        monitoringActive = false;
        systemStatus.set("Real-time monitoring stopped");
    }
    
    /**
     * Checks if real-time monitoring is active.
     * 
     * @return true if monitoring is active, false otherwise
     */
    public boolean isMonitoringActive() {
        return monitoringActive;
    }
    
    /**
     * Gets the total number of deadlocks detected.
     * 
     * @return The total number of deadlocks
     */
    public int getTotalDeadlocks() {
        return totalDeadlocks;
    }
    
    /**
     * Gets the number of resolved deadlocks.
     * 
     * @return The number of resolved deadlocks
     */
    public int getResolvedDeadlocks() {
        return resolvedDeadlocks;
    }
    
    /**
     * Gets the number of prevented deadlocks.
     * 
     * @return The number of prevented deadlocks
     */
    public int getPreventedDeadlocks() {
        return preventedDeadlocks;
    }
    
    /**
     * Gets the list of deadlock events.
     * 
     * @return The list of deadlock events
     */
    public List<DeadlockEvent> getDeadlockEvents() {
        return new ArrayList<>(deadlockEvents);
    }
    
    /**
     * Gets the counts of resolution strategies used.
     * 
     * @return A map of strategy names to counts
     */
    public Map<String, Integer> getResolutionStrategyCounts() {
        return new HashMap<>(resolutionStrategyCounts);
    }
    
    /**
     * Gets the average resolution time in milliseconds.
     * 
     * @return The average resolution time, or 0 if no deadlocks have been resolved
     */
    public double getAverageResolutionTime() {
        return resolvedDeadlocks > 0 ? (double) totalResolutionTime / resolvedDeadlocks : 0;
    }
    
    /**
     * Gets the deadlock detected property for binding.
     * 
     * @return The deadlock detected property
     */
    public SimpleBooleanProperty deadlockDetectedProperty() {
        return deadlockDetected;
    }
    
    /**
     * Gets the deadlocked process count property for binding.
     * 
     * @return The deadlocked process count property
     */
    public SimpleIntegerProperty deadlockedProcessCountProperty() {
        return deadlockedProcessCount;
    }
    
    /**
     * Gets the system status property for binding.
     * 
     * @return The system status property
     */
    public SimpleStringProperty systemStatusProperty() {
        return systemStatus;
    }
    
    /**
     * Records a detection time measurement.
     * 
     * @param detectionTimeMs The time taken to detect a potential deadlock in milliseconds
     */
    public void recordDetectionTime(double detectionTimeMs) {
        detectionTimeHistory.add(detectionTimeMs);
        
        // Update max detection time
        if (detectionTimeMs > maxDetectionTime) {
            maxDetectionTime = detectionTimeMs;
        }
        
        // Update average detection time
        double sum = 0;
        for (Double time : detectionTimeHistory) {
            sum += time;
        }
        averageDetectionTime = sum / detectionTimeHistory.size();
    }
    
    /**
     * Records a resource utilization measurement.
     * 
     * @param utilization The resource utilization as a decimal (0.0 to 1.0)
     */
    public void recordResourceUtilization(double utilization) {
        if (utilization >= 0.0 && utilization <= 1.0) {
            utilizationHistory.add(utilization);
            resourceUtilization = utilization;
        }
    }
    
    /**
     * Updates the deadlock frequency based on recent activity.
     * 
     * @param deadlocksInTimeframe Number of deadlocks in the timeframe
     * @param timeframeMinutes The timeframe in minutes
     */
    public void updateDeadlockFrequency(int deadlocksInTimeframe, double timeframeMinutes) {
        if (timeframeMinutes > 0) {
            deadlockFrequency = deadlocksInTimeframe / timeframeMinutes;
        }
    }
    
    /**
     * Gets the average detection time in milliseconds.
     * 
     * @return The average detection time
     */
    public double getAverageDetectionTime() {
        return averageDetectionTime;
    }
    
    /**
     * Gets the maximum detection time in milliseconds.
     * 
     * @return The maximum detection time
     */
    public double getMaxDetectionTime() {
        return maxDetectionTime;
    }
    
    /**
     * Gets the deadlock frequency (deadlocks per minute).
     * 
     * @return The deadlock frequency
     */
    public double getDeadlockFrequency() {
        return deadlockFrequency;
    }
    
    /**
     * Gets the current resource utilization (0.0 to 1.0).
     * 
     * @return The resource utilization
     */
    public double getResourceUtilization() {
        return resourceUtilization;
    }
    
    /**
     * Gets the history of detection time measurements.
     * 
     * @return The list of detection time measurements
     */
    public List<Double> getDetectionTimeHistory() {
        return new ArrayList<>(detectionTimeHistory);
    }
    
    /**
     * Gets the history of resource utilization measurements.
     * 
     * @return The list of resource utilization measurements
     */
    public List<Double> getUtilizationHistory() {
        return new ArrayList<>(utilizationHistory);
    }
    
    /**
     * Resets all performance metrics.
     */
    public void resetMetrics() {
        totalDeadlocks = 0;
        resolvedDeadlocks = 0;
        preventedDeadlocks = 0;
        resolutionStrategyCounts.clear();
        deadlockEvents.clear();
        totalResolutionTime = 0;
        detectionTimeHistory.clear();
        utilizationHistory.clear();
        maxDetectionTime = 0.0;
        averageDetectionTime = 0.0;
        deadlockFrequency = 0.0;
        resourceUtilization = 0.75;
        
        // Reset observable properties
        deadlockDetected.set(false);
        deadlockedProcessCount.set(0);
        systemStatus.set("Performance metrics reset");
    }
    
    /**
     * Generates a performance report as a formatted string.
     * 
     * @return A formatted performance report
     */
    public String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("DEADLOCK TOOLKIT PERFORMANCE REPORT\n");
        report.append("===================================\n\n");
        
        report.append("DEADLOCK STATISTICS:\n");
        report.append("Total Deadlocks Detected: ").append(totalDeadlocks).append("\n");
        report.append("Deadlocks Resolved: ").append(resolvedDeadlocks).append("\n");
        report.append("Deadlocks Prevented: ").append(preventedDeadlocks).append("\n");
        report.append("Deadlock Frequency: ").append(String.format("%.2f", deadlockFrequency)).append(" per minute\n\n");
        
        report.append("PERFORMANCE METRICS:\n");
        report.append("Average Detection Time: ").append(String.format("%.2f", averageDetectionTime)).append(" ms\n");
        report.append("Maximum Detection Time: ").append(String.format("%.2f", maxDetectionTime)).append(" ms\n");
        report.append("Average Resolution Time: ").append(String.format("%.2f", getAverageResolutionTime())).append(" ms\n");
        report.append("Resource Utilization: ").append(String.format("%.1f", resourceUtilization * 100)).append("%\n\n");
        
        report.append("RESOLUTION STRATEGIES:\n");
        for (Map.Entry<String, Integer> entry : resolutionStrategyCounts.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Represents a deadlock event with detection and resolution information.
     */
    public static class DeadlockEvent {
        private List<Integer> involvedProcesses;
        private LocalDateTime detectionTime;
        private LocalDateTime resolutionTime;
        private boolean resolved;
        private String resolutionStrategy;
        private long resolutionDurationMs;
        
        public DeadlockEvent(List<Integer> involvedProcesses) {
            this.involvedProcesses = new ArrayList<>(involvedProcesses);
            this.detectionTime = LocalDateTime.now();
            this.resolved = false;
            this.resolutionStrategy = "";
        }
        
        public List<Integer> getInvolvedProcesses() {
            return involvedProcesses;
        }
        
        public LocalDateTime getDetectionTime() {
            return detectionTime;
        }
        
        public LocalDateTime getResolutionTime() {
            return resolutionTime;
        }
        
        public void setResolutionTime(LocalDateTime resolutionTime) {
            this.resolutionTime = resolutionTime;
        }
        
        public boolean isResolved() {
            return resolved;
        }
        
        public void setResolved(boolean resolved) {
            this.resolved = resolved;
        }
        
        public String getResolutionStrategy() {
            return resolutionStrategy;
        }
        
        public void setResolutionStrategy(String resolutionStrategy) {
            this.resolutionStrategy = resolutionStrategy;
        }
        
        public long getResolutionDurationMs() {
            return resolutionDurationMs;
        }
        
        public void setResolutionDurationMs(long resolutionDurationMs) {
            this.resolutionDurationMs = resolutionDurationMs;
        }
    }
}
