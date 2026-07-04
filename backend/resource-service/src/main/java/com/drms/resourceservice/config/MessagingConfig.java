package com.drms.resourceservice.config;

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
    public Queue resourceAuditQueue() {
        return new Queue("drms.resource.audit", true);
    }

    @Bean
    public Binding shortageCreatedBinding(Queue resourceAuditQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(resourceAuditQueue).to(eventExchange).with("shortage.created");
    }

    @Bean
    public Binding transferCompletedBinding(Queue resourceAuditQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(resourceAuditQueue).to(eventExchange).with("transfer.completed");
    }
}
