import java.util.*;

public class SJFScheduler implements Scheduler {
    
    private List<Process> processes;
    private int contextSwitchTime;
    private SimulationClock clock;
    private ReadyQueue readyQueue;
    private ExecutionLog executionLog;
    private List<Process> notYetArrived;
    
    public SJFScheduler(int contextSwitchTime) {
        this.contextSwitchTime = contextSwitchTime;
        this.clock = new SimulationClock();
        this.readyQueue = new ReadyQueue();
        this.executionLog = new ExecutionLog();
        this.notYetArrived = new ArrayList<>();
    }
    
    public ExecutionLog getExecutionLog() {
        return executionLog;
    }
    
    @Override
    public void run(List<Process> processes) {
        this.processes = new ArrayList<>(processes);
        this.notYetArrived = new ArrayList<>(processes);
        notYetArrived.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        clock.reset();
        executionLog.clear();
        
        int completed = 0;
        int total = processes.size();
        
        Process currentProcess = null;
        int currentProcessStartTime = -1;
        
        while (completed < total) {
            int currentTime = clock.getCurrentTime();
            
            // Add any newly arrived processes to the ready queue
            readyQueue.addArrivedFrom(notYetArrived, currentTime, true);
            
            // Find process with shortest remaining time
            Process shortestJob = findShortestJob();
            
            // Check if we need to preempt the current process
            if (currentProcess != null && shortestJob != null && shortestJob != currentProcess) {
                if (shortestJob.getRemainingTime() < currentProcess.getRemainingTime()) {
                    // Preempt: current process goes back to ready queue
                    int executionTime = currentTime - currentProcessStartTime;
                    executionLog.addRecord(currentProcess.getName(), currentProcessStartTime, currentTime);
                    currentProcess = null;
                    currentProcessStartTime = -1;
                }
            }
            
            // If no current process, select the shortest job
            if (currentProcess == null && shortestJob != null) {
                // Add context switch time if switching from a different process
                if (!executionLog.getRecords().isEmpty() && currentTime > 0) {
                    clock.incrementByContextSwitch(contextSwitchTime);
                    currentTime = clock.getCurrentTime();
                }
                
                currentProcess = shortestJob;
                currentProcessStartTime = currentTime;
            }
            
            // Execute current process for 1 time unit
            if (currentProcess != null) {
                clock.incrementByExecution(1);
                currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
                
                // If process is finished, record it
                if (currentProcess.isFinished()) {
                    int endTime = clock.getCurrentTime();
                    executionLog.addRecord(currentProcess.getName(), currentProcessStartTime, endTime);
                    
                    currentProcess.setCompletionTime(endTime);
                    currentProcess.setTurnaroundTime(endTime - currentProcess.getArrivalTime());
                    currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
                    
                    completed++;
                    currentProcess = null;
                    currentProcessStartTime = -1;
                }
            } else if (!notYetArrived.isEmpty()) {
                // No process ready, jump to next arrival time
                int nextArrivalTime = notYetArrived.get(0).getArrivalTime();
                if (nextArrivalTime > currentTime) {
                    clock.advance(nextArrivalTime - currentTime);
                } else {
                    clock.advance(1);
                }
            } else {
                break;
            }
        }
    }
    

    private Process findShortestJob() {
        List<Process> readyList = readyQueue.asList();
        if (readyList.isEmpty()) {
            return null;
        }
        
        return readyList.stream()
            .filter(p -> !p.isFinished())
            .min(Comparator
                .comparingInt(Process::getRemainingTime)
                .thenComparingInt(Process::getArrivalTime)
                .thenComparingInt(Process::getPriority))
            .orElse(null);
    }
    
    @Override
    public void printExecutionOrder() {
        System.out.println("=== SJF Scheduling Execution Order ===");
        executionLog.printExecutionOrder();
        System.out.println("Execution Sequence: " + executionLog.getExecutionSequenceString());
    }
    
    @Override
    public void printProcessMetrics() {
        System.out.println("\n=== SJF Scheduling Process Metrics ===");
        System.out.printf("%-10s %-12s %-10s %-15s %-15s %-15s%n",
            "Process", "Arrival", "Burst", "Completion", "Waiting", "Turnaround");
        System.out.println("-".repeat(80));
        
        processes.sort(Comparator.comparingInt(Process::getArrivalTime)
            .thenComparing(Process::getName));
        
        for (Process p : processes) {
            System.out.printf("%-10s %-12d %-10d %-15d %-15d %-15d%n",
                p.getName(),
                p.getArrivalTime(),
                p.getBurstTime(),
                p.getCompletionTime(),
                p.getWaitingTime(),
                p.getTurnaroundTime());
        }
    }
    
    @Override
    public void printAverageMetrics() {
        if (processes == null || processes.isEmpty()) {
            System.out.println("No processes scheduled.");
            return;
        }
        
        double totalWaiting = 0;
        double totalTurnaround = 0;
        
        for (Process p : processes) {
            totalWaiting += p.getWaitingTime();
            totalTurnaround += p.getTurnaroundTime();
        }
        
        double avgWaiting = totalWaiting / processes.size();
        double avgTurnaround = totalTurnaround / processes.size();
        
        System.out.println("\n=== SJF Scheduling Average Metrics ===");
        System.out.printf("Average Waiting Time: %.2f%n", avgWaiting);
        System.out.printf("Average Turnaround Time: %.2f%n", avgTurnaround);
    }
    
    public void printAllResults() {
        printExecutionOrder();
        printProcessMetrics();
        printAverageMetrics();
    }
}