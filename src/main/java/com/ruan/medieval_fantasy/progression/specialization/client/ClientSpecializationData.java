package com.ruan.medieval_fantasy.progression.specialization.client;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ClientSpecializationData {

    private static final Map<String, Boolean> PENDING = new LinkedHashMap<>();
    private static final Map<String, String> CHOICES = new LinkedHashMap<>();
    private static final Set<String> TITLES = new LinkedHashSet<>();
    private static String equippedTitle = "";

    private ClientSpecializationData() {
    }

    public static void set(Map<String, Boolean> pending, Map<String, String> choices, Set<String> titles, String equipped) {
        PENDING.clear();
        PENDING.putAll(pending);
        CHOICES.clear();
        CHOICES.putAll(choices);
        TITLES.clear();
        TITLES.addAll(titles);
        equippedTitle = equipped == null ? "" : equipped;
    }

    public static Map<String, Boolean> pending() {
        return PENDING;
    }

    public static Map<String, String> choices() {
        return CHOICES;
    }

    public static Set<String> titles() {
        return TITLES;
    }

    public static String equippedTitle() {
        return equippedTitle;
    }
}
