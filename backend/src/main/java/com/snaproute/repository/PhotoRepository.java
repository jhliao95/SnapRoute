package com.snaproute.repository;

import com.snaproute.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByTripId(Long tripId);
    List<Photo> findByTripIdOrderByTakenTimeAsc(Long tripId);
    
    @Query("SELECT p FROM Photo p WHERE p.trip.id = :tripId ORDER BY COALESCE(p.takenTime, p.uploadTime) ASC")
    List<Photo> findByTripIdOrderByTakenTimeOrUploadTimeAsc(@Param("tripId") Long tripId);
} 