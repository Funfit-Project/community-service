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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final BestPostCacheService bestPostCacheService;
    private final UserDataProvider userDataProvider;

    public ReadPostResponse readPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        String postUserName = userDataProvider.getUserName(post.getWriterEmail());
        List<String> imageUrls = post.getImages()
                .stream()
                .map(image -> image.getUrl())
                .toList();
        return new ReadPostResponse(postUserName, post.getTitle(), post.getContent(), post.getCategory().getName(),
                post.getCreatedAt(), post.getUpdatedAt(), post.getBookmarks().size(), post.getLikes().size(), post.getViews(), imageUrls);
    }

    public Slice<ReadPostInListResponse> readPostList(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(post -> {
                    String userName = userDataProvider.getUserName(post.getWriterEmail());
                    return new ReadPostInListResponse(post.getTitle(), userName, post.getCategory().getName(),
                            post.getCreatedAt().toString(), post.getUpdatedAt().toString(),
                            post.getCommentCount(), post.getLikeCount(), post.getBookmarkCount(), post.getViews());
                });
    }

    public BestPostsResponse readBestPosts(LocalDateTime time) {
        return bestPostCacheService.readBestPosts(time);
    }
}
