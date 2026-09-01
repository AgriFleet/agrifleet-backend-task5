package com.example.agrifleettask5.model;


public record TourSolution(
        int[] farmOrder,
        double totalDistance,
        String algorithm,
        boolean optimalityGuaranteed,
        String timeComplexity,
        String spaceComplexity,
        long elapsedNanoseconds
) {
}
