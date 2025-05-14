package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.repository.CommentRepository;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.service.FileStorageService;
import com.example.server.UsPinterest.model.Tag;
import java.util.stream.Collectors;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class PinStructMapperDecorator implements PinStructMapper {

    private final PinStructMapper delegate;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    public PinStructMapperDecorator(PinStructMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public PinResponse toDto(Pin pin) {
        PinResponse dto = delegate.toDto(pin);
        // Update URL
        if (dto.getImageUrl() != null && !dto.getImageUrl().isEmpty()) {
            dto.setImageUrl(fileStorageService.updateImageUrl(dto.getImageUrl()));
        }
        // Counts
        dto.setLikesCount(pin.getLikesCount() != null ? pin.getLikesCount() : 0);
        dto.setCommentsCount((int) commentRepository.countByPinId(pin.getId()));
        // Aspect ratio
        Integer w = pin.getImageWidth(), h = pin.getImageHeight();
        dto.setAspectRatio(w != null && h != null && h > 0 ? w.doubleValue() / h : 1.0);
        // Tags
        if (pin.getTags() != null) {
            List<String> tagNames = pin.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
            dto.setTags(tagNames);
        }
        return dto;
    }

    @Override
    public Pin toEntity(PinRequest request) {
        return delegate.toEntity(request);
    }
}