package com.example.agrifleettask5.controller;

import com.example.agrifleettask5.model.OptimizeSequenceRequest;
import com.example.agrifleettask5.model.OptimizeSequenceResponse;
import com.example.agrifleettask5.service.VisitSequenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tours")
public class VisitSequenceController {
    private final VisitSequenceService service;

    public VisitSequenceController(VisitSequenceService service) {
        this.service = service;
    }

    @PostMapping("/optimize-sequence")
    public ResponseEntity<OptimizeSequenceResponse> optimizeSequence(
            @RequestBody OptimizeSequenceRequest request) {
        return ResponseEntity.ok(service.optimize(request));
    }
}
