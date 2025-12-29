import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * ReadyQueue - a FIFO queue for ready processes used by CPU schedulers.
 *
 * Features:
 * - Internally uses a FIFO queue (LinkedList)
 * - addIfArrived: adds a process only if its arrivalTime <= currentTime
 * - addArrivedFrom: adds all processes from a collection whose arrivalTime <=
 * currentTime
 * - poll / peek to obtain the next process
 * - isEmpty and size for checks
 *
 * This class is intentionally small and scheduler-agnostic so it can be reused
 * by different scheduling algorithms (FCFS, RR, etc.).
 */
public class ReadyQueue {

    private final Queue<Process> queue;

    /** Create an empty ReadyQueue. */
    public ReadyQueue() {
        this.queue = new LinkedList<>();
    }

    /**
     * Add the process to the ready queue if its arrival time is <= currentTime.
     *
     * @param p           Process to consider adding
     * @param currentTime Current simulation time
     * @return true if the process was added, false otherwise
     * @throws IllegalArgumentException if p is null
     */
    public synchronized boolean addIfArrived(Process p, int currentTime) {
        if (p == null)
            throw new IllegalArgumentException("Process cannot be null");
        if (p.getArrivalTime() <= currentTime) {
            queue.add(p);
            return true;
        }
        return false;
    }

    /**
     * Scan the given collection and add every process whose arrival time is
     * <= currentTime to the ready queue.
     *
     * If {@code removeAdded} is true, the method will remove added processes
     * from the provided collection using the collection's iterator. This is
     * convenient when callers maintain a separate "not-yet-arrived" list and
     * want to move processes into the ready queue.
     *
     * @param processes   Collection of candidate processes (may be modified
     *                    when {@code removeAdded} is true)
     * @param currentTime Current simulation time
     * @param removeAdded Whether to remove added processes from the input
     *                    collection
     * @return the number of processes added to the ready queue
     * @throws IllegalArgumentException if processes is null
     */
    public synchronized int addArrivedFrom(Collection<Process> processes, int currentTime, boolean removeAdded) {
        if (processes == null)
            throw new IllegalArgumentException("processes cannot be null");
        int added = 0;
        Iterator<Process> it = processes.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            if (p == null)
                continue;
            if (p.getArrivalTime() <= currentTime) {
                queue.add(p);
                added++;
                if (removeAdded) {
                    it.remove();
                }
            }
        }
        return added;
    }

    /**
     * Finds and removes the process that arrived earliest.
     * If multiple processes arrived at the same time, it picks the one
     * with the highest priority (lowest priority value).
     *
     * @return The selected Process, or null if the queue is empty.
     */
    public synchronized Process pollEarliestArrivalHighestPriority() {
        if (queue.isEmpty())
            return null;

        Process bestMatch = null;

        // Iterate through the current queue to find the best candidate
        for (Process p : queue) {
            if (bestMatch == null) {
                bestMatch = p;
                continue;
            }

            // Rule 1: Check Arrival Time (Earliest wins)
            if (p.getArrivalTime() < bestMatch.getArrivalTime()) {
                bestMatch = p;
            }
            // Rule 2: If Arrival Times are equal, check Priority (Lower value wins)
            else if (p.getArrivalTime() == bestMatch.getArrivalTime()) {
                if (p.getPriority() < bestMatch.getPriority()) {
                    bestMatch = p;
                }
            }
        }

        // Remove the found process from the internal queue and return it
        if (bestMatch != null) {
            queue.remove(bestMatch);
        }

        return bestMatch;
    }

    /**
     * Finds and removes the process with the highest priority (lowest priority
     * value).
     * If multiple processes have the same priority, it selects the one
     * that arrived earliest.
     *
     * @return The selected Process, or null if the queue is empty.
     */
    public synchronized Process pollHighestPriorityEarliestArrival() {
        if (queue.isEmpty())
            return null;

        Process bestMatch = null;

        // Iterate through the current queue to find the best candidate
        for (Process p : queue) {
            if (bestMatch == null) {
                bestMatch = p;
                continue;
            }

            // Rule 1: Check Priority (Lower value wins)
            if (p.getPriority() < bestMatch.getPriority()) {
                bestMatch = p;
            }
            // Rule 2: If priorities are equal, check Arrival Time (Earliest wins)
            else if (p.getPriority() == bestMatch.getPriority()) {
                if (p.getArrivalTime() < bestMatch.getArrivalTime()) {
                    bestMatch = p;
                } else if (p.getArrivalTime() == bestMatch.getArrivalTime()
                        && p.getName().compareTo(bestMatch.getName()) < 0) {
                    bestMatch = p;
                }
            }
        }

        // Remove the found process from the internal queue and return it
        if (bestMatch != null) {
            queue.remove(bestMatch);
        }

        return bestMatch;
    }

    // Peek version: returns highest-priority earliest-arrival without removing
    public synchronized Process peekHighestPriorityEarliestArrival() {
        if (queue.isEmpty())
            return null;

        Process bestMatch = null;
        for (Process p : queue) {
            if (bestMatch == null) {
                bestMatch = p;
                continue;
            }

            if (p.getPriority() < bestMatch.getPriority()
                    || (p.getPriority() == bestMatch.getPriority()
                            && p.getArrivalTime() < bestMatch.getArrivalTime())
                    || (p.getPriority() == bestMatch.getPriority()
                            && p.getArrivalTime() == bestMatch.getArrivalTime()
                            && p.getName().compareTo(bestMatch.getName()) < 0)) {
                bestMatch = p;
            }
        }
        return bestMatch;
    }

    /**
     * Remove and return the next process from the ready queue, or null if empty.
     */
    public synchronized Process poll() {
        return queue.poll();
    }

    /**
     * Return but do not remove the next process, or null if empty.
     */
    public synchronized Process peek() {
        return queue.peek();
    }

    /**
     * Check whether the ready queue is empty.
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Return number of processes currently in the ready queue.
     */
    public synchronized int size() {
        return queue.size();
    }

    /**
     * Remove all processes from the ready queue.
     */
    public synchronized void clear() {
        queue.clear();
    }

    public List<Process> asList() {
        return new ArrayList<>(queue);
    }
}
