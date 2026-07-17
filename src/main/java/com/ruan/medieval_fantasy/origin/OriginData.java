package com.ruan.medieval_fantasy.origin;

import java.util.List;
import java.util.Map;

public class OriginData {

    private final OriginType type;
    private final String displayName;
    private final String subtitle;
    private final String description;
    private final List<String> traits;
    private final Map<String, Integer> initialReputation;

    public OriginData(OriginType type, String displayName, String subtitle, String description,
                      List<String> traits, Map<String, Integer> initialReputation) {
        this.type = type;
        this.displayName = displayName;
        this.subtitle = subtitle;
        this.description = description;
        this.traits = List.copyOf(traits);
        this.initialReputation = Map.copyOf(initialReputation);
    }

    public OriginType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTraits() {
        return traits;
    }

    public Map<String, Integer> getInitialReputation() {
        return initialReputation;
    }
}
