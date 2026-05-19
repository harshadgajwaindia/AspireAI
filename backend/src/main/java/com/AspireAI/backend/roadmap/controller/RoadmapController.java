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

/**
 * REST controller for the Roadmap Agent.
 *
 * Endpoints:
 *
 * POST   /api/v1/roadmap/generate          → Create a new plan from gap report
 * GET    /api/v1/roadmap/{planId}           → Get full plan with all weeks/items
 * GET    /api/v1/roadmap/today/{userId}     → Get today's tasks for dashboard
 * PATCH  /api/v1/roadmap/items/{itemId}/complete  → Mark item done
 * PATCH  /api/v1/roadmap/items/{itemId}/skip      → Mark item skipped
 *
 * Example frontend integration:
 * After Analyzer returns SkillGapReportDTO, the "Build Roadmap →" button
 * POSTs to /generate with that report + user preferences.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapAgentService roadmapAgent;

    /**
     * Generate a new study roadmap.
     *
     * Example curl:
     * curl -X POST http://localhost:8080/api/v1/roadmap/generate \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "userId": "550e8400-...",
     *     "targetCompany": "TCS Digital",
     *     "durationDays": 30,
     *     "dailyStudyMinutes": 120,
     *     "skillGapReport": { ...from analyzer... }
     *   }'
     */
    @PostMapping("/generate")
    public ResponseEntity<RoadmapPlanDTO> generate(
            @RequestBody RoadmapRequestDTO request) {
        log.info("Roadmap generate: user={} company={} days={}",
                request.userId(), request.targetCompany(), request.durationDays());
        return ResponseEntity.ok(roadmapAgent.generatePlan(request));
    }

    /**
     * Get a full plan by ID (all items grouped by week).
     */
    @GetMapping("/{planId}")
    public ResponseEntity<RoadmapPlanDTO> getPlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(roadmapAgent.getPlan(planId));
    }

    /**
     * Get today's pending tasks — used by the dashboard "Today's Focus" widget.
     */
    @GetMapping("/today/{userId}")
    public ResponseEntity<List<RoadmapItemDTO>> getToday(@PathVariable UUID userId) {
        return ResponseEntity.ok(roadmapAgent.getTodaysTasks(userId));
    }

    /**
     * Mark a roadmap item as completed.
     * Called when the student ticks off a task.
     */
    @PatchMapping("/items/{itemId}/complete")
    public ResponseEntity<Void> complete(@PathVariable UUID itemId) {
        roadmapAgent.markItemComplete(itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mark a roadmap item as skipped.
     * The frontend can prompt: "Skipped? We'll add this back next week."
     */
    @PatchMapping("/items/{itemId}/skip")
    public ResponseEntity<Void> skip(@PathVariable UUID itemId) {
        roadmapAgent.markItemSkipped(itemId);
        return ResponseEntity.noContent().build();
    }
}

