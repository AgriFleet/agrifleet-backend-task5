package com.example.agrifleettask5.model;

/**
 * Immutable record representing the result of a tour optimization algorithm.
 * Contains the optimized tour order and associated performance metrics.
 *
 * @param farmOrder array of farm indices representing the visit order (0-based)
 * @param totalDistance total distance of the optimized tour in kilometers
 * @param algorithm name of the algorithm used (e.g., "HELD_KARP_DP", "NEAREST_NEIGHBOUR", "GA")
 * @param optimalityGuaranteed whether the solution is guaranteed to be globally optimal
 * @param timeComplexity worst-case time complexity as a Big-O string
 * @param spaceComplexity worst-case space complexity as a Big-O string
 * @param elapsedNanoseconds execution time in nanoseconds
 */
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
