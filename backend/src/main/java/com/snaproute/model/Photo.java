package com.snaproute.model;

import com.snaproute.entity.Trip;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private String fileName;
    private String filePath;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime takenTime;
    private LocalDateTime uploadTime = LocalDateTime.now();
} 