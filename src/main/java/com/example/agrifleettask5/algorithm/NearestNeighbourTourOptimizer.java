package com.example.agrifleettask5.algorithm;

import com.example.agrifleettask5.model.TourSolution;
import org.springframework.stereotype.Component;


@Component
public class NearestNeighbourTourOptimizer {

    public TourSolution optimize(double[][] distances, boolean returnToDepot) {
        long startedAt = System.nanoTime();
        int farmCount = distances.length - 1;
        int[] order = new int[farmCount];
        boolean[] visited = new boolean[farmCount];
        int currentMatrixIndex = 0;
        double totalDistance = 0.0;

        for (int position = 0; position < farmCount; position++) {
            int nearest = -1;
            double nearestDistance = Double.POSITIVE_INFINITY;
            for (int farm = 0; farm < farmCount; farm++) {
                double candidate = distances[currentMatrixIndex][farm + 1];
                if (!visited[farm] && candidate < nearestDistance) {
                    nearest = farm;
                    nearestDistance = candidate;
                }
            }
            order[position] = nearest;
            visited[nearest] = true;
            totalDistance += nearestDistance;
            currentMatrixIndex = nearest + 1;
        }

        if (returnToDepot && farmCount > 0) {
            totalDistance += distances[currentMatrixIndex][0];
        }

        long elapsed = System.nanoTime() - startedAt;
        return new TourSolution(order, totalDistance, "NEAREST_NEIGHBOUR", false,
                "O(n^2)", "O(n)", elapsed);
    }
}
