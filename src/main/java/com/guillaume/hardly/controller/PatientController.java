package com.guillaume.hardly.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.guillaume.hardly.model.Patient;
import com.guillaume.hardly.service.PatientService;

@Controller
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // ================================
    // Afficher la liste des patients
    // GET /patients
    // ================================
    // Liste des patients — avec venues
    @GetMapping
    public String afficherPatients(Model model) {
        model.addAttribute("patients", patientService.findAllAvecDonnees());
        return "liste-patients";
    }

    // ================================
    // Afficher le formulaire de création
    // GET /patients/creer
    // ================================
    @GetMapping("/creer")
    public String afficherFormulaire(Model model) {
        model.addAttribute("patient", new Patient());
        return "creer-patient";
    }

    // ================================
    // Traiter la soumission du formulaire
    // POST /patients/creer
    // ================================
    @PostMapping("/creer")
    public String creerPatient(
            @RequestParam("nomNaissance") String nomNaissance,
            @RequestParam("prenom") String prenom,
            @RequestParam("dateNaissance") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissance,
            Model model) {

        // Validation — champs vides
        if (nomNaissance.isEmpty() || prenom.isEmpty() || dateNaissance == null) {
            model.addAttribute("erreur", "Vous n'avez pas renseigné tous les champs.");
            model.addAttribute("patient", new Patient());
            return "creer-patient";
        }

        // Vérification doublon — remplace VerifyPatientFromAll
        Optional<Patient> existant = patientService.verifyPatient(
            nomNaissance.toUpperCase(),
            prenom.toUpperCase(),
            dateNaissance
        );

        if (existant.isPresent()) {
            model.addAttribute("erreur",
                "Le patient existe déjà : "
                + patientService.getResumePatient(existant.get()));
            model.addAttribute("patient", new Patient());
            return "creer-patient";
        }

        // Création du patient
        Patient patient = new Patient();
        patient.setNomNaissance(nomNaissance.toUpperCase());
        patient.setPrenom(prenom.toUpperCase());
        patient.setDateNaissance(dateNaissance);
        patient.setIpp(patientService.getNextIpp());

        patientService.insertOrUpdate(patient);

        // Redirige vers la liste après création
        return "redirect:/patients";
    }

    // ================================
    // Fiche patient
    // GET /patients/{ipp}
    // ================================
    @GetMapping("/{ipp}")
    public String afficherFichePatient(@PathVariable String ipp, Model model) {
        patientService.findByIpp(ipp).ifPresentOrElse(
            patient -> model.addAttribute("patient", patient),
            () -> model.addAttribute("erreur", "Patient introuvable pour l'IPP : " + ipp)
        );
        return "fiche-patient";
    }

    // ================================
    // Modification du patient
    // POST /patients/{ipp}/modifier
    // ================================
    @PostMapping("/{ipp}/modifier")
    public String modifierPatient(
            @PathVariable String ipp,
            @RequestParam("nomNaissance") String nomNaissance,
            @RequestParam("nomUsage") String nomUsage,
            @RequestParam("prenom") String prenom,
            @RequestParam("sexe") String sexe,
            @RequestParam("dateNaissance") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissance,
            Model model) {

        // Validation
        if (nomNaissance.isEmpty() || prenom.isEmpty() || dateNaissance == null) {
            model.addAttribute("erreur", "Veuillez renseigner tous les champs obligatoires.");
            patientService.findByIpp(ipp).ifPresent(p -> model.addAttribute("patient", p));
            return "fiche-patient";
        }

        patientService.findByIpp(ipp).ifPresentOrElse(patient -> {
            patient.setNomNaissance(nomNaissance.toUpperCase());
            patient.setNomUsage(nomUsage.toUpperCase());
            patient.setPrenom(prenom.toUpperCase());
            patient.setSexe(sexe);
            patient.setDateNaissance(dateNaissance);
            patientService.insertOrUpdate(patient);
            model.addAttribute("patient", patient);
            model.addAttribute("succes", "Patient modifié avec succès.");
        },
        () -> model.addAttribute("erreur", "Patient introuvable pour l'IPP : " + ipp));

        return "fiche-patient";
    }

}