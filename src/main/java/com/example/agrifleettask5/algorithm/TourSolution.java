package com.example.agrifleettask5.algorithm;

public record TourSolution(
        int[] farmOrder,
        double totalDistance,
        String algorithm,
        boolean optimalityGuaranteed,
        String timeComplexity,
        String spaceComplexity
) {
}
