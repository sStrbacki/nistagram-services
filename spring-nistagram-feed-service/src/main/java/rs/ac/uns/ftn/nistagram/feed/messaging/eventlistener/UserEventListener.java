package rs.ac.uns.ftn.nistagram.feed.messaging.eventlistener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.nistagram.feed.messaging.config.RabbitMQConfig;
import rs.ac.uns.ftn.nistagram.feed.messaging.event.user.RegistrationFailedEvent;
import rs.ac.uns.ftn.nistagram.feed.messaging.event.user.UserBannedReply;
import rs.ac.uns.ftn.nistagram.feed.messaging.util.Converter;

@Slf4j
@Component
@AllArgsConstructor
public class UserEventListener {

    private static final String ROUTING_KEY = "";
    private final RabbitTemplate rabbitTemplate;
    private final Converter converter;

    @Async
    @EventListener
    public void onRegistrationFailedEvent(RegistrationFailedEvent event) {

        log.info("Publishing an {}, event: {}",
                RabbitMQConfig.REGISTRATION_FAILED_TOPIC, event);

        rabbitTemplate.convertAndSend(RabbitMQConfig.REGISTRATION_FAILED_TOPIC, ROUTING_KEY, converter.toJSON(event));

    }

    @Async
    @EventListener
    public void onUserBanFailedEvent(UserBannedReply reply){

        log.info("Publishing a reply to {}, reply: {}",
                RabbitMQConfig.USER_BAN_SAGA_REPLY_CHANNEL, reply);

        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_BAN_SAGA_REPLY_CHANNEL, converter.toJSON(reply));
    }

}
