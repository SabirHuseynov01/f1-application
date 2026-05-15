package com.example.f1sessionservice.controller;

import com.example.f1sessionservice.dto.response.ApiResponse;
import com.example.f1sessionservice.dto.response.SeasonResponseDTO;
import com.example.f1sessionservice.mapper.SessionMapper;
import com.example.f1sessionservice.service.SessionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
public class SeasonController extends BaseController{

    private final SessionQueryService queryService;
    private final SessionMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeasonResponseDTO>>> getAllSeasons() {
        List<SeasonResponseDTO> seasons = queryService.getAllSeasons().stream()
                .map(mapper::toSeasonDTO)
                .toList();
        return ok(seasons);
    }
}
