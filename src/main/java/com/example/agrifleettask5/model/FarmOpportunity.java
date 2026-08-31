package com.example.agrifleettask5.model;

public record FarmOpportunity(
        long id,
        String name,
        double acreageHa,
        double bookingValue,
        String cropType
) {
    public FarmOpportunity {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (!Double.isFinite(acreageHa) || acreageHa < 0) {
            throw new IllegalArgumentException("acreageHa must be finite and non-negative");
        }
        if (!Double.isFinite(bookingValue) || bookingValue < 0) {
            throw new IllegalArgumentException("bookingValue must be finite and non-negative");
        }
    }
}
