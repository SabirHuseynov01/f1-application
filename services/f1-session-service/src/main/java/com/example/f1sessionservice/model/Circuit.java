package com.example.f1sessionservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "circuits")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Circuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "circuit_key", unique = true, nullable = false)
    private Integer circuitKey;

    @Column(nullable = false)
    private String name;

    private String country;
    private String city;

    @Column(name = "track_length_km")
    private Double trackLengthKm;

    private Integer corners;
}
