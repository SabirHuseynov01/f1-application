package com.example.f1replayservice.controller;

import com.example.f1replayservice.dto.ReplayControlDTO;
import com.example.f1replayservice.dto.ReplayFrameDTO;
import com.example.f1replayservice.dto.ReplaySessionDTO;
import com.example.f1replayservice.service.ReplayEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/replay")
@RequiredArgsConstructor
public class ReplayController {

    private final ReplayEngineService replayEngineService;

    @PostMapping("/create/{sessionKey}")
    public ResponseEntity<ReplaySessionDTO> createReplay(
            @PathVariable Integer sessionKey,
            @RequestParam String name) {
        return ResponseEntity.ok(replayEngineService.createReplay(sessionKey, name));
    }

    @PostMapping("/control")
    public ResponseEntity<Void> controlReplay(@RequestBody ReplayControlDTO control) {
        replayEngineService.controlReplay(control);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{replaySessionId}/frame")
    public ResponseEntity<ReplayFrameDTO> getFrame(@PathVariable Long replaySessionId) {
        return ResponseEntity.ok(replayEngineService.getCurrentFrame(replaySessionId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("replay-service UP");
    }
}
