package funfit.community.post.repository;

import funfit.community.post.dto.ReadBestPostsResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BestPostsRepository extends MongoRepository<ReadBestPostsResponse, String> {
}
