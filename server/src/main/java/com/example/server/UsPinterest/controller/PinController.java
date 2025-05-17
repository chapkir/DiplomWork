package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.HateoasResponse;
import com.example.server.UsPinterest.dto.CursorPageResponse;
import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.CommentRequest;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.MessageResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.PinFullHdResponse;
import com.example.server.UsPinterest.dto.PinThumbnailResponse;
import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.model.Tag;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.CommentRepository;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.repository.TagRepository;
import com.example.server.UsPinterest.service.NotificationService;
import com.example.server.UsPinterest.service.NotificationPublisher;
import com.example.server.UsPinterest.service.PaginationService;
import com.example.server.UsPinterest.service.PinCrudService;
import com.example.server.UsPinterest.service.PinQueryService;
import com.example.server.UsPinterest.service.FileStorageService;
import com.example.server.UsPinterest.service.UserService;
import com.example.server.UsPinterest.util.HateoasUtil;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.Path;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.example.server.UsPinterest.dto.UploadRequest;
import com.example.server.UsPinterest.model.Picture;
import com.example.server.UsPinterest.repository.PictureRepository;
import com.example.server.UsPinterest.dto.PictureResponse;
import com.example.server.UsPinterest.dto.PinThumbnailBasicResponse;
import com.example.server.UsPinterest.service.NotificationSender;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/pins", "/api/spots", "/api/spot"})
public class PinController {

    private static final Logger logger = LoggerFactory.getLogger(PinController.class);

    private final PinCrudService pinCrudService;
    private final PinQueryService pinQueryService;
    private final PaginationService paginationService;
    private final PinRepository pinRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;
    private final Bucket bucket;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final HateoasUtil hateoasUtil;
    private final TagRepository tagRepository;
    private final PictureRepository pictureRepository;
    private final NotificationSender notificationSender;

