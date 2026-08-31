package com.example.agrifleettask5.controller;

import com.example.agrifleettask5.model.GeneticAlgorithmRequest;
import com.example.agrifleettask5.model.GeneticAlgorithmResponse;
import com.example.agrifleettask5.service.GeneticAlgorithmService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AG-20: Genetic Algorithm tour optimization controller.
 * Provides REST endpoint for optimizing visit sequences using genetic algorithm.
 * Suitable for large farm sets where exact algorithms are impractical.
 */
@RestController
@RequestMapping("/api/v1/sequence")
public class GeneticAlgorithmController {
    private final GeneticAlgorithmService geneticAlgorithmService;

    public GeneticAlgorithmController(GeneticAlgorithmService geneticAlgorithmService) {
        this.geneticAlgorithmService = geneticAlgorithmService;
    }

    /**
     * Optimize tour sequence using genetic algorithm.
     * AG-20: Genetic Algorithm optimization for large-scale TSP instances.
     *
     * @param request the optimization request with depot, farms, and optional GA parameters
     * @return optimized tour sequence with distance and fuel estimates
     */
    @PostMapping("/optimize-genetic-algorithm")
    public ResponseEntity<GeneticAlgorithmResponse> optimizeUsingGeneticAlgorithm(
            @RequestBody GeneticAlgorithmRequest request) {
        try {
            GeneticAlgorithmResponse response = geneticAlgorithmService.optimize(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
