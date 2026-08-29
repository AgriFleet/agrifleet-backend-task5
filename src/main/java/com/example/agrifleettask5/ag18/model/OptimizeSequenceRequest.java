package com.example.agrifleettask5.ag18.model;

import java.util.List;

public record OptimizeSequenceRequest(
        FarmLocation depot,
        List<FarmLocation> farms,
        double[][] distanceMatrix,
        Boolean returnToDepot,
        Double fuelConsumptionLitresPerKm
) {
}
