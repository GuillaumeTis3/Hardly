package com.guillaume.hardly.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guillaume.hardly.model.Numerotation;

public interface NumerotationRepository extends JpaRepository<Numerotation, Long> {

    // Récupérer la configuration par type (IPP ou IEP)
    Optional<Numerotation> findByType(String type);

}