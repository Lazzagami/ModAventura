package com.ruan.medieval_fantasy.progression.specialization;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class TitleRegistry {

    private static final Map<String, TitleDefinition> TITLES = new LinkedHashMap<>();

    static {
        for (PassiveDefinition passive : PassiveRegistry.all()) {
            if (!passive.titleId().isBlank()) {
                register(new TitleDefinition(passive.titleId(), passive.titleName()));
            }
        }
    }

    private TitleRegistry() {
    }

    public static void register(TitleDefinition title) {
        TITLES.put(title.id(), title);
    }

    public static Optional<TitleDefinition> byId(String id) {
        return Optional.ofNullable(TITLES.get(id));
    }

    public static Collection<TitleDefinition> all() {
        return TITLES.values();
    }
}
