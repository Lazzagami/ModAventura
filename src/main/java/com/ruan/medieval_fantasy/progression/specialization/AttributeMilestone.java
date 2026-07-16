package com.ruan.medieval_fantasy.progression.specialization;

import com.ruan.medieval_fantasy.scaling.PlayerAttribute;

public record AttributeMilestone(PlayerAttribute attribute, int value) {

    public String key() {
        return attribute.id() + "." + value;
    }
}
