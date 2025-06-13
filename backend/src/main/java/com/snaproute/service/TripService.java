package com.snaproute.service;

import com.snaproute.entity.Trip;
import com.snaproute.model.Photo;
import com.snaproute.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class TripService {
    
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PhotoService photoService;

    public List<Trip> getAllTrips() {
        List<Trip> trips = tripRepository.findAll();
        // 为每个行程加载第一张照片作为封面
        for (Trip trip : trips) {
            List<Photo> photos = photoService.getPhotosByTripId(trip.getId());
            if (!photos.isEmpty()) {
                Photo firstPhoto = photos.get(0);
                String filePath = firstPhoto.getFilePath();
                // 提取相对于 uploads 目录的路径
                if (filePath.contains("uploads/")) {
                    String relativePath = filePath.substring(filePath.indexOf("uploads/") + 8);
                    trip.setImageUrl("/uploads/" + relativePath);
                } else {
                    trip.setImageUrl("/uploads/" + filePath);
                }
            }
        }
        return trips;
    }

    public Optional<Trip> getTripById(Long id) {
        return tripRepository.findById(id);
    }

    public Trip createTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip updateTrip(Long id, Trip tripDetails) {
        Trip trip = tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trip not found with id: " + id));

        trip.setTitle(tripDetails.getTitle());
        trip.setDate(tripDetails.getDate());
        trip.setDescription(tripDetails.getDescription());
        trip.setImageUrl(tripDetails.getImageUrl());
        trip.setIsFavorite(tripDetails.getIsFavorite());

        return tripRepository.save(trip);
    }

    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("未找到ID为 " + id + " 的行程"));

        // 删除关联的照片
        photoService.deletePhotosByTripId(id);

        // 删除行程目录
        try {
            Path tripDir = Paths.get("uploads", String.valueOf(id));
            if (Files.exists(tripDir)) {
                Files.walk(tripDir)
                    .sorted((a, b) -> b.compareTo(a)) // 先删除文件，再删除目录
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            // 记录错误但不抛出异常，确保删除操作继续进行
                            System.err.println("删除文件失败: " + path + ", 错误: " + e.getMessage());
                        }
                    });
            }
        } catch (Exception e) {
            // 记录错误但不抛出异常，确保删除操作继续进行
            System.err.println("删除行程目录失败: " + e.getMessage());
        }

        // 删除行程记录
        tripRepository.delete(trip);
    }

    @Transactional
    public Trip toggleFavorite(Long id) {
        Trip trip = tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trip not found with id: " + id));
        
        trip.setIsFavorite(!trip.getIsFavorite());
        return tripRepository.save(trip);
    }

    public List<Trip> getFavoriteTrips() {
        List<Trip> trips = tripRepository.findByIsFavoriteTrue();
        // 为每个行程加载第一张照片作为封面
        for (Trip trip : trips) {
            List<Photo> photos = photoService.getPhotosByTripId(trip.getId());
            if (!photos.isEmpty()) {
                Photo firstPhoto = photos.get(0);
                String filePath = firstPhoto.getFilePath();
                // 提取相对于 uploads 目录的路径
                if (filePath.contains("uploads/")) {
                    String relativePath = filePath.substring(filePath.indexOf("uploads/") + 8);
                    trip.setImageUrl("/uploads/" + relativePath);
                } else {
                    trip.setImageUrl("/uploads/" + filePath);
                }
            }
        }
        return trips;
    }
} 