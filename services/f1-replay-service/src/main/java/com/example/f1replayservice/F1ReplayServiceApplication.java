package com.example.f1replayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class F1ReplayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(F1ReplayServiceApplication.class, args);
    }

}
