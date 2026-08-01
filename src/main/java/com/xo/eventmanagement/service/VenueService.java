package com.xo.eventmanagement.service;

import com.xo.eventmanagement.entity.Venue;
import com.xo.eventmanagement.exception.ResourceNotFoundException;
import com.xo.eventmanagement.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    @Autowired
    private VenueRepository venueRepository;

    public List<Venue> getAll() {
        return venueRepository.findAll();
    }

    public Venue create(Venue venue) {
        return venueRepository.save(venue);
    }

    public Venue update(Long id, Venue venue) {
        Venue existing = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
        existing.setName(venue.getName());
        existing.setAddress(venue.getAddress());
        existing.setCity(venue.getCity());
        existing.setCapacity(venue.getCapacity());
        return venueRepository.save(existing);
    }

    public void delete(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue not found");
        }
        venueRepository.deleteById(id);
    }
}
