package com.example.f1sessionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class F1SessionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(F1SessionServiceApplication.class, args);
    }
}
