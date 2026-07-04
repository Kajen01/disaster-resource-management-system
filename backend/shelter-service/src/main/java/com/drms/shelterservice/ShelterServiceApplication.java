package com.drms.shelterservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ShelterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShelterServiceApplication.class, args);
    }
}
