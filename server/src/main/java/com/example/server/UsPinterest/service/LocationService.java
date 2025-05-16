package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.LocationRequest;
import com.example.server.UsPinterest.dto.LocationResponse;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Location;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.repository.LocationRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.PostRepository;
import com.example.server.UsPinterest.service.GeocodingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final PostRepository postRepository;
    private final PinRepository pinRepository;
    private final GeocodingService geocodingService;

    public LocationService(LocationRepository locationRepository,
                           PostRepository postRepository,
                           PinRepository pinRepository,
                           GeocodingService geocodingService) {
        this.locationRepository = locationRepository;
        this.postRepository = postRepository;
        this.pinRepository = pinRepository;
        this.geocodingService = geocodingService;
    }

    @Transactional
    public LocationResponse createLocation(LocationRequest request) {
        Location location = new Location();
        if (request.getPostId() != null) {
            Post post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Пост не найден с id: " + request.getPostId()));
            location.setPost(post);
        }
        if (request.getPictureId() != null) {
            Pin pin = pinRepository.findById(request.getPictureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден с id: " + request.getPictureId()));
            location.setPin(pin);
        }
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setNameplace(request.getPlaceName());
        Location saved = locationRepository.save(location);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationsByPost(Long postId) {
        return locationRepository.findByPostId(postId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationsByPicture(Long pictureId) {
        return locationRepository.findByPinId(pictureId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private LocationResponse toDto(Location location) {
        LocationResponse dto = new LocationResponse();
        dto.setId(location.getId());
        if (location.getPost() != null) {
            dto.setPostId(location.getPost().getId());
        }
        if (location.getPin() != null) {
            dto.setPictureId(location.getPin().getId());
        }
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setPlaceName(location.getNameplace());
        dto.setCreatedAt(location.getCreatedAt());
        return dto;
    }
}