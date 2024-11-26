package funfit.community.post.service;

import funfit.community.post.dto.BestPostsResponse;
import funfit.community.post.dto.ReadPostInListResponse;
import funfit.community.post.dto.ReadPostResponse;
import funfit.community.post.entity.*;
import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.repository.BookmarkRepository;
import funfit.community.post.repository.PostRepository;
import funfit.community.user.UserDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final BestPostCacheService bestPostCacheService;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserDataProvider userDataProvider;
    private final PostLikeRedisService postLikeRedisService;
    @Transactional
    public long create(CreatePostRequest createPostRequest, String email) {
        Post post = Post.create(email, createPostRequest.getTitle(), createPostRequest.getContent(), Category.find(createPostRequest.getCategoryName()));
        postRepository.save(post);
        return post.getId();
    }

    @Transactional
    public void increaseViews(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        post.increaseViews();
        bestPostCacheService.reflectPostViewsInCache(postId);
    }

    @Transactional
    public void bookmark(long postId, String email) {
        Post post = postRepository.findByIdWithBookmarkWithLock(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Optional<Bookmark> alreadyBookmark = post.getBookmarks().stream()
                .filter(bookmark -> bookmark.getBookmarkUserEmail().equals(email))
                .findAny();

        alreadyBookmark.ifPresentOrElse(bookmark -> {
            post.deleteBookmark(bookmark);
            bookmarkRepository.delete(bookmark);
        }, () -> post.addBookmark(Bookmark.create(email)));
    }

    @Transactional
    public void likePost(long postId, String email) {
        postRepository.findById(postId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        postLikeRedisService.likePost(postId, email);
    }

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
