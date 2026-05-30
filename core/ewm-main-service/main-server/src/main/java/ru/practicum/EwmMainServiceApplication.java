package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum")
public class EwmMainServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EwmMainServiceApplication.class, args);
    }
}
