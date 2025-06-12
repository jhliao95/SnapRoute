package com.snaproute.controller;

import com.snaproute.model.Photo;
import com.snaproute.model.Trip;
import com.snaproute.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private PhotoService photoService;

    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        trip.setPhotos(new ArrayList<>());
        // TODO: 保存trip到数据库
        return ResponseEntity.ok(trip);
    }

    @PostMapping("/{tripId}/photos")
    public ResponseEntity<Photo> uploadPhoto(
            @PathVariable Long tripId,
            @RequestParam("file") MultipartFile file) {
        try {
            Photo photo = photoService.processPhoto(file);
            // TODO: 关联trip和保存到数据库
            return ResponseEntity.ok(photo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<Trip> getTrip(@PathVariable Long tripId) {
        // TODO: 从数据库获取trip
        return ResponseEntity.ok(new Trip());
    }

    @GetMapping("/{tripId}/route")
    public ResponseEntity<List<Photo>> getTripRoute(@PathVariable Long tripId) {
        // TODO: 获取按时间排序的照片列表
        return ResponseEntity.ok(new ArrayList<>());
    }
} 