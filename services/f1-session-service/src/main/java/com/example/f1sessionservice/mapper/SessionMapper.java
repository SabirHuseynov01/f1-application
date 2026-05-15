package com.example.f1sessionservice.mapper;

import com.example.f1sessionservice.dto.response.DriverResponseDTO;
import com.example.f1sessionservice.dto.response.SeasonResponseDTO;
import com.example.f1sessionservice.dto.response.SessionResponseDTO;
import com.example.f1sessionservice.model.Season;
import com.example.f1sessionservice.model.Session;
import com.example.f1sessionservice.model.SessionDrivers;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionResponseDTO toSessionDTO(Session session) {
        return SessionResponseDTO.builder()
                .id(session.getId())
                .sessionKey(session.getSessionKey())
                .seasonYear(session.getSeason().getYear())
                .circuitName(session.getCircuit().getName())
                .country(session.getCircuit().getCountry())
                .city(session.getCircuit().getCity())
                .sessionType(session.getSessionType())
                .dateStart(session.getDateStart())
                .dateEnd(session.getDateEnd())
                .build();
    }

    public DriverResponseDTO toDriverDTO(SessionDrivers driver) {
        return DriverResponseDTO.builder()
                .id(driver.getId())
                .driverNumber(driver.getDriverNumber())
                .driverCode(driver.getDriverCode())
                .fullName(driver.getFullName())
                .teamName(driver.getTeamName())
                .teamColour(driver.getTeamColour())
                .build();
    }

    public SeasonResponseDTO toSeasonDTO(Season season) {
        return SeasonResponseDTO.builder()
                .id(season.getId())
                .year(season.getYear())
                .championshipName(season.getChampionshipName())
                .build();
    }
}
