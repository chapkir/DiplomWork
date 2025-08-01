package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.CommentRequest;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.CommentRepository;
import com.example.server.UsPinterest.repository.PostRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.server.UsPinterest.model.Tag;
import com.example.server.UsPinterest.repository.TagRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TagRepository tagRepository;

    @Transactional
    public CommentResponse addCommentToPost(Long postId, CommentRequest commentRequest, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Пост не найден с ID: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));

        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        // Обработка тегов в комментарии
        Set<Tag> commentTags = new HashSet<>();
        Pattern tagPattern = Pattern.compile("#(\\w+)");
        Matcher tagMatcher = tagPattern.matcher(comment.getText());
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName)));
            commentTags.add(tag);
        }
        comment.setTags(commentTags);
        // Обработка упоминаний в комментарии
        Set<User> commentMentions = new HashSet<>();
        Pattern mentionPattern = Pattern.compile("@(\\w+)");
        Matcher mentionMatcher = mentionPattern.matcher(comment.getText());
        while (mentionMatcher.find()) {
            String usernameMention = mentionMatcher.group(1);
            userRepository.findByUsername(usernameMention).ifPresent(mentionedUser -> {
                commentMentions.add(mentionedUser);
                notificationService.createMentionNotification(user, comment, mentionedUser);
            });
        }
        comment.setMentions(commentMentions);

        Comment savedComment = commentRepository.save(comment);
        logger.info("Комментарий создан: {} для поста: {} пользователем: {}",
                savedComment.getId(), postId, user.getUsername());

        // Обновляем счетчик комментариев в посте
        long totalComments = commentRepository.countByPostId(postId);
        post.setCommentsCount((int) totalComments);
        postRepository.save(post);

        // Создаем уведомление о комментарии к посту
        notificationService.createPostCommentNotification(user, post, commentRequest.getText());

        return convertToCommentResponse(savedComment);
    }


    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Пост не найден с ID: " + postId));

        List<Comment> comments = commentRepository.findByPostOrderByCreatedAtDesc(post);

        return comments.stream()
                .map(this::convertToCommentResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteComment(Long commentId, Long postId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден с ID: " + commentId));

        // Проверка, что комментарий принадлежит указанному посту
        if (comment.getPost() == null || !comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Комментарий не принадлежит указанному посту");
        }

        // Проверка, что пользователь является автором комментария
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("У вас нет прав для удаления этого комментария");
        }

        commentRepository.delete(comment);
        // После удаления обновляем счетчик комментариев
        long totalCommentsAfterDelete = commentRepository.countByPostId(postId);
        Post parentPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Пост не найден с ID: " + postId));
        parentPost.setCommentsCount((int) totalCommentsAfterDelete);
        postRepository.save(parentPost);

        logger.info("Комментарий удален: {} из поста: {} пользователем: {}",
                commentId, postId, user.getUsername());
    }


    private CommentResponse convertToCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setText(comment.getText());
        if (comment.getUser() != null) {
            response.setUsername(comment.getUser().getUsername());
            response.setUserProfileImageUrl(comment.getUser().getProfileImageUrl());
            response.setUserId(comment.getUser().getId());
        } else {
            response.setUsername("Unknown");
        }
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}