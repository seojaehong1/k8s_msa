package com.example.board.config;

import com.example.board.entity.Comment;
import com.example.board.entity.Post;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 테스트 게시글 생성
            for (int i = 1; i <= 50; i++) {
                Post post = new Post();
                post.setTitle("테스트 게시글 " + i);
                post.setContent("이것은 테스트 게시글 " + i + "의 내용입니다.\n\n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n\n" +
                        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n\n" +
                        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.");
                post.setCreatedAt(LocalDateTime.now().minusDays(new Random().nextInt(30)));
                postRepository.save(post);

                // 각 게시글에 댓글 추가
                for (int j = 1; j <= new Random().nextInt(5) + 1; j++) {
                    Comment comment = new Comment();
                    comment.setContent("테스트 댓글 " + j + "입니다.");
                    comment.setPost(post);
                    comment.setCreatedAt(LocalDateTime.now().minusDays(new Random().nextInt(10)));
                    commentRepository.save(comment);
                }
            }
        };
    }
} 