import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("AG Scheduler - Parameterized Tests")
public class AGSchedulerTest {

    static class AGTestCase {
        final String name;
        final List<ProcessInput> processes;
        final List<String> expectedExecutionOrder;
        final Map<String, ProcessMetrics> expectedMetrics;
        final double expectedAvgWaitingTime;
        final double expectedAvgTurnaroundTime;

        AGTestCase(String name,
                   List<ProcessInput> processes,
                   List<String> expectedExecutionOrder,
                   Map<String, ProcessMetrics> expectedMetrics,
                   double expectedAvgWaitingTime,
                   double expectedAvgTurnaroundTime) {
            this.name = name;
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
        final int quantum;

        ProcessInput(String name, int arrival, int burst, int priority, int quantum) {
            this.name = name;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.quantum = quantum;
        }
    }

    static class ProcessMetrics {
        final int waitingTime;
        final int turnaroundTime;
        final List<Integer> quantumHistory;

        ProcessMetrics(int waitingTime, int turnaroundTime, List<Integer> quantumHistory) {
            this.waitingTime = waitingTime;
            this.turnaroundTime = turnaroundTime;
            this.quantumHistory = quantumHistory;
        }
    }

    static Stream<AGTestCase> provideAGTestCases() {
        List<AGTestCase> testCases = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            for (int i = 1; i <= 6; i++) {
                String resourcePath = "AG/AG_test" + i + ".json";
                InputStream inputStream = AGSchedulerTest.class.getClassLoader()
                        .getResourceAsStream(resourcePath);

                if (inputStream != null) {
                    JsonNode root = mapper.readTree(inputStream);
                    AGTestCase testCase = parseAGTestCase(root);
                    testCases.add(testCase);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load AG test cases from JSON", e);
        }

        return testCases.stream();
    }

    static AGTestCase parseAGTestCase(JsonNode root) {
        String name = root.get("name") != null ? root.get("name").asText() : "AG Test";
        JsonNode inputNode = root.get("input");
        JsonNode outputNode = root.get("expectedOutput");

        List<ProcessInput> processes = new ArrayList<>();
        JsonNode processesNode = inputNode.get("processes");
        for (JsonNode pNode : processesNode) {
            processes.add(new ProcessInput(
                    pNode.get("name").asText(),
                    pNode.get("arrival").asInt(),
                    pNode.get("burst").asInt(),
                    pNode.get("priority").asInt(),
                    pNode.get("quantum").asInt()
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
            
            List<Integer> quantumHistory = new ArrayList<>();
            if (pNode.has("quantumHistory")) {
                for (JsonNode qNode : pNode.get("quantumHistory")) {
                    quantumHistory.add(qNode.asInt());
                }
            }
            
            metrics.put(processName, new ProcessMetrics(waitingTime, turnaroundTime, quantumHistory));
        }

        double avgWaitingTime = outputNode.get("averageWaitingTime").asDouble();
        double avgTurnaroundTime = outputNode.get("averageTurnaroundTime").asDouble();

        return new AGTestCase(name, processes, executionOrder, metrics, avgWaitingTime, avgTurnaroundTime);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideAGTestCases")
    @DisplayName("AG Scheduler - All Test Cases")
    void testAGScheduler(AGTestCase testCase) {
        List<Process> processes = new ArrayList<>();
        for (ProcessInput input : testCase.processes) {
            processes.add(new Process(
                    input.name,
                    input.arrival,
                    input.burst,
                    input.priority,
                    input.quantum
            ));
        }

        SimulationEngine engine = new SimulationEngine(processes, 0); // context switch = 1
        ExecutionLog executionLog = new ExecutionLog();

        Scheduler scheduler = new AGScheduler(
                engine.getClock(),
                engine.getContextSwitchManager(),
                executionLog
        );

        engine.run(scheduler);


        List<ExecutionLog.Record> records = executionLog.getRecords();
        List<String> actualExecutionOrder = new ArrayList<>();
        for (ExecutionLog.Record record : records) {
            actualExecutionOrder.add(record.getProcessName());
        }

        assertEquals(
                testCase.expectedExecutionOrder,
                actualExecutionOrder,
                String.format("Execution order mismatch for %s. Expected: %s, Got: %s",
                        testCase.name, testCase.expectedExecutionOrder, actualExecutionOrder)
        );

        for (Process process : engine.getProcesses()) {
            ProcessMetrics expected = testCase.expectedMetrics.get(process.getName());
            assertNotNull(expected,
                    String.format("Missing expected metrics for process %s", process.getName()));

            assertEquals(
                    expected.waitingTime,
                    process.getWaitingTime(),
                    String.format("Waiting time mismatch for %s in %s. Expected: %d, Got: %d",
                            process.getName(), testCase.name, expected.waitingTime, process.getWaitingTime())
            );

            assertEquals(
                    expected.turnaroundTime,
                    process.getTurnaroundTime(),
                    String.format("Turnaround time mismatch for %s in %s. Expected: %d, Got: %d",
                            process.getName(), testCase.name, expected.turnaroundTime, process.getTurnaroundTime())
            );

            assertEquals(
                    expected.quantumHistory,
                    process.getQuantumHistory(),
                    String.format("Quantum history mismatch for %s in %s. Expected: %s, Got: %s",
                            process.getName(), testCase.name, expected.quantumHistory, process.getQuantumHistory())
            );
        }

        double actualAvgWaitingTime = calculateAverageWaitingTime(engine.getProcesses());
        assertEquals(
                testCase.expectedAvgWaitingTime,
                actualAvgWaitingTime,
                0.01,
                String.format("Average waiting time mismatch for %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgWaitingTime, actualAvgWaitingTime)
        );

        double actualAvgTurnaroundTime = calculateAverageTurnaroundTime(engine.getProcesses());
        assertEquals(
                testCase.expectedAvgTurnaroundTime,
                actualAvgTurnaroundTime,
                0.01,
                String.format("Average turnaround time mismatch for %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgTurnaroundTime, actualAvgTurnaroundTime)
        );
    }

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
