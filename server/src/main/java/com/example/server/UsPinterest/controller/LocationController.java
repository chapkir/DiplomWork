package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.LocationRequest;
import com.example.server.UsPinterest.dto.LocationResponse;
import com.example.server.UsPinterest.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@RequestBody LocationRequest request) {
        LocationResponse response = locationService.createLocation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<LocationResponse> getLocationByPost(@PathVariable Long postId) {
        List<LocationResponse> locations = locationService.getLocationsByPost(postId);
        if (locations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(locations.get(0));
    }

    @GetMapping("/pictures/{pictureId}")
    public ResponseEntity<LocationResponse> getLocationByPicture(@PathVariable Long pictureId) {
        List<LocationResponse> locations = locationService.getLocationsByPicture(pictureId);
        if (locations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(locations.get(0));
    }
}