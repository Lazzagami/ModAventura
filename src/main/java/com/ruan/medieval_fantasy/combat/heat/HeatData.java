package com.ruan.medieval_fantasy.combat.heat;

public class HeatData {

    public static final int MIN_HEAT = 0;
    public static final int MAX_HEAT = 100;
    public static final int HEAT_PER_VALID_HIT = 1;

    public static final int WARMING_MIN = 21;
    public static final int OVERHEATED_MIN = 41;
    public static final int COMBUSTION_MIN = 61;
    public static final int INTERNAL_COMBUSTION_MIN = 81;

    public static final int NO_ATTACK_COOL_DELAY_TICKS = 20 * 3;
    public static final int SELECTED_COOL_INTERVAL_TICKS = 20 * 2;
    public static final int STORED_COOL_INTERVAL_TICKS = 20;
    public static final int RAIN_COOL_INTERVAL_TICKS = 20;
    public static final int RAIN_COOL_AMOUNT = 2;

    public static final int COMBUSTION_REFRESH_INTERVAL_TICKS = 20;
    public static final int COMBUSTION_SECONDS = 2;
    public static final int THERMAL_DAMAGE_INTERVAL_TICKS = 20;
    public static final float THERMAL_DAMAGE = 1.0F;

    public static final String HEAT_TAG = "medieval_fantasy.eternal_fire_blade_heat";
    public static final String LAST_ATTACK_TICK_TAG = "medieval_fantasy.eternal_fire_blade_last_attack";
    public static final String SELECTED_COOL_TICK_TAG = "medieval_fantasy.eternal_fire_blade_selected_cool_tick";
    public static final String STORED_COOL_TICK_TAG = "medieval_fantasy.eternal_fire_blade_stored_cool_tick";
    public static final String RAIN_COOL_TICK_TAG = "medieval_fantasy.eternal_fire_blade_rain_cool_tick";
    public static final String THERMAL_TICK_TAG = "medieval_fantasy.eternal_fire_blade_thermal_tick";
    public static final String SOUND_TICK_TAG = "medieval_fantasy.eternal_fire_blade_sound_tick";
    public static final String LAST_SYNC_HEAT_TAG = "medieval_fantasy.eternal_fire_blade_last_sync_heat";

    private HeatData() {
    }
}
