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

        boolean activeProcessExecuted = false;

        // Initialize lastTime for aging
        for (Process p : processes)
            p.setLastTime(p.getArrivalTime());

        while (!readyQueue.isEmpty() || !notArrived.isEmpty() || activeProcess != null) {

            // 1️⃣ Add arrived processes
            readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);

            // 2️⃣ Apply aging
            applyAging(readyQueue);

            boolean decisionStable = false;

            // 3️⃣ Selection + comparison loop
            while (!decisionStable) {

                Process bestCandidate = readyQueue.peekHighestPriorityEarliestArrival();

                if (bestCandidate == null) {
                    decisionStable = true;
                    break;
                }

                if (activeProcess == null) {
                    activeProcess = readyQueue.pollHighestPriorityEarliestArrival();
                    activeProcessExecuted = false; // ❗ has not run yet
                    decisionStable = true;
                    break;
                }

                boolean shouldPreempt = bestCandidate.getPriority() < activeProcess.getPriority()
                        || (bestCandidate.getPriority() == activeProcess.getPriority()
                                && bestCandidate.getArrivalTime() < activeProcess.getArrivalTime())
                        || (bestCandidate.getPriority() == activeProcess.getPriority()
                                && bestCandidate.getArrivalTime() == activeProcess.getArrivalTime()
                                && bestCandidate.getName().compareTo(activeProcess.getName()) < 0);

                if (shouldPreempt) {

                    bestCandidate = readyQueue.pollHighestPriorityEarliestArrival();
                    // Log execution slice (only meaningful if it ran)
                    executionLog.addRecord(
                            activeProcess.getName(),
                            activeProcess.getStartTime(),
                            clock.getCurrentTime());
                    if (activeProcessExecuted) {
                        // ✅ Update lastTime ONLY if it actually ran
                        activeProcess.setLastTime(clock.getCurrentTime());
                    }

                    readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                    contextSwitchManager.applyContextSwitch(clock);

                    readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);
                    applyAging(readyQueue);

                    // Switch active process
                    activeProcess = bestCandidate;
                    activeProcessExecuted = false; // ❗ new process hasn’t run yet

                } else {
                    decisionStable = true;
                }
            }

            // 5️⃣ Execute 1 time unit
            if (activeProcess != null) {
                activeProcess.setStartTime(clock.getCurrentTime());
                activeProcess.setRemainingTime(activeProcess.getRemainingTime() - 1);
                clock.incrementByExecution(1);

                activeProcessExecuted = true; // ✅ it actually ran
            }

            readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);
            applyAging(readyQueue);
            // 7️⃣ Finish process
            if (activeProcess != null && activeProcess.isFinished()) {
                finishProcess(activeProcess, activeProcess.getStartTime());
                Process nextCandidate = readyQueue.pollHighestPriorityEarliestArrival();
                if (nextCandidate != null) {
                    activeProcess = nextCandidate;
                    activeProcessExecuted = false;

                } else {
                    activeProcess = null;
                    activeProcessExecuted = false;
                }

                contextSwitchManager.applyContextSwitch(clock);
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
