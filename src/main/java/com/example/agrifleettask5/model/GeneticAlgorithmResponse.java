package com.example.agrifleettask5.model;

import java.util.List;

/**
 * Response DTO for AG-20: Genetic Algorithm tour optimization results.
 * Contains the optimized visit sequence and associated metrics.
 *
 * @param visitSequence ordered list of locations to visit (depot first, optionally last)
 * @param legs list of travel segments with distances
 * @param totalDistanceKm total travel distance in kilometers
 * @param estimatedFuelLitres estimated fuel consumption for the tour
 * @param algorithm name of the algorithm used (always "GA" for this response)
 * @param timeComplexity worst-case time complexity of the algorithm
 * @param spaceComplexity worst-case space complexity of the algorithm
 * @param elapsedNanoseconds time taken to compute the solution in nanoseconds
 */
public record GeneticAlgorithmResponse(
        List<FarmLocation> visitSequence,
        List<RouteLeg> legs,
        double totalDistanceKm,
        double estimatedFuelLitres,
        String algorithm,
        String timeComplexity,
        String spaceComplexity,
        long elapsedNanoseconds
) {
}
