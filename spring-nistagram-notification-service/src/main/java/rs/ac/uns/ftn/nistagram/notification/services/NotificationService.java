package rs.ac.uns.ftn.nistagram.notification.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.nistagram.notification.controllers.mappers.NotificationMapper;
import rs.ac.uns.ftn.nistagram.notification.domain.Notification;
import rs.ac.uns.ftn.nistagram.notification.exceptions.OperationNotPermittedException;
import rs.ac.uns.ftn.nistagram.notification.http.UserClient;
import rs.ac.uns.ftn.nistagram.notification.http.UserGraphClient;
import rs.ac.uns.ftn.nistagram.notification.http.payload.NotificationPreferencesDTO;
import rs.ac.uns.ftn.nistagram.notification.repositories.NotificationRepository;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;
    private final UserGraphClient userGraphClient;
    private final SimpMessagingTemplate template;

    public void handleTaggedUsers(List<Notification> taggedUserNotifications) {
        taggedUserNotifications.forEach(this::handleTaggedUser);
    }

    public List<Notification> get(String username) {
        List<Notification> notifications = notificationRepository.findByUsername(username);
        if (notifications == null)
            return new ArrayList<>();
        return notifications;
    }

    public Notification hide(String username, Long notificationId) {
        log.info("Hiding notification with an id: {} for an user '{}'", notificationId, username);

        Notification notification = notificationRepository.getById(notificationId);
        if (!notification.getTarget().equals(username)) {
            log.warn("Cannot hide notification with an id: {} because user '{}' doesn't own it", notificationId, username);
            throw new OperationNotPermittedException("Cannot hide this notification");
        }

        log.info("Notification with an id: {} successfully hidden", notificationId);
        notification.setSeen(true);
        notificationRepository.save(notification);
        return notification;
    }

    public void handleNewFollow(Notification newFollowNotification) {
        log.info("Handling new follow notification for an user '{}'",
                newFollowNotification.getTarget());

        NotificationPreferencesDTO preferences = userClient.getNotificationPreferences(newFollowNotification.getTarget());

        handleNotification(newFollowNotification,
                preferences.isNewFollowerNotificationEnabled());

    }

    public void handleFollowRequested(Notification followRequestedNotification) {
        log.info("Handling follow requested notification for an user '{}'",
                followRequestedNotification.getTarget());
        NotificationPreferencesDTO preferences = userClient.getNotificationPreferences(followRequestedNotification.getTarget());

        handleNotification(followRequestedNotification,
                preferences.isFollowerRequestNotificationEnabled());

    }

    public void handleFollowAccepted(Notification followAcceptedNotification) {
        log.info("Handling follow accepted notification for an user '{}'",
                followAcceptedNotification.getTarget());

        NotificationPreferencesDTO preferences = userClient.getNotificationPreferences(followAcceptedNotification.getTarget());

        handleNotification(followAcceptedNotification,
                preferences.isFollowRequestAcceptedNotificationEnabled());

    }

    public void handlePostComments(Notification postCommentedNotification) {
        log.info("Handling post commented notification for an user '{}'",
                postCommentedNotification.getTarget());

        NotificationPreferencesDTO preferences = userClient.getNotificationPreferences(postCommentedNotification.getTarget());

        handleNotification(postCommentedNotification,
                commentsPreferencesSatisfied(postCommentedNotification, preferences));

    }

    public void handlePostLiked(Notification postLikedNotification) {
        log.info("Handling post liked notification for an user '{}'", postLikedNotification.getTarget());

        NotificationPreferencesDTO preferences = userClient.getNotificationPreferences(postLikedNotification.getTarget());

        handleNotification(postLikedNotification,
                postLikedPreferencesSatisfied(postLikedNotification, preferences));
    }


    public void handleTaggedUser(Notification taggedUserNotification) {
        log.info("Handling tagged on post notification for an user '{}'", taggedUserNotification.getTarget());

        NotificationPreferencesDTO preferences = userClient.getNotificationPreferences(taggedUserNotification.getTarget());

        handleNotification(taggedUserNotification,
                taggedPreferencesSatisfied(taggedUserNotification, preferences));

    }

    private void handleNotification(Notification notification, Boolean condition) {
        if (condition) {
            notificationRepository.save(notification);
            log.info("Notification satisfies user preferences");
            notify(notification.getTarget(), notification);
            return;
        }
        log.info("Notification doesn't satisfy user preferences");
    }

    private void notify(String username, Notification notification) {
        log.info("Sending notification {} to an user '{}'",
                notification.getText(), username);
        this.template.convertAndSendToUser(username, "/queue/notify",
                NotificationMapper.toDto(notification));
    }

    private boolean commentsPreferencesSatisfied(Notification postCommentedNotification, NotificationPreferencesDTO preferences) {
        return preferences.commentNotificationEnabled() ||
                (preferences.commentNotificationEnabledForFollowers()
                        && isFollowing(postCommentedNotification.getSubject(), postCommentedNotification.getTarget()));
    }

    private boolean taggedPreferencesSatisfied(Notification taggedUserNotification, NotificationPreferencesDTO preferences) {
        return preferences.tagNotificationEnabled() ||
                (preferences.tagNotificationEnabledForFollowers()
                        && isFollowing(taggedUserNotification.getSubject(), taggedUserNotification.getTarget()));
    }

    private boolean postLikedPreferencesSatisfied(Notification postLikedNotification, NotificationPreferencesDTO preferences) {
        return preferences.likeNotificationEnabled() ||
                (preferences.likeNotificationEnabledForFollowers()
                        && isFollowing(postLikedNotification.getSubject(), postLikedNotification.getTarget()));
    }

    private boolean isFollowing(String subject, String target) {
        return userGraphClient.checkFollowing(subject, target).isFollowing();
    }

}
