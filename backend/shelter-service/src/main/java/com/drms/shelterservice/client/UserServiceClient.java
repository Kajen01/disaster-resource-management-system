package com.drms.shelterservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/internal/exists/{id}")
    boolean existsByIdAndRole(@PathVariable("id") Long id, @RequestParam("role") String role);
}
