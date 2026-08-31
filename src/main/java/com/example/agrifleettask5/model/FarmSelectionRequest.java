package com.example.agrifleettask5.model;

import java.util.List;

public record FarmSelectionRequest(
        List<FarmOpportunity> availableFarms,
        Integer maxFarms,
        Double acreageWeight,
        Double bookingValueWeight
) {
}
