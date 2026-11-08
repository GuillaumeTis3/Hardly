package com.guillaume.hardly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guillaume.hardly.model.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    // Toutes les venues d'un patient par IPP
    List<Venue> findByPatientIpp(String ipp);

    // Recherche par numéro de venue (IEP)
    Optional<Venue> findByIep(String iep);

}