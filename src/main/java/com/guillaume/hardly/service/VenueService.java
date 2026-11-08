package com.guillaume.hardly.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guillaume.hardly.model.Patient;
import com.guillaume.hardly.model.Venue;
import com.guillaume.hardly.repository.PatientRepository;
import com.guillaume.hardly.repository.VenueRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VenueService {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private NumerotationService numerotationService;

    // Toutes les venues d'un patient par IPP
    @Transactional
    public List<Venue> findByPatientIpp(String ipp) {
        return venueRepository.findByPatientIpp(ipp);
    }

    // Recherche par numéro de venue (IEP)
    public Optional<Venue> findByIep(String iep) {
        return venueRepository.findByIep(iep);
    }

    public String reserverIep() {
        return numerotationService.genererNumero("IEP");
    }

    // Annuler une réservation — appelé si abandon
    public void annulerReservationIep() {
        numerotationService.annulerReservation("IEP");
    }

    // Créer une nouvelle venue pour un patient
    public Venue creerVenue(String ipp, String iep, Venue venue) {

        // Récupérer le patient via son IPP
        Patient patient = patientRepository.findByIpp(ipp).orElseThrow(() ->
                    new EntityNotFoundException("Patient introuvable pour l'IPP : " + ipp)
            );

        // Génération automatique de l'IEP
        venue.setIep(iep);
        venue.setPatient(patient);

        return venueRepository.save(venue);
    }

    public String getProchainIep() {
        return numerotationService.consulterProchainNumero("IEP");
    }

}