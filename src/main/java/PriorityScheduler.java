import java.util.*;

public class PriorityScheduler implements Scheduler {

    private final SimulationClock clock;
    private final ContextSwitchManager contextSwitchManager;
    private final ExecutionLog executionLog;
    private final int agingInterval;

    private static final int MIN_PRIORITY = 1;

    public PriorityScheduler(SimulationClock clock,
            ContextSwitchManager contextSwitchManager,
            ExecutionLog executionLog,
            int agingInterval) {
        this.clock = Objects.requireNonNull(clock);
        this.contextSwitchManager = Objects.requireNonNull(contextSwitchManager);
        this.executionLog = Objects.requireNonNull(executionLog);
        this.agingInterval = Math.max(1, agingInterval);
    }

    @Override
    public void run(List<Process> processes) {

        ReadyQueue readyQueue = new ReadyQueue();
        List<Process> notArrived = new ArrayList<>(processes);
        Process activeProcess = null;

        // Initialize lastTime for aging
        for (Process p : processes)
            p.setLastTime(p.getArrivalTime());

        while (!readyQueue.isEmpty() || !notArrived.isEmpty() || activeProcess != null) {

            int currentTime = clock.getCurrentTime();

            // 1️⃣ Add newly arrived processes
            readyQueue.addArrivedFrom(notArrived, currentTime, true);

            // 3️⃣ Pick CPU if idle
            if (activeProcess == null) {
                activeProcess = readyQueue.pollHighestPriorityEarliestArrival();
                if (activeProcess != null)
                    activeProcess.setLastTime(clock.getCurrentTime());
            }
            applyAging(readyQueue);

            // 2️⃣ Apply aging immediately after arrival
            if (!readyQueue.isEmpty()) {
                Process candidate1 = readyQueue.pollHighestPriorityEarliestArrival();
                if (candidate1.getPriority() < activeProcess.getPriority()
                        || (candidate1.getPriority() == activeProcess.getPriority()
                                && candidate1.getArrivalTime() < activeProcess.getArrivalTime())) {
                    executionLog.addRecord(activeProcess.getName(), activeProcess.getStartTime(),
                            clock.getCurrentTime());
                    contextSwitchManager.applyContextSwitch(clock);
                    readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                    activeProcess = candidate1;
                } else {
                    readyQueue.addIfArrived(candidate1, clock.getCurrentTime());
                }
            }

            // 4️⃣ Preemption loop: continuously check for better candidates
            activeProcess.setStartTime(clock.getCurrentTime());
            if (activeProcess != null) {
                while (true) {
                    Process candidate = readyQueue.pollHighestPriorityEarliestArrival();
                    if (candidate == null)
                        break;

                    boolean shouldPreempt = candidate.getPriority() < activeProcess.getPriority()
                            || (candidate.getPriority() == activeProcess.getPriority()
                                    && candidate.getArrivalTime() < activeProcess.getArrivalTime());

                    if (shouldPreempt) {
                        // Log current slice
                        executionLog.addRecord(activeProcess.getName(), activeProcess.getStartTime(),
                                clock.getCurrentTime());

                        // Put active process back to ready queue
                        readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                        activeProcess.setLastTime(clock.getCurrentTime());

                        // Context switch
                        contextSwitchManager.applyContextSwitch(clock);

                        // Apply aging after context switch
                        applyAging(readyQueue);

                        // Activate the new highest priority process
                        activeProcess = candidate;
                        activeProcess.setLastTime(clock.getCurrentTime());
                    } else {
                        // Candidate not better, put it back and exit loop
                        readyQueue.addIfArrived(candidate, clock.getCurrentTime());
                        break;
                    }
                }
            }

            int startTime = clock.getCurrentTime();

            // 5️⃣ Execute 1 time unit
            if (activeProcess != null) {
                activeProcess.setRemainingTime(activeProcess.getRemainingTime() - 1);
                clock.incrementByExecution(1);
            }

            // 6️⃣ Add newly arrived during this tick
            readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);

            // 7️⃣ Finish process if done
            if (activeProcess != null && activeProcess.isFinished()) {
                finishProcess(activeProcess, startTime);
                contextSwitchManager.applyContextSwitch(clock);
                activeProcess = null;
            }
        }
    }

    /* ================= HELPER METHODS ================= */

    private void applyAging(ReadyQueue queue) {
        int currentTime = clock.getCurrentTime();
        for (Process p : queue.asList()) {
            if (p.getPriority() > MIN_PRIORITY) {
                int waited = currentTime - p.getLastTime();
                int increments = waited / agingInterval; // integer division
                if (increments > 0) {
                    p.setPriority(Math.max(MIN_PRIORITY, p.getPriority() - increments));
                    p.setLastTime(currentTime); // update lastTime
                }
            }
        }
    }

    private void finishProcess(Process process, int startTime) {
        process.setCompletionTime(clock.getCurrentTime());
        process.setTurnaroundTime(process.getCompletionTime() - process.getArrivalTime());
        process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
        executionLog.addRecord(process.getName(), startTime, clock.getCurrentTime());
        process.setLastTime(clock.getCurrentTime());
    }

    /* ================= PRINT METHODS ================= */

    @Override
    public void printExecutionOrder() {
        executionLog.printExecutionOrder();
    }

    @Override
    public void printProcessMetrics() {
        System.out.println("(Metrics are stored inside Process objects)");
    }

    @Override
    public void printAverageMetrics() {
        System.out.println("(Use SimulationEngine process list for averages)");
    }
}
