package com.example.board.service;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;

    public Page<Post> findAll(int page) {
        return postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Transactional
    public Post update(Long id, Post post) {
        Post existingPost = findById(id);
        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        return postRepository.save(existingPost);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }
} 