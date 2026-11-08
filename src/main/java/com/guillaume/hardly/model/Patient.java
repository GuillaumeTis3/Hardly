package com.guillaume.hardly.model;

import java.time.LocalDate; 
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "PATIENT")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pat_id")
    private Long id;

    @Column(name = "pat_ipp", unique = true)
    private String ipp;

    @Column(name = "pat_nom_naissance")
    private String nomNaissance;

    @Column(name = "pat_nom_usage")
    private String nomUsage;

    @Column(name = "pat_prenom")
    private String prenom;

    @Column(name = "pat_sexe")
    private String sexe;

    @Column(name = "pat_date_naissance")
    private LocalDate dateNaissance;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Venue> venues;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents;

    public Long getId() { return id; }

    public String getIpp() { return ipp; }
    public void setIpp(String ipp) { this.ipp = ipp; }

    public String getNomNaissance() { return nomNaissance; }
    public void setNomNaissance(String nomNaissance) { this.nomNaissance = nomNaissance; }

    public String getNomUsage() { return nomUsage; }
    public void setNomUsage(String nomUsage) { this.nomUsage = nomUsage; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public List<Venue> getVenues() { return venues; }
    public void setVenues(List<Venue> venues) { this.venues = venues; }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }
}