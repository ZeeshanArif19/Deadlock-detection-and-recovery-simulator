package deadlocktoolkit.core;

import java.util.*;

/**
 * Implements various deadlock prevention strategies.
 */
public class DeadlockPrevention {
    private DeadlockEngine engine;
    
    public enum PreventionStrategy {
        RESOURCE_ORDERING,
        PREEMPTION,
        TIMEOUT,
        ALL_OR_NOTHING,
        WAIT_DIE,
        WOUND_WAIT
    }
    
    public DeadlockPrevention(DeadlockEngine engine) {
        this.engine = engine;
    }
    
    /**
     * Applies a deadlock prevention strategy to the current system state.
     * @param strategy The prevention strategy to apply
     * @return A description of the actions taken
     */
    public String applyPreventionStrategy(PreventionStrategy strategy) {
        switch (strategy) {
            case RESOURCE_ORDERING:
                return applyResourceOrdering();
            case PREEMPTION:
                return applyPreemption();
            case TIMEOUT:
                return applyTimeout();
            case ALL_OR_NOTHING:
                return applyAllOrNothing();
            case WAIT_DIE:
                return applyWaitDie();
            case WOUND_WAIT:
                return applyWoundWait();
            default:
                return "Unknown strategy";
        }
    }
    
    /**
     * Applies resource ordering strategy by reordering resource requests
     * to prevent circular wait conditions.
     */
    private String applyResourceOrdering() {
        ResourceAllocationGraph rag = engine.getResourceAllocationGraph();
        int[][] requestEdges = rag.getRequestEdges();
        StringBuilder result = new StringBuilder();
        
        // Find processes with multiple resource requests that could cause deadlock
        for (int i = 0; i < requestEdges.length; i++) {
            List<Integer> requestedResources = new ArrayList<>();
            
            // Find all resources requested by this process
            for (int j = 0; j < requestEdges[i].length; j++) {
                if (requestEdges[i][j] > 0) {
                    requestedResources.add(j);
                }
            }
            
            // If process has multiple requests, reorder them
            if (requestedResources.size() > 1) {
                // Sort resources by ID (simple ordering strategy)
                Collections.sort(requestedResources);
                
                result.append("Process P").append(i)
                      .append(": Reordered resource requests to: ");
                
                for (Integer resource : requestedResources) {
                    result.append("R").append(resource).append(" ");
                }
                result.append("\n");
            }
        }
        
        if (result.length() == 0) {
            return "No resource reordering needed.";
        }
        
        return "Applied Resource Ordering Strategy:\n" + result.toString();
    }
    
    /**
     * Applies preemption strategy by identifying resources that could be
     * preempted to resolve potential deadlocks.
     */
    private String applyPreemption() {
        List<Integer> deadlockedProcesses = engine.getDeadlockedProcesses();
        if (deadlockedProcesses.isEmpty()) {
            return "No deadlock detected, no preemption needed.";
        }
        
        StringBuilder result = new StringBuilder();
        ResourceAllocationGraph rag = engine.getResourceAllocationGraph();
        int[][] allocationEdges = rag.getAllocationEdges();
        
        // For each deadlocked process, find resources it holds
        for (Integer process : deadlockedProcesses) {
            result.append("Process P").append(process).append(": ");
            boolean hasResources = false;
            
            for (int j = 0; j < allocationEdges[process].length; j++) {
                if (allocationEdges[process][j] > 0) {
                    // Preempt this resource
                    result.append("Preempt R").append(j)
                          .append(" (").append(allocationEdges[process][j])
                          .append(" units) ");
                    hasResources = true;
                }
            }
            
            if (!hasResources) {
                result.append("No resources to preempt.");
            }
            
            result.append("\n");
        }
        
        return "Applied Preemption Strategy:\n" + result.toString();
    }
    
    /**
     * Applies timeout strategy by identifying long-waiting resource requests
     * that should be canceled to prevent deadlock.
     */
    private String applyTimeout() {
        ResourceAllocationGraph rag = engine.getResourceAllocationGraph();
        int[][] requestEdges = rag.getRequestEdges();
        List<Integer> deadlockedProcesses = engine.getDeadlockedProcesses();
        
        if (deadlockedProcesses.isEmpty()) {
            return "No deadlock detected, no timeouts needed.";
        }
        
        StringBuilder result = new StringBuilder();
        
        // For each deadlocked process, timeout its requests
        for (Integer process : deadlockedProcesses) {
            result.append("Process P").append(process).append(": ");
            boolean hasRequests = false;
            
            for (int j = 0; j < requestEdges[process].length; j++) {
                if (requestEdges[process][j] > 0) {
                    // Timeout this request
                    result.append("Timeout request for R").append(j)
                          .append(" (").append(requestEdges[process][j])
                          .append(" units) ");
                    hasRequests = true;
                }
            }
            
            if (!hasRequests) {
                result.append("No requests to timeout.");
            }
            
            result.append("\n");
        }
        
        return "Applied Timeout Strategy:\n" + result.toString();
    }
    
