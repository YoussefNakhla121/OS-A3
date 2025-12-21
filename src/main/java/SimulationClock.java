
/**
 * SimulationClock - shared global CPU time manager for schedulers.
 *
 * Provides thread-safe methods to advance time by execution durations and
 * context-switch durations and a getter for the current time. Time is stored
 * as an int to match the project's `Process` timing fields.
 */
public class SimulationClock {

    private int currentTime;

    /** Create a SimulationClock starting at time 0. */
    public SimulationClock() {
        this.currentTime = 0;
    }

    /**
     * Get the current simulation time.
     *
     * @return current time (non-negative)
     */
    public synchronized int getCurrentTime() {
        return currentTime;
    }

    /**
     * Increment the clock by the given execution duration.
     *
     * @param duration non-negative execution time to add
     * @throws IllegalArgumentException if duration is negative
     */
    public synchronized void incrementByExecution(int duration) {
        if (duration < 0) throw new IllegalArgumentException("duration must be non-negative");
        currentTime += duration;
    }

    /**
     * Increment the clock by the given context switching duration.
     *
     * @param contextSwitchTime non-negative context switch time to add
     * @throws IllegalArgumentException if contextSwitchTime is negative
     */
    public synchronized void incrementByContextSwitch(int contextSwitchTime) {
        if (contextSwitchTime < 0) throw new IllegalArgumentException("contextSwitchTime must be non-negative");
        currentTime += contextSwitchTime;
    }

    /**
     * Advance the clock by a generic duration (execution or context switch).
     * This is a convenience alias for callers that do not need to distinguish types.
     *
     * @param duration non-negative amount to advance
     */
    public synchronized void advance(int duration) {
        if (duration < 0) throw new IllegalArgumentException("duration must be non-negative");
        currentTime += duration;
    }

    /**
     * Reset the clock to zero. Useful for running multiple simulations.
     */
    public synchronized void reset() {
        currentTime = 0;
    }
}
