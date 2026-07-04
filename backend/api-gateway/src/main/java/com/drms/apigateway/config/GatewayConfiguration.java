package com.drms.apigateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RouteSecurityProperties.class)
public class GatewayConfiguration {
}
