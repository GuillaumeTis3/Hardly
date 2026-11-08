package com.guillaume.hardly.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "NUMEROTATION")
public class Numerotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NUM_ID")
    private Long id;

    @Column(name = "NUM_TYPE")
    private String type;

    @Column(name = "NUM_PREFIXE")
    private String prefixe;

    @Column(name = "NUM_SUFFIXE")
    private String suffixe;

    @Column(name = "NUM_LONGUEUR")
    private int longueur;

    @Column(name = "NUM_VALEUR")
    private Long valeur;

    public Long getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPrefixe() { return prefixe; }
    public void setPrefixe(String prefixe) { this.prefixe = prefixe; }

    public String getSuffixe() { return suffixe; }
    public void setSuffixe(String suffixe) { this.suffixe = suffixe; }

    public int getLongueur() { return longueur; }
    public void setLongueur(int longueur) { this.longueur = longueur; }

    public Long getValeur() { return valeur; }
    public void setValeur(Long valeur) { this.valeur = valeur; }
}