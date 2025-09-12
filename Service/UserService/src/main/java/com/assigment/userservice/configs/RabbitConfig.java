package com.assigment.userservice.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "mail.exchange";

    // Verification
    public static final String VERIFICATION_ROUTING_KEY = "mail.verification";
    public static final String VERIFICATION_QUEUE = "mail.verification.queue";

    // Password Reset
    public static final String RESET_ROUTING_KEY = "mail.reset";
    public static final String RESET_QUEUE = "mail.reset.queue";

    @Bean
    public TopicExchange mailExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Verification queue + binding
    @Bean
    public Queue verificationQueue() {
        return new Queue(VERIFICATION_QUEUE, true);
    }

    @Bean
    public Binding verificationBinding(Queue verificationQueue, TopicExchange mailExchange) {
        return BindingBuilder.bind(verificationQueue)
                .to(mailExchange)
                .with(VERIFICATION_ROUTING_KEY);
    }

    // Reset queue + binding
    @Bean
    public Queue resetQueue() {
        return new Queue(RESET_QUEUE, true);
    }

    @Bean
    public Binding resetBinding(Queue resetQueue, TopicExchange mailExchange) {
        return BindingBuilder.bind(resetQueue)
                .to(mailExchange)
                .with(RESET_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
