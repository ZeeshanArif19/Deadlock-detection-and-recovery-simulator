package deadlocktoolkit.core;

import java.util.*;

public class DeadlockRecovery {
    private DeadlockEngine engine;
    
    public DeadlockRecovery(DeadlockEngine engine) {
        this.engine = engine;
    }
    
    public void resolveDeadlock(List<Integer> deadlockedProcesses) {
        if (deadlockedProcesses.isEmpty()) {
            return;
        }
        
        // Find process with minimum allocated resources to terminate
        int victimProcess = findMinimumResourceProcess(deadlockedProcesses);
        terminateProcess(victimProcess);
    }
    
    private int findMinimumResourceProcess(List<Integer> processes) {
        int minResources = Integer.MAX_VALUE;
        int victimProcess = -1;
        
        for (int processId : processes) {
            int resources = countAllocatedResources(processId);
            if (resources < minResources) {
                minResources = resources;
                victimProcess = processId;
            }
        }
        
        return victimProcess;
    }
    
    private int countAllocatedResources(int processId) {
        int count = 0;
        int[][] allocationMatrix = engine.getBankersAlgorithm().getAllocationMatrix();
        
        for (int j = 0; j < allocationMatrix[processId].length; j++) {
            count += allocationMatrix[processId][j];
        }
        
        return count;
    }
    
    private void terminateProcess(int processId) {
        BankersAlgorithm bankersAlg = engine.getBankersAlgorithm();
        int[][] allocationMatrix = bankersAlg.getAllocationMatrix();
        int[] availableResources = bankersAlg.getAvailableResources();
        
        // Release all resources held by the process
        for (int j = 0; j < allocationMatrix[processId].length; j++) {
            availableResources[j] += allocationMatrix[processId][j];
            allocationMatrix[processId][j] = 0;
        }
        
        // Clear max claims
        int[][] maxMatrix = bankersAlg.getMaxMatrix();
        Arrays.fill(maxMatrix[processId], 0);
        
        // Update matrices
        bankersAlg.setAllocationMatrix(allocationMatrix);
        bankersAlg.setMaxMatrix(maxMatrix);
        bankersAlg.setAvailableResources(availableResources);
        
        // Clear any pending requests in the RAG
        ResourceAllocationGraph rag = engine.getResourceAllocationGraph();
        for (int j = 0; j < availableResources.length; j++) {
            rag.removeRequest(processId, j);
            rag.removeAllocation(processId, j, Integer.MAX_VALUE);
        }
    }
}
