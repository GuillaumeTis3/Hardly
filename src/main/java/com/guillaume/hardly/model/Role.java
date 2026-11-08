package com.guillaume.hardly.model;

public enum Role {

    ROLE_ADMIN(1),
    ROLE_MEDECIN(2),
    ROLE_SECRETAIRE(3),
    ROLE_ACCUEIL(4),
    ROLE_READONLY(5);

    private final int id;

    Role(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // Retourne le rôle sans le préfixe ROLE_ pour l'affichage
    public String getLibelle() {
        return this.name().replace("ROLE_", "");
    }

    // Retrouver un rôle par son ID
    public static Role fromId(int id) {
        for (Role role : values()) {
            if (role.getId() == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rôle introuvable pour l'ID : " + id);
    }
}