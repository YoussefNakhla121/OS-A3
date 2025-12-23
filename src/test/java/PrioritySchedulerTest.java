import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test suite for Priority Scheduler (Preemptive with
 * Aging)
 *
 * Tests include:
 * - Context switching
 * - Preemption
 * - Aging using agingInterval
 *
 * Each test validates:
 * - Execution order
 * - Waiting time
 * - Turnaround time
 * - Average waiting & turnaround time
 */
@DisplayName("Priority Scheduler - Parameterized Tests")
public class PrioritySchedulerTest {

    /* ===================== TEST CASE CONTAINER ===================== */

    static class PriorityTestCase {
        final String name;
        final int contextSwitchTime;
        final int agingInterval;
        final List<ProcessInput> processes;
        final List<String> expectedExecutionOrder;
        final Map<String, ProcessMetrics> expectedMetrics;
        final double expectedAvgWaitingTime;
        final double expectedAvgTurnaroundTime;

        PriorityTestCase(String name,
                int contextSwitchTime,
                int agingInterval,
                List<ProcessInput> processes,
                List<String> expectedExecutionOrder,
                Map<String, ProcessMetrics> expectedMetrics,
                double expectedAvgWaitingTime,
                double expectedAvgTurnaroundTime) {
            this.name = name;
            this.contextSwitchTime = contextSwitchTime;
            this.agingInterval = agingInterval;
            this.processes = processes;
            this.expectedExecutionOrder = expectedExecutionOrder;
            this.expectedMetrics = expectedMetrics;
            this.expectedAvgWaitingTime = expectedAvgWaitingTime;
            this.expectedAvgTurnaroundTime = expectedAvgTurnaroundTime;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /* ===================== INPUT REPRESENTATION ===================== */

    static class ProcessInput {
        final String name;
        final int arrival;
        final int burst;
        final int priority;

        ProcessInput(String name, int arrival, int burst, int priority) {
            this.name = name;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
        }
    }

    /* ===================== EXPECTED METRICS ===================== */

    static class ProcessMetrics {
        final int waitingTime;
        final int turnaroundTime;

        ProcessMetrics(int waitingTime, int turnaroundTime) {
            this.waitingTime = waitingTime;
            this.turnaroundTime = turnaroundTime;
        }
    }

    /* ===================== TEST CASE PROVIDER ===================== */

    static Stream<PriorityTestCase> providePriorityTestCases() {
        List<PriorityTestCase> testCases = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            for (int i = 1; i <= 6; i++) {
                String resourcePath = "Other_Schedulers/test_" + i + ".json";
                InputStream inputStream = PrioritySchedulerTest.class.getClassLoader()
                        .getResourceAsStream(resourcePath);

                if (inputStream != null) {
                    JsonNode root = mapper.readTree(inputStream);
                    PriorityTestCase testCase = parsePriorityTestCase(root);
                    testCases.add(testCase);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Priority test cases from JSON", e);
        }

        return testCases.stream();
    }

    static PriorityTestCase parsePriorityTestCase(JsonNode root) {
        String name = root.get("name") != null ? root.get("name").asText() : "Priority Test";
        JsonNode inputNode = root.get("input");
        JsonNode outputNode = root.get("expectedOutput").get("Priority");

        int contextSwitch = inputNode.get("contextSwitch").asInt();
        int agingInterval = inputNode.has("agingInterval") ? inputNode.get("agingInterval").asInt() : 0;

        List<ProcessInput> processes = new ArrayList<>();
        JsonNode processesNode = inputNode.get("processes");
        for (JsonNode pNode : processesNode) {
            processes.add(new ProcessInput(
                    pNode.get("name").asText(),
                    pNode.get("arrival").asInt(),
                    pNode.get("burst").asInt(),
                    pNode.get("priority").asInt()
            ));
        }

        List<String> executionOrder = new ArrayList<>();
        for (JsonNode node : outputNode.get("executionOrder")) {
            executionOrder.add(node.asText());
        }

        Map<String, ProcessMetrics> metrics = new HashMap<>();
        JsonNode resultsNode = outputNode.get("processResults");
        for (JsonNode pNode : resultsNode) {
            String processName = pNode.get("name").asText();
            int waitingTime = pNode.get("waitingTime").asInt();
            int turnaroundTime = pNode.get("turnaroundTime").asInt();
            metrics.put(processName, new ProcessMetrics(waitingTime, turnaroundTime));
        }

        double avgWaitingTime = outputNode.get("averageWaitingTime").asDouble();
        double avgTurnaroundTime = outputNode.get("averageTurnaroundTime").asDouble();

        return new PriorityTestCase(name, contextSwitch, agingInterval, processes, executionOrder, metrics,
                avgWaitingTime, avgTurnaroundTime);
    }

    /* ===================== PARAMETERIZED TEST ===================== */

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePriorityTestCases")
    @DisplayName("Priority Scheduler - All Test Cases")
    void testPriorityScheduler(PriorityTestCase testCase) {

        /* ===================== SETUP ===================== */

        List<Process> processes = new ArrayList<>();
        for (ProcessInput input : testCase.processes) {
            processes.add(new Process(
                    input.name,
                    input.arrival,
                    input.burst,
                    input.priority,
                    0));
        }

        SimulationEngine engine = new SimulationEngine(processes, testCase.contextSwitchTime);

        ExecutionLog executionLog = new ExecutionLog();

        Scheduler scheduler = new PriorityScheduler(
                engine.getClock(),
                engine.getContextSwitchManager(),
                executionLog,
                testCase.agingInterval);

        /* ===================== EXECUTE ===================== */

        engine.run(scheduler);

        /* ===================== VERIFY ===================== */

        // 1️⃣ Execution order
        List<String> actualExecutionOrder = new ArrayList<>();
        for (ExecutionLog.Record record : executionLog.getRecords()) {
            actualExecutionOrder.add(record.getProcessName());
        }

        assertEquals(
                testCase.expectedExecutionOrder,
                actualExecutionOrder,
                String.format("Execution order mismatch in %s", testCase.name));

        // 2️⃣ Per-process metrics
        for (Process process : engine.getProcesses()) {
            ProcessMetrics expected = testCase.expectedMetrics.get(process.getName());

            assertNotNull(expected,
                    "Missing expected metrics for process " + process.getName());

            assertEquals(
                    expected.waitingTime,
                    process.getWaitingTime(),
                    "Waiting time mismatch for " + process.getName());

            assertEquals(
                    expected.turnaroundTime,
                    process.getTurnaroundTime(),
                    "Turnaround time mismatch for " + process.getName());
        }

        // 3️⃣ Average waiting time
        assertEquals(
                testCase.expectedAvgWaitingTime,
                calculateAverageWaitingTime(engine.getProcesses()),
                0.01);

        // 4️⃣ Average turnaround time
        assertEquals(
                testCase.expectedAvgTurnaroundTime,
                calculateAverageTurnaroundTime(engine.getProcesses()),
                0.01);
    }

    /* ===================== HELPERS ===================== */

    private double calculateAverageWaitingTime(List<Process> processes) {
        return processes.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageTurnaroundTime(List<Process> processes) {
        return processes.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0.0);
    }
}
