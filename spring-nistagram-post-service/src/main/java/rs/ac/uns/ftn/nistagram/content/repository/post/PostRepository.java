package rs.ac.uns.ftn.nistagram.content.repository.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.nistagram.content.domain.core.post.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select p from Post p where p.author = ?1")
    List<Post> getByUsername(String username);

    @Query("select count(p) from Post p where p.author = :username")
    Long getCountByUsername(@Param("username") String username);

}
