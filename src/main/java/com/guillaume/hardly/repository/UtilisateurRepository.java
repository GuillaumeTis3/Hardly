package com.guillaume.hardly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guillaume.hardly.model.Role;
import com.guillaume.hardly.model.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Recherche par login — utilisé par Spring Security
    Optional<Utilisateur> findByLogin(String login);

    // Recherche par rôle — pour la gestion des utilisateurs
    List<Utilisateur> findByRole(Role role);

    // Recherche par actif — pour lister les comptes actifs/inactifs
    List<Utilisateur> findByActif(boolean actif);

    // Vérifier si un login existe déjà — pour éviter les doublons
    boolean existsByLogin(String login);

}