package com.example.f1analyticsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class F1AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(F1AnalyticsServiceApplication.class, args);
    }

}
