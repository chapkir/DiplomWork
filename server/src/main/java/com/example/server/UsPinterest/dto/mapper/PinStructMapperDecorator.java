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
import com.example.server.UsPinterest.repository.PictureRepository;
import com.example.server.UsPinterest.repository.LocationRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
@Primary
public abstract class PinStructMapperDecorator implements PinStructMapper {

    @Autowired
    @Qualifier("delegate")
    private PinStructMapper delegate;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LocationRepository locationRepository;

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
        // Profile image
        if (dto.getUserProfileImageUrl() != null && !dto.getUserProfileImageUrl().isEmpty()) {
            dto.setUserProfileImageUrl(fileStorageService.updateImageUrl(dto.getUserProfileImageUrl()));
        }
        // Images: заполняем агрегированные поля из сущности Picture
        pictureRepository.findByPinId(pin.getId()).ifPresent(picture -> {
            if (picture.getImageUrl() != null) {
                dto.setImageUrl(fileStorageService.updateImageUrl(picture.getImageUrl()));
            }
            if (picture.getThumbnailImageUrl() != null && !picture.getThumbnailImageUrl().isEmpty()) {
                dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(picture.getThumbnailImageUrl()));
            }
            dto.setImageWidth(picture.getImageWidth());
            dto.setImageHeight(picture.getImageHeight());
            dto.setFullhdWidth(picture.getFullhdWidth());
            dto.setFullhdHeight(picture.getFullhdHeight());
            dto.setThumbnailWidth(picture.getThumbnailWidth());
            dto.setThumbnailHeight(picture.getThumbnailHeight());
            // Количество картинок для места
            int count = 0;
            if (picture.getImageUrl1() != null) count++;
            if (picture.getImageUrl2() != null) count++;
            if (picture.getImageUrl3() != null) count++;
            if (picture.getImageUrl4() != null) count++;
            if (picture.getImageUrl5() != null) count++;
            dto.setPicturesCount(count);
        });
        // Локация: координаты и подробности места
        locationRepository.findByPinId(pin.getId()).stream().findFirst().ifPresent(loc -> {
            dto.setLatitude(loc.getLatitude());
            dto.setLongitude(loc.getLongitude());
            dto.setAddress(loc.getAddress());
            dto.setPlaceName(loc.getNameplace());
        });
        return dto;
    }

    @Override
    public Pin toEntity(PinRequest request) {
        return delegate.toEntity(request);
    }
}