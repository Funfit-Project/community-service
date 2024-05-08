package funfit.community.post.service;

import funfit.community.rabbitMq.service.UserService;
import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.dto.CreatePostResponse;
import funfit.community.post.dto.ReadPostListResponse;
import funfit.community.post.dto.ReadPostResponse;
import funfit.community.post.entity.Bookmark;
import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.BookmarkRepository;
import funfit.community.post.repository.PostRepository;
import funfit.community.rabbitMq.dto.UserDto;
import funfit.community.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final BestPostCacheService bestPostCacheService;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;

    public CreatePostResponse create(CreatePostRequest createPostRequest, HttpServletRequest request) {
        UserDto userDto = userService.getUserDto(jwtUtils.getEmailFromHeader(request));
        Post post = Post.create(userDto.getEmail(), createPostRequest.getTitle(), createPostRequest.getContent(), Category.find(createPostRequest.getCategoryName()));
        postRepository.save(post);
        return new CreatePostResponse(userDto.getUserName(), post.getTitle(), post.getContent(), post.getCategory().getName(), post.getCreatedAt());
    }

    public ReadPostResponse readOne(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        post.increaseViews();
        bestPostCacheService.reflectPostViewsInRedis(postId);

        UserDto postUserDto = userService.getUserDto(post.getEmail());
        int bookmarkCount = bookmarkRepository.findByPost(post).size();
        return new ReadPostResponse(postUserDto, post, bookmarkCount);
    }

    public ReadPostResponse bookmark(long postId, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        UserDto readUserDto = userService.getUserDto(jwtUtils.getEmailFromHeader(request));

        bookmarkRepository.findByPostAndUserId(post, readUserDto.getUserId())
                .ifPresentOrElse(bookmark -> bookmarkRepository.delete(bookmark),
                        () -> bookmarkRepository.save(Bookmark.create(post, readUserDto.getUserId())));

        UserDto postUserDto = userService.getUserDto(post.getEmail());
        int bookmarkCount = bookmarkRepository.findByPost(post).size();
        return new ReadPostResponse(postUserDto, post, bookmarkCount);
    }

    public ReadPostListResponse readBestPosts() {
        return bestPostCacheService.readBestPosts();
    }

    public Slice<ReadPostListResponse.ReadPostResponseInList> readPage(Pageable pageable) {
        Slice<Post> postsSlice = postRepository.findSliceBy(pageable);
        return postsSlice.map(post -> getPostDtoInList(post));
    }

    private ReadPostListResponse.ReadPostResponseInList getPostDtoInList(Post post) {
        UserDto postUserDto = userService.getUserDto(post.getEmail());
        int bookmarkCount = bookmarkRepository.findByPost(post).size();
        return new ReadPostListResponse.ReadPostResponseInList(postUserDto, post, bookmarkCount);
    }
}
