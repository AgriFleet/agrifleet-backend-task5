package com.example.agrifleettask5.model;

import java.util.List;

public record FarmSelectionResponse(
        List<FarmOpportunity> selectedFarms,
        double totalAcreageHa,
        double totalBookingValue,
        double objectiveScore,
        String algorithm,
        String objective
) {
}
