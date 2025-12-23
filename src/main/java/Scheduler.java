
import java.util.List;


public interface Scheduler {
    

    void run(List<Process> processes);
    

    void printExecutionOrder();
    

    void printProcessMetrics();
    
    void printAverageMetrics();
}
