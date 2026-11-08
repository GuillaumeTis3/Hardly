package com.guillaume.hardly.model;

import java.util.Arrays;
import java.util.Optional;

public enum HL7EvenementType {

    // ================================
    // Événements PATIENT — type ADT
    // ================================
    A28("A28", "Création patient",     CategorieEvenement.PATIENT, TypeMessage.ADT),
    A31("A31", "Modification patient", CategorieEvenement.PATIENT, TypeMessage.ADT),
    A40("A40", "Fusion patient",       CategorieEvenement.PATIENT, TypeMessage.ADT),

    // ================================
    // Événements VENUE — type ADT
    // ================================
    A01("A01", "Admission",            CategorieEvenement.VENUE, TypeMessage.ADT),
    A02("A02", "Transfert",            CategorieEvenement.VENUE, TypeMessage.ADT),
    A03("A03", "Sortie",               CategorieEvenement.VENUE, TypeMessage.ADT),
    A04("A04", "Pré-admission",        CategorieEvenement.VENUE, TypeMessage.ADT),
    A08("A08", "Mise à jour venue",    CategorieEvenement.VENUE, TypeMessage.ADT),

    // ================================
    // Événements BIOLOGIE — type ORU
    // ================================
    R01("R01", "Résultat d'examen",    CategorieEvenement.BIOLOGIE, TypeMessage.ORU);

    // ================================
    // Types de message
    // ================================
    public enum TypeMessage {
        ADT,
        ORU,
        ORM,
        ACK
    }

    // ================================
    // Catégories
    // ================================
    public enum CategorieEvenement {
        PATIENT,
        VENUE,
        BIOLOGIE
    }

    private final String code;
    private final String libelle;
    private final CategorieEvenement categorie;
    private final TypeMessage typeMessage;

    HL7EvenementType(String code, String libelle,
                     CategorieEvenement categorie, TypeMessage typeMessage) {
        this.code = code;
        this.libelle = libelle;
        this.categorie = categorie;
        this.typeMessage = typeMessage;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public CategorieEvenement getCategorie() { return categorie; }
    public TypeMessage getTypeMessage() { return typeMessage; }

    // Retourne "ADT^A28", "ORU^R01"...
    public String getTypeComplet() {
        return typeMessage.name() + "^" + code;
    }

    // ================================
    // Recherche par code événement
    // ================================
    public static Optional<HL7EvenementType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst();
    }

    // Recherche par type ET code — plus précis
    public static Optional<HL7EvenementType> fromTypeEtCode(String type, String code) {
        return Arrays.stream(values())
                .filter(e -> e.getTypeMessage().name().equals(type)
                          && e.getCode().equals(code))
                .findFirst();
    }

    // ================================
    // Vérifications
    // ================================
    public static boolean estConnu(String type, String code) {
        return fromTypeEtCode(type, code).isPresent();
    }

    public static boolean estPatient(String code) {
        return fromCode(code)
                .map(e -> e.getCategorie() == CategorieEvenement.PATIENT)
                .orElse(false);
    }

    public static boolean estVenue(String code) {
        return fromCode(code)
                .map(e -> e.getCategorie() == CategorieEvenement.VENUE)
                .orElse(false);
    }

    public static boolean estBiologie(String code) {
        return fromCode(code)
                .map(e -> e.getCategorie() == CategorieEvenement.BIOLOGIE)
                .orElse(false);
    }

    public static String getLibelle(String code) {
        return fromCode(code)
                .map(HL7EvenementType::getLibelle)
                .orElse("Événement inconnu : " + code);
    }
}