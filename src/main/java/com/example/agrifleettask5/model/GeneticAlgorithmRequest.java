package com.example.agrifleettask5.model;

import java.util.List;

/**
 * Request DTO for AG-20: Genetic Algorithm tour optimization.
 * Extends the base AG-18 optimization with GA-specific parameters.
 *
 * @param depot the starting point for the tour
 * @param farms list of farm locations to visit
 * @param distanceMatrix optional pre-computed distance matrix; if null, Haversine distances are calculated
 * @param returnToDepot whether the tour must return to the depot (round trip) or end at the last farm
 * @param fuelConsumptionLitresPerKm fuel consumption rate for calculating total fuel needed
 * @param populationSize size of the genetic algorithm population (default: 100)
 * @param generations number of evolutionary generations (default: 200)
 * @param mutationRate probability of mutation per gene (default: 0.02)
 */
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
