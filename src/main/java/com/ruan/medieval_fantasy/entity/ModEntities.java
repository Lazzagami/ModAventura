package com.ruan.medieval_fantasy.entity;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExampleMod.MODID);

    public static final RegistryObject<EntityType<CavaleiroDasCinzas>> CAVALEIRO_DAS_CINZAS =
            ENTITY_TYPES.register("cavaleiro_das_cinzas",
                    () -> EntityType.Builder.of(CavaleiroDasCinzas::new, MobCategory.MONSTER)
                            .sized(1.2F, 3.9F)
                            .build("cavaleiro_das_cinzas"));
}
