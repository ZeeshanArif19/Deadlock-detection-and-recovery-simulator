package deadlocktoolkit.core;

import java.util.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class DeadlockEngine {
    private ResourceAllocationGraph rag;
    private BankersAlgorithm bankersAlg;
    private DeadlockRecovery recovery;
    private DeadlockPrevention prevention;
    private List<SystemState> stateHistory;
    private int currentStateIndex;
    private PerformanceTracker performanceTracker;
    private Timeline monitoringTimeline;
    private List<DeadlockListener> deadlockListeners;
    
    public DeadlockEngine() {
        stateHistory = new ArrayList<>();
        currentStateIndex = -1;
        performanceTracker = new PerformanceTracker();
        deadlockListeners = new ArrayList<>();
        
        // Set up real-time monitoring timeline
        monitoringTimeline = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
            if (performanceTracker.isMonitoringActive() && rag != null) {
                checkAndNotifyDeadlocks();
            }
        }));
        monitoringTimeline.setCycleCount(Animation.INDEFINITE);
    }
    
    public void initialize(int numProcesses, int numResources, int[] availableResources) {
        rag = new ResourceAllocationGraph(numProcesses, numResources);
        bankersAlg = new BankersAlgorithm(numProcesses, numResources, availableResources);
        recovery = new DeadlockRecovery(this);
        prevention = new DeadlockPrevention(this);
        
        // Reset performance tracker
        performanceTracker.resetMetrics();
        
        // Record initial state
        recordState();
        
        // Start monitoring if it was active
        if (performanceTracker.isMonitoringActive()) {
            startMonitoring();
        }
    }
    
    public boolean requestResource(int processId, int resourceId, int units) {
        // Check if request would be safe using Banker's Algorithm
        if (!bankersAlg.isSafeState(processId, resourceId, units)) {
            System.out.println("Request denied: Would lead to unsafe state");
            performanceTracker.recordDeadlockPrevention("Banker's Algorithm");
            performanceTracker.updateSystemStatus("Request denied: Would lead to unsafe state");
            return false;
        }
        
        // Allocate the resource
        bankersAlg.allocateResource(processId, resourceId, units);
        rag.addRequest(processId, resourceId, units);
        
        // Check for deadlock after allocation
        if (rag.detectDeadlock()) {
            System.out.println("Warning: Deadlock detected after allocation");
            List<Integer> deadlockedProcesses = rag.getDeadlockedProcesses();
            PerformanceTracker.DeadlockEvent event = performanceTracker.recordDeadlockDetection(deadlockedProcesses);
            notifyDeadlockDetected(deadlockedProcesses, event);
        } else {
            performanceTracker.updateSystemStatus("Resource allocated successfully");
        }
        
        // Record the new state
        recordState();
        return true;
    }
    
    public void releaseResource(int processId, int resourceId, int units) {
        bankersAlg.releaseResource(processId, resourceId, units);
        rag.removeAllocation(processId, resourceId, units);
        
        performanceTracker.updateSystemStatus("Resource released successfully");
        
        // Record the new state
        recordState();
    }
    
    public boolean detectDeadlock() {
        boolean deadlockExists = rag.detectDeadlock();
        
        if (deadlockExists) {
            List<Integer> deadlockedProcesses = rag.getDeadlockedProcesses();
            PerformanceTracker.DeadlockEvent event = performanceTracker.recordDeadlockDetection(deadlockedProcesses);
            notifyDeadlockDetected(deadlockedProcesses, event);
        }
        
        return deadlockExists;
    }
    
    private void checkAndNotifyDeadlocks() {
        if (rag.detectDeadlock()) {
            List<Integer> deadlockedProcesses = rag.getDeadlockedProcesses();
            PerformanceTracker.DeadlockEvent event = performanceTracker.recordDeadlockDetection(deadlockedProcesses);
            notifyDeadlockDetected(deadlockedProcesses, event);
        }
    }
    
    public List<Integer> getDeadlockedProcesses() {
        return rag.getDeadlockedProcesses();
    }
    
    public void resolveDeadlock() {
        List<Integer> deadlockedProcesses = getDeadlockedProcesses();
        if (deadlockedProcesses.isEmpty()) {
            return;
        }
        
        // Find the latest unresolved deadlock event
        List<PerformanceTracker.DeadlockEvent> events = performanceTracker.getDeadlockEvents();
        PerformanceTracker.DeadlockEvent latestEvent = null;
        for (int i = events.size() - 1; i >= 0; i--) {
            if (!events.get(i).isResolved()) {
                latestEvent = events.get(i);
                break;
            }
        }
        
        String strategy = "Process Termination";
        recovery.resolveDeadlock(deadlockedProcesses);
        
        if (latestEvent != null) {
            performanceTracker.recordDeadlockResolution(latestEvent, strategy);
        }
        
        notifyDeadlockResolved(deadlockedProcesses, strategy);
        recordState();
    }
    
    private void recordState() {
        SystemState currentState = new SystemState(
            bankersAlg.getAllocationMatrix().clone(),
            bankersAlg.getMaxMatrix().clone(),
            bankersAlg.getNeedMatrix().clone(),
            bankersAlg.getAvailableResources().clone(),
            rag.clone()
        );
        
        // Add to history and update index
        if (currentStateIndex < stateHistory.size() - 1) {
            // If we've gone back in history and now made a change,
            // remove all future states
            stateHistory = stateHistory.subList(0, currentStateIndex + 1);
        }
        
        stateHistory.add(currentState);
        currentStateIndex = stateHistory.size() - 1;
    }
    
    public SystemState getCurrentState() {
        if (currentStateIndex >= 0 && currentStateIndex < stateHistory.size()) {
            return stateHistory.get(currentStateIndex);
        }
        return null;
    }
    
    public boolean canGoBack() {
        return currentStateIndex > 0;
    }
    
    public boolean canGoForward() {
        return currentStateIndex < stateHistory.size() - 1;
    }
    
    public void goBack() {
        if (canGoBack()) {
            currentStateIndex--;
            restoreState(stateHistory.get(currentStateIndex));
        }
    }
    
    public void goForward() {
        if (canGoForward()) {
            currentStateIndex++;
            restoreState(stateHistory.get(currentStateIndex));
        }
    }
    
    private void restoreState(SystemState state) {
        bankersAlg.setAllocationMatrix(state.getAllocationMatrix());
        bankersAlg.setMaxMatrix(state.getMaxMatrix());
        bankersAlg.setNeedMatrix(state.getNeedMatrix());
        bankersAlg.setAvailableResources(state.getAvailableResources());
        rag = state.getResourceAllocationGraph().clone();
    }
    
    public BankersAlgorithm getBankersAlgorithm() {
        return bankersAlg;
    }
    
    public ResourceAllocationGraph getResourceAllocationGraph() {
        return rag;
    }
    
    public DeadlockPrevention getDeadlockPrevention() {
        return prevention;
    }
    
    public PerformanceTracker getPerformanceTracker() {
        return performanceTracker;
    }
    
    /**
     * Starts real-time monitoring of the system for deadlocks.
     */
    public void startMonitoring() {
        if (!monitoringTimeline.getStatus().equals(Animation.Status.RUNNING)) {
            monitoringTimeline.play();
            performanceTracker.startMonitoring();
        }
    }
    
    /**
     * Stops real-time monitoring of the system.
     */
    public void stopMonitoring() {
        if (monitoringTimeline.getStatus().equals(Animation.Status.RUNNING)) {
            monitoringTimeline.stop();
            performanceTracker.stopMonitoring();
        }
    }
    
    /**
     * Adds a listener for deadlock events.
     * 
     * @param listener The listener to add
     */
    public void addDeadlockListener(DeadlockListener listener) {
        if (!deadlockListeners.contains(listener)) {
            deadlockListeners.add(listener);
        }
    }
    
    /**
     * Removes a listener for deadlock events.
     * 
     * @param listener The listener to remove
     */
    public void removeDeadlockListener(DeadlockListener listener) {
        deadlockListeners.remove(listener);
    }
    
    /**
     * Notifies all listeners that a deadlock has been detected.
     * 
     * @param processes The processes involved in the deadlock
     * @param event The deadlock event
     */
    private void notifyDeadlockDetected(List<Integer> processes, PerformanceTracker.DeadlockEvent event) {
        for (DeadlockListener listener : deadlockListeners) {
            listener.onDeadlockDetected(processes, event);
        }
    }
    
    /**
     * Notifies all listeners that a deadlock has been resolved.
     * 
     * @param processes The processes that were involved in the deadlock
     * @param strategy The strategy used to resolve the deadlock
     */
    private void notifyDeadlockResolved(List<Integer> processes, String strategy) {
        for (DeadlockListener listener : deadlockListeners) {
            listener.onDeadlockResolved(processes, strategy);
        }
    }
    
    /**
     * Interface for deadlock event listeners.
     */
    public interface DeadlockListener {
        /**
         * Called when a deadlock is detected.
         * 
         * @param processes The processes involved in the deadlock
         * @param event The deadlock event
         */
        void onDeadlockDetected(List<Integer> processes, PerformanceTracker.DeadlockEvent event);
        
        /**
         * Called when a deadlock is resolved.
         * 
         * @param processes The processes that were involved in the deadlock
         * @param strategy The strategy used to resolve the deadlock
         */
        void onDeadlockResolved(List<Integer> processes, String strategy);
    }
}
