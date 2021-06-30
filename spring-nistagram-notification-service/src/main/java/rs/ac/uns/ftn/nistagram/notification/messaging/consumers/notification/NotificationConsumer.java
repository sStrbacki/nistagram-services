package rs.ac.uns.ftn.nistagram.notification.messaging.consumers.notification;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.nistagram.notification.domain.NotificationType;
import rs.ac.uns.ftn.nistagram.notification.messaging.config.RabbitMQConfig;
import rs.ac.uns.ftn.nistagram.notification.messaging.mappers.EventPayloadMapper;
import rs.ac.uns.ftn.nistagram.notification.messaging.payload.notification.PostCommentedTopicPayload;
import rs.ac.uns.ftn.nistagram.notification.messaging.payload.notification.PostInteractionTopicPayload;
import rs.ac.uns.ftn.nistagram.notification.messaging.payload.notification.UserRelationTopicPayload;
import rs.ac.uns.ftn.nistagram.notification.messaging.payload.notification.UsersTaggedTopicPayload;
import rs.ac.uns.ftn.nistagram.notification.services.NotificationService;

import java.io.IOException;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.USERS_TAGGED_NOTIFICATION_SERVICE)
    public void consumeUsersTagged(UsersTaggedTopicPayload payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        acknowledgeMessage(channel, tag);
        notificationService.handleTaggedUsers(EventPayloadMapper.toDomain(payload));
    }

    @RabbitListener(queues = RabbitMQConfig.POST_COMMENTED_NOTIFICATION_SERVICE)
    public void consumePostCommented(PostCommentedTopicPayload payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        acknowledgeMessage(channel, tag);
        notificationService.handlePostComments(EventPayloadMapper.toDomain(payload));
    }

    @RabbitListener(queues = RabbitMQConfig.POST_LIKED_NOTIFICATION_SERVICE)
    public void consumePostLiked(PostInteractionTopicPayload payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        acknowledgeMessage(channel, tag);
        notificationService.handlePostLiked(EventPayloadMapper.toDomain(payload, NotificationType.NEW_LIKE));
    }

    @RabbitListener(queues = RabbitMQConfig.POST_DISLIKED_NOTIFICATION_SERVICE)
    public void consumePostDisliked(PostInteractionTopicPayload payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        acknowledgeMessage(channel, tag);
        notificationService.handlePostDisliked(EventPayloadMapper.toDomain(payload, NotificationType.NEW_DISLIKE));
    }

    @RabbitListener(queues = RabbitMQConfig.POST_SHARED_NOTIFICATION_SERVICE)
    public void consumePostShared(PostInteractionTopicPayload payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        acknowledgeMessage(channel, tag);
        notificationService.handlePostShared(EventPayloadMapper.toDomain(payload, NotificationType.NEW_SHARE));
    }

    @RabbitListener(queues = RabbitMQConfig.NEW_FOLLOW_NOTIFICATION_SERVICE)
    public void consumeUserTagged(UserRelationTopicPayload payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        acknowledgeMessage(channel, tag);
        notificationService.handleNewFollow(EventPayloadMapper.toDomain(payload));
    }

    @RabbitListener(queues = RabbitMQConfig.FOLLOW_REQUESTED_NOTIFICATION_SERVICE)
    public void consumeFollowRequested(UserRelationTopicPayload payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        acknowledgeMessage(channel, tag);
        notificationService.handleFollowRequested(EventPayloadMapper.toDomain(payload));
    }

    @RabbitListener(queues = RabbitMQConfig.FOLLOW_ACCEPTED_NOTIFICATION_SERVICE)
    public void consumeFollowAccepted(UserRelationTopicPayload payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        acknowledgeMessage(channel, tag);
        notificationService.handleFollowAccepted(EventPayloadMapper.toDomain(payload));
    }

    private void acknowledgeMessage(Channel channel, long tag) throws IOException {
        try {
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, true);
        }
    }
}
