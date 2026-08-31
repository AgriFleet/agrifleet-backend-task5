package com.example.agrifleettask5.service;

import com.example.agrifleettask5.algorithm.HeldKarpTourOptimizer;
import com.example.agrifleettask5.algorithm.NearestNeighbourTourOptimizer;
import com.example.agrifleettask5.algorithm.TourSolution;
import com.example.agrifleettask5.model.FarmLocation;
import com.example.agrifleettask5.model.OptimizeSequenceRequest;
import com.example.agrifleettask5.model.OptimizeSequenceResponse;
import com.example.agrifleettask5.model.RouteLeg;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VisitSequenceService {
    static final int EXACT_ALGORITHM_MAX_FARMS = 18;
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final HeldKarpTourOptimizer heldKarp;
    private final NearestNeighbourTourOptimizer nearestNeighbour;

    public VisitSequenceService(HeldKarpTourOptimizer heldKarp,
                                NearestNeighbourTourOptimizer nearestNeighbour) {
        this.heldKarp = heldKarp;
        this.nearestNeighbour = nearestNeighbour;
    }

    public OptimizeSequenceResponse optimize(OptimizeSequenceRequest request) {
        validate(request);
        boolean returnToDepot = request.returnToDepot() == null || request.returnToDepot();
        double fuelRate = request.fuelConsumptionLitresPerKm() == null
                ? 0.0 : request.fuelConsumptionLitresPerKm();
        double[][] matrix = request.distanceMatrix() == null
                ? buildHaversineMatrix(request.depot(), request.farms())
                : copyMatrix(request.distanceMatrix());

        long startedAt = System.nanoTime();
        TourSolution solution = request.farms().size() <= EXACT_ALGORITHM_MAX_FARMS
                ? heldKarp.optimize(matrix, returnToDepot)
                : nearestNeighbour.optimize(matrix, returnToDepot);
        long elapsed = System.nanoTime() - startedAt;

        List<FarmLocation> sequence = new ArrayList<>();
        sequence.add(request.depot());
        for (int farmIndex : solution.farmOrder()) {
            sequence.add(request.farms().get(farmIndex));
        }
        if (returnToDepot && !request.farms().isEmpty()) {
            sequence.add(request.depot());
        }

        List<RouteLeg> legs = buildLegs(sequence, request, matrix);
        return new OptimizeSequenceResponse(
                List.copyOf(sequence),
                legs,
                round(solution.totalDistance()),
                round(solution.totalDistance() * fuelRate),
                solution.algorithm(),
                solution.optimalityGuaranteed(),
                solution.timeComplexity(),
                solution.spaceComplexity(),
                elapsed
        );
    }

    private void validate(OptimizeSequenceRequest request) {
        if (request == null || request.depot() == null) {
            throw new IllegalArgumentException("depot is required");
        }
        validateLocation(request.depot(), "depot");
        if (request.farms() == null) {
            throw new IllegalArgumentException("farms is required");
        }
        Set<Long> ids = new HashSet<>();
        ids.add(request.depot().id());
        for (int i = 0; i < request.farms().size(); i++) {
            FarmLocation farm = request.farms().get(i);
            if (farm == null) {
                throw new IllegalArgumentException("farms[" + i + "] cannot be null");
            }
            validateLocation(farm, "farms[" + i + "]");
            if (!ids.add(farm.id())) {
                throw new IllegalArgumentException("location IDs must be unique");
            }
        }
        if (request.fuelConsumptionLitresPerKm() != null
                && (!Double.isFinite(request.fuelConsumptionLitresPerKm())
                || request.fuelConsumptionLitresPerKm() < 0)) {
            throw new IllegalArgumentException("fuelConsumptionLitresPerKm must be finite and non-negative");
        }
        if (request.distanceMatrix() != null) {
            validateMatrix(request.distanceMatrix(), request.farms().size() + 1);
        }
    }

    private void validateLocation(FarmLocation location, String field) {
        if (location.name() == null || location.name().isBlank()) {
            throw new IllegalArgumentException(field + ".name is required");
        }
        if (!Double.isFinite(location.latitude()) || location.latitude() < -90 || location.latitude() > 90) {
            throw new IllegalArgumentException(field + ".latitude must be between -90 and 90");
        }
        if (!Double.isFinite(location.longitude()) || location.longitude() < -180 || location.longitude() > 180) {
            throw new IllegalArgumentException(field + ".longitude must be between -180 and 180");
        }
    }

    private void validateMatrix(double[][] matrix, int expectedSize) {
        if (matrix.length != expectedSize) {
            throw new IllegalArgumentException("distanceMatrix must be " + expectedSize + " x " + expectedSize);
        }
        for (int row = 0; row < expectedSize; row++) {
            if (matrix[row] == null || matrix[row].length != expectedSize) {
                throw new IllegalArgumentException("distanceMatrix must be square");
            }
            for (int column = 0; column < expectedSize; column++) {
                double value = matrix[row][column];
                if (!Double.isFinite(value) || value < 0) {
                    throw new IllegalArgumentException("distanceMatrix values must be finite and non-negative");
                }
                if (row == column && value != 0.0) {
                    throw new IllegalArgumentException("distanceMatrix diagonal values must be zero");
                }
            }
        }
    }

    private double[][] buildHaversineMatrix(FarmLocation depot, List<FarmLocation> farms) {
        List<FarmLocation> allLocations = new ArrayList<>(farms.size() + 1);
        allLocations.add(depot);
        allLocations.addAll(farms);
        double[][] matrix = new double[allLocations.size()][allLocations.size()];
        for (int from = 0; from < allLocations.size(); from++) {
            for (int to = from + 1; to < allLocations.size(); to++) {
                double distance = haversine(allLocations.get(from), allLocations.get(to));
                matrix[from][to] = distance;
                matrix[to][from] = distance;
            }
        }
        return matrix;
    }

    private double haversine(FarmLocation from, FarmLocation to) {
        double latitudeDifference = Math.toRadians(to.latitude() - from.latitude());
        double longitudeDifference = Math.toRadians(to.longitude() - from.longitude());
        double fromLatitude = Math.toRadians(from.latitude());
        double toLatitude = Math.toRadians(to.latitude());
        double a = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                + Math.cos(fromLatitude) * Math.cos(toLatitude)
                * Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private List<RouteLeg> buildLegs(List<FarmLocation> sequence,
                                     OptimizeSequenceRequest request,
                                     double[][] matrix) {
        List<RouteLeg> legs = new ArrayList<>();
        for (int i = 0; i + 1 < sequence.size(); i++) {
            FarmLocation from = sequence.get(i);
            FarmLocation to = sequence.get(i + 1);
            int fromIndex = matrixIndex(from.id(), request);
            int toIndex = matrixIndex(to.id(), request);
            legs.add(new RouteLeg(from.id(), to.id(), round(matrix[fromIndex][toIndex])));
        }
        return List.copyOf(legs);
    }

    private int matrixIndex(long locationId, OptimizeSequenceRequest request) {
        if (locationId == request.depot().id()) {
            return 0;
        }
        for (int i = 0; i < request.farms().size(); i++) {
            if (request.farms().get(i).id() == locationId) {
                return i + 1;
            }
        }
        throw new IllegalStateException("Unknown location ID " + locationId);
    }

    private double[][] copyMatrix(double[][] source) {
        double[][] copy = new double[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
