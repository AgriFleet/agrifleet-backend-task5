package com.example.agrifleettask5.ag18.algorithm;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeldKarpTourOptimizerTest {
    private final HeldKarpTourOptimizer optimizer = new HeldKarpTourOptimizer();

    @Test
    void findsExactMinimumCycle() {
        double[][] distances = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };

        TourSolution result = optimizer.optimize(distances, true);

        assertEquals(80.0, result.totalDistance(), 0.0001);
        assertTrue(Arrays.equals(new int[]{0, 2, 1}, result.farmOrder())
                || Arrays.equals(new int[]{1, 2, 0}, result.farmOrder()));
        assertTrue(result.optimalityGuaranteed());
    }

    @Test
    void supportsAnOpenRouteWithoutReturningToDepot() {
        double[][] distances = {
                {0, 2, 9},
                {2, 0, 3},
                {9, 3, 0}
        };

        TourSolution result = optimizer.optimize(distances, false);

        assertArrayEquals(new int[]{0, 1}, result.farmOrder());
        assertEquals(5.0, result.totalDistance(), 0.0001);
    }
}
