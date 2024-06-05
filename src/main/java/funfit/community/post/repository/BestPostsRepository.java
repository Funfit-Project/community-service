package funfit.community.post.repository;

import funfit.community.post.entity.BestPosts;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BestPostsRepository extends MongoRepository<BestPosts, String> {
    Optional<BestPosts> findByTime(String time);
}