    /**
     * Applies all-or-nothing strategy by ensuring processes get all resources
     * they need at once or none at all.
     */
    private String applyAllOrNothing() {
        BankersAlgorithm bankersAlg = engine.getBankersAlgorithm();
        int[][] allocationMatrix = bankersAlg.getAllocationMatrix();
        int[][] needMatrix = bankersAlg.getNeedMatrix();
        int[] available = bankersAlg.getAvailableResources();
        
        StringBuilder result = new StringBuilder();
        
        // For each process, check if it can get all needed resources
        for (int i = 0; i < allocationMatrix.length; i++) {
            boolean canGetAll = true;
            
            // Check if all needed resources are available
            for (int j = 0; j < needMatrix[i].length; j++) {
                if (needMatrix[i][j] > available[j]) {
                    canGetAll = false;
                    break;
                }
            }
            
            result.append("Process P").append(i).append(": ");
            
            if (canGetAll) {
                result.append("Can acquire all needed resources at once.");
            } else {
                result.append("Cannot acquire all needed resources at once - would need to wait.");
            }
            
            result.append("\n");
        }
        
        return "Applied All-or-Nothing Strategy:\n" + result.toString();
    }
    
    /**
     * Implements the Wait-Die scheme for deadlock prevention.
     */
    private String applyWaitDie() {
        List<Integer> deadlockedProcesses = engine.getDeadlockedProcesses();
        if (deadlockedProcesses.isEmpty()) {
            return "No deadlock detected, no wait-die action needed.";
        }
        
        StringBuilder result = new StringBuilder();
        ResourceAllocationGraph rag = engine.getResourceAllocationGraph();
        int[][] requestEdges = rag.getRequestEdges();
        int[][] allocationEdges = rag.getAllocationEdges();
        
        for (Integer process : deadlockedProcesses) {
            result.append("Process P").append(process).append(": ");
            boolean actionTaken = false;
            
            for (int j = 0; j < requestEdges[process].length; j++) {
                if (requestEdges[process][j] > 0) {
                    // Check if the process can wait or should die
                    for (int k = 0; k < allocationEdges.length; k++) {
                        if (allocationEdges[k][j] > 0 && k != process) {
                            if (process < k) {
                                result.append("Wait for R").append(j).append(" held by P").append(k).append(" ");
                            } else {
                                result.append("Die (abort) due to R").append(j).append(" held by P").append(k).append(" ");
                                actionTaken = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!actionTaken) {
                result.append("No action needed.");
            }
            result.append("\n");
        }
        return "Applied Wait-Die Strategy:\n" + result.toString();
    }
    
    /**
     * Implements the Wound-Wait scheme for deadlock prevention.
     */
    private String applyWoundWait() {
        List<Integer> deadlockedProcesses = engine.getDeadlockedProcesses();
        if (deadlockedProcesses.isEmpty()) {
            return "No deadlock detected, no wound-wait action needed.";
        }
        
        StringBuilder result = new StringBuilder();
        ResourceAllocationGraph rag = engine.getResourceAllocationGraph();
        int[][] requestEdges = rag.getRequestEdges();
        int[][] allocationEdges = rag.getAllocationEdges();
        
        for (Integer process : deadlockedProcesses) {
            result.append("Process P").append(process).append(": ");
            boolean actionTaken = false;
            
            for (int j = 0; j < requestEdges[process].length; j++) {
                if (requestEdges[process][j] > 0) {
                    // Check if the process should wound or wait
                    for (int k = 0; k < allocationEdges.length; k++) {
                        if (allocationEdges[k][j] > 0 && k != process) {
                            if (process < k) {
                                result.append("Wound P").append(k).append(" for R").append(j).append(" ");
                                actionTaken = true;
                                break;
                            } else {
                                result.append("Wait for R").append(j).append(" held by P").append(k).append(" ");
                            }
                        }
                    }
                }
            }
            if (!actionTaken) {
                result.append("No action needed.");
            }
            result.append("\n");
        }
        return "Applied Wound-Wait Strategy:\n" + result.toString();
    }
}
