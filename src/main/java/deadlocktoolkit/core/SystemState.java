package deadlocktoolkit.core;

public class SystemState {
    private int[][] allocationMatrix;
    private int[][] maxMatrix;
    private int[][] needMatrix;
    private int[] availableResources;
    private ResourceAllocationGraph resourceAllocationGraph;
    
    public SystemState(int[][] allocationMatrix, int[][] maxMatrix, int[][] needMatrix,
                      int[] availableResources, ResourceAllocationGraph resourceAllocationGraph) {
        // Deep copy all matrices and arrays
        this.allocationMatrix = deepCopy(allocationMatrix);
        this.maxMatrix = deepCopy(maxMatrix);
        this.needMatrix = deepCopy(needMatrix);
        this.availableResources = availableResources.clone();
        this.resourceAllocationGraph = resourceAllocationGraph.clone();
    }
    
    private int[][] deepCopy(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = matrix[i].clone();
        }
        return copy;
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
    
    public ResourceAllocationGraph getResourceAllocationGraph() {
        return resourceAllocationGraph;
    }
}
