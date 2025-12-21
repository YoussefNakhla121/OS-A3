
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SimulationEngine initializes simulation components and runs a Scheduler.
 *
 * Responsibilities:
 * - Hold the process list used for the simulation (copy of input list)
 * - Provide a shared SimulationClock instance
 * - Provide a ContextSwitchManager configured with a context switch time
 * - Invoke the provided Scheduler's {@code run} method with the process list
 *
 * The engine intentionally contains no scheduling logic so it can work with
 * any implementation of the {@code Scheduler} interface.
 */
public class SimulationEngine {

    private final List<Process> processes;
    private final SimulationClock clock;
    private final ContextSwitchManager contextSwitchManager;

    /**
     * Create a SimulationEngine using a copy of the provided process list and
     * a newly constructed SimulationClock and ContextSwitchManager.
     *
     * @param processes list of processes to schedule (must not be null)
     * @param contextSwitchTime configured context switch time (non-negative)
     */
    public SimulationEngine(List<Process> processes, int contextSwitchTime) {
        this(processes, new SimulationClock(), new ContextSwitchManager(contextSwitchTime));
    }

    /**
     * Create a SimulationEngine using provided helper instances. Callers can
     * reuse their own SimulationClock or ContextSwitchManager if desired.
     *
     * @param processes list of processes to schedule (must not be null)
     * @param clock shared SimulationClock (must not be null)
     * @param contextSwitchManager shared ContextSwitchManager (must not be null)
     */
    public SimulationEngine(List<Process> processes, SimulationClock clock, ContextSwitchManager contextSwitchManager) {
        Objects.requireNonNull(processes, "processes cannot be null");
        this.processes = new ArrayList<>(processes);
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
        this.contextSwitchManager = Objects.requireNonNull(contextSwitchManager, "contextSwitchManager cannot be null");
    }

    /** Return an unmodifiable view of the processes (copy made at construction). */
    public List<Process> getProcesses() {
        return Collections.unmodifiableList(processes);
    }

    /** Return the shared SimulationClock instance. */
    public SimulationClock getClock() {
        return clock;
    }

    /** Return the ContextSwitchManager instance. */
    public ContextSwitchManager getContextSwitchManager() {
        return contextSwitchManager;
    }

    /**
     * Run the provided Scheduler. The engine resets the simulation clock
     * to zero before invoking the scheduler to ensure a fresh simulation.
     * The scheduler receives a mutable copy of the process list so it may
     * modify per-process fields (completion time, waiting time, etc.) as
     * needed.
     *
     * @param scheduler scheduler implementation to execute (must not be null)
     */
    public void run(Scheduler scheduler) {
        Objects.requireNonNull(scheduler, "scheduler cannot be null");

        // Reset clock for a fresh simulation run
        clock.reset();

        // Provide the scheduler with a fresh mutable list derived from the
        // engine's process list so the engine's internal list is preserved.
        List<Process> toSchedule = new ArrayList<>();
        for (Process p : processes) {
            // shallow copy reference is fine; Process objects hold mutable state
            toSchedule.add(p);
        }

        scheduler.run(toSchedule);
    }
}
