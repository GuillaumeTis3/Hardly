package com.guillaume.hardly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guillaume.hardly.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Tous les documents d'un patient par IPP
    List<Document> findByPatientIpp(String ipp);

    // Tous les documents d'une venue par IEP
    List<Document> findByVenueIep(String iep);

    // Documents d'un patient liés à une venue spécifique
    List<Document> findByPatientIppAndVenueIep(String ipp, String iep);

    // Documents par responsable
    List<Document> findByResponsableId(Long responsableId);

    // Documents par auteur
    List<Document> findByAuteurId(Long auteurId);

}