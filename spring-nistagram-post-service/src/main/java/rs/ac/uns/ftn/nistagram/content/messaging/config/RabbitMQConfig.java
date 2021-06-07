package rs.ac.uns.ftn.nistagram.content.messaging.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String POST_CREATED_FEED_SERVICE = "post.created.feed-service";
    public static final String POST_DELETED_FEED_SERVICE = "post.deleted.feed-service";

    @Bean
    public RabbitTemplate jsonRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonConverter());
        return template;
    }

    @Bean
    public MessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue postCreatedFeedQueue() {
        return new Queue(POST_CREATED_FEED_SERVICE);
    }
    @Bean
    public Queue postDeletedFeedQueue(){ return new Queue(POST_DELETED_FEED_SERVICE); }
}