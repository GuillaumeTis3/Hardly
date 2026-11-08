package com.guillaume.hardly.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "DOCUMENT")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DOC_ID")
    private Long id;

    @Column(name = "DOC_NOM")
    private String nom;

    @Column(name = "DOC_TYPE")
    private String type;

    @Lob
    @Column(name = "DOC_CONTENU")
    private byte[] contenu;

    @Column(name = "DOC_DATE")
    private LocalDateTime date;

    // Lien obligatoire vers le patient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAT_ID", nullable = false)
    private Patient patient;

    // Lien optionnel vers la venue
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VEN_ID", nullable = true)
    private Venue venue;

    // Responsable — modifiable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOC_RESPONSABLE_ID")
    private Utilisateur responsable;

    // Auteur — fixé à la création
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOC_AUTEUR_ID")
    private Utilisateur auteur;

    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public byte[] getContenu() { return contenu; }
    public void setContenu(byte[] contenu) { this.contenu = contenu; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public Utilisateur getResponsable() { return responsable; }
    public void setResponsable(Utilisateur responsable) { this.responsable = responsable; }
    
    public Utilisateur getAuteur() { return auteur; }
    public void setAuteur(Utilisateur auteur) { this.auteur = auteur; }
}