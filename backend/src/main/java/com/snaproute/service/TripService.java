package com.snaproute.service;

import com.snaproute.entity.Trip;
import com.snaproute.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TripService {
    
    @Autowired
    private TripRepository tripRepository;

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
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

    public void deleteTrip(Long id) {
        tripRepository.deleteById(id);
    }

    @Transactional
    public Trip toggleFavorite(Long id) {
        Trip trip = tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trip not found with id: " + id));
        
        trip.setIsFavorite(!trip.getIsFavorite());
        return tripRepository.save(trip);
    }

    public List<Trip> getFavoriteTrips() {
        return tripRepository.findByIsFavoriteTrue();
    }
} 