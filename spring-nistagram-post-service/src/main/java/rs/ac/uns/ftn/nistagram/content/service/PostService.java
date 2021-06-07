package rs.ac.uns.ftn.nistagram.content.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.nistagram.content.communication.External;
import rs.ac.uns.ftn.nistagram.content.domain.core.post.Post;
import rs.ac.uns.ftn.nistagram.content.domain.core.post.collection.CustomPostCollection;
import rs.ac.uns.ftn.nistagram.content.domain.core.post.collection.PostInCollection;
import rs.ac.uns.ftn.nistagram.content.domain.core.post.collection.SavedPost;
import rs.ac.uns.ftn.nistagram.content.domain.core.post.social.Comment;
import rs.ac.uns.ftn.nistagram.content.domain.core.post.social.UserInteraction;
import rs.ac.uns.ftn.nistagram.content.repository.post.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserInteractionRepository interactionRepository;
    private final CommentRepository commentRepository;
    private final SavedPostRepository savedPostRepository;
    private final CustomPostCollectionRepository collectionRepository;
    private final PostInCollectionRepository postInCollectionRepository;
    private final External.GraphClient graphClient;

    public void create(Post post) {
        post.setTime(LocalDateTime.now());
        postRepository.save(post);
    }

    public void delete(String username, long postId) {
        Post post = postRepository.findById(postId).orElseThrow(RuntimeException::new);
        if (!post.getAuthor().equals(username))
            throw new RuntimeException("You are not the owner of this post.");
        else postRepository.delete(post);
    }

    public Post getById(long postId) {
        return postRepository.findById(postId).orElseThrow(RuntimeException::new);
    }

    public List<Post> getByUsername(String caller, String username) {
        boolean followsAuthor = graphClient.checkFollowing(caller, username).getStatus();
        if (followsAuthor)
            return postRepository.getByUsername(username);
        else throw new RuntimeException("You do not follow " + username + "!");
    }

    // TODO Check whether the user follows the author of this post!
    public void like(long postId, String username) {
        addInteraction(postId, username, UserInteraction.Sentiment.LIKE);
    }

    // TODO Check whether the user follows the author of this post!
    public void dislike(long postId, String username) {
        addInteraction(postId, username, UserInteraction.Sentiment.DISLIKE);
    }

    private void addInteraction(long postId, String username, UserInteraction.Sentiment sentiment) {
        Optional<UserInteraction> optionalInteraction = interactionRepository.findByPostAndUser(postId, username);
        if (optionalInteraction.isEmpty()) {
            Post post = postRepository.findById(postId).orElseThrow(RuntimeException::new);
            interactionRepository.save(
                    UserInteraction.builder()
                            .username(username)
                            .post(post)
                            .sentiment(sentiment)
                        .build()
            );
        }
        else {
            UserInteraction interaction = optionalInteraction.get();
            if (interaction.getSentiment() == sentiment)
                return;
            interaction.setSentiment(sentiment);
            interactionRepository.save(interaction);
        }
    }

    // TODO Check whether the user follows the author of this post
    public void comment(Comment comment, long postId) {
        comment.setPost(postRepository.findById(postId).orElseThrow(RuntimeException::new));
        comment.setTime(LocalDateTime.now());
        commentRepository.save(comment);
    }

    // TODO Check whether the user follows the author of this post
    public void save(String username, long postId) {
        Post post = postRepository.findById(postId).orElseThrow(RuntimeException::new);
        Optional<SavedPost> savedPost = savedPostRepository.findByUserAndPost(username, postId);
        if (savedPost.isPresent())
            throw new RuntimeException("Post already saved");
        savedPostRepository.save(
                SavedPost.builder().post(post).username(username).build()
        );
    }

    @Transactional
    public void unsave(String username, long postId) {
        SavedPost savedPost = savedPostRepository.findByUserAndPost(username, postId).orElseThrow(RuntimeException::new);
        savedPostRepository.delete(savedPost);

        // Fetch collection IDs from this user
        List<Long> collectionIds = collectionRepository.getIdsByUser(username);
        if (collectionIds.isEmpty()) return;

        postInCollectionRepository.deletePostFromUserCollections(postId, collectionIds);
    }

    public List<SavedPost> getSaved(String username) {
        return savedPostRepository.findByUser(username);
    }

    public void createCollection(String username, String collectionName) {
        if (collectionRepository.getByUserAndName(username, collectionName).isPresent())
            throw new RuntimeException("Collection '" + collectionName + "' already exists.");

        collectionRepository.save(
            CustomPostCollection.builder().name(collectionName).owner(username).build()
        );
    }

    public void addPostToCollection(String username, String collectionName, long postId) {
        CustomPostCollection customPostCollection =
                collectionRepository.getByUserAndName(username, collectionName)
                        .orElseThrow(RuntimeException::new);

        Post post = postRepository.findById(postId).orElseThrow(RuntimeException::new);
        if (postInCollectionRepository.postFromCollection(post.getId(), customPostCollection.getId()).isPresent())
            throw new RuntimeException("Post already present in this collection");

        try {
            save(username, postId); // This will throw if the post is already saved
        }
        catch (RuntimeException ignored) {}

        postInCollectionRepository.save(PostInCollection.builder().collection(customPostCollection).post(post).build());
    }

    public void removePostFromCollection(String username, String collectionName, long postId) {
        CustomPostCollection collection = collectionRepository.getByUserAndName(username, collectionName).orElseThrow(RuntimeException::new);
        postInCollectionRepository.deletePostFromCollection(postId, collection.getId());
    }

    public List<PostInCollection> getAllFromCollection(String username, String collectionName) {
        long collectionId =
                collectionRepository.getByUserAndName(username, collectionName).orElseThrow(RuntimeException::new).getId();
        return postInCollectionRepository.allPostsFromCollection(collectionId);
    }

    public void deleteCollection(String username, String collectionName) {
        CustomPostCollection collection = collectionRepository.getByUserAndName(username, collectionName).orElseThrow(RuntimeException::new);

        postInCollectionRepository.deleteAllFromCollection(collection.getId());
        collectionRepository.delete(collection);
    }
}
