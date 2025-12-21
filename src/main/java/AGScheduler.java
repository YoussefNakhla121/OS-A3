import java.util.*;

/**
 * AGScheduler
 *
 * Implements Adaptive General (AG) Scheduling:
 *  - FCFS for first 25% of quantum (non-preemptive)
 *  - Non-preemptive Priority for next 25% (can be preempted at start of this phase)
 *  - Preemptive Shortest Job First (SJF) for remaining 50% (can be preempted anytime)
 *
 * Quantum update rules:
 *  - If quantum fully used: quantum += 2
 *  - If preempted in Priority phase: quantum += ceil(remaining/2)
 *  - If preempted in SJF phase: quantum += remaining
 *  - If process finishes: quantum = 0
 */
public class AGScheduler implements Scheduler {

    private final SimulationClock clock;
    private final ContextSwitchManager contextSwitchManager;
    private final ExecutionLog executionLog;

    public AGScheduler(SimulationClock clock, ContextSwitchManager contextSwitchManager, ExecutionLog executionLog) {
        this.clock = Objects.requireNonNull(clock);
        this.contextSwitchManager = Objects.requireNonNull(contextSwitchManager);
        this.executionLog = Objects.requireNonNull(executionLog);
    }

    @Override
    public void run(List<Process> processes) {

        ReadyQueue readyQueue = new ReadyQueue();
        List<Process> notArrived = new ArrayList<>(processes);
        Process activeProcess = null;

        while (!readyQueue.isEmpty() || !notArrived.isEmpty() || activeProcess != null) {

            int currentTime = clock.getCurrentTime();
            readyQueue.addArrivedFrom(notArrived, currentTime, true);

            /* ================= CPU IDLE ================= */
            if (readyQueue.isEmpty() && activeProcess == null) {
                if (notArrived.isEmpty()) {
                    break;
                }
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : notArrived) {
                    nextArrival = Math.min(nextArrival, p.getArrivalTime());
                }
                clock.incrementByExecution(nextArrival - currentTime);
                continue;
            }

            // Get a process to execute
            if (activeProcess == null) {
                activeProcess = readyQueue.poll();
                if (activeProcess == null) {
                    continue;
                }
            }

            int startTime = clock.getCurrentTime();
            int quantum = activeProcess.getQuantum();
            int remainingQuantum = quantum;

            // Add quantum to history at the start of execution
            if(activeProcess.getQuantumHistory().isEmpty() ) {
                activeProcess.addQuantumToHistory(quantum);
            }

            // Calculate phase boundaries
            int q1 = (int) Math.ceil(quantum * 0.25);  // First 25% - FCFS
            int q2 = (int) Math.ceil(quantum * 0.5);   // Up to 50% - Priority phase ends here

            /* ================= PHASE 1: FCFS (25%) ================= */
            int execTime1 = Math.min(q1, activeProcess.getRemainingTime());
            activeProcess.setRemainingTime(activeProcess.getRemainingTime() - execTime1);
            clock.incrementByExecution(execTime1);
            remainingQuantum -= execTime1;

            // Check if process finished during FCFS phase
            if (activeProcess.isFinished()) {
                activeProcess.setCompletionTime(clock.getCurrentTime());
                activeProcess.setTurnaroundTime(activeProcess.getCompletionTime() - activeProcess.getArrivalTime());
                activeProcess.setWaitingTime(activeProcess.getTurnaroundTime() - activeProcess.getBurstTime());
                activeProcess.addQuantumToHistory(0);
                activeProcess.setQuantum(0);
                executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                activeProcess = null;
                continue;
            }

            // Add newly arrived processes
            readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);

            /* ================= PHASE 2: NON-PREEMPTIVE PRIORITY (25%) ================= */
            // Check for higher priority activeProcess at the START of this phase
            Process higherPriority = getHigherPriorityProcess(activeProcess, readyQueue);
            
            if (higherPriority != null) {
                // Preempted during Priority phase
                int remaining = remainingQuantum;
                activeProcess.setQuantum(activeProcess.getQuantum() + (int) Math.ceil(remaining / 2.0));
                activeProcess.addQuantumToHistory(activeProcess.getQuantum());
                executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                contextSwitchManager.applyContextSwitch(clock);
                readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                // Remove the higher priority activeProcess from queue and make it active
                removeProcessFromQueue(readyQueue, higherPriority);
                activeProcess = higherPriority;
                continue; // Start fresh with new activeProcess
            }

            // Execute Priority phase (non-preemptive, so execute full 25% if possible)
            int priorityPhaseTime = q1;  // Time for Priority phase
            int execTime2 = Math.min(priorityPhaseTime, activeProcess.getRemainingTime());
            activeProcess.setRemainingTime(activeProcess.getRemainingTime() - execTime2);
            clock.incrementByExecution(execTime2);
            remainingQuantum -= execTime2;

