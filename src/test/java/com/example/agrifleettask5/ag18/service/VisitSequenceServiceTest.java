package com.example.agrifleettask5.ag18.service;

import com.example.agrifleettask5.ag18.algorithm.HeldKarpTourOptimizer;
import com.example.agrifleettask5.ag18.algorithm.NearestNeighbourTourOptimizer;
import com.example.agrifleettask5.ag18.model.FarmLocation;
import com.example.agrifleettask5.ag18.model.OptimizeSequenceRequest;
import com.example.agrifleettask5.ag18.model.OptimizeSequenceResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitSequenceServiceTest {
    private final VisitSequenceService service = new VisitSequenceService(
            new HeldKarpTourOptimizer(), new NearestNeighbourTourOptimizer());

    @Test
    void returnsDepotFarmOrderDepotAndFuelEstimate() {
        FarmLocation depot = new FarmLocation(1, "Depot", 8.31, 80.40);
        List<FarmLocation> farms = List.of(
                new FarmLocation(2, "A", 8.32, 80.41),
                new FarmLocation(3, "B", 8.33, 80.42),
                new FarmLocation(4, "C", 8.34, 80.43)
        );
        double[][] distances = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };

        OptimizeSequenceResponse response = service.optimize(
                new OptimizeSequenceRequest(depot, farms, distances, true, 0.25));

        assertEquals(80.0, response.totalDistanceKm(), 0.0001);
        assertEquals(20.0, response.estimatedFuelLitres(), 0.0001);
        assertEquals(5, response.visitSequence().size());
        assertEquals(depot.id(), response.visitSequence().get(0).id());
        assertEquals(depot.id(), response.visitSequence().get(4).id());
        assertEquals(4, response.legs().size());
        assertEquals("HELD_KARP_DP", response.algorithm());
    }

    @Test
    void rejectsDuplicateLocationIds() {
        FarmLocation depot = new FarmLocation(1, "Depot", 8.31, 80.40);
        FarmLocation invalidFarm = new FarmLocation(1, "Duplicate", 8.32, 80.41);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.optimize(new OptimizeSequenceRequest(
                        depot, List.of(invalidFarm), null, true, 0.25)));

        assertTrue(exception.getMessage().contains("unique"));
    }
}
