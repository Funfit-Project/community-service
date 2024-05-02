package funfit.community.post.controller;

import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.dto.CreatePostResponse;
import funfit.community.post.dto.ReadPostListResponse;
import funfit.community.post.dto.ReadPostResponse;
import funfit.community.post.service.PostService;
import funfit.community.responseDto.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/community/post")
    public ResponseEntity create(@RequestBody CreatePostRequest createPostRequest,
                                 HttpServletRequest request) {
        CreatePostResponse createPostResponse = postService.create(createPostRequest, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 등록 성공", createPostResponse));
    }

    @GetMapping("/community/post/{postId}")
    public ResponseEntity readOne(@PathVariable("postId") long postId) {
        ReadPostResponse readPostResponse = postService.readOne(postId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 조회 성공", readPostResponse));
    }

    @PostMapping("/community/post/{postId}/bookmark")
    public ResponseEntity bookmark(@PathVariable("postId") long postId, HttpServletRequest request) {
        ReadPostResponse readPostResponse = postService.bookmark(postId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("북마크 등록/취소 성공", readPostResponse));
    }

    @GetMapping("/community/posts")
    public ResponseEntity readPage(@PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Slice<ReadPostListResponse.ReadPostResponseInList> readPostResponseInLists = postService.readPage(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 리스트 조회 성공", readPostResponseInLists));
    }

    @GetMapping("/community/posts/best")
    public ResponseEntity readBestPosts() {
        ReadPostListResponse readPostListResponse = postService.readBestPosts();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("실시간 인기글 조회 성공", readPostListResponse));
    }
}
