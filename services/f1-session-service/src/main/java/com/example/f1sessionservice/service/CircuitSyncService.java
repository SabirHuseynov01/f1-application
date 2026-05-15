package com.example.f1sessionservice.service;

import com.example.f1sessionservice.model.Circuit;
import com.example.f1sessionservice.repository.CircuitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CircuitSyncService {

    private final CircuitRepository circuitRepository;

    private static final Map<String, CircuitInfo> STATIC_CIRCUIT_DATA = Map.ofEntries(
            Map.entry("Sakhir", new CircuitInfo("Bahrain International Circuit", 5.412, 15)),
            Map.entry("Jeddah", new CircuitInfo("Jeddah Street Circuit", 6.174, 27)),
            Map.entry("Melbourne", new CircuitInfo("Albert Park Circuit", 5.278, 14)),
            Map.entry("Suzuka", new CircuitInfo("Suzuka International Racing Course", 5.807, 18)),
            Map.entry("Shanghai", new CircuitInfo("Shanghai International Circuit", 5.451, 16)),
            Map.entry("Miami", new CircuitInfo("Miami International Autodrome", 5.410, 19)),
            Map.entry("Imola", new CircuitInfo("Imola Circuit (Autodromo Enzo e Dino Ferrari)", 4.909, 18)),
            Map.entry("Monte Carlo", new CircuitInfo("Circuit de Monaco", 3.337, 19)),
            Map.entry("Montreal", new CircuitInfo("Circuit Gilles-Villeneuve", 4.361, 14)),
            Map.entry("Catalunya", new CircuitInfo("Circuit de Barcelona-Catalunya", 4.675, 14)),
            Map.entry("Spielberg", new CircuitInfo("Red Bull Ring", 4.318, 10)),
            Map.entry("Silverstone", new CircuitInfo("Silverstone Circuit", 5.891, 18)),
            Map.entry("Hungaroring", new CircuitInfo("Hungaroring", 4.381, 14)),
            Map.entry("Spa-Francorchamps", new CircuitInfo("Circuit de Spa-Francorchamps", 7.004, 19)),
            Map.entry("Zandvoort", new CircuitInfo("Circuit Zandvoort", 4.259, 14)),
            Map.entry("Monza", new CircuitInfo("Autodromo Nazionale Monza", 5.793, 11)),
            Map.entry("Baku", new CircuitInfo("Baku City Circuit", 6.003, 20)),
            Map.entry("Singapore", new CircuitInfo("Marina Bay Street Circuit", 5.063, 19)),
            Map.entry("Austin", new CircuitInfo("Circuit of the Americas", 5.513, 20)),
            Map.entry("Mexico City", new CircuitInfo("Autodromo Hermanos Rodriguez", 4.304, 17)),
            Map.entry("Interlagos", new CircuitInfo("Interlagos Circuit", 4.309, 15)),
            Map.entry("Las Vegas", new CircuitInfo("Las Vegas Strip Circuit", 6.201, 17)),
            Map.entry("Lusail", new CircuitInfo("Lusail International Circuit", 5.419, 16)),
            Map.entry("Yas Marina Circuit", new CircuitInfo("Yas Marina Circuit", 5.281, 16))
    );

    @Transactional
    public void enrichCircuitDetails(Integer circuitKey) {
        Circuit circuit = circuitRepository.findByCircuitKey(circuitKey)
                .orElseThrow(() -> new RuntimeException("Circuit not found: " + circuitKey));

        CircuitInfo info = STATIC_CIRCUIT_DATA.get(circuit.getName());
        if (info != null) {
            circuit.setTrackLengthKm(info.length);
            circuit.setCorners(info.corners);
            log.info("Circuit {} enriched: {} km, {} corners",
                    circuit.getName(), info.length, info.corners);
        } else {
            log.warn("Circuit name '{}' for static data not found", circuit.getName());
        }

        circuitRepository.save(circuit);
    }

    @Transactional
    public void enrichAllCircuits() {
        List<Circuit> circuits = circuitRepository.findAll();
        List<Circuit> updatedCircuits = new ArrayList<>();

        for (Circuit circuit : circuits) {
            CircuitInfo info = STATIC_CIRCUIT_DATA.get(circuit.getName());
            if (info != null) {
                circuit.setTrackLengthKm(info.length);
                circuit.setCorners(info.corners);
                updatedCircuits.add(circuit);
                log.info("Circuit {} updated: {} km, {} corners",
                        circuit.getName(), info.length, info.corners);
            } else {
                log.warn("Circuit name '{}' not found in static data", circuit.getName());
            }
        }

        if (!updatedCircuits.isEmpty()) {
            circuitRepository.saveAll(updatedCircuits);
            log.info("Total {} circuits enriched and saved", updatedCircuits.size());
        }
    }

    private record CircuitInfo(String fullName, Double length, Integer corners) {}
}
