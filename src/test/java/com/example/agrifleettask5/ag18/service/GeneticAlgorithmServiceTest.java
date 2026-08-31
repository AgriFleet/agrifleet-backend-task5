package com.example.agrifleettask5.ag18.service;

import com.example.agrifleettask5.algorithm.GeneticAlgorithmTourOptimizer;
import com.example.agrifleettask5.model.FarmLocation;
import com.example.agrifleettask5.model.GeneticAlgorithmRequest;
import com.example.agrifleettask5.model.GeneticAlgorithmResponse;
import com.example.agrifleettask5.service.GeneticAlgorithmService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneticAlgorithmServiceTest {
    private final GeneticAlgorithmService service = new GeneticAlgorithmService(
            new GeneticAlgorithmTourOptimizer());

    @Test
    void returnsValidTourWithFuelEstimate() {
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

        GeneticAlgorithmResponse response = service.optimize(
                new GeneticAlgorithmRequest(depot, farms, distances, true, 0.25, 100, 100, 0.02));

        assertNotNull(response);
        assertEquals(5, response.visitSequence().size());
        assertEquals(depot.id(), response.visitSequence().get(0).id());
        assertEquals(depot.id(), response.visitSequence().get(4).id());
        assertEquals(4, response.legs().size());
        assertEquals("GA", response.algorithm());
        assertTrue(response.totalDistanceKm() > 0);
        assertTrue(response.estimatedFuelLitres() > 0);
    }

    @Test
    void acceptsCustomGAParameters() {
        FarmLocation depot = new FarmLocation(1, "Depot", 0.0, 0.0);
        List<FarmLocation> farms = List.of(
                new FarmLocation(2, "A", 1.0, 1.0),
                new FarmLocation(3, "B", 2.0, 2.0)
        );
        double[][] distances = {
                {0, 100, 200},
                {100, 0, 150},
                {200, 150, 0}
        };

        GeneticAlgorithmResponse response = service.optimize(
                new GeneticAlgorithmRequest(depot, farms, distances, true, 0.5, 50, 200, 0.05));

        assertNotNull(response);
        assertTrue(response.visitSequence().size() >= 3);
    }

    @Test
    void rejectsDuplicateLocationIds() {
        FarmLocation depot = new FarmLocation(1, "Depot", 0.0, 0.0);
        FarmLocation invalidFarm = new FarmLocation(1, "Duplicate", 1.0, 1.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.optimize(new GeneticAlgorithmRequest(
                        depot, List.of(invalidFarm), null, true, 0.25, null, null, null)));

        assertTrue(exception.getMessage().contains("unique"));
    }

    @Test
    void rejectsInvalidCoordinates() {
        FarmLocation depot = new FarmLocation(1, "Depot", 91.0, 0.0); // Invalid latitude
        FarmLocation farm = new FarmLocation(2, "A", 0.0, 0.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.optimize(new GeneticAlgorithmRequest(
                        depot, List.of(farm), null, true, 0.25, null, null, null)));

        assertTrue(exception.getMessage().contains("latitude"));
    }

    @Test
    void rejectsNullDepot() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.optimize(new GeneticAlgorithmRequest(
                        null, List.of(), null, true, 0.25, null, null, null)));

        assertTrue(exception.getMessage().contains("depot"));
    }

    @Test
    void rejectsNullFarmsList() {
        FarmLocation depot = new FarmLocation(1, "Depot", 0.0, 0.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.optimize(new GeneticAlgorithmRequest(
                        depot, null, null, true, 0.25, null, null, null)));

        assertTrue(exception.getMessage().contains("farms"));
    }

    @Test
    void rejectsInvalidPopulationSize() {
        FarmLocation depot = new FarmLocation(1, "Depot", 0.0, 0.0);
        List<FarmLocation> farms = List.of(new FarmLocation(2, "A", 1.0, 1.0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.optimize(new GeneticAlgorithmRequest(
                        depot, farms, null, true, 0.25, 5, 100, 0.02))); // Too small

        assertTrue(exception.getMessage().contains("populationSize"));
    }

    @Test
    void rejectsInvalidMutationRate() {
        FarmLocation depot = new FarmLocation(1, "Depot", 0.0, 0.0);
        List<FarmLocation> farms = List.of(new FarmLocation(2, "A", 1.0, 1.0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.optimize(new GeneticAlgorithmRequest(
                        depot, farms, null, true, 0.25, 100, 100, 1.5))); // Invalid rate

        assertTrue(exception.getMessage().contains("mutationRate"));
    }

    @Test
    void calculatesHaversineDistancesWhenMatrixIsNull() {
        FarmLocation depot = new FarmLocation(1, "Depot", 8.31, 80.40);
        List<FarmLocation> farms = List.of(
                new FarmLocation(2, "A", 8.32, 80.41),
                new FarmLocation(3, "B", 8.33, 80.42)
        );

        GeneticAlgorithmResponse response = service.optimize(
                new GeneticAlgorithmRequest(depot, farms, null, true, 0.25, 100, 100, 0.02));

        assertNotNull(response);
        assertTrue(response.visitSequence().size() >= 3);
        assertTrue(response.totalDistanceKm() > 0);
    }

    @Test
    void handlesOpenRouteCorrectly() {
        FarmLocation depot = new FarmLocation(1, "Depot", 0.0, 0.0);
        List<FarmLocation> farms = List.of(new FarmLocation(2, "A", 1.0, 1.0));
        double[][] distances = {{0, 100}, {100, 0}};

        GeneticAlgorithmResponse responseRoundTrip = service.optimize(
                new GeneticAlgorithmRequest(depot, farms, distances, true, 0.0, 100, 100, 0.02));
        GeneticAlgorithmResponse responseOpen = service.optimize(
                new GeneticAlgorithmRequest(depot, farms, distances, false, 0.0, 100, 100, 0.02));

        assertTrue(responseRoundTrip.totalDistanceKm() >= responseOpen.totalDistanceKm());
    }

    @Test
    void defaultGAParametersWork() {
        FarmLocation depot = new FarmLocation(1, "Depot", 0.0, 0.0);
        List<FarmLocation> farms = List.of(
                new FarmLocation(2, "A", 1.0, 1.0),
                new FarmLocation(3, "B", 2.0, 2.0)
        );
        double[][] distances = {
                {0, 100, 200},
                {100, 0, 150},
                {200, 150, 0}
        };

        // All GA parameters are null — should use defaults
        GeneticAlgorithmResponse response = service.optimize(
                new GeneticAlgorithmRequest(depot, farms, distances, true, 0.5, null, null, null));

        assertNotNull(response);
        assertTrue(response.visitSequence().size() >= 3);
    }
}
