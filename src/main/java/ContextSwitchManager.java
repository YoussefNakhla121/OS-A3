
/**
 * ContextSwitchManager - manages context switching time and applies it to
 * the shared SimulationClock.
 *
 * This class stores a configurable context switch duration and provides
 * methods to apply that duration (or an override) to a provided
 * `SimulationClock` instance. It contains no scheduling logic and is
 * intended to be reusable by different scheduling algorithms.
 */
public class ContextSwitchManager {

    private int contextSwitchTime;

    /**
     * Create a ContextSwitchManager with the given context switch time.
     *
     * @param contextSwitchTime non-negative context switch duration
     * @throws IllegalArgumentException if contextSwitchTime is negative
     */
    public ContextSwitchManager(int contextSwitchTime) {
        if (contextSwitchTime < 0) throw new IllegalArgumentException("contextSwitchTime must be non-negative");
        this.contextSwitchTime = contextSwitchTime;
    }

    /**
     * Get the configured context switch time.
     *
     * @return context switch time (non-negative)
     */
    public synchronized int getContextSwitchTime() {
        return contextSwitchTime;
    }

    /**
     * Update the configured context switch time.
     *
     * @param contextSwitchTime non-negative new context switch time
     * @throws IllegalArgumentException if contextSwitchTime is negative
     */
    public synchronized void setContextSwitchTime(int contextSwitchTime) {
        if (contextSwitchTime < 0) throw new IllegalArgumentException("contextSwitchTime must be non-negative");
        this.contextSwitchTime = contextSwitchTime;
    }

    /**
     * Apply the configured context switch time to the provided SimulationClock.
     *
     * @param clock the shared SimulationClock (must not be null)
     * @throws IllegalArgumentException if clock is null
     */
    public void applyContextSwitch(SimulationClock clock) {
        if (clock == null) throw new IllegalArgumentException("SimulationClock cannot be null");
        clock.incrementByContextSwitch(getContextSwitchTime());
    }

    /**
     * Apply a custom context switch time (override) to the provided SimulationClock.
     *
     * @param clock the shared SimulationClock (must not be null)
     * @param overrideTime non-negative override duration to apply
     * @throws IllegalArgumentException if clock is null or overrideTime is negative
     */
    public void applyContextSwitch(SimulationClock clock, int overrideTime) {
        if (clock == null) throw new IllegalArgumentException("SimulationClock cannot be null");
        if (overrideTime < 0) throw new IllegalArgumentException("overrideTime must be non-negative");
        clock.incrementByContextSwitch(overrideTime);
    }
}
