

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;



import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test suite for RoundRobinScheduler using parameterized tests.
 *
 * Tests all 6 provided test cases covering:
 * - Basic mixed arrivals
 * - All processes arriving at time 0
 * - Varied burst times with starvation risk
 * - Large bursts with gaps in arrivals
 * - Short bursts with high frequency
 * - Mixed comprehensive scenario
 *
 * Each test validates:
 * - Execution order (exact match)
 * - Per-process waiting times and turnaround times
 * - Average waiting time and turnaround time
 */
@DisplayName("Round Robin Scheduler - Parameterized Tests")
public class RoundRobinSchedulerTest {

    /**
     * Test container for a single test case with all inputs and expected outputs.
     */
    static class RoundRobinTestCase {
        final String name;
        final int contextSwitchTime;
        final int rrQuantum;
        final List<ProcessInput> processes;
        final List<String> expectedExecutionOrder;
        final Map<String, ProcessMetrics> expectedMetrics;
        final double expectedAvgWaitingTime;
        final double expectedAvgTurnaroundTime;

        RoundRobinTestCase(String name, int contextSwitchTime, int rrQuantum,
                        List<ProcessInput> processes,
                        List<String> expectedExecutionOrder,
                        Map<String, ProcessMetrics> expectedMetrics,
                        double expectedAvgWaitingTime,
                        double expectedAvgTurnaroundTime) {
            this.name = name;
            this.contextSwitchTime = contextSwitchTime;
            this.rrQuantum = rrQuantum;
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

        ProcessInput(String name, int arrival, int burst, int priority) {
            this.name = name;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
        }
    }

    /**
     * Expected metrics for a single process.
     */
    static class ProcessMetrics {
        final int waitingTime;
        final int turnaroundTime;

        ProcessMetrics(int waitingTime, int turnaroundTime) {
            this.waitingTime = waitingTime;
            this.turnaroundTime = turnaroundTime;
        }
    }

    /**
     * Provides all 6 test cases for the Round Robin scheduler.
     * Each test case includes complete input and expected output data.
     */
    static Stream<RoundRobinTestCase> provideRoundRobinTestCases() {
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
     * Test Case 1: Basic mixed arrivals
     */
    static RoundRobinTestCase createTestCase1() {
        return new RoundRobinTestCase(
                "Test Case 1: Basic mixed arrivals",
                1,  // contextSwitch
                2,  // rrQuantum
                Arrays.asList(
                        new ProcessInput("P1", 0, 8, 3),
                        new ProcessInput("P2", 1, 4, 1),
                        new ProcessInput("P3", 2, 2, 4),
                        new ProcessInput("P4", 3, 1, 2),
                        new ProcessInput("P5", 4, 3, 5)
                ),
                Arrays.asList("P1", "P2", "P3", "P1", "P4", "P5", "P2", "P1", "P5", "P1"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(19, 27));
                    put("P2", new ProcessMetrics(14, 18));
                    put("P3", new ProcessMetrics(4, 6));
                    put("P4", new ProcessMetrics(9, 10));
                    put("P5", new ProcessMetrics(17, 20));
                }},
                12.6,  // expectedAvgWaitingTime
                16.2   // expectedAvgTurnaroundTime
        );
    }

