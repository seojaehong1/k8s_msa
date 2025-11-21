package com.example.board.controller;

import com.example.board.dto.CommentDto;
import com.example.board.entity.Comment;
import com.example.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDto>> findByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.findByPostId(postId));
    }

    @PostMapping
    public ResponseEntity<CommentDto> save(@PathVariable Long postId, @RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.save(postId, comment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> update(@PathVariable Long id, @RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.update(id, comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.ok().build();
    }
} 