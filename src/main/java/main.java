import java.util.*;

public class main {

    public static void main(String[] args) {
        List<ProcessInput> processesInput = Arrays.asList(
            new ProcessInput("P1", 0, 20, 5, 8),
            new ProcessInput("P2", 3, 4, 3, 6),
            new ProcessInput("P3", 6, 3, 4, 5),
            new ProcessInput("P4", 10, 2, 2, 4),
            new ProcessInput("P5", 15, 5, 6, 7),
            new ProcessInput("P6", 20, 6, 1, 3));
        
        List<Process> processes = new ArrayList<>();
        for (ProcessInput input : processesInput) {
            processes.add(new Process(
                    input.name,
                    input.arrival,
                    input.burst,
                    input.priority,
                    input.quantum
            ));
        }
        ExecutionLog log = new ExecutionLog();
        SimulationClock clock = new SimulationClock();
        ContextSwitchManager contextSwitchManager = new ContextSwitchManager(0);

        AGScheduler scheduler = new AGScheduler(clock, contextSwitchManager, log);
        scheduler.run(processes);

        scheduler.printExecutionOrder();
    }
}

class ProcessInput {
        final String name;
        final int arrival;
        final int burst;
        final int priority;
        final int quantum;

        ProcessInput(String name, int arrival, int burst, int priority, int quantum) {
            this.name = name;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.quantum = quantum;
        }
}