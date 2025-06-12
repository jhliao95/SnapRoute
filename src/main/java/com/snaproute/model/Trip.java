package com.snaproute.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<Photo> photos;
    
    // 行程路线的关键点
    @ElementCollection
    private List<String> waypoints;
} 