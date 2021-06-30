package rs.ac.uns.ftn.nistagram.user.graph.messaging.eventhandler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.nistagram.user.graph.messaging.config.RabbitMQConfig;
import rs.ac.uns.ftn.nistagram.user.graph.messaging.event.user.UserBannedEvent;
import rs.ac.uns.ftn.nistagram.user.graph.messaging.event.user.UserCreatedEvent;
import rs.ac.uns.ftn.nistagram.user.graph.messaging.event.user.UserUpdatedEvent;
import rs.ac.uns.ftn.nistagram.user.graph.messaging.mappers.EventPayloadMapper;
import rs.ac.uns.ftn.nistagram.user.graph.messaging.util.Converter;
import rs.ac.uns.ftn.nistagram.user.graph.messaging.util.TransactionIdHolder;
import rs.ac.uns.ftn.nistagram.user.graph.services.UserService;

@Slf4j
@Component
@AllArgsConstructor
public class UserEventHandler {

    private final UserService userService;
    private final Converter converter;
    private final TransactionIdHolder transactionIdHolder;

    @RabbitListener(queues = {RabbitMQConfig.USER_CREATED_EVENT})
    public void handleUserCreated(@Payload String payload) {

        log.debug("Handling a created user event {}", payload);

        UserCreatedEvent event = converter.toObject(payload, UserCreatedEvent.class);

        transactionIdHolder.setCurrentTransactionId(event.getTransactionId());

        userService.create(EventPayloadMapper.toDomain(event.getUserEventPayload()));

    }

    @RabbitListener(queues = {RabbitMQConfig.USER_UPDATED_EVENT})
    public void handleUserUpdated(@Payload String payload) {

        log.debug("Handling a created user event {}", payload);

        UserUpdatedEvent event = converter.toObject(payload, UserUpdatedEvent.class);

        transactionIdHolder.setCurrentTransactionId(event.getTransactionId());

        userService.create(EventPayloadMapper.toDomain(event.getUserEventPayload()));

    }

    @RabbitListener(queues = {RabbitMQConfig.USER_BANNED_EVENT})
    public void handleUserBanned(@Payload String payload) {

        log.debug("Handling a banned user event {}", payload);

        UserBannedEvent event = converter.toObject(payload, UserBannedEvent.class);

        transactionIdHolder.setCurrentTransactionId(event.getTransactionId());

        userService.delete(EventPayloadMapper.toDomain(event.getUserEventPayload()));

    }


}
