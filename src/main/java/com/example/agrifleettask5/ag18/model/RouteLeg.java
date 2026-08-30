package com.example.agrifleettask5.ag18.model;

public record RouteLeg(
        long fromId,
        long toId,
        double distanceKm
) {
}
