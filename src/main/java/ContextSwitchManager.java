

public class ContextSwitchManager {

    private int contextSwitchTime;


    public ContextSwitchManager(int contextSwitchTime) {
        if (contextSwitchTime < 0) throw new IllegalArgumentException("contextSwitchTime must be non-negative");
        this.contextSwitchTime = contextSwitchTime;
    }


    public synchronized int getContextSwitchTime() {
        return contextSwitchTime;
    }

    public synchronized void setContextSwitchTime(int contextSwitchTime) {
        if (contextSwitchTime < 0) throw new IllegalArgumentException("contextSwitchTime must be non-negative");
        this.contextSwitchTime = contextSwitchTime;
    }

    public void applyContextSwitch(SimulationClock clock) {
        if (clock == null) throw new IllegalArgumentException("SimulationClock cannot be null");
        clock.incrementByContextSwitch(getContextSwitchTime());
    }


    public void applyContextSwitch(SimulationClock clock, int overrideTime) {
        if (clock == null) throw new IllegalArgumentException("SimulationClock cannot be null");
        if (overrideTime < 0) throw new IllegalArgumentException("overrideTime must be non-negative");
        clock.incrementByContextSwitch(overrideTime);
    }
}
