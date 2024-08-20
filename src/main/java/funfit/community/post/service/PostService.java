package funfit.community.post.service;

import funfit.community.post.entity.*;
import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.repository.BookmarkRepository;
import funfit.community.post.repository.LikeRepository;
import funfit.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final BestPostCacheService bestPostCacheService;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;

    public long create(CreatePostRequest createPostRequest, String email) {
        Post post = Post.create(email, createPostRequest.getTitle(), createPostRequest.getContent(), Category.find(createPostRequest.getCategoryName()));
        postRepository.save(post);
        return post.getId();
    }

    public void increaseViews(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        post.increaseViews();
        bestPostCacheService.reflectPostViewsInRedis(postId);
    }

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

    public void likePost(long postId, String email) {

        // post 조회 (with Lock)
        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Optional<Like> optionalLike = likeRepository.findByLikeUserEmailAndPost(email, post);

        optionalLike.ifPresentOrElse(like -> {
            post.deleteLike(like);
            likeRepository.delete(like);
        }, () -> post.addLike(Like.create(email)));
    }
}
