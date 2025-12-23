

public class SimulationClock {

    private int currentTime;

    public SimulationClock() {
        this.currentTime = 0;
    }


    public synchronized int getCurrentTime() {
        return currentTime;
    }


    public synchronized void incrementByExecution(int duration) {
        if (duration < 0) throw new IllegalArgumentException("duration must be non-negative");
        currentTime += duration;
    }

    public synchronized void incrementByContextSwitch(int contextSwitchTime) {
        if (contextSwitchTime < 0) throw new IllegalArgumentException("contextSwitchTime must be non-negative");
        currentTime += contextSwitchTime;
    }


    public synchronized void advance(int duration) {
        if (duration < 0) throw new IllegalArgumentException("duration must be non-negative");
        currentTime += duration;
    }


    public synchronized void reset() {
        currentTime = 0;
    }
}
