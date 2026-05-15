package com.example.f1timingservice.mapper;


import com.example.f1timingservice.dto.response.LapTimeResponseDTO;
import com.example.f1timingservice.dto.response.StintResponseDTO;
import com.example.f1timingservice.model.LapTime;
import com.example.f1timingservice.model.Stint;
import org.springframework.stereotype.Component;

@Component
public class TimingMapper {

    public LapTimeResponseDTO toLapDTO(LapTime lapTime) {
        if (lapTime == null) return null;

        return LapTimeResponseDTO.builder()
                .id(lapTime.getId())
                .sessionKey(lapTime.getSessionKey())
                .driverNumber(lapTime.getDriverNumber())
                .lapNumber(lapTime.getLapNumber())
                .lapDuration(lapTime.getLapDuration())
                .sector1Duration(lapTime.getSector1Duration())
                .sector2Duration(lapTime.getSector2Duration())
                .sector3Duration(lapTime.getSector3Duration())
                .isPitOutLap(lapTime.getIsPitOutLap())
                .build();
    }

    public StintResponseDTO toStintDTO(Stint stint) {
        if (stint == null) return null;

        return StintResponseDTO.builder()
                .id(stint.getId())
                .sessionKey(stint.getSessionKey())
                .driverNumber(stint.getDriverNumber())
                .stintNumber(stint.getStintNumber())
                .lapStart(stint.getLapStart())
                .lapEnd(stint.getLapEnd())
                .compound(stint.getCompound())
                .tyreAgeAtStart(stint.getTyreAgeAtStart())
                .build();
    }
}
