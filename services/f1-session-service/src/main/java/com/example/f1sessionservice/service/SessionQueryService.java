package com.example.f1sessionservice.service;


    import com.example.f1sessionservice.exception.SessionNotFoundException;
    import com.example.f1sessionservice.model.*;
    import com.example.f1sessionservice.repository.CircuitRepository;
    import com.example.f1sessionservice.repository.SeasonRepository;
    import com.example.f1sessionservice.repository.SessionDriverRepository;
    import com.example.f1sessionservice.repository.SessionRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.cache.annotation.Cacheable;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class SessionQueryService {

        private final SessionRepository sessionRepository;
        private final SessionDriverRepository sessionDriverRepository;
        private final SeasonRepository seasonRepository;
        private final CircuitRepository circuitRepository;

        @Cacheable(value = "sessionsByYear", key = "#year")
        @Transactional(readOnly = true)
        public List<Session> getSessionsByYear(Integer year) {
            return sessionRepository.findBySeasonYear(year);
        }

        @Cacheable(value = "sessionsByYearAndType", key = "#year + '_' + #type")
        @Transactional(readOnly = true)
        public List<Session> getSessionsByYearAndType(Integer year, SessionType type) {
            return sessionRepository.findBySeasonYearAndType(year, type);
        }

        @Transactional(readOnly = true)
        public Session getSessionByKey(Integer sessionKey) {
            return sessionRepository.findBySessionKey(sessionKey)
                    .orElseThrow(() -> new SessionNotFoundException(sessionKey));
        }

        @Transactional(readOnly = true)
        public List<SessionDrivers> getDriversBySession(Long sessionId) {
            return sessionDriverRepository.findBySessionId(sessionId);
        }

        @Transactional(readOnly = true)
        @Cacheable(value = "seasons")
        public List<Season> getAllSeasons() {
            return seasonRepository.findAll();
        }

        @Transactional(readOnly = true)
        public List<SessionDrivers> getAllDrivers() {
            return sessionDriverRepository.findAll();
        }

        @Transactional(readOnly = true)
        public List<Circuit> getAllCircuits() {
            return circuitRepository.findAll();
        }
    }