    @GetMapping({""})
    public ResponseEntity<?> getAllPins(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        // Rate limiting
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }
        // Получаем результат курсорной пагинации из сервиса
        CursorPageResponse<PinResponse, String> pageResponse = pinQueryService.getPinsCursor(cursor, size, sortDirection);
        // Формируем HATEOAS-ответ
        HateoasResponse<CursorPageResponse<PinResponse, String>> response =
                hateoasUtil.buildCursorPageResponse(pageResponse, cursor, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getPinById(@PathVariable Long id, Authentication authentication) {
        // Используем сервис для получения пина с комментариями и лайками
        Pin pin = pinQueryService.getPinWithLikesAndComments(id);

        String username = authentication != null ? authentication.getName() : null;
        User currentUser = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        PinResponse pinResponse = pinQueryService.convertToPinResponse(pin, currentUser);
        // Добавляем список FullHD изображений из таблицы Picture
        pictureRepository.findByPinId(id).ifPresent(pic -> {
            java.util.List<String> fullhdImages = new java.util.ArrayList<>();
            if (pic.getFullhdImageUrl1() != null) fullhdImages.add(fileStorageService.updateImageUrl(pic.getFullhdImageUrl1()));
            if (pic.getFullhdImageUrl2() != null) fullhdImages.add(fileStorageService.updateImageUrl(pic.getFullhdImageUrl2()));
            if (pic.getFullhdImageUrl3() != null) fullhdImages.add(fileStorageService.updateImageUrl(pic.getFullhdImageUrl3()));
            if (pic.getFullhdImageUrl4() != null) fullhdImages.add(fileStorageService.updateImageUrl(pic.getFullhdImageUrl4()));
            if (pic.getFullhdImageUrl5() != null) fullhdImages.add(fileStorageService.updateImageUrl(pic.getFullhdImageUrl5()));
            pinResponse.setFullhdImages(fullhdImages);
        });
        HateoasResponse<PinResponse> response = hateoasUtil.buildPinDetailResponse(pinResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPin(@Valid @RequestBody PinRequest pinRequest, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        String username = authentication.getName();
        Pin pin = pinCrudService.createPin(pinRequest, username);
        PinResponse pinResponse = pinQueryService.convertToPinResponse(pin,
                userRepository.findByUsername(username).orElse(null));

        HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);

        // Добавляем HATEOAS ссылки
        response.addSelfLink("/api/pins/detail/" + pin.getId());
        response.addLink("all-pins", "/api/pins", "GET");
        response.addUpdateLink("/api/pins/detail/" + pin.getId());
        response.addDeleteLink("/api/pins/detail/" + pin.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/likes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likePin(@PathVariable Long id, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        Map<String, Object> likeResult = pinCrudService.likePin(id, authentication.getName());

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        // Публикуем событие для асинхронной отправки уведомления
        notificationPublisher.publishLikeNotification(user.getId(), pin.getId());
        // Отправляем push-уведомление
        notificationSender.sendNotification(
            pin.getUser(),
            "Новый лайк", 
            String.format("%s лайкнул ваш пин '%s'", user.getUsername(), pin.getTitle())
        );

        HateoasResponse<Void> response = new HateoasResponse<>(null);
        response.addSelfLink("/api/pins/" + id + "/like");
        response.addLink("pin", "/api/pins/detail/" + id, "GET");
        response.addLink("unlike", "/api/pins/" + id + "/unlike", "POST");
        response.getMeta().setMessage("Лайк успешно добавлен");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/likes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unlikePin(@PathVariable Long id, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        Map<String, Object> unlikeResult = pinCrudService.unlikePin(id, authentication.getName());

        HateoasResponse<Void> response = new HateoasResponse<>(null);
        response.addSelfLink("/api/pins/" + id + "/unlike");
        response.addLink("pin", "/api/pins/detail/" + id, "GET");
        response.getMeta().setMessage((Boolean) unlikeResult.get("liked") ? "" : "Лайк удалён");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest commentRequest,
            Authentication authentication) {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        // Загружаем пользователя и пин
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinQueryService.getPinWithLikesAndComments(id);

        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setPin(pin);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        // Обработка тегов в комментарии
        Set<Tag> commentTags = new HashSet<>();
        Pattern tagPattern = Pattern.compile("#(\\w+)");
        Matcher tagMatcher = tagPattern.matcher(comment.getText());
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            tagRepository.findByNameIgnoreCase(tagName)
                .ifPresentOrElse(commentTags::add,
                    () -> commentTags.add(tagRepository.save(new Tag(tagName))));
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
                // уведомление об упоминании
                notificationService.createMentionNotification(user, comment, mentionedUser);
            });
        }
        comment.setMentions(commentMentions);

        // Сохраняем комментарий и обновляем пин
        commentRepository.save(comment);
        pin.getComments().add(comment);
        long totalCommentsLong = commentRepository.countByPinId(id);
        pin.setCommentsCount(Math.toIntExact(totalCommentsLong));
        pinRepository.save(pin);

        // Публикуем уведомление
        notificationPublisher.publishCommentNotification(user.getId(), pin.getId(), commentRequest.getText());
        // Отправляем push-уведомление
        notificationSender.sendNotification(
            pin.getUser(),
            "Новый комментарий",
            String.format("%s прокомментировал ваш пин: %s", user.getUsername(), commentRequest.getText())
        );

        // Формируем ответ
        PinResponse pinResponse = pinQueryService.convertToPinResponse(pin, user);
        HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);
        response.addSelfLink("/api/pins/" + id + "/comments");
        response.addLink("pin", "/api/pins/detail/" + id, "GET");
        response.addLink("all-comments", "/api/pins/" + id + "/comments", "GET");
        response.getMeta().setMessage("Комментарий успешно добавлен");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getPinComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Загружаем пин
        Pin pin = pinQueryService.getPinWithLikesAndComments(id);
        // Создаём pageable
        org.springframework.data.domain.Pageable pageable = paginationService.createPageable(page, size, "createdAt", "desc");
        // Получаем страницу комментариев
        org.springframework.data.domain.Page<com.example.server.UsPinterest.entity.Comment> commentPage =
                commentRepository.findByPin(pin, pageable);
        // Маппинг комментариев в DTO
        java.util.List<CommentResponse> commentResponses = commentPage.getContent().stream()
                .map(comment -> {
                    CommentResponse cr = new CommentResponse();
                    cr.setId(comment.getId());
                    cr.setText(comment.getText());
                    cr.setCreatedAt(comment.getCreatedAt());
                    if (comment.getUser() != null) {
                        cr.setUsername(comment.getUser().getUsername());
                        String userImg = comment.getUser().getProfileImageUrl();
                        if (userImg != null && !userImg.isEmpty()) {
                            userImg = fileStorageService.updateImageUrl(userImg);
                        }
                        cr.setUserProfileImageUrl(userImg);
                        cr.setUserId(comment.getUser().getId());
                    } else {
                        cr.setUsername("Unknown");
                    }
                    return cr;
                }).collect(Collectors.toList());
        // Формируем PageResponse
        com.example.server.UsPinterest.dto.PageResponse<CommentResponse> pageResponse =
                new com.example.server.UsPinterest.dto.PageResponse<>(
                        commentResponses,
                        commentPage.getNumber(),
                        commentPage.getSize(),
                        commentPage.getTotalElements(),
                        commentPage.getTotalPages(),
                        commentPage.isLast()
                );
        // Формируем HATEOAS-ответ
        HateoasResponse<com.example.server.UsPinterest.dto.PageResponse<CommentResponse>> response =
                new HateoasResponse<>(pageResponse);
        response.addSelfLink("/api/pins/" + id + "/comments?page=" + page + "&size=" + size);
        response.addLink("pin", "/api/pins/detail/" + id, "GET");
        response.addLink("add-comment", "/api/pins/" + id + "/comments", "POST");
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadImages(
            @Valid @ModelAttribute UploadRequest uploadRequest,
            Authentication authentication) {

        MultipartFile[] files = uploadRequest.getFile();
        String text = uploadRequest.getText();
        String title = uploadRequest.getTitle();
        String description = uploadRequest.getDescription();
        Double rating = uploadRequest.getRating();

        logger.info("uploadImages called: filesCount={}, text={}, title={}, description={}, rating={}",
                files.length, text, title, description, rating);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов. Пожалуйста, попробуйте позже."));
        }

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body(new MessageResponse("Файлы не выбраны"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        try {
            // Сначала создаём Pin без полей изображений
            Pin pin = new Pin();
            String pinTitle = (title != null && !title.isEmpty()) ? title : (text != null ? text : "");
            pin.setTitle(pinTitle);
            pin.setDescription(description);
            pin.setRating(rating);
            pin.setUser(user);
            pin.setCreatedAt(LocalDateTime.now());
            Pin savedPin = pinRepository.save(pin);

            // Сохраняем все загруженные файлы в сущность Picture
            Picture picture = new Picture();
            picture.setPin(savedPin);
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Файл не выбран"));
                }
                String contentTypeItem = file.getContentType();
                if (contentTypeItem == null || !contentTypeItem.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Файл должен быть изображением"));
                }
                String imageUrl = fileStorageService.storeFile(file);
                // Вычисляем размеры оригинального изображения
                BufferedImage origImg = ImageIO.read(file.getInputStream());
                int origWidth = origImg.getWidth();
                int origHeight = origImg.getHeight();
                FileStorageService.ImageInfo fullhdInfo = fileStorageService.storeFullhdFileAsync(file, fileStorageService.getFilenameFromUrl(imageUrl)).join();
                FileStorageService.ImageInfo thumbnailInfo = fileStorageService.storeThumbnailFileAsync(file, fileStorageService.getFilenameFromUrl(imageUrl)).join();
                // Устанавливаем агрегированные поля только для первого файла
                if (i == 0) {
                    picture.setImageUrl(imageUrl);
                    picture.setImageWidth(origWidth);
                    picture.setImageHeight(origHeight);
                    picture.setFullhdImageUrl(fullhdInfo.getUrl());
                    picture.setFullhdWidth(fullhdInfo.getWidth());
                    picture.setFullhdHeight(fullhdInfo.getHeight());
                    picture.setThumbnailImageUrl(thumbnailInfo.getUrl());
                    picture.setThumbnailWidth(thumbnailInfo.getWidth());
                    picture.setThumbnailHeight(thumbnailInfo.getHeight());
                }
                switch (i) {
                    case 0:
                        picture.setImageUrl1(imageUrl);
                        picture.setFullhdImageUrl1(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl1(thumbnailInfo.getUrl());
                        break;
                    case 1:
                        picture.setImageUrl2(imageUrl);
                        picture.setFullhdImageUrl2(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl2(thumbnailInfo.getUrl());
                        break;
                    case 2:
                        picture.setImageUrl3(imageUrl);
                        picture.setFullhdImageUrl3(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl3(thumbnailInfo.getUrl());
                        break;
                    case 3:
                        picture.setImageUrl4(imageUrl);
                        picture.setFullhdImageUrl4(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl4(thumbnailInfo.getUrl());
                        break;
                    case 4:
                        picture.setImageUrl5(imageUrl);
                        picture.setFullhdImageUrl5(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl5(thumbnailInfo.getUrl());
                        break;
                }
            }
            Picture saved = pictureRepository.save(picture);

            PictureResponse resp = new PictureResponse();
            resp.setImage1(saved.getImageUrl1());
            resp.setImage2(saved.getImageUrl2());
            resp.setImage3(saved.getImageUrl3());
            resp.setImage4(saved.getImageUrl4());
            resp.setImage5(saved.getImageUrl5());
            resp.setFullhd1(saved.getFullhdImageUrl1());
            resp.setFullhd2(saved.getFullhdImageUrl2());
            resp.setFullhd3(saved.getFullhdImageUrl3());
            resp.setFullhd4(saved.getFullhdImageUrl4());
            resp.setFullhd5(saved.getFullhdImageUrl5());
            resp.setThumb1(saved.getThumbnailImageUrl1());
            resp.setThumb2(saved.getThumbnailImageUrl2());
            resp.setThumb3(saved.getThumbnailImageUrl3());
            resp.setThumb4(saved.getThumbnailImageUrl4());
            resp.setThumb5(saved.getThumbnailImageUrl5());
            resp.setId(saved.getId());

            HateoasResponse<PictureResponse> response = new HateoasResponse<>(resp);
            response.addSelfLink("/api/pins/" + savedPin.getId() + "/pictures");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при обработке загрузки изображений: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при загрузке изображений: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deletePin(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Пользователь не авторизован"));
            }

            Pin pin = pinRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин с ID " + id + " не найден"));

            if (!pin.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("У вас нет прав на удаление этого пина"));
            }

            notificationService.deleteNotificationsByPin(pin);

            pinCrudService.deletePin(id);

            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Ошибка при удалении пина: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при удалении пина: " + e.getMessage()));
        }
    }

    @GetMapping("/fullhd")
    public ResponseEntity<?> getPinsFullhd(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        CursorPageResponse<PinFullHdResponse, String> page = pinQueryService.getPinsFullhdCursor(cursor, size, sortDirection);
        HateoasResponse<CursorPageResponse<PinFullHdResponse, String>> response = hateoasUtil.buildCursorPageResponse(page, cursor, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/thumbnails")
    public ResponseEntity<?> getPinsThumbnails(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        CursorPageResponse<PinThumbnailResponse, String> page = pinQueryService.getPinsThumbnailCursor(cursor, size, sortDirection);
        HateoasResponse<CursorPageResponse<PinThumbnailResponse, String>> response = hateoasUtil.buildCursorPageResponse(page, cursor, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{pinId}/pictures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadPicturesToPin(
            @PathVariable Long pinId,
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body(new MessageResponse("Файлы не выбраны"));
        }
        if (files.length > 5) {
            return ResponseEntity.badRequest().body(new MessageResponse("Можно загрузить до 5 фотографий"));
        }
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Пользователь не авторизован"));
        }
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден с id: " + pinId));
        if (!pin.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("У вас нет прав на добавление фотографий к этому пину"));
        }
        // Создаем одну запись с up to 5 изображениями
        try {
            Picture picture = new Picture();
            picture.setPin(pin);
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                String imageUrl = fileStorageService.storeFile(file);
                FileStorageService.ImageInfo fullhdInfo = fileStorageService.storeFullhdFileAsync(file, fileStorageService.getFilenameFromUrl(imageUrl)).join();
                FileStorageService.ImageInfo thumbnailInfo = fileStorageService.storeThumbnailFileAsync(file, fileStorageService.getFilenameFromUrl(imageUrl)).join();
                switch (i) {
                    case 0:
                        picture.setImageUrl1(imageUrl);
                        picture.setFullhdImageUrl1(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl1(thumbnailInfo.getUrl()); break;
                    case 1:
                        picture.setImageUrl2(imageUrl);
                        picture.setFullhdImageUrl2(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl2(thumbnailInfo.getUrl()); break;
                    case 2:
                        picture.setImageUrl3(imageUrl);
                        picture.setFullhdImageUrl3(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl3(thumbnailInfo.getUrl()); break;
                    case 3:
                        picture.setImageUrl4(imageUrl);
                        picture.setFullhdImageUrl4(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl4(thumbnailInfo.getUrl()); break;
                    case 4:
                        picture.setImageUrl5(imageUrl);
                        picture.setFullhdImageUrl5(fullhdInfo.getUrl());
                        picture.setThumbnailImageUrl5(thumbnailInfo.getUrl()); break;
                }
            }
            Picture saved = pictureRepository.save(picture);
            PictureResponse resp = new PictureResponse();
            resp.setImage1(saved.getImageUrl1());
            resp.setImage2(saved.getImageUrl2());
            resp.setImage3(saved.getImageUrl3());
            resp.setImage4(saved.getImageUrl4());
            resp.setImage5(saved.getImageUrl5());
            resp.setFullhd1(saved.getFullhdImageUrl1());
            resp.setFullhd2(saved.getFullhdImageUrl2());
            resp.setFullhd3(saved.getFullhdImageUrl3());
            resp.setFullhd4(saved.getFullhdImageUrl4());
            resp.setFullhd5(saved.getFullhdImageUrl5());
            resp.setThumb1(saved.getThumbnailImageUrl1());
            resp.setThumb2(saved.getThumbnailImageUrl2());
            resp.setThumb3(saved.getThumbnailImageUrl3());
            resp.setThumb4(saved.getThumbnailImageUrl4());
            resp.setThumb5(saved.getThumbnailImageUrl5());
            resp.setId(saved.getId());
            HateoasResponse<PictureResponse> response = new HateoasResponse<>(resp);
            response.addSelfLink("/api/pins/" + pinId + "/pictures");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при загрузке фотографий к пину {}: {}", pinId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при загрузке фотографий: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<PinResponse>> getAllPinsWithThumbnail() {
        // Возвращаем основные данные по всем пинам
        var currentUser = userService.getCurrentUser();
        List<PinResponse> dtos = pinRepository.findAll().stream()
                .map(pin -> {
                    PinResponse dto = pinQueryService.convertToPinResponse(pin, currentUser);
                    // Применяем первую миниатюру из таблицы pictures
                    pictureRepository.findByPinId(pin.getId()).ifPresent(pic -> {
                        String thumb1 = pic.getThumbnailImageUrl1();
                        if (thumb1 != null && !thumb1.isEmpty()) {
                            dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(thumb1));
                        }
                    });
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{pinId}/pictures")
    public ResponseEntity<List<String>> getPicturesByPinId(@PathVariable Long pinId) {
        Picture picture = pictureRepository.findByPinId(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Картинки не найдены для пина " + pinId));
        List<String> thumbnails = new ArrayList<>();
        if (picture.getThumbnailImageUrl1() != null) {
            thumbnails.add(fileStorageService.updateImageUrl(picture.getThumbnailImageUrl1()));
        }
        if (picture.getThumbnailImageUrl2() != null) {
            thumbnails.add(fileStorageService.updateImageUrl(picture.getThumbnailImageUrl2()));
        }
        if (picture.getThumbnailImageUrl3() != null) {
            thumbnails.add(fileStorageService.updateImageUrl(picture.getThumbnailImageUrl3()));
        }
        if (picture.getThumbnailImageUrl4() != null) {
            thumbnails.add(fileStorageService.updateImageUrl(picture.getThumbnailImageUrl4()));
        }
        if (picture.getThumbnailImageUrl5() != null) {
            thumbnails.add(fileStorageService.updateImageUrl(picture.getThumbnailImageUrl5()));
        }
        return ResponseEntity.ok(thumbnails);
    }
}