    /**
     * Test Case 2: All processes arrive at time 0
     */
    static RoundRobinTestCase createTestCase2() {
        return new RoundRobinTestCase(
                "Test Case 2: All processes arrive at time 0",
                1,  // contextSwitch
                3,  // rrQuantum
                Arrays.asList(
                        new ProcessInput("P1", 0, 6, 3),
                        new ProcessInput("P2", 0, 3, 1),
                        new ProcessInput("P3", 0, 8, 2),
                        new ProcessInput("P4", 0, 4, 4),
                        new ProcessInput("P5", 0, 2, 5)
                ),
                Arrays.asList("P1", "P2", "P3", "P4", "P5", "P1", "P3", "P4", "P3"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(16, 22));
                    put("P2", new ProcessMetrics(4, 7));
                    put("P3", new ProcessMetrics(23, 31));
                    put("P4", new ProcessMetrics(24, 28));
                    put("P5", new ProcessMetrics(16, 18));
                }},
                16.6,  // expectedAvgWaitingTime
                21.2   // expectedAvgTurnaroundTime
        );
    }

    /**
     * Test Case 3: Varied burst times with starvation risk
     */
    static RoundRobinTestCase createTestCase3() {
        return new RoundRobinTestCase(
                "Test Case 3: Varied burst times with starvation risk",
                1,  // contextSwitch
                4,  // rrQuantum
                Arrays.asList(
                        new ProcessInput("P1", 0, 10, 5),
                        new ProcessInput("P2", 2, 5, 1),
                        new ProcessInput("P3", 5, 3, 2),
                        new ProcessInput("P4", 8, 7, 1),
                        new ProcessInput("P5", 10, 2, 3)
                ),
                Arrays.asList("P1", "P2", "P1", "P3", "P4", "P2", "P5", "P1", "P4"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(21, 31));
                    put("P2", new ProcessMetrics(18, 23));
                    put("P3", new ProcessMetrics(10, 13));
                    put("P4", new ProcessMetrics(20, 27));
                    put("P5", new ProcessMetrics(16, 18));
                }},
                17.0,  // expectedAvgWaitingTime
                22.4   // expectedAvgTurnaroundTime
        );
    }

    /**
     * Test Case 4: Large bursts with gaps in arrivals
     */
    static RoundRobinTestCase createTestCase4() {
        return new RoundRobinTestCase(
                "Test Case 4: Large bursts with gaps in arrivals",
                2,  // contextSwitch
                5,  // rrQuantum
                Arrays.asList(
                        new ProcessInput("P1", 0, 12, 2),
                        new ProcessInput("P2", 4, 9, 3),
                        new ProcessInput("P3", 8, 15, 1),
                        new ProcessInput("P4", 12, 6, 4),
                        new ProcessInput("P5", 16, 11, 2),
                        new ProcessInput("P6", 20, 5, 5)
                ),
                Arrays.asList("P1", "P2", "P1", "P3", "P4", "P2", "P5", "P1", "P6", "P3", "P4", "P5", "P3", "P5"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(38, 50));
                    put("P2", new ProcessMetrics(26, 35));
                    put("P3", new ProcessMetrics(58, 73));
                    put("P4", new ProcessMetrics(49, 55));
                    put("P5", new ProcessMetrics(57, 68));
                    put("P6", new ProcessMetrics(32, 37));
                }},
                43.33,  // expectedAvgWaitingTime
                53.0    // expectedAvgTurnaroundTime
        );
    }

    /**
     * Test Case 5: Short bursts with high frequency
     */
    static RoundRobinTestCase createTestCase5() {
        return new RoundRobinTestCase(
                "Test Case 5: Short bursts with high frequency",
                1,  // contextSwitch
                2,  // rrQuantum
                Arrays.asList(
                        new ProcessInput("P1", 0, 3, 3),
                        new ProcessInput("P2", 1, 2, 1),
                        new ProcessInput("P3", 2, 4, 2),
                        new ProcessInput("P4", 3, 1, 4),
                        new ProcessInput("P5", 4, 3, 5)
                ),
                Arrays.asList("P1", "P2", "P3", "P1", "P4", "P5", "P3", "P5"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(7, 10));
                    put("P2", new ProcessMetrics(2, 4));
                    put("P3", new ProcessMetrics(12, 16));
                    put("P4", new ProcessMetrics(8, 9));
                    put("P5", new ProcessMetrics(13, 16));
                }},
                8.4,   // expectedAvgWaitingTime
                11.0   // expectedAvgTurnaroundTime
        );
    }

    /**
     * Test Case 6: Mixed scenario - comprehensive test
     */
    static RoundRobinTestCase createTestCase6() {
        return new RoundRobinTestCase(
                "Test Case 6: Mixed scenario - comprehensive test",
                1,  // contextSwitch
                4,  // rrQuantum
                Arrays.asList(
                        new ProcessInput("P1", 0, 14, 4),
                        new ProcessInput("P2", 3, 7, 2),
                        new ProcessInput("P3", 6, 10, 5),
                        new ProcessInput("P4", 9, 5, 1),
                        new ProcessInput("P5", 12, 8, 3),
                        new ProcessInput("P6", 15, 4, 6)
                ),
                Arrays.asList("P1", "P2", "P1", "P3", "P4", "P2", "P5", "P1", "P6", "P3", "P4", "P5", "P1", "P3"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(44, 58));
                    put("P2", new ProcessMetrics(18, 25));
                    put("P3", new ProcessMetrics(45, 55));
                    put("P4", new ProcessMetrics(36, 41));
                    put("P5", new ProcessMetrics(35, 43));
                    put("P6", new ProcessMetrics(24, 28));
                }},
                33.67,  // expectedAvgWaitingTime
                41.67   // expectedAvgTurnaroundTime
        );
    }

    /**
     * Parameterized test that runs all 6 test cases.
     * Validates execution order, per-process metrics, and averages.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRoundRobinTestCases")
    @DisplayName("Round Robin Scheduler - All Test Cases")
    void testRoundRobinScheduler(RoundRobinTestCase testCase) {
        // ==================== SETUP ====================
        // Create Process objects from test case input
        List<Process> processes = new ArrayList<>();
        for (ProcessInput input : testCase.processes) {
            processes.add(new Process(
                    input.name,
                    input.arrival,
                    input.burst,
                    input.priority,
                    testCase.rrQuantum
            ));
        }

        // Create simulation engine with context switch time
        SimulationEngine engine = new SimulationEngine(processes, testCase.contextSwitchTime);

        // Create execution log for tracking execution order
        ExecutionLog executionLog = new ExecutionLog();

        // Create and run the Round Robin scheduler
        Scheduler scheduler = new RoundRobinScheduler(
                engine.getClock(),
                engine.getContextSwitchManager(),
                executionLog,
                testCase.rrQuantum
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
        }

        // 3. Verify Average Waiting Time
        double actualAvgWaitingTime = calculateAverageWaitingTime(engine.getProcesses());
        assertEquals(
                testCase.expectedAvgWaitingTime,
                actualAvgWaitingTime,
                0.01,  // tolerance of 0.01
                String.format("Average waiting time mismatch for %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgWaitingTime, actualAvgWaitingTime)
        );

        // 4. Verify Average Turnaround Time
        double actualAvgTurnaroundTime = calculateAverageTurnaroundTime(engine.getProcesses());
        assertEquals(
                testCase.expectedAvgTurnaroundTime,
                actualAvgTurnaroundTime,
                0.01,  // tolerance of 0.01
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
