package com.example.agrifleettask5.controller;

import com.example.agrifleettask5.model.FarmSelectionRequest;
import com.example.agrifleettask5.model.FarmSelectionResponse;
import com.example.agrifleettask5.service.FarmSelectionService;
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
@RequestMapping("/api/v1/selection")
public class FarmSelectionController {
    private final FarmSelectionService service;

    public FarmSelectionController(FarmSelectionService service) {
        this.service = service;
    }

    @PostMapping("/maximize-acreage-value")
    public ResponseEntity<FarmSelectionResponse> maximizeAcreageAndValue(
            @RequestBody FarmSelectionRequest request) {
        return ResponseEntity.ok(service.select(request));
    }
}
