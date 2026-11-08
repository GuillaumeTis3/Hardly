package com.guillaume.hardly.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.guillaume.hardly.model.Role;
import com.guillaume.hardly.model.Utilisateur;
import com.guillaume.hardly.repository.UtilisateurRepository;

@Service
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================================
    // UserDetailsService — requis par Spring Security
    // Appelé automatiquement à chaque tentative de connexion
    // ================================
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Utilisateur introuvable : " + login));
    }

    // ================================
    // Récupération
    // ================================

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> findById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public List<Utilisateur> findByRole(Role role) {
        return utilisateurRepository.findByRole(role);
    }

    public List<Utilisateur> findByActif(boolean actif) {
        return utilisateurRepository.findByActif(actif);
    }

    // ================================
    // Création d'un utilisateur
    // ================================
    public Utilisateur creerUtilisateur(String login, String motPasse,
                                        String nom, String prenom, Role role) {

        // Vérifier si le login existe déjà
        if (utilisateurRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Ce login existe déjà : " + login);
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        // Encodage BCrypt du mot de passe
        utilisateur.setMotPasse(passwordEncoder.encode(motPasse));
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setRole(role);
        utilisateur.setActif(true);

        return utilisateurRepository.save(utilisateur);
    }

    // ================================
    // Modification d'un utilisateur
    // ================================
    public Utilisateur modifierUtilisateur(Long id, String nom, String prenom, Role role) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Utilisateur introuvable : " + id));

        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setRole(role);

        return utilisateurRepository.save(utilisateur);
    }

    // ================================
    // Réinitialisation du mot de passe
    // ================================
    public Utilisateur reinitialiserMotPasse(Long id, String nouveauMotPasse) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Utilisateur introuvable : " + id));

        utilisateur.setMotPasse(passwordEncoder.encode(nouveauMotPasse));

        return utilisateurRepository.save(utilisateur);
    }

    // ================================
    // Copier le rôle d'un utilisateur vers un autre
    // ================================
    public Utilisateur copierRole(Long idSource, Long idCible) {
    
        Utilisateur source = utilisateurRepository.findById(idSource)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Utilisateur source introuvable : " + idSource));
                
        Utilisateur cible = utilisateurRepository.findById(idCible)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Utilisateur cible introuvable : " + idCible));
                
        // Copie du rôle uniquement
        cible.setRole(source.getRole());
                
        return utilisateurRepository.save(cible);
    }

    public List<Utilisateur> findAllVisibles(boolean peutVoirAdmins) {
        if (peutVoirAdmins) {
            return utilisateurRepository.findAll(); // ADMIN voit tout
        }
        // Les autres ne voient pas les comptes ADMIN
        return utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.ROLE_ADMIN)
                .toList();
    }

    // ================================
    // Activer / Désactiver un compte
    // ================================
    public Utilisateur toggleActif(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Utilisateur introuvable : " + id));

        utilisateur.setActif(!utilisateur.isActif());

        return utilisateurRepository.save(utilisateur);
    }
}