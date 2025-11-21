package com.example.board.service;

import com.example.board.dto.CommentDto;
import com.example.board.entity.Comment;
import com.example.board.entity.Post;
import com.example.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;

    public List<CommentDto> findByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto save(Long postId, Comment comment) {
        Post post = postService.findById(postId);
        comment.setPost(post);
        return convertToDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto update(Long id, Comment comment) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        existingComment.setContent(comment.getContent());
        return convertToDto(commentRepository.save(existingComment));
    }

    @Transactional
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
} 