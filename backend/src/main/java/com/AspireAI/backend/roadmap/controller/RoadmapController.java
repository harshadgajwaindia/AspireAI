package com.AspireAI.backend.roadmap.controller;

import com.AspireAI.backend.roadmap.dto.RoadmapItemDTO;
import com.AspireAI.backend.roadmap.dto.RoadmapPlanDTO;
import com.AspireAI.backend.roadmap.dto.RoadmapRequestDTO;
import com.AspireAI.backend.roadmap.service.RoadmapAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapAgentService roadmapAgent;

 
    @PostMapping("/generate")
    public ResponseEntity<RoadmapPlanDTO> generate(
            @RequestBody RoadmapRequestDTO request) {
        log.info("Roadmap generate: user={} company={} days={}",
                request.userId(), request.targetCompany(), request.durationDays());
        return ResponseEntity.ok(roadmapAgent.generatePlan(request));
    }

    
    @GetMapping("/{planId}")
    public ResponseEntity<RoadmapPlanDTO> getPlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(roadmapAgent.getPlan(planId));
    }

   
    @GetMapping("/today/{userId}")
    public ResponseEntity<List<RoadmapItemDTO>> getToday(@PathVariable UUID userId) {
        return ResponseEntity.ok(roadmapAgent.getTodaysTasks(userId));
    }

    
    @PatchMapping("/items/{itemId}/complete")
    public ResponseEntity<Void> complete(@PathVariable UUID itemId) {
        roadmapAgent.markItemComplete(itemId);
        return ResponseEntity.noContent().build();
    }

   
    @PatchMapping("/items/{itemId}/skip")
    public ResponseEntity<Void> skip(@PathVariable UUID itemId) {
        roadmapAgent.markItemSkipped(itemId);
        return ResponseEntity.noContent().build();
    }
}

