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
        Process previousProcess = null;
        
        while (completed < total) {
            int currentTime = clock.getCurrentTime();
            
            readyQueue.addArrivedFrom(notYetArrived, currentTime, true);
            
            List<Process> readyList = readyQueue.asList();
            
            if (!readyList.isEmpty()) {
                Process shortestJob = readyList.stream()
                    .min(Comparator
                        .comparingInt(Process::getBurstTime)
                        .thenComparingInt(Process::getArrivalTime)
                        .thenComparingInt(Process::getPriority))
                    .orElse(null);
                
                List<Process> toReadd = new ArrayList<>();
                while (!readyQueue.isEmpty()) {
                    Process p = readyQueue.poll();
                    if (p != shortestJob) {
                        toReadd.add(p);
                    }
                }
                for (Process p : toReadd) {
                    readyQueue.addIfArrived(p, currentTime);
                }
                
                if (shortestJob != null) {
                    if (previousProcess != null && previousProcess != shortestJob) {
                        clock.incrementByContextSwitch(contextSwitchTime);
                        currentTime = clock.getCurrentTime();
                    }
                    
                    int startTime = currentTime;
                    int executionTime = shortestJob.getRemainingTime();
                    
                    clock.incrementByExecution(executionTime);
                    int endTime = clock.getCurrentTime();
                    
                    shortestJob.setRemainingTime(0);
                    shortestJob.setCompletionTime(endTime);
                    shortestJob.setTurnaroundTime(endTime - shortestJob.getArrivalTime());
                    shortestJob.setWaitingTime(shortestJob.getTurnaroundTime() - shortestJob.getBurstTime());
                    
                    executionLog.addRecord(shortestJob.getName(), startTime, endTime);
                    
                    completed++;
                    previousProcess = shortestJob;
                }
            } else {
                if (!notYetArrived.isEmpty()) {
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