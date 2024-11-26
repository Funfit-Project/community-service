package funfit.community.post.controller;

import funfit.community.post.dto.BestPostsResponse;
import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.dto.ReadPostInListResponse;
import funfit.community.post.dto.ReadPostResponse;
import funfit.community.post.service.PostService;
import funfit.community.dto.SuccessResponse;
import funfit.community.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final JwtUtils jwtUtils;
    private final PostService postService;

    @PostMapping("/post")
    public ResponseEntity create(@RequestBody CreatePostRequest createPostRequest,
                                 HttpServletRequest request) {
        Long postId = postService.create(createPostRequest, jwtUtils.getEmailFromHeader(request));
        ReadPostResponse readPostResponse = postService.readPost(postId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 등록 성공", readPostResponse));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity readOne(@PathVariable long postId) {
        ReadPostResponse readPostResponse = postService.readPost(postId);
        increaseView(postId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 조회 성공", readPostResponse));
    }

    @Async
    public void increaseView(long postId) {
        postService.increaseViews(postId);
    }

    @PostMapping("/post/{postId}/bookmark")
    public ResponseEntity bookmark(@PathVariable long postId, HttpServletRequest request) {
        postService.bookmark(postId, jwtUtils.getEmailFromHeader(request));
        ReadPostResponse readPostResponse = postService.readPost(postId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("북마크 등록/취소 성공", readPostResponse));
    }

    @GetMapping("/posts")
    public ResponseEntity readPage(@PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Slice<ReadPostInListResponse> readPostResponseInLists = postService.readPostList(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 리스트 조회 성공", readPostResponseInLists));
    }

    @GetMapping("/posts/best")
    public ResponseEntity readBestPosts(@RequestParam("time") @DateTimeFormat(pattern = "yyyy-MM-dd HH") LocalDateTime time) {
        BestPostsResponse bestPostsResponse = postService.readBestPosts(time);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("실시간 인기글 조회 성공", bestPostsResponse));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity likePost(@PathVariable long postId, HttpServletRequest request) {
        postService.likePost(postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("좋아요 등록/취소 성공", null));
    }
}
