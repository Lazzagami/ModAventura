package com.ruan.medieval_fantasy.scaling;

public enum PlayerAttribute {
    VITALITY("vitalidade"),
    STRENGTH("forca"),
    DEFENSE("defesa"),
    AGILITY("agilidade"),
    RELIC_CONTROL("controle_de_reliquias"),
    MAGIC("magia"),
    ENERGY("energia"),
    LUCK("sorte"),
    REGENERATION("regeneracao"),
    ELEMENTAL_RESISTANCE("resistencia_elemental"),
    PRECISION("precisao");

    private final String id;

    PlayerAttribute(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
