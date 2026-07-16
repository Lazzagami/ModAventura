package com.ruan.medieval_fantasy.item.custom;

import com.ruan.medieval_fantasy.combat.effect.EternalFireEffect;
import com.ruan.medieval_fantasy.combat.heat.HeatManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

import java.util.List;

public class EternalFireBlade extends SwordItem {

    public EternalFireBlade() {
        super(Tiers.DIAMOND, 5, -2.4F, new Item.Properties());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (!target.level().isClientSide()) {
            EternalFireEffect.addStacks(target, 1);
            HeatManager.addHeat(attacker, 1);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Relíquia Lendária").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Aplica Incêndio Eterno sem limite.").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.literal("Cada golpe aquece o portador. Agua resfria completamente.").withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
