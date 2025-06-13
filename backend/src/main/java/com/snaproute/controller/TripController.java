package com.snaproute.controller;

import com.snaproute.entity.Trip;
import com.snaproute.model.Photo;
import com.snaproute.service.PhotoService;
import com.snaproute.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class TripController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private TripService tripService;

    @GetMapping
    public ResponseEntity<?> getAllTrips() {
        try {
            List<Trip> trips = tripService.getAllTrips();
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "获取行程列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(@PathVariable Long id) {
        try {
            return tripService.getTripById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "未找到ID为 " + id + " 的行程");
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "获取行程详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody Trip trip) {
        try {
            Trip savedTrip = tripService.createTrip(trip);
            return ResponseEntity.ok(savedTrip);
        } catch (DataIntegrityViolationException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "创建行程失败: 请确保必填字段已填写");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "创建行程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrip(@PathVariable Long id, @RequestBody Trip tripDetails) {
        try {
            Trip updatedTrip = tripService.updateTrip(id, tripDetails);
            return ResponseEntity.ok(updatedTrip);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "更新行程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(@PathVariable Long id) {
        try {
            tripService.deleteTrip(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "行程删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "删除行程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/toggle-favorite")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long id) {
        try {
            Trip updatedTrip = tripService.toggleFavorite(id);
            return ResponseEntity.ok(updatedTrip);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "更新收藏状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> getFavoriteTrips() {
        try {
            List<Trip> favorites = tripService.getFavoriteTrips();
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "获取收藏行程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{tripId}/photos")
    public ResponseEntity<?> uploadPhoto(
            @PathVariable Long tripId,
            @RequestParam("file") MultipartFile file) {
        try {
            Trip trip = tripService.getTripById(tripId)
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + tripId + " 的行程"));
            
            Photo photo = photoService.savePhoto(file, trip, null);
            return ResponseEntity.ok(photo);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "上传图片失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{tripId}/photos")
    public ResponseEntity<?> getTripPhotos(@PathVariable Long tripId) {
        try {
            List<Photo> photos = photoService.getPhotosByTripId(tripId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "获取图片列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{tripId}/route")
    public ResponseEntity<List<Photo>> getTripRoute(@PathVariable Long tripId) {
        // TODO: 获取按时间排序的照片列表
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/with-photo")
    public ResponseEntity<?> createTripWithPhoto(
            @RequestParam("title") String title,
            @RequestParam("date") String date,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Trip trip = new Trip();
            trip.setTitle(title);
            trip.setDate(LocalDate.parse(date));
            trip.setDescription(description);
            
            Trip savedTrip = tripService.createTrip(trip);
            
            // 如果有照片，上传并设置为封面
            if (file != null && !file.isEmpty()) {
                Photo photo = photoService.savePhoto(file, savedTrip, null);
                String filePath = photo.getFilePath();
                if (filePath.contains("uploads/")) {
                    String relativePath = filePath.substring(filePath.indexOf("uploads/") + 8);
                    savedTrip.setImageUrl("/uploads/" + relativePath);
                } else {
                    savedTrip.setImageUrl("/uploads/" + filePath);
                }
                savedTrip = tripService.updateTrip(savedTrip.getId(), savedTrip);
            }
            
            return ResponseEntity.ok(savedTrip);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "创建行程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 