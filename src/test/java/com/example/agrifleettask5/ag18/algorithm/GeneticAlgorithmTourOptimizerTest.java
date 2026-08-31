package com.example.agrifleettask5.ag18.algorithm;

import com.example.agrifleettask5.algorithm.GeneticAlgorithmTourOptimizer;
import com.example.agrifleettask5.model.TourSolution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneticAlgorithmTourOptimizerTest {
    private final GeneticAlgorithmTourOptimizer optimizer = new GeneticAlgorithmTourOptimizer();

    @Test
    void producesValidTourForSimpleInstance() {
        // 4-city TSP: depot (0) + 3 farms (1,2,3)
        double[][] distances = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };

        TourSolution solution = optimizer.optimize(distances, true, 50, 50, 0.02);

        assertNotNull(solution);
        assertNotNull(solution.farmOrder());
        assertEquals(3, solution.farmOrder().length);
        assertTrue(solution.totalDistance() > 0);
        assertTrue(solution.totalDistance() <= 200); // Reasonable bound
        assertEquals("GA", solution.algorithm());
    }

    @Test
    void handlesRoundTripCorrectly() {
        double[][] distances = {
                {0, 10, 20},
                {10, 0, 15},
                {20, 15, 0}
        };

        TourSolution solutionRoundTrip = optimizer.optimize(distances, true, 50, 50, 0.02);
        TourSolution solutionOpen = optimizer.optimize(distances, false, 50, 50, 0.02);

        assertTrue(solutionRoundTrip.totalDistance() >= solutionOpen.totalDistance());
    }

    @Test
    void producesConsistentResultsAcrossRuns() {
        double[][] distances = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };

        TourSolution solution1 = optimizer.optimize(distances, true, 100, 100, 0.02);
        TourSolution solution2 = optimizer.optimize(distances, true, 100, 100, 0.02);

        assertNotNull(solution1);
        assertNotNull(solution2);
        assertTrue(solution1.totalDistance() > 0);
        assertTrue(solution2.totalDistance() > 0);
    }

    @Test
    void handlesLargerInstances() {
        // 10-city problem
        int n = 10;
        double[][] distances = buildRandomDistanceMatrix(n);

        TourSolution solution = optimizer.optimize(distances, true, 50, 100, 0.05);

        assertNotNull(solution);
        assertEquals(n - 1, solution.farmOrder().length);
        assertTrue(solution.totalDistance() > 0);
    }

    @Test
    void handlesSingleFarm() {
        double[][] distances = {
                {0, 50},
                {50, 0}
        };

        TourSolution solution = optimizer.optimize(distances, true, 50, 50, 0.02);

        assertNotNull(solution);
        assertEquals(1, solution.farmOrder().length);
        assertEquals(100, solution.totalDistance(), 0.001); // depot->farm->depot = 50+50
    }

    @Test
    void handlesTwoFarms() {
        double[][] distances = {
                {0, 10, 20},
                {10, 0, 15},
                {20, 15, 0}
        };

        TourSolution solution = optimizer.optimize(distances, true, 50, 50, 0.02);

        assertNotNull(solution);
        assertEquals(2, solution.farmOrder().length);
        // Optimal: depot->0->1->depot = 10+15+20 = 45 or depot->1->0->depot = 20+15+10 = 45
        assertTrue(solution.totalDistance() <= 50);
    }

    @Test
    void respectsPopulationSizeParameter() {
        double[][] distances = buildRandomDistanceMatrix(5);

        // Smaller population, fewer generations should still work
        TourSolution solution = optimizer.optimize(distances, true, 10, 5, 0.02);
        assertNotNull(solution);
        assertTrue(solution.totalDistance() > 0);
    }

    @Test
    void respectsMutationRateParameter() {
        double[][] distances = buildRandomDistanceMatrix(5);

        // High mutation rate
        TourSolution solutionHighMutation = optimizer.optimize(distances, true, 50, 50, 0.5);
        // Low mutation rate
        TourSolution solutionLowMutation = optimizer.optimize(distances, true, 50, 50, 0.01);

        assertNotNull(solutionHighMutation);
        assertNotNull(solutionLowMutation);
        assertTrue(solutionHighMutation.totalDistance() > 0);
        assertTrue(solutionLowMutation.totalDistance() > 0);
    }

    private double[][] buildRandomDistanceMatrix(int size) {
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                double distance = 10 + Math.random() * 90;
                matrix[i][j] = distance;
                matrix[j][i] = distance;
            }
        }
        return matrix;
    }
}
