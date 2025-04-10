package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.PostRequest;
import com.example.server.UsPinterest.dto.PostResponse;
import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostMapper {

    public Post toEntity(PostRequest postRequest, User user) {
        if (postRequest == null) {
            return null;
        }

        Post post = new Post();
        post.setText(postRequest.getText());
        post.setImageUrl(postRequest.getImageUrl());
        post.setUser(user);
        post.setLikesCount(0);
        post.setCreatedAt(LocalDateTime.now());

        return post;
    }

    public PostResponse toDto(Post post) {
        if (post == null) {
            return null;
        }

        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId().toString());
        postResponse.setText(post.getText());
        postResponse.setImageUrl(post.getImageUrl());
        postResponse.setLikesCount(post.getLikesCount());
        postResponse.setIsLikedByCurrentUser(post.isLikedByCurrentUser());
        postResponse.setCreatedAt(post.getCreatedAt());

        // Устанавливаем информацию о пользователе
        if (post.getUser() != null) {
            postResponse.setUserId(post.getUser().getId().toString());
            postResponse.setUsername(post.getUser().getUsername());
            postResponse.setUserAvatar(post.getUser().getAvatarUrl());
        }

        // Преобразуем комментарии
        if (post.getComments() != null) {
            List<CommentResponse> commentResponses = post.getComments().stream()
                    .map(this::commentToCommentResponse)
                    .collect(Collectors.toList());
            postResponse.setComments(commentResponses);
        } else {
            postResponse.setComments(new ArrayList<>());
        }

        return postResponse;
    }

    private CommentResponse commentToCommentResponse(Comment comment) {
        CommentResponse cr = new CommentResponse();
        cr.setId(comment.getId());
        cr.setText(comment.getText());

        if (comment.getUser() != null) {
            cr.setUsername(comment.getUser().getUsername());
        }

        return cr;
    }

    public List<PostResponse> toDtoList(List<Post> posts) {
        if (posts == null) {
            return null;
        }

        return posts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}