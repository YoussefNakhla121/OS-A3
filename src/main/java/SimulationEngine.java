
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class SimulationEngine {

    private final List<Process> processes;
    private final SimulationClock clock;
    private final ContextSwitchManager contextSwitchManager;


    public SimulationEngine(List<Process> processes, int contextSwitchTime) {
        this(processes, new SimulationClock(), new ContextSwitchManager(contextSwitchTime));
    }


    public SimulationEngine(List<Process> processes, SimulationClock clock, ContextSwitchManager contextSwitchManager) {
        Objects.requireNonNull(processes, "processes cannot be null");
        this.processes = new ArrayList<>(processes);
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
        this.contextSwitchManager = Objects.requireNonNull(contextSwitchManager, "contextSwitchManager cannot be null");
    }

    public List<Process> getProcesses() {
        return Collections.unmodifiableList(processes);
    }

    public SimulationClock getClock() {
        return clock;
    }

    public ContextSwitchManager getContextSwitchManager() {
        return contextSwitchManager;
    }


    public void run(Scheduler scheduler) {
        Objects.requireNonNull(scheduler, "scheduler cannot be null");

        clock.reset();

        List<Process> toSchedule = new ArrayList<>();
        for (Process p : processes) {
            toSchedule.add(p);
        }

        scheduler.run(toSchedule);
    }
}
