package funfit.community.query;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.ReadBestPostsResponse;
import funfit.community.post.dto.ReadPostInListResponse;
import funfit.community.post.dto.ReadPostResponse;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import funfit.community.post.service.BestPostCacheService;
import funfit.community.rabbitMq.dto.User;
import funfit.community.rabbitMq.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final BestPostCacheService bestPostCacheService;
    private final UserService userService;

    public ReadPostResponse readPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        User postUser = userService.getUserDto(post.getWriterEmail());
        List<String> imageUrls = post.getImages()
                .stream()
                .map(image -> image.getUrl())
                .toList();
        return new ReadPostResponse(postUser.getUserName(), post.getTitle(), post.getContent(), post.getCategory().getName(),
                post.getCreatedAt(), post.getUpdatedAt(), post.getBookmarks().size(), post.getLikes().size(), post.getViews(), imageUrls);
    }

    public Slice<ReadPostInListResponse> readPostList(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(post -> {
                    User user = userService.getUserDto(post.getWriterEmail());
                    return new ReadPostInListResponse(post.getTitle(), user.getUserName(), post.getCategory().getName(),
                            post.getCreatedAt(), post.getUpdatedAt(),
                            post.getComments().size(), post.getLikes().size(), post.getBookmarks().size(), post.getViews());
                });
    }

    public ReadBestPostsResponse readBestPosts(LocalDateTime time) {
        return bestPostCacheService.readBestPosts(time);
    }
}
