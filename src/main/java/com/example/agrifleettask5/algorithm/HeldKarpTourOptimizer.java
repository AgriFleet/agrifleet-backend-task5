package com.example.agrifleettask5.algorithm;

import com.example.agrifleettask5.model.TourSolution;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
public class HeldKarpTourOptimizer {

    public TourSolution optimize(double[][] distances, boolean returnToDepot) {
        long startedAt = System.nanoTime();
        int farmCount = distances.length - 1;
        if (farmCount == 0) {
            long elapsed = System.nanoTime() - startedAt;
            return new TourSolution(new int[0], 0.0, "HELD_KARP_DP", true,
                    "O(n^2 * 2^n)", "O(n * 2^n)", elapsed);
        }

        int stateCount = 1 << farmCount;
        double[][] dp = new double[stateCount][farmCount];
        int[][] parent = new int[stateCount][farmCount];
        for (int mask = 0; mask < stateCount; mask++) {
            Arrays.fill(dp[mask], Double.POSITIVE_INFINITY);
            Arrays.fill(parent[mask], -1);
        }

        for (int last = 0; last < farmCount; last++) {
            dp[1 << last][last] = distances[0][last + 1];
        }

        for (int mask = 1; mask < stateCount; mask++) {
            for (int last = 0; last < farmCount; last++) {
                if ((mask & (1 << last)) == 0) {
                    continue;
                }
                int previousMask = mask ^ (1 << last);
                if (previousMask == 0) {
                    continue;
                }
                for (int previous = 0; previous < farmCount; previous++) {
                    if ((previousMask & (1 << previous)) == 0) {
                        continue;
                    }
                    double candidate = dp[previousMask][previous]
                            + distances[previous + 1][last + 1];
                    if (candidate < dp[mask][last]) {
                        dp[mask][last] = candidate;
                        parent[mask][last] = previous;
                    }
                }
            }
        }

        int fullMask = stateCount - 1;
        int bestLast = -1;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int last = 0; last < farmCount; last++) {
            double candidate = dp[fullMask][last];
            if (returnToDepot) {
                candidate += distances[last + 1][0];
            }
            if (candidate < bestDistance) {
                bestDistance = candidate;
                bestLast = last;
            }
        }

        int[] order = new int[farmCount];
        int mask = fullMask;
        int current = bestLast;
        for (int position = farmCount - 1; position >= 0; position--) {
            order[position] = current;
            int previous = parent[mask][current];
            mask ^= 1 << current;
            current = previous;
        }

        long elapsed = System.nanoTime() - startedAt;
        return new TourSolution(order, bestDistance, "HELD_KARP_DP", true,
                "O(n^2 * 2^n)", "O(n * 2^n)", elapsed);
    }
}
