package com.example.f1timingservice.scheduler;

import com.example.f1timingservice.service.TimingSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class TimingSyncScheduler {

    private final TimingSyncService syncService;

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void scheduledSync(){
        log.debug("Scheduled sync started - Waiting for active session list");
    }
}
