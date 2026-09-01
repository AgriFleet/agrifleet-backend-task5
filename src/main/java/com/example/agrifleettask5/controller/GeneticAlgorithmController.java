package com.example.agrifleettask5.controller;

import com.example.agrifleettask5.model.GeneticAlgorithmRequest;
import com.example.agrifleettask5.model.GeneticAlgorithmResponse;
import com.example.agrifleettask5.service.GeneticAlgorithmService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:3000" }, allowedHeaders = "*", methods = {
        org.springframework.web.bind.annotation.RequestMethod.GET,
        org.springframework.web.bind.annotation.RequestMethod.POST,
        org.springframework.web.bind.annotation.RequestMethod.PUT,
        org.springframework.web.bind.annotation.RequestMethod.DELETE,
        org.springframework.web.bind.annotation.RequestMethod.OPTIONS })
@RequestMapping("/api/v1/sequence")
public class GeneticAlgorithmController {
    private final GeneticAlgorithmService geneticAlgorithmService;

    public GeneticAlgorithmController(GeneticAlgorithmService geneticAlgorithmService) {
        this.geneticAlgorithmService = geneticAlgorithmService;
    }


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
