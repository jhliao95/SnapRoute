package com.snaproute.model;

import com.snaproute.entity.Trip;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "photos")
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;
} 