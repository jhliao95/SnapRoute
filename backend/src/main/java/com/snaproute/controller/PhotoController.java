package com.snaproute.controller;

import com.snaproute.entity.Trip;
import com.snaproute.model.Photo;
import com.snaproute.service.PhotoService;
import com.snaproute.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private TripService tripService;

    @PostMapping("/upload/{tripId}")
    public ResponseEntity<?> uploadPhotos(
            @PathVariable Long tripId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "description", required = false) String description) {
        try {
            Trip trip = tripService.getTripById(tripId)
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + tripId + " 的行程"));

            List<Photo> savedPhotos = new ArrayList<>();
            for (MultipartFile file : files) {
                Photo photo = photoService.savePhoto(file, trip, description);
                savedPhotos.add(photo);
            }

            return ResponseEntity.ok(savedPhotos);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "上传图片失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<?> getPhotosByTripId(@PathVariable Long tripId) {
        try {
            List<Photo> photos = photoService.getPhotosByTripId(tripId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "获取图片列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Long photoId) {
        try {
            photoService.deletePhoto(photoId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "图片删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "删除图片失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 