package com.guillaume.hardly.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guillaume.hardly.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Remplace SelectPatientsFromIPP
    Optional<Patient> findByIpp(String ipp);

    // Remplace VerifyPatientFromAll
    Optional<Patient> findByNomNaissanceAndPrenomAndDateNaissance(
        String nomNaissance,
        String prenom,
        LocalDate dateNaissance
    );

    // Remplace getNextIpp
    // @Query nativeQuery — car pas exprimable en méthode nommée
    @Query(value = "SELECT MAX(PAT_IPP) FROM PATIENT", nativeQuery = true)
    String findMaxIpp();

    // SelectPatients  → findAll() déjà disponible
    // InsertOrUpdatePatient → save() déjà disponible
    // UpdatePatient   → save() déjà disponible
}