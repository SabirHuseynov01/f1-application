package com.example.f1sessionservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "session_drivers", uniqueConstraints = @UniqueConstraint
        (columnNames = {"session_id", "driver_number"}, name = "uk_session_driver"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDrivers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(name = "driver_number")
    private Integer driverNumber;

    @Column(name = "driver_code")
    private String driverCode; // "VER", "RUS", "PIA"

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "team_colour")
    private String teamColour; // "#3671C6" hex kodu

}
