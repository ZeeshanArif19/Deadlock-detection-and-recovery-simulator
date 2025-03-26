package deadlocktoolkit.core;

import java.util.Random;

/**
 * Generates predefined and random deadlock scenarios for testing and demonstration.
 */
public class ScenarioGenerator {
    private DeadlockEngine engine;
    private Random random;
    
    public ScenarioGenerator(DeadlockEngine engine) {
        this.engine = engine;
        this.random = new Random();
    }
    
    /**
     * Generates a classic deadlock scenario with circular wait condition.
     * @param numProcesses Number of processes to include in the circular wait
     */
    public void generateCircularWaitScenario(int numProcesses) {
        if (numProcesses < 2) {
            throw new IllegalArgumentException("Need at least 2 processes for a circular wait");
        }
        
        // Initialize the system with appropriate resources
        int numResources = numProcesses;
        int[] availableResources = new int[numResources];
        for (int i = 0; i < numResources; i++) {
            availableResources[i] = numProcesses; // Each resource has numProcesses units
        }
        
        engine.initialize(numProcesses, numResources, availableResources);
        
        // Set maximum demand for each process
        for (int i = 0; i < numProcesses; i++) {
            int[] maxDemand = new int[numResources];
            for (int j = 0; j < numResources; j++) {
                maxDemand[j] = (i == j || i == (j+1) % numResources) ? 2 : 0;
            }
            engine.getBankersAlgorithm().setMaxDemand(i, maxDemand);
        }
        
        // Create the circular wait pattern
        for (int i = 0; i < numProcesses; i++) {
            // Each process holds one resource and requests another
            int heldResource = i;
            int requestedResource = (i + 1) % numProcesses;
            
            // First allocate a resource to each process
            engine.getBankersAlgorithm().allocateResource(i, heldResource, 1);
            engine.getResourceAllocationGraph().addAllocation(heldResource, i, 1);
            
            // Then have each process request another resource
            engine.getResourceAllocationGraph().addRequest(i, requestedResource, 1);
        }
    }
    
    /**
     * Generates the classic dining philosophers deadlock scenario.
     * @param numPhilosophers Number of philosophers (and forks)
     */
    public void generateDiningPhilosophersScenario(int numPhilosophers) {
        if (numPhilosophers < 2) {
            throw new IllegalArgumentException("Need at least 2 philosophers");
        }
        
        // Initialize the system (philosophers = processes, forks = resources)
        int[] availableResources = new int[numPhilosophers];
        for (int i = 0; i < numPhilosophers; i++) {
            availableResources[i] = 1; // Each fork is a single unit resource
        }
        
        engine.initialize(numPhilosophers, numPhilosophers, availableResources);
        
        // Set maximum demand - each philosopher needs 2 forks
        for (int i = 0; i < numPhilosophers; i++) {
            int[] maxDemand = new int[numPhilosophers];
            int leftFork = i;
            int rightFork = (i + 1) % numPhilosophers;
            maxDemand[leftFork] = 1;
            maxDemand[rightFork] = 1;
            engine.getBankersAlgorithm().setMaxDemand(i, maxDemand);
        }
        
        // Create the deadlock scenario - each philosopher picks up their left fork
        for (int i = 0; i < numPhilosophers; i++) {
            int leftFork = i;
            engine.getBankersAlgorithm().allocateResource(i, leftFork, 1);
            engine.getResourceAllocationGraph().addAllocation(leftFork, i, 1);
            
            // Each philosopher tries to pick up their right fork
            int rightFork = (i + 1) % numPhilosophers;
            engine.getResourceAllocationGraph().addRequest(i, rightFork, 1);
        }
    }
    
    /**
     * Generates a random resource allocation scenario that may or may not lead to deadlock.
     * @param deadlockProbability Probability of creating a deadlock (0.0 to 1.0)
     */
    public void generateRandomScenario(int numProcesses, int numResources, double deadlockProbability) {
        if (numProcesses < 2 || numResources < 2) {
            throw new IllegalArgumentException("Need at least 2 processes and 2 resources");
        }
        
        // Initialize the system with random available resources
        int[] availableResources = new int[numResources];
        for (int i = 0; i < numResources; i++) {
            availableResources[i] = 3 + random.nextInt(numProcesses * 2);
        }
        
        engine.initialize(numProcesses, numResources, availableResources);
        
        // Set random maximum demand for each process
        for (int i = 0; i < numProcesses; i++) {
            int[] maxDemand = new int[numResources];
            for (int j = 0; j < numResources; j++) {
                maxDemand[j] = 1 + random.nextInt(3); // Random demand between 1-3 units
            }
            engine.getBankersAlgorithm().setMaxDemand(i, maxDemand);
        }
        
        // Randomly allocate resources to processes
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numResources; j++) {
                if (random.nextDouble() < 0.5) { // 50% chance to allocate this resource
                    int units = 1 + random.nextInt(2); // Allocate 1-2 units
                    int maxForProcess = engine.getBankersAlgorithm().getMaxMatrix()[i][j];
                    units = Math.min(units, maxForProcess); // Don't exceed max demand
                    
                    if (units > 0) {
                        engine.getBankersAlgorithm().allocateResource(i, j, units);
                        engine.getResourceAllocationGraph().addAllocation(j, i, units);
                    }
                }
            }
        }
        
        // Potentially create deadlock by adding circular requests
        if (random.nextDouble() < deadlockProbability) {
            createCircularDependency(numProcesses, numResources);
        }
    }
    
    private void createCircularDependency(int numProcesses, int numResources) {
        // Create a circular dependency among a subset of processes
        int chainLength = 2 + random.nextInt(Math.min(numProcesses - 1, 3)); // Chain of 2-4 processes
        
        int[] processChain = new int[chainLength];
        int[] resourceChain = new int[chainLength];
        
        // Select random processes and resources for the chain
        for (int i = 0; i < chainLength; i++) {
            processChain[i] = random.nextInt(numProcesses);
            resourceChain[i] = random.nextInt(numResources);
        }
        
        // Create the circular dependency
        for (int i = 0; i < chainLength; i++) {
            int process = processChain[i];
            int requestedResource = resourceChain[(i + 1) % chainLength];
            
            // Add request to create dependency
            int units = 1;
            engine.getResourceAllocationGraph().addRequest(process, requestedResource, units);
        }
    }
}
