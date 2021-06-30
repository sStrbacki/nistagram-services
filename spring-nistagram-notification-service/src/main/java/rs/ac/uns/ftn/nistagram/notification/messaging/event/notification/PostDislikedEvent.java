package rs.ac.uns.ftn.nistagram.notification.messaging.event.notification;

import lombok.*;
import rs.ac.uns.ftn.nistagram.notification.messaging.payload.notification.PostInteractionEventPayload;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PostDislikedEvent {

    private String transactionId;

    private PostInteractionEventPayload postDislikedEventPayload;

}
