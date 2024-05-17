package funfit.community.post.service;

import funfit.community.post.entity.Image;
import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.entity.Bookmark;
import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.BookmarkRepository;
import funfit.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final BestPostCacheService bestPostCacheService;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;

    public long create(CreatePostRequest createPostRequest, String email) {
        Post post = Post.create(email, createPostRequest.getTitle(), createPostRequest.getContent(), Category.find(createPostRequest.getCategoryName()));
        if (createPostRequest.getImageUrls() != null && !createPostRequest.getImageUrls().isEmpty()) {
            createPostRequest.getImageUrls().stream()
                    .forEach(imageUrl -> post.addImage(Image.create(imageUrl)));
        }
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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        bookmarkRepository.findByPostAndBookmarkUserEmail(post, email)
                .ifPresentOrElse(bookmark -> {
                    bookmark.deleteFromPost();
                    bookmarkRepository.delete(bookmark);
                }, () -> post.addBookmark(Bookmark.create(email)));
    }
}
