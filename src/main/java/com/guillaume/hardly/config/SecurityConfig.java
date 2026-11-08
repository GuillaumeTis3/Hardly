package com.guillaume.hardly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.guillaume.hardly.service.UtilisateurService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UtilisateurService utilisateurService;

    public SecurityConfig(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // ================================
    // Encodeur BCrypt — partagé dans toute l'appli
    // ================================
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ================================
    // Provider d'authentification
    // ================================
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(utilisateurService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ================================
    // Authentication Manager
    // ================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ================================
    // Configuration des accès par rôle
    // ================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .requiresChannel(channel -> channel
                .anyRequest().requiresSecure()
            )
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth

                // ================================
                // Pages accessibles à tous les rôles connectés
                // ================================
                .requestMatchers("/patients", "/patients/{ipp}")
                    .hasAnyRole("ADMIN", "MEDECIN", "SECRETAIRE", "ACCUEIL", "READONLY")

                .requestMatchers("/venues/**")
                    .hasAnyRole("ADMIN", "MEDECIN", "SECRETAIRE", "ACCUEIL", "READONLY")

                // ================================
                // Créer/modifier patient — ADMIN et ACCUEIL
                // ================================
                .requestMatchers("/patients/creer", "/patients/*/modifier")
                    .hasAnyRole("ADMIN", "ACCUEIL")

                // ================================
                // Créer venue — ADMIN et ACCUEIL
                // ================================
                .requestMatchers("/venues/creer", "/venues/annuler")
                    .hasAnyRole("ADMIN", "ACCUEIL")

                // ================================
                // Documents — pas ACCUEIL
                // ================================
                .requestMatchers("/documents/**")
                    .hasAnyRole("ADMIN", "MEDECIN", "SECRETAIRE", "READONLY")

                // ================================
                // Upload document — ADMIN, MEDECIN, SECRETAIRE
                // ================================
                .requestMatchers("/documents/upload")
                    .hasAnyRole("ADMIN", "MEDECIN", "SECRETAIRE")

                // ================================
                // HL7 — ADMIN uniquement
                // ================================
                .requestMatchers("/hl7/**")
                    .hasRole("ADMIN")

                // ================================
                // Gestion utilisateurs — ADMIN uniquement
                // ================================
                .requestMatchers("/utilisateurs/**")
                    .hasRole("ADMIN")

                .requestMatchers("/historique/**")
                    .hasAnyRole("ADMIN", "MEDECIN", "SECRETAIRE")

                // Toute autre requête nécessite une connexion
                .anyRequest().authenticated()
            )

            // ================================
            // Page de login personnalisée
            // ================================
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?erreur")
                .permitAll()
            )

            // ================================
            // Déconnexion
            // ================================
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?deconnecte")
                .permitAll()
            );

        return http.build();
    }
}