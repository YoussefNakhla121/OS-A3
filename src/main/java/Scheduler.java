
import java.util.List;

/**
 * Scheduler interface for CPU scheduling simulator
 * 
 * Defines the contract for various CPU scheduling algorithm implementations.
 * Supports multiple scheduling strategies including SJF (Shortest Job First),
 * RR (Round-Robin), Priority-based, and AG (Adaptive scheduling).
 */
public interface Scheduler {
    
    /**
     * Executes the scheduling algorithm on the given list of processes
     * 
     * This method should implement the specific scheduling logic and compute
     * metrics such as completion time, waiting time, and turnaround time for
     * each process.
     * 
     * @param processes List of Process objects to be scheduled
     */
    void run(List<Process> processes);
    
    /**
     * Prints the execution order of processes
     * 
     * Displays the sequence in which processes were executed by the scheduler,
     * along with timing information (start time, end time, duration, etc.)
     * in a readable format.
     */
    void printExecutionOrder();
    
    /**
     * Prints waiting time and turnaround time for each process
     * 
     * Displays detailed timing metrics for individual processes including:
     * - Arrival time
     * - Burst time
     * - Completion time
     * - Waiting time
     * - Turnaround time
     */
    void printProcessMetrics();
    
    /**
     * Prints average waiting time and average turnaround time
     * 
     * Calculates and displays:
     * - Average Waiting Time (AWT) = sum of all waiting times / number of processes
     * - Average Turnaround Time (ATT) = sum of all turnaround times / number of processes
     * 
     * These metrics are used to evaluate the efficiency of the scheduling algorithm.
     */
    void printAverageMetrics();
}
