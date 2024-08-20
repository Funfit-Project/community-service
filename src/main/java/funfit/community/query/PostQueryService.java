package funfit.community.query;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.BestPostsResponse;
import funfit.community.post.dto.ReadPostInListResponse;
import funfit.community.post.dto.ReadPostResponse;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import funfit.community.post.service.BestPostCacheService;
import funfit.community.api.UserDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
//@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final BestPostCacheService bestPostCacheService;
    private final UserDataProvider userDataProvider;

    public ReadPostResponse readPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        String postUserName = userDataProvider.getUsername(post.getWriterEmail());
        return new ReadPostResponse(postUserName, post.getTitle(), post.getContent(), post.getCategory().getName(),
                post.getCreatedAt(), post.getUpdatedAt(), post.getBookmarkCount(), post.getLikeCount(), post.getViews());
    }

    public Slice<ReadPostInListResponse> readPostList(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(post -> {
                    String username = userDataProvider.getUsername(post.getWriterEmail());
                    return new ReadPostInListResponse(post.getTitle(), username, post.getCategory().getName(),
                            post.getCreatedAt().toString(), post.getUpdatedAt().toString(),
                            post.getCommentCount(), post.getLikeCount(), post.getBookmarkCount(), post.getViews());
                });
    }

    public BestPostsResponse readBestPosts(LocalDateTime time) {
        return bestPostCacheService.readBestPosts(time);
    }
}
