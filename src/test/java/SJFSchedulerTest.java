import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("SJF Scheduler - Parameterized Tests")
public class SJFSchedulerTest {


    static class SJFTestCase {
        final String name;
        final int contextSwitchTime;
        final List<ProcessInput> processes;
        final List<String> expectedExecutionOrder;
        final Map<String, ProcessMetrics> expectedMetrics;
        final double expectedAvgWaitingTime;
        final double expectedAvgTurnaroundTime;

        SJFTestCase(String name, int contextSwitchTime,
                        List<ProcessInput> processes,
                        List<String> expectedExecutionOrder,
                        Map<String, ProcessMetrics> expectedMetrics,
                        double expectedAvgWaitingTime,
                        double expectedAvgTurnaroundTime) {
            this.name = name;
            this.contextSwitchTime = contextSwitchTime;
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

    static class ProcessMetrics {
        final int waitingTime;
        final int turnaroundTime;

        ProcessMetrics(int waitingTime, int turnaroundTime) {
            this.waitingTime = waitingTime;
            this.turnaroundTime = turnaroundTime;
        }
    }

    static Stream<SJFTestCase> provideSJFTestCases() {
        List<SJFTestCase> testCases = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            for (int i = 1; i <= 6; i++) {
                String resourcePath = "Other_Schedulers/test_" + i + ".json";
                InputStream inputStream = SJFSchedulerTest.class.getClassLoader()
                        .getResourceAsStream(resourcePath);

                if (inputStream != null) {
                    JsonNode root = mapper.readTree(inputStream);
                    SJFTestCase testCase = parseSJFTestCase(root);
                    testCases.add(testCase);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SJF test cases from JSON", e);
        }

        return testCases.stream();
    }

    static SJFTestCase parseSJFTestCase(JsonNode root) {
        String name = root.get("name") != null ? root.get("name").asText() : "SJF Test";
        JsonNode inputNode = root.get("input");
        JsonNode outputNode = root.get("expectedOutput").get("SJF");

        int contextSwitch = inputNode.get("contextSwitch").asInt();


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

        return new SJFTestCase(name, contextSwitch, processes, executionOrder, metrics, 
                avgWaitingTime, avgTurnaroundTime);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideSJFTestCases")
    @DisplayName("SJF Scheduler - All Test Cases")
    void testSJFScheduler(SJFTestCase testCase) {
        // Create processes from test case input
        List<Process> processes = new ArrayList<>();
        for (ProcessInput input : testCase.processes) {
            Process p = new Process(input.name, input.arrival, input.burst, input.priority,0);
            processes.add(p);
        }

        SJFScheduler scheduler = new SJFScheduler(testCase.contextSwitchTime);
        scheduler.run(processes);

        List<String> actualExecutionOrder = scheduler.getExecutionLog().getExecutionSequence();
        assertEquals(testCase.expectedExecutionOrder, actualExecutionOrder,
                String.format("Execution order mismatch in %s\nExpected: %s\nActual: %s",
                        testCase.name, testCase.expectedExecutionOrder, actualExecutionOrder));

        for (Process process : processes) {
            String processName = process.getName();
            ProcessMetrics expected = testCase.expectedMetrics.get(processName);

            assertNotNull(expected, "No expected metrics found for process: " + processName);

            assertEquals(expected.waitingTime, process.getWaitingTime(),
                    String.format("Waiting time mismatch for %s in %s. Expected: %d, Got: %d",
                            processName, testCase.name, expected.waitingTime, process.getWaitingTime()));

            assertEquals(expected.turnaroundTime, process.getTurnaroundTime(),
                    String.format("Turnaround time mismatch for %s in %s. Expected: %d, Got: %d",
                            processName, testCase.name, expected.turnaroundTime, process.getTurnaroundTime()));
        }

        double actualAvgWaitingTime = calculateAverageWaitingTime(processes);
        assertEquals(testCase.expectedAvgWaitingTime, actualAvgWaitingTime, 0.01,
                String.format("Average waiting time mismatch in %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgWaitingTime, actualAvgWaitingTime));

        double actualAvgTurnaroundTime = calculateAverageTurnaroundTime(processes);
        assertEquals(testCase.expectedAvgTurnaroundTime, actualAvgTurnaroundTime, 0.01,
                String.format("Average turnaround time mismatch in %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgTurnaroundTime, actualAvgTurnaroundTime));
    }


    private double calculateAverageWaitingTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return 0;
        }
        double total = processes.stream().mapToInt(Process::getWaitingTime).sum();
        return total / processes.size();
    }


    private double calculateAverageTurnaroundTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return 0;
        }
        double total = processes.stream().mapToInt(Process::getTurnaroundTime).sum();
        return total / processes.size();
    }
}
