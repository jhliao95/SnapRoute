package com.snaproute.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.snaproute.entity.Trip;
import com.snaproute.model.Photo;
import com.snaproute.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {
    
    @Value("${upload.path}")
    private String uploadDir;

    @Autowired
    private PhotoRepository photoRepository;

    public Photo savePhoto(MultipartFile file, Trip trip, String description) throws Exception {
        Photo photo = processPhoto(file, trip.getId());
        photo.setTrip(trip);
        photo.setDescription(description);
        return photoRepository.save(photo);
    }

    public List<Photo> getPhotosByTripId(Long tripId) {
        return photoRepository.findByTripIdOrderByTakenTimeOrUploadTimeAsc(tripId);
    }

    public void deletePhotosByTripId(Long tripId) {
        List<Photo> photos = photoRepository.findByTripId(tripId);
        for (Photo photo : photos) {
            // 删除物理文件
            try {
                Path filePath = Paths.get(photo.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                // 记录错误但不抛出异常，确保删除操作继续进行
                System.err.println("删除照片文件失败: " + photo.getFilePath() + ", 错误: " + e.getMessage());
            }
        }
        // 删除数据库记录
        photoRepository.deleteAll(photos);
    }

    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + photoId + " 的图片"));
        
        // 删除物理文件
        try {
            Path filePath = Paths.get(photo.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            throw new RuntimeException("删除图片文件失败: " + e.getMessage());
        }

        // 删除数据库记录
        photoRepository.delete(photo);
    }

    public Photo processPhoto(MultipartFile file, Long tripId) throws Exception {
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir, String.valueOf(tripId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

        // 创建照片对象
        Photo photo = new Photo();
        photo.setFileName(originalFileName);
        photo.setFilePath(filePath.toString().replace("\\", "/")); // 统一使用正斜杠

        // 提取EXIF信息
        File savedFile = filePath.toFile();
        Metadata metadata = ImageMetadataReader.readMetadata(savedFile);

        // 获取GPS信息
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory != null) {
            if (gpsDirectory.containsTag(GpsDirectory.TAG_LATITUDE) && 
                gpsDirectory.containsTag(GpsDirectory.TAG_LONGITUDE)) {
                photo.setLatitude(gpsDirectory.getGeoLocation().getLatitude());
                photo.setLongitude(gpsDirectory.getGeoLocation().getLongitude());
            }
        }

        // 获取拍摄时间
        ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifDirectory != null) {
            Date date = exifDirectory.getDateOriginal();
            if (date != null) {
                photo.setTakenTime(LocalDateTime.ofInstant(
                    date.toInstant(), ZoneId.systemDefault()));
            }
        }

        return photo;
    }
} 