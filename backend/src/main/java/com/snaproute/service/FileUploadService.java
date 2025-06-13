package com.snaproute.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {
    private final Path uploadPath = Paths.get("uploads");

    public FileUploadService() {
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public String uploadImage(MultipartFile file) throws IOException {
        // 生成唯一的文件名
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        // 返回文件访问URL
        return "/uploads/" + filename;
    }
} 