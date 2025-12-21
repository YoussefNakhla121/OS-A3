import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test suite for AGScheduler using parameterized tests.
 *
 * Tests all 6 provided AG scheduler test cases covering:
 * - Basic adaptive scheduling with varying priorities
 * - All processes arriving simultaneously
 * - Multiple processes with complex scheduling
 * - Mixed scenario with varied bursts and priorities
 * - Large dataset comprehensive test
 * - Complex scenario with 7 processes
 *
 * Each test validates:
 * - Execution order (exact sequence)
 * - Per-process waiting times and turnaround times
 * - Quantum history for each process
 * - Average waiting time and turnaround time
 */
@DisplayName("AG Scheduler - Parameterized Tests")
public class AGSchedulerTest {

    /**
     * Test container for a single AG test case with all inputs and expected outputs.
     */
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

    /**
     * Input representation for a single process.
     */
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

    /**
     * Expected metrics for a single process.
     */
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

    /**
     * Provides all 6 test cases for the AG scheduler.
     */
    static Stream<AGTestCase> provideAGTestCases() {
        return Stream.of(
                createTestCase1(),
                createTestCase2(),
                createTestCase3(),
                createTestCase4(),
                createTestCase5(),
                createTestCase6()
        );
    }

    /**
     * Test Case 1: Basic adaptive scheduling with varying priorities
     */
    static AGTestCase createTestCase1() {
        return new AGTestCase(
                "Test Case 1: Basic adaptive scheduling",
                Arrays.asList(
                        new ProcessInput("P1", 0, 17, 4, 7),
                        new ProcessInput("P2", 2, 6, 7, 9),
                        new ProcessInput("P3", 5, 11, 3, 4),
                        new ProcessInput("P4", 15, 4, 6, 6)
                ),
                Arrays.asList("P1", "P2", "P3", "P2", "P1", "P3", "P4", "P3", "P1", "P4"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(19, 36, Arrays.asList(7, 10, 14, 0)));
                    put("P2", new ProcessMetrics(4, 10, Arrays.asList(9, 12, 0)));
                    put("P3", new ProcessMetrics(10, 21, Arrays.asList(4, 6, 8, 0)));
                    put("P4", new ProcessMetrics(19, 23, Arrays.asList(6, 8, 0)));
                }},
                13.0,
                22.5
        );
    }

    /**
     * Test Case 2: All processes arrive at time 0
     */
    static AGTestCase createTestCase2() {
        return new AGTestCase(
                "Test Case 2: All arrive simultaneously",
                Arrays.asList(
                        new ProcessInput("P1", 0, 10, 3, 4),
                        new ProcessInput("P2", 0, 8, 1, 5),
                        new ProcessInput("P3", 0, 12, 2, 6),
                        new ProcessInput("P4", 0, 6, 4, 3),
                        new ProcessInput("P5", 0, 9, 5, 4)
                ),
                Arrays.asList("P1", "P2", "P3", "P2", "P4", "P3", "P4", "P3", "P5", "P1", "P4", "P1", "P5", "P4", "P5"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(25, 35, Arrays.asList(4, 6, 8, 0)));
                    put("P2", new ProcessMetrics(3, 11, Arrays.asList(5, 7, 0)));
                    put("P3", new ProcessMetrics(11, 23, Arrays.asList(6, 8, 12, 0)));
                    put("P4", new ProcessMetrics(33, 39, Arrays.asList(3, 4, 6, 8, 0)));
                    put("P5", new ProcessMetrics(36, 45, Arrays.asList(4, 6, 8, 0)));
                }},
                21.6,
                30.6
        );
    }

    /**
     * Test Case 3: Mixed scenario with varied bursts and priorities
     */
    static AGTestCase createTestCase3() {
        return new AGTestCase(
                "Test Case 3: Mixed scenario",
                Arrays.asList(
                        new ProcessInput("P1", 0, 20, 5, 8),
                        new ProcessInput("P2", 3, 4, 3, 6),
                        new ProcessInput("P3", 6, 3, 4, 5),
                        new ProcessInput("P4", 10, 2, 2, 4),
                        new ProcessInput("P5", 15, 5, 6, 7),
                        new ProcessInput("P6", 20, 6, 1, 3)
                ),
                Arrays.asList("P1", "P2", "P1", "P4", "P3", "P1", "P6", "P5", "P6", "P1", "P5"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(17, 37, Arrays.asList(8, 12, 17, 23, 0)));
                    put("P2", new ProcessMetrics(1, 5, Arrays.asList(6, 0)));
                    put("P3", new ProcessMetrics(7, 10, Arrays.asList(5, 0)));
                    put("P4", new ProcessMetrics(1, 3, Arrays.asList(4, 0)));
                    put("P5", new ProcessMetrics(20, 25, Arrays.asList(7, 10, 0)));
                    put("P6", new ProcessMetrics(3, 9, Arrays.asList(3, 5, 0)));
                }},
                8.17,
                14.83
        );
    }

    /**
     * Test Case 4: Large bursts with varied quantum values
     */
    static AGTestCase createTestCase4() {
        return new AGTestCase(
                "Test Case 4: Large bursts with gaps",
                Arrays.asList(
                        new ProcessInput("P1", 0, 3, 2, 10),
                        new ProcessInput("P2", 2, 4, 3, 12),
                        new ProcessInput("P3", 5, 2, 1, 8),
                        new ProcessInput("P4", 8, 5, 4, 15),
                        new ProcessInput("P5", 12, 3, 5, 9)
                ),
                Arrays.asList("P1", "P2", "P3", "P2", "P4", "P5"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(0, 3, Arrays.asList(10, 0)));
                    put("P2", new ProcessMetrics(3, 7, Arrays.asList(12, 17, 0)));
                    put("P3", new ProcessMetrics(1, 3, Arrays.asList(8, 0)));
                    put("P4", new ProcessMetrics(1, 6, Arrays.asList(15, 0)));
                    put("P5", new ProcessMetrics(2, 5, Arrays.asList(9, 0)));
                }},
                1.4,
                4.8
        );
    }

    /**
     * Test Case 5: Large dataset comprehensive test
     */
    static AGTestCase createTestCase5() {
        return new AGTestCase(
                "Test Case 5: Large dataset comprehensive",
                Arrays.asList(
                        new ProcessInput("P1", 0, 25, 3, 5),
                        new ProcessInput("P2", 1, 18, 2, 4),
                        new ProcessInput("P3", 3, 22, 4, 6),
                        new ProcessInput("P4", 5, 15, 1, 3),
                        new ProcessInput("P5", 8, 20, 5, 7),
                        new ProcessInput("P6", 12, 12, 6, 4)
                ),
                Arrays.asList("P1", "P2", "P1", "P4", "P3", "P4", "P2", "P4", "P5", "P2", "P1", "P2", "P6", "P1", "P3", "P1", "P5", "P3", "P6", "P3", "P5", "P6", "P5", "P6"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(40, 65, Arrays.asList(5, 7, 10, 14, 16, 0)));
                    put("P2", new ProcessMetrics(25, 43, Arrays.asList(4, 6, 8, 10, 0)));
                    put("P3", new ProcessMetrics(63, 85, Arrays.asList(6, 8, 11, 16, 0)));
                    put("P4", new ProcessMetrics(7, 22, Arrays.asList(3, 5, 7, 0)));
                    put("P5", new ProcessMetrics(77, 97, Arrays.asList(7, 10, 14, 16, 0)));
                    put("P6", new ProcessMetrics(88, 100, Arrays.asList(4, 6, 8, 11, 0)));
                }},
                50.0,
                68.67
        );
    }

    /**
     * Test Case 6: Complex scenario with 7 processes
     */
    static AGTestCase createTestCase6() {
        return new AGTestCase(
                "Test Case 6: Complex scenario 7 processes",
                Arrays.asList(
                        new ProcessInput("P1", 0, 14, 4, 6),
                        new ProcessInput("P2", 4, 9, 2, 8),
                        new ProcessInput("P3", 7, 16, 5, 5),
                        new ProcessInput("P4", 10, 7, 1, 10),
                        new ProcessInput("P5", 15, 11, 3, 4),
                        new ProcessInput("P6", 20, 5, 6, 7),
                        new ProcessInput("P7", 25, 8, 7, 9)
                ),
                Arrays.asList("P1", "P2", "P1", "P4", "P3", "P2", "P1", "P5", "P6", "P5", "P6", "P3", "P5", "P7", "P1", "P3", "P7", "P3", "P7"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(39, 53, Arrays.asList(6, 8, 11, 15, 0)));
                    put("P2", new ProcessMetrics(11, 20, Arrays.asList(8, 10, 0)));
                    put("P3", new ProcessMetrics(45, 61, Arrays.asList(5, 7, 10, 14, 0)));
                    put("P4", new ProcessMetrics(4, 11, Arrays.asList(10, 0)));
                    put("P5", new ProcessMetrics(19, 30, Arrays.asList(4, 6, 8, 0)));
                    put("P6", new ProcessMetrics(13, 18, Arrays.asList(7, 10, 0)));
                    put("P7", new ProcessMetrics(37, 45, Arrays.asList(9, 12, 17, 0)));
                }},
                24.0,
                34.0
        );
    }

    /**
     * Parameterized test that runs all 6 AG test cases.
     * Validates execution order, per-process metrics, quantum history, and averages.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideAGTestCases")
    @DisplayName("AG Scheduler - All Test Cases")
    void testAGScheduler(AGTestCase testCase) {
        // ==================== SETUP ====================
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

        // ==================== EXECUTE ====================
        engine.run(scheduler);

        // ==================== VERIFY ====================

        // 1. Verify Execution Order
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

        // 2. Verify Per-Process Metrics
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

            // 3. Verify Quantum History
            assertEquals(
                    expected.quantumHistory,
                    process.getQuantumHistory(),
                    String.format("Quantum history mismatch for %s in %s. Expected: %s, Got: %s",
                            process.getName(), testCase.name, expected.quantumHistory, process.getQuantumHistory())
            );
        }

        // 4. Verify Average Waiting Time
        double actualAvgWaitingTime = calculateAverageWaitingTime(engine.getProcesses());
        assertEquals(
                testCase.expectedAvgWaitingTime,
                actualAvgWaitingTime,
                0.01,
                String.format("Average waiting time mismatch for %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgWaitingTime, actualAvgWaitingTime)
        );

        // 5. Verify Average Turnaround Time
        double actualAvgTurnaroundTime = calculateAverageTurnaroundTime(engine.getProcesses());
        assertEquals(
                testCase.expectedAvgTurnaroundTime,
                actualAvgTurnaroundTime,
                0.01,
                String.format("Average turnaround time mismatch for %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgTurnaroundTime, actualAvgTurnaroundTime)
        );
    }

    /**
     * Helper method to calculate average waiting time across all processes.
     */
    private double calculateAverageWaitingTime(List<Process> processes) {
        return processes.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0.0);
    }

    /**
     * Helper method to calculate average turnaround time across all processes.
     */
    private double calculateAverageTurnaroundTime(List<Process> processes) {
        return processes.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0.0);
    }
}
