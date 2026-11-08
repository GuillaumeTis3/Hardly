package com.guillaume.hardly.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.guillaume.hardly.model.Document;
import com.guillaume.hardly.model.Patient;
import com.guillaume.hardly.model.Role;
import com.guillaume.hardly.model.Utilisateur;
import com.guillaume.hardly.model.Venue;
import com.guillaume.hardly.repository.DocumentRepository;
import com.guillaume.hardly.repository.PatientRepository;
import com.guillaume.hardly.repository.UtilisateurRepository;
import com.guillaume.hardly.repository.VenueRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final PatientRepository patientRepository;
    private final VenueRepository venueRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DocumentService(DocumentRepository documentRepository,
                           PatientRepository patientRepository,
                           VenueRepository venueRepository,
                           UtilisateurRepository utilisateurRepository) {
        this.documentRepository = documentRepository;
        this.patientRepository = patientRepository;
        this.venueRepository = venueRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ================================
    // Récupération
    // ================================

    public List<Document> findByPatientIpp(String ipp) {
        return documentRepository.findByPatientIpp(ipp);
    }

    public List<Document> findByVenueIep(String iep) {
        return documentRepository.findByVenueIep(iep);
    }

    public List<Document> findByPatientIppAndVenueIep(String ipp, String iep) {
        return documentRepository.findByPatientIppAndVenueIep(ipp, iep);
    }

    public Optional<Document> findById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> findByResponsableId(Long responsableId) {
        return documentRepository.findByResponsableId(responsableId);
    }

    public List<Document> findByAuteurId(Long auteurId) {
        return documentRepository.findByAuteurId(auteurId);
    }

    // ================================
    // Upload depuis l'IHM
    // ================================
    public Document uploadDocument(String ipp, String iep,
                                   Long auteurId, Long responsableId,
                                   MultipartFile fichier) throws IOException {

        // Patient — obligatoire
        Patient patient = patientRepository.findByIpp(ipp)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable : " + ipp));

        // Venue — optionnelle
        Venue venue = null;
        if (iep != null && !iep.isEmpty()) {
            venue = venueRepository.findByIep(iep).orElse(null);
        }

        // Auteur — obligatoire
        Utilisateur auteur = utilisateurRepository.findById(auteurId)
                .orElseThrow(() -> new IllegalArgumentException("Auteur introuvable : " + auteurId));

        // Responsable — obligatoire
        Utilisateur responsable = getResponsableMedecin(responsableId);

        Document document = new Document();
        document.setNom(fichier.getOriginalFilename());
        document.setType(fichier.getContentType());
        document.setContenu(fichier.getBytes());
        document.setDate(LocalDateTime.now());
        document.setPatient(patient);
        document.setVenue(venue);
        document.setAuteur(auteur);
        document.setResponsable(responsable);

        return documentRepository.save(document);
    }

    // ================================
    // Réception depuis un message HL7 ORU
    // ================================
    public Document saveFromHL7(String ipp, String iep,
                                String nomFichier, String contenuBase64,
                                Long auteurId, Long responsableId) {

        // Patient — obligatoire
        Patient patient = patientRepository.findByIpp(ipp)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable : " + ipp));

        // Venue — optionnelle
        Venue venue = null;
        if (iep != null && !iep.isEmpty()) {
            venue = venueRepository.findByIep(iep).orElse(null);
        }

        // Auteur — obligatoire
        Utilisateur auteur = utilisateurRepository.findById(auteurId)
                .orElseThrow(() -> new IllegalArgumentException("Auteur introuvable : " + auteurId));

        // Responsable — optionnel en HL7, peut être null
        Utilisateur responsable = getResponsableMedecin(responsableId);

        byte[] contenu = Base64.getDecoder().decode(contenuBase64);

        Document document = new Document();
        document.setNom(nomFichier);
        document.setType("application/pdf");
        document.setContenu(contenu);
        document.setDate(LocalDateTime.now());
        document.setPatient(patient);
        document.setVenue(venue);
        document.setAuteur(auteur);
        document.setResponsable(responsable);

        return documentRepository.save(document);
    }

    private Utilisateur getResponsableMedecin(Long responsableId) {
        Utilisateur responsable = utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Responsable introuvable : " + responsableId));
                
        if (responsable.getRole() != Role.ROLE_MEDECIN) {
            throw new IllegalArgumentException(
                "Le responsable doit avoir le rôle MEDECIN — "
                + responsable.getLogin() + " a le rôle "
                + responsable.getRole().getLibelle());
        }
    
        return responsable;
    }

    // ================================
    // Modifier le responsable
    // ================================
    public Document modifierResponsable(Long documentId, Long responsableId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Document introuvable : " + documentId));

        // Vérifie rôle MEDECIN et obligatoire
        Utilisateur responsable = getResponsableMedecin(responsableId);

        document.setResponsable(responsable);
        return documentRepository.save(document);
    }
}