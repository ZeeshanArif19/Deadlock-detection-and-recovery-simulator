package deadlocktoolkit.core;

public class BankersAlgorithm {
    private int numProcesses;
    private int numResources;
    private int[][] allocationMatrix;
    private int[][] maxMatrix;
    private int[][] needMatrix;
    private int[] availableResources;
    
    public BankersAlgorithm(int numProcesses, int numResources, int[] availableResources) {
        this.numProcesses = numProcesses;
        this.numResources = numResources;
        this.availableResources = availableResources.clone();
        
        this.allocationMatrix = new int[numProcesses][numResources];
        this.maxMatrix = new int[numProcesses][numResources];
        this.needMatrix = new int[numProcesses][numResources];
        
        // Initialize need matrix
        updateNeedMatrix();
    }
    
    private void updateNeedMatrix() {
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numResources; j++) {
                needMatrix[i][j] = maxMatrix[i][j] - allocationMatrix[i][j];
            }
        }
    }
    
    public void setMaxDemand(int processId, int[] maxDemand) {
        System.arraycopy(maxDemand, 0, maxMatrix[processId], 0, numResources);
        updateNeedMatrix();
    }
    
    public boolean isSafeState(int processId, int resourceId, int units) {
        // Check if request is within maximum claim
        if (allocationMatrix[processId][resourceId] + units > maxMatrix[processId][resourceId]) {
            return false;
        }
        
        // Check if resources are available
        if (units > availableResources[resourceId]) {
            return false;
        }
        
        // Try to allocate resources
        availableResources[resourceId] -= units;
        allocationMatrix[processId][resourceId] += units;
        updateNeedMatrix();
        
        boolean isSafe = checkSafeState();
        
        // Rollback changes
        availableResources[resourceId] += units;
        allocationMatrix[processId][resourceId] -= units;
        updateNeedMatrix();
        
        return isSafe;
    }
    
    private boolean checkSafeState() {
        boolean[] finished = new boolean[numProcesses];
        int[] work = availableResources.clone();
        int count = 0;
        
        while (count < numProcesses) {
            boolean found = false;
            
            for (int i = 0; i < numProcesses; i++) {
                if (!finished[i]) {
                    boolean canAllocate = true;
                    
                    for (int j = 0; j < numResources; j++) {
                        if (needMatrix[i][j] > work[j]) {
                            canAllocate = false;
                            break;
                        }
                    }
                    
                    if (canAllocate) {
                        for (int j = 0; j < numResources; j++) {
                            work[j] += allocationMatrix[i][j];
                        }
                        finished[i] = true;
                        count++;
                        found = true;
                    }
                }
            }
            
            if (!found) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if the current system state is safe.
     * A state is safe if there exists a sequence in which all processes can
     * complete without causing deadlock.
     * 
     * @return true if the system is in a safe state, false otherwise
     */
    public boolean checkSystemSafety() {
        return checkSafeState();
    }
    
    public void allocateResource(int processId, int resourceId, int units) {
        allocationMatrix[processId][resourceId] += units;
        availableResources[resourceId] -= units;
        updateNeedMatrix();
    }
    
    public void releaseResource(int processId, int resourceId, int units) {
        allocationMatrix[processId][resourceId] -= units;
        availableResources[resourceId] += units;
        updateNeedMatrix();
    }
    
    public int[][] getAllocationMatrix() {
        return allocationMatrix;
    }
    
    public int[][] getMaxMatrix() {
        return maxMatrix;
    }
    
    public int[][] getNeedMatrix() {
        return needMatrix;
    }
    
    public int[] getAvailableResources() {
        return availableResources;
    }
    
    public void setAllocationMatrix(int[][] allocationMatrix) {
        this.allocationMatrix = allocationMatrix;
        updateNeedMatrix();
    }
    
    public void setMaxMatrix(int[][] maxMatrix) {
        this.maxMatrix = maxMatrix;
        updateNeedMatrix();
    }
    
    public void setNeedMatrix(int[][] needMatrix) {
        this.needMatrix = needMatrix;
    }
    
    public void setAvailableResources(int[] availableResources) {
        this.availableResources = availableResources;
    }
}
