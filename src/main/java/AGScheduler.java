import java.util.*;


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

            if(activeProcess.getQuantumHistory().isEmpty() ) {
                activeProcess.addQuantumToHistory(quantum);
            }

            int q1 = (int) Math.ceil(quantum * 0.25);  
            int q2 = (int) Math.ceil(quantum * 0.5);   

            int execTime1 = Math.min(q1, activeProcess.getRemainingTime());
            activeProcess.setRemainingTime(activeProcess.getRemainingTime() - execTime1);
            clock.incrementByExecution(execTime1);
            remainingQuantum -= execTime1;

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

            readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);

            Process higherPriority = getHigherPriorityProcess(activeProcess, readyQueue);
            
            if (higherPriority != null) {
                int remaining = remainingQuantum;
                activeProcess.setQuantum(activeProcess.getQuantum() + (int) Math.ceil(remaining / 2.0));
                activeProcess.addQuantumToHistory(activeProcess.getQuantum());
                executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                contextSwitchManager.applyContextSwitch(clock);
                readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                removeProcessFromQueue(readyQueue, higherPriority);
                activeProcess = higherPriority;
                continue; 
            }

            int priorityPhaseTime = q1;  
            int execTime2 = Math.min(priorityPhaseTime, activeProcess.getRemainingTime());
            activeProcess.setRemainingTime(activeProcess.getRemainingTime() - execTime2);
            clock.incrementByExecution(execTime2);
            remainingQuantum -= execTime2;

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

            readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);

            boolean wasPreempted = false;
            
            Process shorter = getShorterProcess(activeProcess, readyQueue);
            if (shorter != null && shorter.getRemainingTime() < activeProcess.getRemainingTime()) {
                activeProcess.setQuantum(activeProcess.getQuantum() + remainingQuantum);
                activeProcess.addQuantumToHistory(activeProcess.getQuantum());
                
                executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                contextSwitchManager.applyContextSwitch(clock);
                
                readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                
                removeProcessFromQueue(readyQueue, shorter);
                activeProcess = shorter;
                continue; 
            }
            
            int execTime3 = Math.min(remainingQuantum, activeProcess.getRemainingTime());
            activeProcess.setRemainingTime(activeProcess.getRemainingTime() - execTime3);
            clock.incrementByExecution(execTime3);
            remainingQuantum -= execTime3;
            
            readyQueue.addArrivedFrom(notArrived, clock.getCurrentTime(), true);
            
            if (activeProcess.isFinished()) {
                finishProcess(activeProcess, startTime);
                activeProcess = null;
                continue;
            }

            if (remainingQuantum == 0) {
                activeProcess.setQuantum(activeProcess.getQuantum() + 2);
                activeProcess.addQuantumToHistory(activeProcess.getQuantum());
                
                executionLog.addRecord(activeProcess.getName(), startTime, clock.getCurrentTime());
                contextSwitchManager.applyContextSwitch(clock);
                
                readyQueue.addIfArrived(activeProcess, clock.getCurrentTime());
                activeProcess = null; 
            }
        }
        
    }

    private void finishProcess(Process process, int startTime) {
        process.setCompletionTime(clock.getCurrentTime());
        process.setTurnaroundTime(process.getCompletionTime() - process.getArrivalTime());
        process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
        process.addQuantumToHistory(0);
        process.setQuantum(0);
        executionLog.addRecord(process.getName(), startTime, clock.getCurrentTime());
    }

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

    private void removeProcessFromQueue(ReadyQueue queue, Process processToRemove) {
        List<Process> processes = new ArrayList<>();
        Process p;
        while ((p = queue.poll()) != null) {
            if (p != processToRemove) {
                processes.add(p);
            }
        }
        for (Process proc : processes) {
            queue.addIfArrived(proc, clock.getCurrentTime());
        }
    }


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
