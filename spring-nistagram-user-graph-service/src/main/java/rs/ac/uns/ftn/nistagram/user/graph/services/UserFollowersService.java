package rs.ac.uns.ftn.nistagram.user.graph.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.nistagram.user.graph.domain.User;
import rs.ac.uns.ftn.nistagram.user.graph.exceptions.EntityAlreadyExistsException;
import rs.ac.uns.ftn.nistagram.user.graph.exceptions.OperationNotPermittedException;
import rs.ac.uns.ftn.nistagram.user.graph.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class UserFollowersService {

    private final UserRepository userRepository;

    //TODO: dodaj endpoint koji vraca sve korisnike koje korisnik prati
    public List<User> findFollowers(String username) {
        userPresenceCheck(username);

        if(!userRepository.hasFollowers(username)) {
            log.info("User {} has no followers", username);
            return new ArrayList<>();
        }

        List<User> followers = userRepository.findFollowers(username);
        log.info("Found {} followers for user {}", followers.size(), username);

        return followers;
    }

    @Transactional
    public void follow(String subject, String target){
        log.info("Received a follow request from {} to {}",
                subject,
                target);

        userPresenceCheck(subject);
        userPresenceCheck(target);
        blockedConstraintCheck(subject, target);
        alreadyFollowsCheck(subject,target);

        var subjectUser = userRepository.findById(subject).get();
        var targetUser = userRepository.findById(target).get();

        subjectUser.addFollowing(targetUser);
        userRepository.save(subjectUser);
        log.info("User {} follows {}",
                subject,
                target);
    }
    @Transactional
    public void unfollow(String subject, String target) {
        log.info("Received a unfollow request from {} to {}",
                subject,
                target);

        userPresenceCheck(subject);
        userPresenceCheck(target);
        followingConstraintCheck(subject, target);

        userRepository.unfollow(subject,target);
        log.info("User {} is no longer following {}",
                subject,
                target);
    }

    private void blockedConstraintCheck(String subject, String target) {
        if(userRepository.hasBlocked(subject, target)) {
            var message = String.format("User %s is blocked by user %s",
                    subject,
                    target);
            log.warn(message);
            throw new OperationNotPermittedException(message);
        }
    }

    private void userPresenceCheck(String username){
        if(!userRepository.existsByUsername(username)){
            var message = String.format("User %s doesn't exist", username);
            log.warn(message);
            throw new EntityAlreadyExistsException(message);
        }
    }

    private void followingConstraintCheck(String subject, String target) {
        if(!userRepository.isFollowing(subject, target)) {
            var message = String.format("User %s doesn't follow user %s",
                    subject,
                    target);
            log.warn(message);
            throw new OperationNotPermittedException(message);
        }
    }

    private void alreadyFollowsCheck(String subject, String target){
        if(userRepository.isFollowing(subject, target)) {
            var message = String.format("User %s already follows user %s",
                    subject,
                    target);
            log.warn(message);
            throw new OperationNotPermittedException(message);
        }
    }
}
