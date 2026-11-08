package com.guillaume.hardly.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guillaume.hardly.model.Patient;
import com.guillaume.hardly.repository.PatientRepository;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.segment.PID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final HL7ParserService hl7ParserService;
    private final NumerotationService numerotationService;
    
    public PatientService(PatientRepository patientRepository,
                          HL7ParserService hl7ParserService,
                          NumerotationService numerotationService) {
        this.patientRepository = patientRepository;
        this.hl7ParserService = hl7ParserService;
        this.numerotationService = numerotationService;
    }

    // Sans venues — pour les listes légères
    @Transactional
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    // Avec venues — quand on a besoin du détail
    @Transactional
    public List<Patient> findAllAvecDonnees() {
        List<Patient> patients = patientRepository.findAll();
        patients.forEach(p -> {
            p.getVenues().size();
            p.getDocuments().size(); // ← ajout
        });
        return patients;
    }

    // Par IPP — sans venues
    public Optional<Patient> findByIpp(String ipp) {
        return patientRepository.findByIpp(ipp);
    }

    // Avec venues — pour un seul patient par IPP
    @Transactional(readOnly = true)
    public Optional<Patient> findByIppAvecVenues(String ipp) {
    
        Optional<Patient> patient = patientRepository.findByIpp(ipp);
    
        patient.ifPresent(p -> p.getVenues().size());
    
        return patient;
    }

    // Remplace VerifyPatientFromAll
    public Optional<Patient> verifyPatient(String nomNaissance, String prenom, LocalDate dateNaissance) {
        return patientRepository.findByNomNaissanceAndPrenomAndDateNaissance(
            nomNaissance, prenom, dateNaissance
        );
    }

    // Remplace InsertOrUpdatePatient
    public Patient insertOrUpdate(Patient patient) {
        return patientRepository.save(patient);
    }

    // Remplace getNextIpp
    public String getNextIppFromPatients() {
        String maxIpp = patientRepository.findMaxIpp();
        if (maxIpp == null) return "000000001";
        return String.format("%09d", Integer.parseInt(maxIpp) + 1);
    }

    public String getNextIpp() {
        return numerotationService.genererNumero("IPP");
    }

    // Remplace getResumePatient — sorti du modèle Patient
    public String getResumePatient(Patient p) {
        String prenomFormate = StringUtils.capitalize(p.getPrenom().toLowerCase());
        String dateFormatee = hl7ParserService.getDateLisible(p.getDateNaissance());

        if (p.getNomUsage() == null || p.getNomUsage().isEmpty()) {
            return "IPP n°" + p.getIpp() + ", "
                + p.getNomNaissance().toUpperCase() + " "
                + prenomFormate + " le " + dateFormatee;
        } else {
            return "IPP n°" + p.getIpp() + ", "
                + p.getNomUsage().toUpperCase() + " "
                + prenomFormate + " né(e) "
                + p.getNomNaissance().toUpperCase() + " le " + dateFormatee;
        }
    }

    // Retourne le Patient sauvegardé pour le lier au message
    public Patient addPatient(Message messageRecu) {

        PID segmentPID = hl7ParserService.getPID(messageRecu);
        String iPP = hl7ParserService.getIPP(segmentPID);

        Patient p = patientRepository.findByIpp(iPP).orElseGet(Patient::new);
        p.setIpp(iPP);
        p.setNomNaissance(hl7ParserService.getNomPatient(segmentPID, "L"));
        p.setNomUsage(hl7ParserService.getNomPatient(segmentPID, "D"));
        p.setPrenom(hl7ParserService.getPrenom(segmentPID));
        p.setSexe(hl7ParserService.getSexe(segmentPID));

        try {
            p.setDateNaissance(hl7ParserService.getDateNaissance(segmentPID));
        } catch (DataTypeException e) {
            throw new RuntimeException("Erreur date de naissance", e);
        }

        return patientRepository.save(p);
    }

}