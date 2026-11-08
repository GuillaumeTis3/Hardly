package com.guillaume.hardly.controller;

import com.guillaume.hardly.model.Role;
import com.guillaume.hardly.service.UtilisateurService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // ================================
    // Liste des utilisateurs
    // GET /utilisateurs
    // ================================
    @GetMapping
    public String afficherUtilisateurs(Model model) {
        boolean peutVoirAdmins = SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("utilisateurs", utilisateurService.findAllVisibles(peutVoirAdmins));
        model.addAttribute("roles", Role.values());
        return "liste-utilisateurs";
    }

    // ================================
    // Formulaire création
    // GET /utilisateurs/creer
    // ================================
    @GetMapping("/creer")
    public String afficherFormulaireCreation(Model model) {
        model.addAttribute("roles", Role.values());
        return "creer-utilisateur";
    }

    // ================================
    // Traitement création
    // POST /utilisateurs/creer
    // ================================
    @PostMapping("/creer")
    public String creerUtilisateur(
            @RequestParam("login") String login,
            @RequestParam("motPasse") String motPasse,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("role") String role,
            Model model) {

        if (login.isEmpty() || motPasse.isEmpty() || role.isEmpty()) {
            model.addAttribute("erreur", "Veuillez renseigner tous les champs obligatoires.");
            model.addAttribute("roles", Role.values());
            return "creer-utilisateur";
        }

        try {
            utilisateurService.creerUtilisateur(
                login, motPasse, nom, prenom, Role.valueOf(role)
            );
            return "redirect:/utilisateurs";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "creer-utilisateur";
        }
    }

    // ================================
    // Fiche utilisateur
    // GET /utilisateurs/{id}
    // ================================
    @GetMapping("/{id}")
    public String afficherFicheUtilisateur(@PathVariable Long id, Model model) {
        utilisateurService.findById(id).ifPresentOrElse(
            utilisateur -> {
                model.addAttribute("utilisateur", utilisateur);
                model.addAttribute("roles", Role.values());
                model.addAttribute("utilisateurs", utilisateurService.findAll());
            },
            () -> model.addAttribute("erreur", "Utilisateur introuvable.")
        );
        return "fiche-utilisateur";
    }

    // ================================
    // Modification
    // POST /utilisateurs/{id}/modifier
    // ================================
    @PostMapping("/{id}/modifier")
    public String modifierUtilisateur(
            @PathVariable Long id,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("role") String role,
            Model model) {

        try {
            utilisateurService.modifierUtilisateur(id, nom, prenom, Role.valueOf(role));
            model.addAttribute("succes", "Utilisateur modifié avec succès.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
        }

        utilisateurService.findById(id).ifPresent(u -> {
            model.addAttribute("utilisateur", u);
            model.addAttribute("roles", Role.values());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
        });

        return "fiche-utilisateur";
    }

    // ================================
    // Activer / Désactiver
    // POST /utilisateurs/{id}/toggle
    // ================================
    @PostMapping("/{id}/toggle")
    public String toggleActif(@PathVariable Long id) {
        utilisateurService.toggleActif(id);
        return "redirect:/utilisateurs/" + id;
    }

    // ================================
    // Réinitialiser mot de passe
    // POST /utilisateurs/{id}/reinitialiser
    // ================================
    @PostMapping("/{id}/reinitialiser")
    public String reinitialiserMotPasse(
            @PathVariable Long id,
            @RequestParam("nouveauMotPasse") String nouveauMotPasse,
            Model model) {

        try {
            utilisateurService.reinitialiserMotPasse(id, nouveauMotPasse);
            model.addAttribute("succes", "Mot de passe réinitialisé avec succès.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
        }

        utilisateurService.findById(id).ifPresent(u -> {
            model.addAttribute("utilisateur", u);
            model.addAttribute("roles", Role.values());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
        });

        return "fiche-utilisateur";
    }

    // ================================
    // Copier le rôle
    // POST /utilisateurs/{id}/copierRole
    // ================================
    @PostMapping("/{id}/copierRole")
    public String copierRole(
            @PathVariable Long id,
            @RequestParam("idSource") Long idSource,
            Model model) {

        try {
            utilisateurService.copierRole(idSource, id);
            model.addAttribute("succes", "Rôle copié avec succès.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
        }

        utilisateurService.findById(id).ifPresent(u -> {
            model.addAttribute("utilisateur", u);
            model.addAttribute("roles", Role.values());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
        });

        return "fiche-utilisateur";
    }
}