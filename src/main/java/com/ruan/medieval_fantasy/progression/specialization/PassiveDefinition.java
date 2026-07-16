package com.ruan.medieval_fantasy.progression.specialization;

import com.ruan.medieval_fantasy.scaling.PlayerAttribute;

import java.util.UUID;

public record PassiveDefinition(
        String id,
        PlayerAttribute attribute,
        int milestone,
        int option,
        String titleId,
        String titleName,
        String displayName,
        String description,
        PassiveType type,
        double value,
        int durationTicks,
        int cooldownTicks,
        int maxStacks,
        UUID modifierUuid
) {

    public boolean reserved() {
        return option <= 0;
    }

    public static Builder builder(String id, PlayerAttribute attribute, int milestone, int option) {
        return new Builder(id, attribute, milestone, option);
    }

    public static final class Builder {
        private final String id;
        private final PlayerAttribute attribute;
        private final int milestone;
        private final int option;
        private String titleId = "";
        private String titleName = "";
        private String displayName = "";
        private String description = "";
        private PassiveType type = PassiveType.RESERVED;
        private double value;
        private int durationTicks;
        private int cooldownTicks;
        private int maxStacks;
        private UUID modifierUuid;

        private Builder(String id, PlayerAttribute attribute, int milestone, int option) {
            this.id = id;
            this.attribute = attribute;
            this.milestone = milestone;
            this.option = option;
        }

        public Builder title(String titleId, String titleName) {
            this.titleId = titleId;
            this.titleName = titleName;
            return this;
        }

        public Builder display(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
            return this;
        }

        public Builder effect(PassiveType type, double value) {
            this.type = type;
            this.value = value;
            return this;
        }

        public Builder timing(int durationTicks, int cooldownTicks) {
            this.durationTicks = durationTicks;
            this.cooldownTicks = cooldownTicks;
            return this;
        }

        public Builder stacks(int maxStacks) {
            this.maxStacks = maxStacks;
            return this;
        }

        public Builder modifier(String uuid) {
            this.modifierUuid = UUID.fromString(uuid);
            return this;
        }

        public PassiveDefinition build() {
            return new PassiveDefinition(id, attribute, milestone, option, titleId, titleName, displayName,
                    description, type, value, durationTicks, cooldownTicks, maxStacks, modifierUuid);
        }
    }
}
