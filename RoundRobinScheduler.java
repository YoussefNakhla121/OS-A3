import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

public class RoundRobinScheduler implements Scheduler {

    private final SimulationClock clock;
    private final ContextSwitchManager contextSwitchManager;
    private final ExecutionLog executionLog;
    private final int timeQuantum;


    public RoundRobinScheduler(SimulationClock clock, ContextSwitchManager contextSwitchManager,
                                ExecutionLog executionLog, int timeQuantum) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.contextSwitchManager = Objects.requireNonNull(contextSwitchManager, "contextSwitchManager must not be null");
        this.executionLog = Objects.requireNonNull(executionLog, "executionLog must not be null");
        
        if (timeQuantum < 0) {
            throw new IllegalArgumentException("timeQuantum must be non-negative");
        }
        this.timeQuantum = timeQuantum;
    }

    @Override
    public void run(List<Process> processes) {

    ReadyQueue readyQueue = new ReadyQueue();
    List<Process> notYetArrived = new ArrayList<>(processes);

    while (!readyQueue.isEmpty() || !notYetArrived.isEmpty()) {

        int currentTime = clock.getCurrentTime();

        readyQueue.addArrivedFrom(notYetArrived, currentTime, true);

        if (readyQueue.isEmpty() && !notYetArrived.isEmpty()) {
            int nextArrival = Integer.MAX_VALUE;
            for (Process p : notYetArrived) {
                nextArrival = Math.min(nextArrival, p.getArrivalTime());
            }
            clock.incrementByExecution(nextArrival - currentTime);
            continue;
        }

        Process currentProcess = readyQueue.poll();
        if (currentProcess == null) break;

        int startTime = clock.getCurrentTime();
        int executionTime = Math.min(timeQuantum, currentProcess.getRemainingTime());

        currentProcess.setRemainingTime(currentProcess.getRemainingTime() - executionTime);
        clock.incrementByExecution(executionTime);

        int endTime = clock.getCurrentTime();

        executionLog.addRecord(currentProcess.getName(),startTime,endTime);

        readyQueue.addArrivedFrom(notYetArrived, endTime, true);

        if (currentProcess.getRemainingTime() > 0) {
            readyQueue.addIfArrived(currentProcess, clock.getCurrentTime());
        } 
        else {
            currentProcess.setCompletionTime(clock.getCurrentTime());
            currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
            currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
        }

        if (!readyQueue.isEmpty() || !notYetArrived.isEmpty()) {
            contextSwitchManager.applyContextSwitch(clock);
        }
    }
}


    @Override
    public void printExecutionOrder() {
        List<ExecutionLog.Record> records = executionLog.getRecords();

        System.out.println("Execution Order:");
        System.out.printf("%-20s %10s %10s %10s%n", "Process", "Start", "End", "Duration");
        System.out.println("----------------------------------------------------------------");

        for (ExecutionLog.Record r : records) {
            int duration = r.getEndTime() - r.getStartTime();
            System.out.printf("%-20s %10d %10d %10d%n",
                    r.getProcessName(), r.getStartTime(), r.getEndTime(), duration);
        }
    }


    @Override
    public void printProcessMetrics() {
        List<Process> procList = null;
        try {
            java.lang.reflect.Field f = this.getClass().getDeclaredField("processes");
            f.setAccessible(true);
            Object val = f.get(this);
            if (val instanceof List) {
                procList = (List<Process>) val;
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            procList = null;
        }

        if (procList == null) {
            System.out.println("(no process list available to print metrics)");
            return;
        }

        System.out.println("Process Metrics:");
        System.out.printf("%-15s %12s %10s %15s %12s %15s%n",
                "Process Name", "Arrival", "Burst", "Completion", "Waiting", "Turnaround");
        System.out.println("-------------------------------------------------------------------------------------------");

        for (Process p : procList) {
            System.out.printf("%-15s %12d %10d %15d %12d %15d%n",
                    p.getName(), p.getArrivalTime(), p.getBurstTime(),
                    p.getCompletionTime(), p.getWaitingTime(), p.getTurnaroundTime());
        }
    }


    @Override
    public void printAverageMetrics() {
        List<Process> procList = null;
        try {
            java.lang.reflect.Field f = this.getClass().getDeclaredField("processes");
            f.setAccessible(true);
            Object val = f.get(this);
            if (val instanceof List) {
                procList = (List<Process>) val;
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            procList = null;
        }

        if (procList == null) {
            System.out.println("No processes to calculate averages.");
            return;
        }

        if (procList.isEmpty()) {
            System.out.println("No processes to calculate averages.");
            return;
        }

        double sumWaiting = 0.0;
        double sumTurnaround = 0.0;
        int count = procList.size();

        for (Process p : procList) {
            sumWaiting += p.getWaitingTime();
            sumTurnaround += p.getTurnaroundTime();
        }

        double averageWaiting = sumWaiting / count;
        double averageTurnaround = sumTurnaround / count;

        System.out.printf("Average Waiting Time     : %.2f%n", averageWaiting);
        System.out.printf("Average Turnaround Time  : %.2f%n", averageTurnaround);
    }
}
