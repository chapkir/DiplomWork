//package com.example.server.UsPinterest.controller;
//
//import com.example.server.UsPinterest.model.Photo;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/photos")
//public class PhotoController {
//    private final PhotoService photoService;
//
//    public PhotoController(PhotoService photoService) {
//        this.photoService = photoService;
//    }
//
//    @PostMapping
//    public ResponseEntity<Photo> uploadPhoto(@RequestBody Photo photo) {
//        return ResponseEntity.ok(photoService.savePhoto(photo));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Photo> getPhoto(@PathVariable Long id) {
//        return ResponseEntity.ok(photoService.getPhotoById(id));
//    }
//}