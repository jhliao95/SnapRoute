package com.snaproute.repository;

import com.snaproute.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByIsFavoriteTrue();
    List<Trip> findAllByOrderByDateAsc();
    List<Trip> findByIsFavoriteTrueOrderByDateAsc();
} 