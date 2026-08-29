package com.example.agrifleettask5.ag18.model;

import java.util.List;

public record OptimizeSequenceResponse(
        List<FarmLocation> visitSequence,
        List<RouteLeg> legs,
        double totalDistanceKm,
        double estimatedFuelLitres,
        String algorithm,
        boolean optimalityGuaranteed,
        String timeComplexity,
        String spaceComplexity,
        long executionTimeNanos
) {
}
