package com.example.agrifleettask5.model;

public record RouteLeg(
        long fromId,
        long toId,
        double distanceKm
) {
}
