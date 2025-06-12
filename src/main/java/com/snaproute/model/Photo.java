package com.snaproute.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String filePath;
    
    private LocalDateTime takenTime;
    
    // 地理位置信息
    private Double latitude;
    private Double longitude;
    private String location;
    
    // EXIF信息
    private String camera;
    private String lens;
    private String aperture;
    private String shutterSpeed;
    private Integer iso;
    
    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;
} 