            // Check if activeProcess finished during Priority phase
            if (activeProcess.isFinished()) {
                activeProcess.setCompletionTime(clock.getCurrentTime());
                activeProcess.setTurnaroundTime(activeProcess.getCompletionTime() - activeProcess.getArrivalTime());
                activeProcess.setWaitingTime(activeProcess.getTurnaroundTime() - activeProcess.getBurstTime());
                activeProcess.addQuantumToHistory(0);
                activeProcess.setQuantum(0);
                executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                activeProcess = null;
                continue;
            }

            // Add newly arrived processes before SJF phase
            readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);

            /* ================= PHASE 3: PREEMPTIVE SJF (50%) ================= */
            // Execute remaining quantum time, but check for preemption continuously
            boolean wasPreempted = false;
            
                // Check for shorter job in ready queue
                Process shorter = getShorterProcess(activeProcess, readyQueue);
                
                if (shorter != null && shorter.getRemainingTime() < activeProcess.getRemainingTime()) {
                    // Preempted during SJF phase
                    activeProcess.setQuantum(activeProcess.getQuantum() + remainingQuantum);
                    activeProcess.addQuantumToHistory(activeProcess.getQuantum());
                    executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                    contextSwitchManager.applyContextSwitch(clock);
                    readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                    // Remove the shorter activeProcess from queue and make it active
                    removeProcessFromQueue(readyQueue, shorter);
                    activeProcess = shorter;
                    wasPreempted = true;
                    continue;
                }
                
                // Execute one time unit in SJF phase
                int execTime3 = Math.min(q2, activeProcess.getRemainingTime());
                activeProcess.setRemainingTime(activeProcess.getRemainingTime() - execTime3);
                clock.incrementByExecution(execTime3);
                remainingQuantum -= execTime3;
                
                // Add newly arrived processes
                readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);
                
                if (activeProcess.isFinished()) {
                    activeProcess.setCompletionTime(clock.getCurrentTime());
                    activeProcess.setTurnaroundTime(activeProcess.getCompletionTime() - activeProcess.getArrivalTime());
                    activeProcess.setWaitingTime(activeProcess.getTurnaroundTime() - activeProcess.getBurstTime());
                    activeProcess.addQuantumToHistory(0);
                    activeProcess.setQuantum(0);
                    executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                    activeProcess = null;
                    continue;
                }

            // If we broke out due to preemption, continue to restart with new activeProcess
            if (wasPreempted) {
                continue;
            }

            // If process finished, we already handled it above
            if (activeProcess == null) {
                continue;
            }

            /* ================= QUANTUM FULLY USED ================= */
            if (!activeProcess.isFinished() && remainingQuantum == 0) {
                // Process used all quantum but still has work
                activeProcess.setQuantum(activeProcess.getQuantum() + 2);
                executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                contextSwitchManager.applyContextSwitch(clock);
                readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                activeProcess = null;
            }
        }
    }

    /* ================= HELPER METHODS ================= */

    /**
     * Finishes a process by setting completion metrics and logging execution.
     */
    private void finishProcess(Process process, int startTime) {
        process.setCompletionTime(clock.getCurrentTime());
        process.setTurnaroundTime(process.getCompletionTime() - process.getArrivalTime());
        process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
        process.addQuantumToHistory(0);
        process.setQuantum(0);
        executionLog.addRecord(process.getName(), startTime, clock.getCurrentTime());
    }

    /**
     * Returns a process with higher priority (lower priority number) than current,
     * or null if none exists.
     */
    private Process getHigherPriorityProcess(Process current, ReadyQueue queue) {
        Process best = null;
        for (Process p : queue.asList()) {
            if (p.getPriority() < current.getPriority()) {
                if (best == null || p.getPriority() < best.getPriority()) {
                    best = p;
                }
            }
        }
        return best;
    }

    /**
     * Returns a process with shorter remaining time than current,
     * or null if none exists.
     */
    private Process getShorterProcess(Process current, ReadyQueue queue) {
        Process shortest = null;
        for (Process p : queue.asList()) {
            if (p.getRemainingTime() < current.getRemainingTime()) {
                if (shortest == null || p.getRemainingTime() < shortest.getRemainingTime()) {
                    shortest = p;
                }
            }
        }
        return shortest;
    }

    /**
     * Removes a specific process from the ready queue by polling all processes
     * and re-adding those that shouldn't be removed.
     */
    private void removeProcessFromQueue(ReadyQueue queue, Process processToRemove) {
        List<Process> processes = new ArrayList<>();
        Process p;
        while ((p = queue.poll()) != null) {
            if (p != processToRemove) {
                processes.add(p);
            }
        }
        // Re-add all processes except the one we removed
        for (Process proc : processes) {
            queue.addIfArrived(proc, clock.getCurrentTime());
        }
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
