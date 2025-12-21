import java.util.ArrayList;
import java.util.List;

/**
 * Process class for CPU scheduling simulator
 * 
 * Represents a process with scheduling-related attributes including
 * arrival time, burst time, priority, and metrics for scheduling analysis.
 */
public class Process {
    
    // Process identification
    private String name;
    
    // Timing attributes
    private int arrivalTime;      // Time when process arrives in the system
    private int burstTime;        // Total CPU time required
    private int remainingTime;    // Remaining CPU time to be executed
    
    // Scheduling attributes
    private int priority;         // Priority level (lower value = higher priority)
    private int quantum;          // Time quantum for round-robin scheduling
    
    // Performance metrics
    private int completionTime;   // Time when process completes execution
    private int waitingTime;      // Total time spent waiting in queue
    private int turnaroundTime;   // Total time from arrival to completion
    private List<Integer> quantumHistory = new ArrayList<>();

    
    /**
     * Constructor to initialize a Process with basic scheduling information
     * 
     * @param name Process identifier
     * @param arrivalTime Time when process arrives
     * @param burstTime Total CPU time required
     * @param priority Priority level
     * @param quantum Time quantum for round-robin
     */
    public Process(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;  // Initially equals burst time
        this.priority = priority;
        this.quantum = quantum;
        this.completionTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }
    
    /**
     * Checks if the process has finished execution
     * 
     * @return true if remainingTime is 0, false otherwise
     */
    public boolean isFinished() {
        return remainingTime == 0;
    }
    
    // ============== Getters ==============
    
    public String getName() {
        return name;
    }
    
    public int getArrivalTime() {
        return arrivalTime;
    }
    
    public int getBurstTime() {
        return burstTime;
    }
    
    public int getRemainingTime() {
        return remainingTime;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public int getQuantum() {
        return quantum;
    }
    
    public int getCompletionTime() {
        return completionTime;
    }
    
    public int getWaitingTime() {
        return waitingTime;
    }
    
    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    
    public List<Integer> getQuantumHistory() {
        return quantumHistory;
    }
    
    
    // ============== Setters ==============
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }
    
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }
    
    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }
    
    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }
    
    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }
    public void addQuantumToHistory(int q) {
    quantumHistory.add(q);
    }
    
    /**
     * String representation of the Process
     * 
     * @return Process details in readable format
     */
    @Override
    public String toString() {
        return "Process{" +
                "name='" + name + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", burstTime=" + burstTime +
                ", remainingTime=" + remainingTime +
                ", priority=" + priority +
                ", quantum=" + quantum +
                ", completionTime=" + completionTime +
                ", waitingTime=" + waitingTime +
                ", turnaroundTime=" + turnaroundTime +
                '}';
    }
}
