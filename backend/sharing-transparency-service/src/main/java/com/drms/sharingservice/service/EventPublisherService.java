package com.drms.sharingservice.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherService {

    private static final String EXCHANGE = "drms.events";
    private static final Logger log = LoggerFactory.getLogger(EventPublisherService.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String routingKey, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
        } catch (AmqpException ex) {
            log.warn("RabbitMQ publish skipped for routing key {}: {}", routingKey, ex.getMessage());
        }
    }
}
