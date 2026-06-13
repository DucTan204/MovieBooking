package com.moviebooking.controller;

import com.moviebooking.entity.MovieComment;
import com.moviebooking.service.CustomerCareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/movies/{movieId}/comments")
public class MovieCommentController {

    @Autowired private CustomerCareService customerCareService;

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long movieId) {
        return ResponseEntity.ok(Map.of(
                "comments",      customerCareService.getApprovedComments(movieId),
                "averageRating", customerCareService.getAverageRating(movieId)
        ));
    }

    @PostMapping
    public ResponseEntity<?> addComment(
            @PathVariable Long movieId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        // Lấy userId từ security context (tuỳ implementation của bạn)
        Long userId = extractUserId(userDetails);
        String content = (String) body.get("content");
        int rating = Integer.parseInt(body.get("rating").toString());

        MovieComment comment = customerCareService.addComment(movieId, userId, content, rating);
        return ResponseEntity.ok(comment);
    }

    private Long extractUserId(org.springframework.security.core.userdetails.UserDetails ud) {
        // Tuỳ cách bạn implement UserDetails, ví dụ cast về custom class:
        // return ((CustomUserDetails) ud).getId();
        // Tạm thời trả về 1L, bạn cần sửa lại phù hợp với project
        throw new UnsupportedOperationException("Sửa lại hàm này phù hợp với UserDetails implementation của bạn");
    }
}