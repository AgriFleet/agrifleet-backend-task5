package com.example.agrifleettask5.model;

import java.util.List;


public record GeneticAlgorithmRequest(
        FarmLocation depot,
        List<FarmLocation> farms,
        double[][] distanceMatrix,
        Boolean returnToDepot,
        Double fuelConsumptionLitresPerKm,
        Integer populationSize,
        Integer generations,
        Double mutationRate
) {
}
