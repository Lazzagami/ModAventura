package com.ruan.medieval_fantasy.progression.experience;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public final class ExperienceRewardRegistry {

    private static final Map<ResourceLocation, Integer> ENTITY_XP = new HashMap<>();
    private static final Map<String, Integer> BOSS_XP = new HashMap<>();

    static {
        ENTITY_XP.put(new ResourceLocation("minecraft", "zombie"), 8);
        ENTITY_XP.put(new ResourceLocation("minecraft", "skeleton"), 9);
        ENTITY_XP.put(new ResourceLocation("minecraft", "creeper"), 12);
        ENTITY_XP.put(new ResourceLocation("minecraft", "enderman"), 20);
        ENTITY_XP.put(new ResourceLocation("minecraft", "warden"), 500);

        BOSS_XP.put("cavaleiro_das_cinzas", 1200);
        BOSS_XP.put("jardineiro_eterno", 1800);
        BOSS_XP.put("senhor_da_peste", 2500);
        BOSS_XP.put("guardiao_do_tempo", 3500);
        BOSS_XP.put("imperador_do_vazio", 6000);
    }

    private ExperienceRewardRegistry() {
    }

    public static void registerEntityXp(ResourceLocation entityId, int xp) {
        ENTITY_XP.put(entityId, Math.max(0, xp));
    }

    public static void registerBossXp(String bossId, int xp) {
        BOSS_XP.put(bossId, Math.max(0, xp));
    }

    public static int getEntityXp(LivingEntity entity) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id != null && ENTITY_XP.containsKey(id)) {
            return ENTITY_XP.get(id);
        }

        EntityType<?> type = entity.getType();
        if (type.getCategory() != MobCategory.MONSTER) {
            return 0;
        }

        double maxHealth = entity.getMaxHealth();
        if (maxHealth >= 80.0D) {
            return 40;
        }
        if (maxHealth >= 40.0D) {
            return 20;
        }
        return 8;
    }

    public static int getBossXp(String bossId) {
        return BOSS_XP.getOrDefault(bossId, 0);
    }

    public static String bossIdFor(LivingEntity entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId != null && ExampleMod.MODID.equals(entityId.getNamespace())
                && "cavaleiro_das_cinzas".equals(entityId.getPath())) {
            return "cavaleiro_das_cinzas";
        }
        return "";
    }
}
