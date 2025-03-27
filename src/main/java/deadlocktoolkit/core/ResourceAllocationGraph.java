package deadlocktoolkit.core;

import java.util.*;

public class ResourceAllocationGraph implements Cloneable {
    private int numProcesses;
    private int numResources;
    private int[][] allocationEdges; // Process -> Resource
    private int[][] requestEdges;    // Resource -> Process
    
    public ResourceAllocationGraph(int numProcesses, int numResources) {
        this.numProcesses = numProcesses;
        this.numResources = numResources;
        this.allocationEdges = new int[numProcesses][numResources];
        this.requestEdges = new int[numResources][numProcesses];
    }
    
    public void addRequest(int processId, int resourceId, int units) {
        requestEdges[resourceId][processId] = units;
    }
    
    public void addAllocation(int processId, int resourceId, int units) {
        allocationEdges[processId][resourceId] = units;
    }
    
    public void removeRequest(int processId, int resourceId) {
        requestEdges[resourceId][processId] = 0;
    }
    
    public void removeAllocation(int processId, int resourceId, int units) {
        allocationEdges[processId][resourceId] -= units;
        if (allocationEdges[processId][resourceId] < 0) {
            allocationEdges[processId][resourceId] = 0;
        }
    }
    
    public boolean detectDeadlock() {
        boolean[] visited = new boolean[numProcesses];
        boolean[] recursionStack = new boolean[numProcesses];
        
        // Start DFS from each process
        for (int i = 0; i < numProcesses; i++) {
            if (hasCycle(i, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasCycle(int processId, boolean[] visited, boolean[] recursionStack) {
        if (recursionStack[processId]) {
            return true;
        }
        
        if (visited[processId]) {
            return false;
        }
        
        visited[processId] = true;
        recursionStack[processId] = true;
        
        // Check for cycles through resource requests
        for (int resourceId = 0; resourceId < numResources; resourceId++) {
            if (requestEdges[resourceId][processId] > 0) {
                // Check all processes that hold this resource
                for (int otherProcess = 0; otherProcess < numProcesses; otherProcess++) {
                    if (allocationEdges[otherProcess][resourceId] > 0) {
                        if (hasCycle(otherProcess, visited, recursionStack)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        recursionStack[processId] = false;
        return false;
    }
    
    public List<Integer> getDeadlockedProcesses() {
        List<Integer> deadlocked = new ArrayList<>();
        boolean[] visited = new boolean[numProcesses];
        boolean[] recursionStack = new boolean[numProcesses];
        
        for (int i = 0; i < numProcesses; i++) {
            if (!visited[i]) {
                findDeadlockedProcesses(i, visited, recursionStack, deadlocked);
            }
        }
        
        return deadlocked;
    }
    
    private void findDeadlockedProcesses(int processId, boolean[] visited, boolean[] recursionStack, List<Integer> deadlocked) {
        visited[processId] = true;
        recursionStack[processId] = true;
        
        for (int resourceId = 0; resourceId < numResources; resourceId++) {
            if (requestEdges[resourceId][processId] > 0) {
                for (int otherProcess = 0; otherProcess < numProcesses; otherProcess++) {
                    if (allocationEdges[otherProcess][resourceId] > 0) {
                        if (!visited[otherProcess]) {
                            findDeadlockedProcesses(otherProcess, visited, recursionStack, deadlocked);
                        } else if (recursionStack[otherProcess] && !deadlocked.contains(otherProcess)) {
                            deadlocked.add(otherProcess);
                        }
                    }
                }
            }
        }
        
        recursionStack[processId] = false;
    }
    
    @Override
    public ResourceAllocationGraph clone() {
        try {
            ResourceAllocationGraph cloned = (ResourceAllocationGraph) super.clone();
            cloned.allocationEdges = new int[numProcesses][numResources];
            cloned.requestEdges = new int[numResources][numProcesses];
            
            for (int i = 0; i < numProcesses; i++) {
                System.arraycopy(allocationEdges[i], 0, cloned.allocationEdges[i], 0, numResources);
            }
            
            for (int i = 0; i < numResources; i++) {
                System.arraycopy(requestEdges[i], 0, cloned.requestEdges[i], 0, numProcesses);
            }
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
    
    public int[][] getAllocationEdges() {
        return allocationEdges;
    }
    
    public int[][] getRequestEdges() {
        return requestEdges;
    }
}
