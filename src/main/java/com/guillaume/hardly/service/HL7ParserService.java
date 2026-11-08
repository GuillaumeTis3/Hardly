package com.guillaume.hardly.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.guillaume.hardly.model.HL7EvenementType;
import com.guillaume.hardly.model.Patient;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.parser.Parser;

@Service
public class HL7ParserService {

    HapiContext context = new DefaultHapiContext();
    Parser parser = context.getGenericParser();

    public boolean verifierStructure(String message) {
        try {
            parser.parse(message);
            return true;
        } catch (HL7Exception e) {
            return false;
        }
    }

    public MSH getMSH(Message message) {
        try {
            return (MSH) message.get("MSH");
        } catch (HL7Exception e) {
            throw new RuntimeException("Impossible de récupérer le segment MSH", e);
        }
    }

    public PID getPID(Message message) {
        try {
            return (PID) message.get("PID");
        } catch (HL7Exception e) {
            throw new RuntimeException("Impossible de récupérer le segment PID", e);
        }
    }
    
    public PV1 getPV1(Message message) {
        try {
            return (PV1) message.get("PV1");
        } catch (HL7Exception e) {
            throw new RuntimeException("Impossible de récupérer le segment PV1", e);
        }
    }

    public String getIPP(PID segmentPIDPourIPP) {
        String IPP = "IPP non trouvé. ";
        for (CX iD : segmentPIDPourIPP.getPatientIdentifierList()) {
            if (iD.getIdentifierTypeCode().toString().equals("PI")) {
                IPP = iD.getIDNumber().getValue();
            }
        }
        return IPP;
    }

    public Patient getPatient(String iPP, String nomNaissance, String nomUsage,
            String prenom, String sexe, LocalDate dateNaissance) {
        Patient patient = new Patient();
        patient.setIpp(iPP);
        patient.setNomNaissance(nomNaissance);
        patient.setNomUsage(nomUsage);
        patient.setPrenom(prenom);
        patient.setSexe(sexe);
        patient.setDateNaissance(dateNaissance);
        return patient;
    }

    public String getSexe(PID segmentPID) {
        return segmentPID.getAdministrativeSex().toString();
    }

    public LocalDate getDateNaissance(PID segmentPID) throws DataTypeException {
        Date date = segmentPID.getDateTimeOfBirth().getTime().getValueAsDate();
        return date.toInstant()
                   .atZone(java.time.ZoneId.systemDefault())
                   .toLocalDate();
    }

    public String getNomPatient(PID segmentPID, String typeNom) {
        XPN[] listeNoms = segmentPID.getPatientName();
        String nomFamille = "";
        for (XPN nom : listeNoms) {
            if (nom.getNameTypeCode().toString().equals(typeNom)) {
                nomFamille = nom.getFamilyName().getSurname().getValue();
            }
        }
        return nomFamille;
    }

    public String getPrenom(PID segmentPID) {
        XPN[] listeNoms = segmentPID.getPatientName();
        String prenom = "";
        for (XPN noms : listeNoms) {
            if (noms.getNameTypeCode().toString().equals("L")) {
                prenom = noms.getGivenName().toString();
            }
            if (noms.getNameTypeCode().toString().equals("D") && prenom.equals("")) {
                prenom = noms.getGivenName().toString();
            }
        }
        return prenom;
    }

    public String getIEP(PV1 segmentPV1) {
        return segmentPV1.getVisitNumber().getIDNumber().getValue();
    }

    public String getDateLisible(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    // ================================
    // Extraire le type de message (ADT, ORU...)
    // ================================
    public String getTypeMessage(Message message) {
        try {
            MSH msh = getMSH(message);
            return msh.getMessageType().getMessageCode().getValue(); // MSH.9.1
        } catch (Exception e) {
            return "INCONNU";
        }
    }
    
    // ================================
    // Extraire le code événement (A28, R01...)
    // ================================
    public String getCodeEvenement(Message message) {
        try {
            MSH msh = getMSH(message);
            return msh.getMessageType().getTriggerEvent().getValue(); // MSH.9.2
        } catch (Exception e) {
            return "INCONNU";
        }
    }

    // ================================
    // Générer la base patient du résumé
    // ================================ 
    public String getResumeBasePatient(Message message){
        PID pid = getPID(message);

        String ipp = getIPP(pid);
        String nom = getNomPatient(pid, "L");
        String prenom = getPrenom(pid);
        String sexe = getSexe(pid);
        String dateNaissance = "";
        try {
            LocalDate ddn = getDateNaissance(pid);
            dateNaissance = ddn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            dateNaissance = "date inconnue";
        }

        // Base commune — IPP + identité + sexe + date naissance
        String basePatient = "IPP: " + ipp + " — "
                + nom + " " + prenom + " (" + sexe + ") " + " — "
                + dateNaissance;

        return basePatient; 
    }

    // ================================
    // Générer la base venue du résumé
    // ================================ 
    public String getResumeBaseVenue(Message message){
        PV1 pv1 = getPV1(message);

        String iep = "";
        try {
            iep = getIEP(pv1);
        } catch (Exception e) {
            iep = "IEP inconnu";
        }

        //LocalDateTime dateAdmission;
        //try {
        //    dateAdmission = LocalDateTime.from(pv1.getAdmitDateTime().getTime().getValueAsDate().toInstant());
        //} catch (Exception e) {
        //    dateAdmission = LocalDateTime.now();
        //}

        String baseVenue= "IEP " + iep 
        //+ " du " +  dateAdmission.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ;

        return baseVenue; 

    }
    
    // ================================
    // Générer le résumé lisible du message
    // ================================
    public String genererResume(Message message) {
        try {
            String type = getTypeMessage(message);
            String code = getCodeEvenement(message);

            // Vérifier si l'événement est connu
            if (!HL7EvenementType.estConnu(type, code)) {
                return "[INCONNU] " + type + "^" + code;
            }

            String libelle = HL7EvenementType.getLibelle(code);
            
            String basePatient = getResumeBasePatient(message);

            // Résumé selon la catégorie
            if (HL7EvenementType.estPatient(code)) {
                return "[" + libelle.toUpperCase() + "] " + basePatient;
            } else if (HL7EvenementType.estVenue(code)) {
                String baseVenue = getResumeBaseVenue(message);
                
                return "[" + libelle.toUpperCase() + "] "
                        + basePatient + " — " + baseVenue;

            } else if (HL7EvenementType.estBiologie(code)) {
                return "[" + libelle.toUpperCase() + "] " + basePatient;
            }

            return "[" + libelle.toUpperCase() + "] " + basePatient;

        } catch (Exception e) {
            return "Résumé indisponible";
        }
    }

}