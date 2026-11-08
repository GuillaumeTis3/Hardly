package com.guillaume.hardly.controller;

import java.time.LocalDateTime;
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

import com.guillaume.hardly.model.Venue;
import com.guillaume.hardly.service.PatientService;
import com.guillaume.hardly.service.VenueService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private PatientService patientService;

    // ================================
    // Liste des venues d'un patient
    // GET /venues/{ipp}
    // ================================
    @GetMapping("/{ipp}")
    public String afficherVenues(@PathVariable String ipp, Model model) {
        model.addAttribute("venues", venueService.findByPatientIpp(ipp));
        model.addAttribute("ipp", ipp);
        model.addAttribute("patient", patientService.findByIpp(ipp).orElse(null));
        return "liste-venues";
    }

    // ================================
    // Recherche par numéro de venue
    // GET /venues/recherche?iep=00001
    // ================================
    @GetMapping("/recherche")
    public String rechercherVenue(@RequestParam("iep") String iep, Model model) {
        Optional<Venue> venue = venueService.findByIep(iep);
        if (venue.isPresent()) {
            String ipp = venue.get().getPatient().getIpp();
            return "redirect:/venues/" + ipp;
        } else {
            model.addAttribute("erreur", "Aucune venue trouvée pour le numéro : " + iep);
            model.addAttribute("venues", null);
            return "liste-venues";
        }
    }

    // ================================
    // Formulaire de création
    // GET /venues/creer
    // ================================
    @GetMapping("/creer")
    public String afficherFormulaireCreation(
            @RequestParam(value = "ipp", required = false) String ipp,
            HttpSession session,
            Model model) {

        // Si un IEP était déjà réservé dans la session → on l'annule
        String iepExistant = (String) session.getAttribute("iepReserve");
        if (iepExistant != null) {
            venueService.annulerReservationIep();
            session.removeAttribute("iepReserve");
        }

        // Réserver un nouvel IEP
        String iepReserve = venueService.reserverIep();
        session.setAttribute("iepReserve", iepReserve);

        model.addAttribute("venue", new Venue());
        model.addAttribute("ipp", ipp != null ? ipp : "");
        model.addAttribute("iepReserve", iepReserve);
        return "creer-venue";
    }

    // ================================
    // Traitement de la création
    // POST /venues/creer
    // ================================
    @PostMapping("/creer")
    public String creerVenue(
            @RequestParam("ipp") String ipp,
            @RequestParam("dateDebut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(value = "dateFin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            HttpSession session,
            Model model) {

        // Récupérer l'IEP réservé depuis la session
        String iepReserve = (String) session.getAttribute("iepReserve");

        if (iepReserve == null) {
            model.addAttribute("erreur", "Session expirée, veuillez recommencer.");
            model.addAttribute("ipp", ipp);
            return "creer-venue";
        }

        // Validation
        if (ipp.isEmpty() || dateDebut == null) {
            model.addAttribute("erreur", "Veuillez renseigner tous les champs obligatoires.");
            model.addAttribute("ipp", ipp);
            model.addAttribute("iepReserve", iepReserve);
            return "creer-venue";
        }

        try {
            Venue venue = new Venue();
            venue.setDateDebut(dateDebut);
            venue.setDateFin(dateFin);
            venueService.creerVenue(ipp, iepReserve, venue);

            // Supprimer la réservation de la session
            session.removeAttribute("iepReserve");

            return "redirect:/venues/" + ipp;

        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("ipp", ipp);
            model.addAttribute("iepReserve", iepReserve);
            return "creer-venue";
        }
    }

    // ================================
    // Annulation explicite
    // GET /venues/annuler
    // ================================
    @GetMapping("/annuler")
    public String annulerCreation(
            HttpSession session,
            @RequestParam(value = "ipp", required = false) String ipp) {

        // Annuler la réservation si elle existe
        String iepReserve = (String) session.getAttribute("iepReserve");
        if (iepReserve != null) {
            venueService.annulerReservationIep();
            session.removeAttribute("iepReserve");
        }

        // Retourner vers la liste du patient si IPP connu
        if (ipp != null && !ipp.isEmpty()) {
            return "redirect:/venues/" + ipp;
        }
        return "redirect:/";
    }
}