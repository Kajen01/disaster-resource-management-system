package com.drms.apigateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record RouteSecurityProperties(
        String jwtSecret,
        List<String> publicPaths,
        List<RoleRule> roleRules
) {

    public record RoleRule(String path, List<String> roles) {
    }
}
