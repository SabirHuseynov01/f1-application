package com.example.f1notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class F1NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(F1NotificationServiceApplication.class, args);
    }

}
