
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ExecutionLog records CPU execution history as a sequence of records.
 *
 * Each record contains the process name, start time and end time. The log
 * preserves insertion order so callers can print the execution order in
 * sequence. Methods are synchronized for safe use by multiple schedulers.
 */
public class ExecutionLog {

    /** Immutable record describing a single execution slice. */
    public static final class Record {
        private final String processName;
        private final int startTime;
        private final int endTime;

        public Record(String processName, int startTime, int endTime) {
            this.processName = Objects.requireNonNull(processName, "processName");
            if (startTime < 0 || endTime < 0) throw new IllegalArgumentException("times must be non-negative");
            if (endTime < startTime) throw new IllegalArgumentException("endTime must be >= startTime");
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getProcessName() { return processName; }
        public int getStartTime() { return startTime; }
        public int getEndTime() { return endTime; }

        @Override
        public String toString() {
            return processName + "[" + startTime + "-" + endTime + "]";
        }
    }

    private final List<Record> records = new ArrayList<>();

    /** Add a new execution record to the log. */
    public synchronized void addRecord(String processName, int startTime, int endTime) {
        records.add(new Record(processName, startTime, endTime));
    }

    /** Add an existing Record object to the log. */
    public synchronized void addRecord(Record record) {
        records.add(Objects.requireNonNull(record, "record"));
    }

    /** Return an immutable snapshot of the records in insertion order. */
    public synchronized List<Record> getRecords() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    /**
     * Print the execution order in sequence to standard output. Each line
     * contains the process name and its start/end times in chronological order
     * (the order records were added).
     */
    public synchronized void printExecutionOrder() {
        if (records.isEmpty()) {
            System.out.println("(no execution records)");
            return;
        }
        for (Record r : records) {
            System.out.println(r.getProcessName() + ": start=" + r.getStartTime() + ", end=" + r.getEndTime());
        }
    }

    /**
     * Return a compact textual representation of the execution sequence.
     * Example: "P1[0-3] -> P2[3-5] -> P1[5-8]"
     */
    public synchronized String getExecutionSequenceString() {
        if (records.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Record r : records) {
            if (!first) sb.append(" -> ");
            sb.append(r.toString());
            first = false;
        }
        return sb.toString();
    }

    /**
     * Return the execution sequence as a list of process names in order.
     * Example: ["P1", "P2", "P1", "P3"]
     */
    public synchronized List<String> getExecutionSequence() {
        List<String> sequence = new ArrayList<>();
        for (Record r : records) {
            sequence.add(r.getProcessName());
        }
        return sequence;
    }

    /** Clear all records from the log. Useful when running multiple simulations. */
    public synchronized void clear() {
        records.clear();
    }
}
