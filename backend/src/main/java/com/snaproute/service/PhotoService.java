package com.snaproute.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.snaproute.model.Photo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class PhotoService {
    
    private final String uploadDir = "uploads";

    public Photo processPhoto(MultipartFile file) throws Exception {
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // 创建照片对象
        Photo photo = new Photo();
        photo.setFileName(fileName);
        photo.setFilePath(filePath.toString());

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