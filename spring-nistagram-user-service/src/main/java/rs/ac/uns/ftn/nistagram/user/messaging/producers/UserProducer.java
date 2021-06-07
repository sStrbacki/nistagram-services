package rs.ac.uns.ftn.nistagram.user.messaging.producers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.nistagram.user.domain.user.User;
import rs.ac.uns.ftn.nistagram.user.messaging.config.RabbitMQConfig;
import rs.ac.uns.ftn.nistagram.user.messaging.mappers.UserTopicPayloadMapper;

@Service
@Slf4j
@AllArgsConstructor
public class UserProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishCreatedUser(User user){

        var userTopicPayload = UserTopicPayloadMapper.toPayload(user);
        log.info("User with an username {} published to a {} ",
                user.getUsername(), RabbitMQConfig.USER_CREATED_GRAPH_SERVICE);
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_CREATED_GRAPH_SERVICE,
                userTopicPayload);
        log.info("User with an username {} published to a {} ",
                user.getUsername(), RabbitMQConfig.USER_CREATED_FEED_SERVICE);
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_CREATED_FEED_SERVICE,
                userTopicPayload);
    }


}