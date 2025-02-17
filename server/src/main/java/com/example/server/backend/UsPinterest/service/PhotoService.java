package com.example.diplomwork.backend.UsPinterest.service;

import com.UsPinterest.exception.ResourceNotFoundException;
import com.UsPinterest.model.Photo;
import com.UsPinterest.repository.PhotoRepository;

import org.springframework.stereotype.Service;

@Service
public class PhotoService {
    private final PhotoRepository photoRepository;

    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public Photo savePhoto(Photo photo) {
        return photoRepository.save(photo);
    }

    public Photo getPhotoById(Long id) {
        return photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + id));
    }
} 