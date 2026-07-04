package com.drms.sharingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SharingTransparencyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SharingTransparencyServiceApplication.class, args);
    }
}
