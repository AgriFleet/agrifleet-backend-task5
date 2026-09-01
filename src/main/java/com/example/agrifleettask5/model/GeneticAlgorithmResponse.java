package com.example.agrifleettask5.model;

import java.util.List;


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
