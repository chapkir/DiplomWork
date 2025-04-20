package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.PostRequest;
import com.example.server.UsPinterest.dto.PostResponse;
import com.example.server.UsPinterest.dto.mapper.PostStructMapper;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PostRepository;
import com.example.server.UsPinterest.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostStructMapper postStructMapper;

    @Autowired
    public PostService(PostRepository postRepository, UserService userService, PostStructMapper postStructMapper) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.postStructMapper = postStructMapper;
    }

    @Transactional
    public PostResponse createPost(PostRequest postRequest, Long userId) {
        User user = userService.getUserWithCollections(userId);
        Post post = postStructMapper.toEntity(postRequest);
        post.setUser(user);

        Post savedPost = postRepository.save(post);
        return postStructMapper.toDto(savedPost);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(UserPrincipal currentUser) {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

        // Если пользователь авторизован, проверяем, какие посты он лайкнул
        if (currentUser != null) {
            User user = userService.getUserWithCollections(currentUser.getId());
            posts.forEach(post -> {
                // Проверка наличия лайка от текущего пользователя
                post.setLikedByCurrentUser(userService.hasUserLikedPost(user.getId(), post.getId()));
                // Получаем актуальное количество лайков
                post.setLikesCount(userService.getLikesCountForPost(post.getId()));
            });
        }

        return posts.stream()
                .map(postStructMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPaginatedPosts(Pageable pageable, UserPrincipal currentUser) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        // Если пользователь авторизован, проверяем, какие посты он лайкнул
        if (currentUser != null) {
            User user = userService.getUserWithCollections(currentUser.getId());
            posts.forEach(post -> {
                // Проверка наличия лайка от текущего пользователя
                post.setLikedByCurrentUser(userService.hasUserLikedPost(user.getId(), post.getId()));
                // Получаем актуальное количество лайков
                post.setLikesCount(userService.getLikesCountForPost(post.getId()));
            });
        }

        return posts.map(postStructMapper::toDto);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, UserPrincipal currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Получаем актуальное количество лайков
        post.setLikesCount(userService.getLikesCountForPost(post.getId()));

        // Если пользователь авторизован, проверяем, лайкнул ли он этот пост
        if (currentUser != null) {
            User user = userService.getUserWithCollections(currentUser.getId());
            post.setLikedByCurrentUser(userService.hasUserLikedPost(user.getId(), post.getId()));
        }

        return postStructMapper.toDto(post);
    }

    @Transactional(readOnly = true)
    public PostResponse convertToPostResponse(Post post, User currentUser) {
        if (post == null) {
            return null;
        }

        // Получаем актуальное количество лайков
        post.setLikesCount(userService.getLikesCountForPost(post.getId()));

        // Если пользователь авторизован, проверяем, лайкнул ли он этот пост
        if (currentUser != null) {
            post.setLikedByCurrentUser(userService.hasUserLikedPost(currentUser.getId(), post.getId()));
        }

        return postStructMapper.toDto(post);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostRequest postRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        post.setText(postRequest.getText());
        if (postRequest.getImageUrl() != null) {
            post.setImageUrl(postRequest.getImageUrl());
        }

        Post updatedPost = postRepository.save(post);
        // Получаем актуальное количество лайков
        updatedPost.setLikesCount(userService.getLikesCountForPost(updatedPost.getId()));

        return postStructMapper.toDto(updatedPost);
    }

    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Transactional
    public PostResponse likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userService.getUserWithCollections(userId);

        // Проверяем, лайкнул ли уже пользователь этот пост
        boolean hasLiked = userService.hasUserLikedPost(userId, postId);

        if (!hasLiked) {
            // Если нет, то добавляем лайк
            userService.addLikeToPost(user, post);
            post.setLikedByCurrentUser(true);
        } else {
            // Если да, то убираем лайк
            userService.removeLikeFromPost(user, post);
            post.setLikedByCurrentUser(false);
        }

        // Сохраняем изменения
        Post updatedPost = postRepository.save(post);
        // Получаем актуальное количество лайков
        updatedPost.setLikesCount(userService.getLikesCountForPost(updatedPost.getId()));

        return postStructMapper.toDto(updatedPost);
    }

    /**
     * Метод для явного удаления лайка с поста
     *
     * @param postId ID поста
     * @param userId ID пользователя
     * @return Обновленный ответ с информацией о посте
     */
    @Transactional
    public PostResponse unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userService.getUserWithCollections(userId);

        // Проверяем, лайкнул ли пользователь этот пост
        boolean hasLiked = userService.hasUserLikedPost(userId, postId);

        if (hasLiked) {
            // Если да, то убираем лайк
            userService.removeLikeFromPost(user, post);
            post.setLikedByCurrentUser(false);
        }

        // Сохраняем изменения
        Post updatedPost = postRepository.save(post);
        // Получаем актуальное количество лайков
        updatedPost.setLikesCount(userService.getLikesCountForPost(updatedPost.getId()));

        return postStructMapper.toDto(updatedPost);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getUserPosts(Long userId, UserPrincipal currentUser) {
        User user = userService.getUserWithCollections(userId);
        List<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user);

        // Если пользователь авторизован, проверяем, какие посты он лайкнул
        if (currentUser != null) {
            Long currentUserId = currentUser.getId();
            posts.forEach(post -> {
                // Проверка наличия лайка от текущего пользователя
                post.setLikedByCurrentUser(userService.hasUserLikedPost(currentUserId, post.getId()));
                // Получаем актуальное количество лайков
                post.setLikesCount(userService.getLikesCountForPost(post.getId()));
            });
        }

        return posts.stream()
                .map(postStructMapper::toDto)
                .collect(Collectors.toList());
    }
}