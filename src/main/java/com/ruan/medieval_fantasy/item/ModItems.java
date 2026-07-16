package com.ruan.medieval_fantasy.item;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.entity.ModEntities;
import com.ruan.medieval_fantasy.item.custom.EternalFireBlade;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ExampleMod.MODID);

    public static final RegistryObject<Item> MEDIEVAL_SWORD =
            ITEMS.register("medieval_sword",
                    () -> new SwordItem(Tiers.IRON, 3, -2.4F,
                            new Item.Properties()));

    public static final RegistryObject<Item> ETERNAL_FIRE_BLADE =
            ITEMS.register("eternal_fire_blade",
                    () -> new EternalFireBlade());

    public static final RegistryObject<Item> CAVALEIRO_DAS_CINZAS_SPAWN_EGG =
            ITEMS.register("cavaleiro_das_cinzas_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.CAVALEIRO_DAS_CINZAS, 0x17110f, 0xff5a18, new Item.Properties()));

}
