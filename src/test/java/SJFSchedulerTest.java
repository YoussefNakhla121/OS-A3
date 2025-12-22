import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test suite for SJFScheduler using parameterized tests.
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
@DisplayName("SJF Scheduler - Parameterized Tests")
public class SJFSchedulerTest {

    /**
     * Test container for a single test case with all inputs and expected outputs.
     */
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
     * Provides all 6 test cases for the SJF scheduler.
     * Each test case includes complete input and expected output data.
     */
    static Stream<SJFTestCase> provideSJFTestCases() {
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
    static SJFTestCase createTestCase1() {
        return new SJFTestCase(
                "Test Case 1: Basic mixed arrivals",
                1,
                Arrays.asList(
                        new ProcessInput("P1", 0, 8, 3),
                        new ProcessInput("P2", 1, 4, 1),
                        new ProcessInput("P3", 2, 2, 4),
                        new ProcessInput("P4", 3, 1, 2),
                        new ProcessInput("P5", 4, 3, 5)
                ),
                Arrays.asList("P1", "P2", "P4", "P3", "P2", "P5", "P1"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(16, 24));
                    put("P2", new ProcessMetrics(7, 11));
                    put("P3", new ProcessMetrics(4, 6));
                    put("P4", new ProcessMetrics(1, 2));
                    put("P5", new ProcessMetrics(9, 12));
                }},
                7.4,
                11.0
        );
    }

    /**
     * Test Case 2: All processes arrive at time 0
     */
    static SJFTestCase createTestCase2() {
        return new SJFTestCase(
                "Test Case 2: All processes arrive at time 0",
                1,
                Arrays.asList(
                        new ProcessInput("P1", 0, 6, 3),
                        new ProcessInput("P2", 0, 3, 1),
                        new ProcessInput("P3", 0, 8, 2),
                        new ProcessInput("P4", 0, 4, 4),
                        new ProcessInput("P5", 0, 2, 5)
                ),
                Arrays.asList("P5", "P2", "P4", "P1", "P3"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(12, 18));
                    put("P2", new ProcessMetrics(3, 6));
                    put("P3", new ProcessMetrics(19, 27));
                    put("P4", new ProcessMetrics(7, 11));
                    put("P5", new ProcessMetrics(0, 2));
                }},
                8.2,
                12.8
        );
    }

    /**
     * Test Case 3: Varied burst times with starvation risk
     */
    static SJFTestCase createTestCase3() {
        return new SJFTestCase(
                "Test Case 3: Varied burst times with starvation risk",
                1,
                Arrays.asList(
                        new ProcessInput("P1", 0, 10, 5),
                        new ProcessInput("P2", 2, 5, 1),
                        new ProcessInput("P3", 5, 3, 2),
                        new ProcessInput("P4", 8, 7, 1),
                        new ProcessInput("P5", 10, 2, 3)
                ),
                Arrays.asList("P1", "P2", "P3", "P5", "P4", "P1"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(22, 32));
                    put("P2", new ProcessMetrics(1, 6));
                    put("P3", new ProcessMetrics(4, 7));
                    put("P4", new ProcessMetrics(8, 15));
                    put("P5", new ProcessMetrics(3, 5));
                }},
                7.6,
                13.0
        );
    }

    /**
     * Test Case 4: Large bursts with gaps in arrivals
     */
    static SJFTestCase createTestCase4() {
        return new SJFTestCase(
                "Test Case 4: Large bursts with gaps in arrivals",
                2,
                Arrays.asList(
                        new ProcessInput("P1", 0, 12, 2),
                        new ProcessInput("P2", 4, 9, 3),
                        new ProcessInput("P3", 8, 15, 1),
                        new ProcessInput("P4", 12, 6, 4),
                        new ProcessInput("P5", 16, 11, 2),
                        new ProcessInput("P6", 20, 5, 5)
                ),
                Arrays.asList("P1", "P4", "P6", "P2", "P5", "P3"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(0, 12));
                    put("P2", new ProcessMetrics(25, 34));
                    put("P3", new ProcessMetrics(45, 60));
                    put("P4", new ProcessMetrics(2, 8));
                    put("P5", new ProcessMetrics(24, 35));
                    put("P6", new ProcessMetrics(2, 7));
                }},
                16.33,
                26.0
        );
    }

    /**
     * Test Case 5: Short bursts with high frequency
     */
    static SJFTestCase createTestCase5() {
        return new SJFTestCase(
                "Test Case 5: Short bursts with high frequency",
                1,
                Arrays.asList(
                        new ProcessInput("P1", 0, 3, 3),
                        new ProcessInput("P2", 1, 2, 1),
                        new ProcessInput("P3", 2, 4, 2),
                        new ProcessInput("P4", 3, 1, 4),
                        new ProcessInput("P5", 4, 3, 5)
                ),
                Arrays.asList("P1", "P4", "P2", "P5", "P3"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(0, 3));
                    put("P2", new ProcessMetrics(5, 7));
                    put("P3", new ProcessMetrics(11, 15));
                    put("P4", new ProcessMetrics(1, 2));
                    put("P5", new ProcessMetrics(5, 8));
                }},
                4.4,
                7.0
        );
    }

    /**
     * Test Case 6: Mixed scenario - comprehensive test
     */
    static SJFTestCase createTestCase6() {
        return new SJFTestCase(
                "Test Case 6: Mixed scenario - comprehensive test",
                1,
                Arrays.asList(
                        new ProcessInput("P1", 0, 14, 4),
                        new ProcessInput("P2", 3, 7, 2),
                        new ProcessInput("P3", 6, 10, 5),
                        new ProcessInput("P4", 9, 5, 1),
                        new ProcessInput("P5", 12, 8, 3),
                        new ProcessInput("P6", 15, 4, 6)
                ),
                Arrays.asList("P1", "P2", "P4", "P6", "P5", "P3", "P1"),
                new HashMap<String, ProcessMetrics>() {{
                    put("P1", new ProcessMetrics(40, 54));
                    put("P2", new ProcessMetrics(1, 8));
                    put("P3", new ProcessMetrics(26, 36));
                    put("P4", new ProcessMetrics(3, 8));
                    put("P5", new ProcessMetrics(11, 19));
                    put("P6", new ProcessMetrics(3, 7));
                }},
                14.0,
                22.0
        );
    }

    /**
     * Parameterized test that runs all 6 test cases.
     * Validates execution order, per-process metrics, and averages.
     */
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

        // Run the SJF scheduler
        SJFScheduler scheduler = new SJFScheduler(testCase.contextSwitchTime);
        scheduler.run(processes);

        // Verify execution order
        List<String> actualExecutionOrder = scheduler.getExecutionLog().getExecutionSequence();
        assertEquals(testCase.expectedExecutionOrder, actualExecutionOrder,
                String.format("Execution order mismatch in %s\nExpected: %s\nActual: %s",
                        testCase.name, testCase.expectedExecutionOrder, actualExecutionOrder));

        // Verify per-process metrics
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

        // Verify average waiting time
        double actualAvgWaitingTime = calculateAverageWaitingTime(processes);
        assertEquals(testCase.expectedAvgWaitingTime, actualAvgWaitingTime, 0.01,
                String.format("Average waiting time mismatch in %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgWaitingTime, actualAvgWaitingTime));

        // Verify average turnaround time
        double actualAvgTurnaroundTime = calculateAverageTurnaroundTime(processes);
        assertEquals(testCase.expectedAvgTurnaroundTime, actualAvgTurnaroundTime, 0.01,
                String.format("Average turnaround time mismatch in %s. Expected: %.2f, Got: %.2f",
                        testCase.name, testCase.expectedAvgTurnaroundTime, actualAvgTurnaroundTime));
    }

    /**
     * Helper method to calculate average waiting time across all processes.
     */
    private double calculateAverageWaitingTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return 0;
        }
        double total = processes.stream().mapToInt(Process::getWaitingTime).sum();
        return total / processes.size();
    }

    /**
     * Helper method to calculate average turnaround time across all processes.
     */
    private double calculateAverageTurnaroundTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return 0;
        }
        double total = processes.stream().mapToInt(Process::getTurnaroundTime).sum();
        return total / processes.size();
    }
}
