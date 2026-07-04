package com.drms.sharingservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange("drms.events");
    }

    @Bean
    public Queue sharingAuditQueue() {
        return new Queue("drms.sharing.audit", true);
    }

    @Bean
    public Binding reservedBinding(Queue sharingAuditQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(sharingAuditQueue).to(eventExchange).with("resource.reserved");
    }

    @Bean
    public Binding donationBinding(Queue sharingAuditQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(sharingAuditQueue).to(eventExchange).with("donation.logged");
    }
}
