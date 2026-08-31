package com.example.agrifleettask5.service;

import com.example.agrifleettask5.model.FarmOpportunity;
import com.example.agrifleettask5.model.FarmSelectionRequest;
import com.example.agrifleettask5.model.FarmSelectionResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FarmSelectionService {
    public FarmSelectionResponse select(FarmSelectionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.availableFarms() == null || request.availableFarms().isEmpty()) {
            return new FarmSelectionResponse(List.of(), 0.0, 0.0, 0.0,
                    "AG19_WEIGHTED_SCORE", "maximize acreage and booking value");
        }

        int maxFarms = request.maxFarms() == null || request.maxFarms() <= 0
                ? request.availableFarms().size()
                : Math.min(request.maxFarms(), request.availableFarms().size());
        double acreageWeight = request.acreageWeight() == null ? 0.5 : request.acreageWeight();
        double bookingValueWeight = request.bookingValueWeight() == null ? 0.5 : request.bookingValueWeight();

        if (acreageWeight < 0 || bookingValueWeight < 0) {
            throw new IllegalArgumentException("weights must be non-negative");
        }
        double totalWeight = acreageWeight + bookingValueWeight;
        if (totalWeight <= 0) {
            throw new IllegalArgumentException("weight sum must be greater than zero");
        }

        double maxAcreage = request.availableFarms().stream()
                .mapToDouble(FarmOpportunity::acreageHa)
                .max()
                .orElse(0.0);
        double maxBookingValue = request.availableFarms().stream()
                .mapToDouble(FarmOpportunity::bookingValue)
                .max()
                .orElse(0.0);

        List<FarmOpportunity> ranked = request.availableFarms().stream()
                .sorted(Comparator
                        .comparingDouble((FarmOpportunity farm) -> score(farm, acreageWeight, bookingValueWeight,
                                maxAcreage, maxBookingValue))
                        .reversed()
                        .thenComparingDouble(FarmOpportunity::acreageHa).reversed()
                        .thenComparingDouble(FarmOpportunity::bookingValue).reversed()
                        .thenComparing(FarmOpportunity::name))
                .limit(maxFarms)
                .toList();

        double totalAcreage = ranked.stream().mapToDouble(FarmOpportunity::acreageHa).sum();
        double totalBookingValue = ranked.stream().mapToDouble(FarmOpportunity::bookingValue).sum();
        double objectiveScore = ranked.stream()
                .mapToDouble(farm -> score(farm, acreageWeight, bookingValueWeight, maxAcreage, maxBookingValue))
                .average()
                .orElse(0.0);

        return new FarmSelectionResponse(ranked, totalAcreage, totalBookingValue,
                objectiveScore, "AG19_WEIGHTED_SCORE",
                "maximize acreage and booking value");
    }

    private double score(FarmOpportunity farm, double acreageWeight, double bookingValueWeight,
                         double maxAcreage, double maxBookingValue) {
        double normalizedAcreage = maxAcreage == 0 ? 0.0 : farm.acreageHa() / maxAcreage;
        double normalizedBookingValue = maxBookingValue == 0 ? 0.0 : farm.bookingValue() / maxBookingValue;
        return ((acreageWeight / (acreageWeight + bookingValueWeight)) * normalizedAcreage)
                + ((bookingValueWeight / (acreageWeight + bookingValueWeight)) * normalizedBookingValue);
    }
}
