package com.ruan.medieval_fantasy.entity.custom;

import com.ruan.medieval_fantasy.combat.effect.EternalFireEffect;
import com.ruan.medieval_fantasy.combat.heat.HeatManager;
import com.ruan.medieval_fantasy.dialogue.DialogueManager;
import com.ruan.medieval_fantasy.dialogue.DialogueMemory;
import com.ruan.medieval_fantasy.item.ModItems;
import com.ruan.medieval_fantasy.progression.experience.CombatParticipationTracker;
import com.ruan.medieval_fantasy.progression.experience.PlayerExperienceManager;
import com.ruan.medieval_fantasy.scaling.BossScalingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CavaleiroDasCinzas extends Zombie implements GeoEntity {

    private enum CombatStyle {
        STUDYING,
        EXECUTOR,
        DUELIST,
        GUARDIAN
    }

    private static final Component BOSS_NAME = Component.literal("Cavaleiro das Cinzas - O Último Guardião de Eldrath");
    private static final RawAnimation IDLE_SHEATHED = RawAnimation.begin().thenLoop("animation.cavaleiro_das_cinzas.idle_sheathed");
    private static final RawAnimation IDLE_COMBAT = RawAnimation.begin().thenLoop("animation.cavaleiro_das_cinzas.idle_combat");
    private static final RawAnimation WALK_HEAVY = RawAnimation.begin().thenLoop("animation.cavaleiro_das_cinzas.walk_heavy");
    private static final RawAnimation RUN_HEAVY = RawAnimation.begin().thenLoop("animation.cavaleiro_das_cinzas.run_heavy");
    private static final RawAnimation DRAW_SWORD = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.draw_sword");
    private static final RawAnimation BASIC_SLASH = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.horizontal_slash");
    private static final RawAnimation VERTICAL_SLASH = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.vertical_slash");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.charge");
    private static final RawAnimation CIRCULAR_SLASH = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.combo");
    private static final RawAnimation FLAME_LEAP = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.flame_leap");
    private static final RawAnimation FLAME_IMPACT = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.sword_slam");
    private static final RawAnimation HURT_REACT = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.hurt");
    private static final RawAnimation STAGGER = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.stagger");
    private static final RawAnimation PHASE_TRANSITION = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.phase_transition");
    private static final RawAnimation GUARD_OBSERVE = RawAnimation.begin().thenPlayAndHold("animation.cavaleiro_das_cinzas.sword_rest");
    private static final RawAnimation PROJECTILE_BLOCK = RawAnimation.begin().thenPlay("animation.cavaleiro_das_cinzas.projectile_block");
    private static final RawAnimation DEATH_KNEEL = RawAnimation.begin().thenPlayAndHold("animation.cavaleiro_das_cinzas.death");
    private static final int CHARGE_COOLDOWN = 20 * 12;
    private static final int CIRCULAR_SLASH_COOLDOWN = 20 * 8;
    private static final int FLAME_LEAP_COOLDOWN = 20 * 15;
    public static final int ANIMATION_IDLE = 0;
    public static final int ANIMATION_BASIC_SLASH = 1;
    public static final int ANIMATION_CHARGE = 2;
    public static final int ANIMATION_CIRCULAR_SLASH = 3;
    public static final int ANIMATION_FLAME_LEAP = 4;
    public static final int ANIMATION_FLAME_IMPACT = 5;
    public static final int ANIMATION_DEATH_KNEEL = 6;
    public static final int ANIMATION_VERTICAL_SLASH = 7;
    public static final int ANIMATION_DRAW_SWORD = 8;
    public static final int ANIMATION_HURT = 9;
    public static final int ANIMATION_STAGGER = 10;
    public static final int ANIMATION_PHASE_TRANSITION = 11;
    public static final int ANIMATION_GUARD_OBSERVE = 12;
    public static final int ANIMATION_PROJECTILE_BLOCK = 13;

    private static final EntityDataAccessor<Integer> BOSS_ANIMATION =
            SynchedEntityData.defineId(CavaleiroDasCinzas.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOSS_ANIMATION_TICKS =
            SynchedEntityData.defineId(CavaleiroDasCinzas.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FIGHT_STARTED =
            SynchedEntityData.defineId(CavaleiroDasCinzas.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = new ServerBossEvent(BOSS_NAME, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private int chargeCooldown = 80;
    private int circularSlashCooldown = 120;
    private int flameLeapCooldown = 160;
    private int leapImpactTicks;
    private int chargeAttackTicks;
    private int basicAttackCooldown;
    private int combatRecoveryTicks;
    private int movementDecisionTicks;
    private int observationTicks;
    private int projectileBlockCooldown;
    private int chargeWindupTicks = 20;
    private int styleStudyTicks;
    private int passivePressureTicks;
    private int closeRangeTicks;
    private int farRangeTicks;
    private int retreatMemory;
    private int leftDodgeMemory;
    private int rightDodgeMemory;
    private int aggressionMemory;
    private int projectileMemory;
    private int repeatedPatternMemory;
    private int meleeBlockChance;
    private int meleeBlockCooldown;
    private int blockCounterAttackTicks;
    private int antiFacetankCooldown;
    private int facetankPressure;
    private int revengeAttackTicks;
    private int lastBasicAttackAnimation = ANIMATION_VERTICAL_SLASH;
    private int waterCampTicks;
    private int waterPunishCooldown;
    private int siegePressureTicks;
    private int siegeLeapCooldown;
    private int confidence = 50;
    private int feintCooldown;
    private int opportunityCooldown;
    private int tacticalPauseTicks;
    private int falseFatigueCooldown;
    private double lastTargetX;
    private double lastTargetZ;
    private boolean hasLastTargetPosition;
    private CombatStyle combatStyle = CombatStyle.STUDYING;
    private double chargeDashSpeed = 0.46D;
    private int chargeDashTicks;
    private int chargeDashTicksTotal;
    private int introTicks;
    private int deathSequenceTicks;
    private int finalPhaseHealingTicks;
    private int strafeDirection = 1;
    private boolean chargeAfterWater;
    private boolean chargeDamageDone;
    private boolean fightStarted;
    private boolean dialogueStarted;
    private boolean dialogueMode;
    private boolean deathSequenceStarted;
    private boolean relicDropped;
    private boolean finalPhaseHealingUsed;
    private double chargeDirectionX;
    private double chargeDirectionZ;
    private boolean introLine;
    private boolean firstEternalFireLine;
    private boolean phaseTwoLine;
    private boolean finalPhaseLine;
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);

    public CavaleiroDasCinzas(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
        setPersistenceRequired();
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 6, state -> {
            if (getBossAnimation() != ANIMATION_IDLE) {
                return PlayState.STOP;
            }

            double horizontalSpeed = getDeltaMovement().horizontalDistanceSqr();
            if (horizontalSpeed > 0.09D) {
                return state.setAndContinue(RUN_HEAVY);
            }
            if (horizontalSpeed > 0.0025D) {
                return state.setAndContinue(WALK_HEAVY);
            }
            return state.setAndContinue(isFightStartedClientSafe() ? IDLE_COMBAT : IDLE_SHEATHED);
        }));

        controllers.add(new AnimationController<>(this, "combat", 0, state -> {
            RawAnimation animation = switch (getBossAnimation()) {
                case ANIMATION_DRAW_SWORD -> DRAW_SWORD;
                case ANIMATION_BASIC_SLASH -> BASIC_SLASH;
                case ANIMATION_VERTICAL_SLASH -> VERTICAL_SLASH;
                case ANIMATION_CHARGE -> CHARGE;
                case ANIMATION_CIRCULAR_SLASH -> CIRCULAR_SLASH;
                case ANIMATION_FLAME_LEAP -> FLAME_LEAP;
                case ANIMATION_FLAME_IMPACT -> FLAME_IMPACT;
                case ANIMATION_HURT -> HURT_REACT;
                case ANIMATION_STAGGER -> STAGGER;
                case ANIMATION_PHASE_TRANSITION -> PHASE_TRANSITION;
                case ANIMATION_GUARD_OBSERVE -> GUARD_OBSERVE;
                case ANIMATION_PROJECTILE_BLOCK -> PROJECTILE_BLOCK;
                case ANIMATION_DEATH_KNEEL -> DEATH_KNEEL;
                default -> null;
            };

            if (animation == null) {
                return PlayState.STOP;
            }
            return state.setAndContinue(animation);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geckoCache;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BOSS_ANIMATION, ANIMATION_IDLE);
        entityData.define(BOSS_ANIMATION_TICKS, 0);
        entityData.define(FIGHT_STARTED, false);
    }

    public int getBossAnimation() {
        return entityData.get(BOSS_ANIMATION);
    }

    public int getBossAnimationTicks() {
        return entityData.get(BOSS_ANIMATION_TICKS);
    }

    private boolean isFightStartedClientSafe() {
        return entityData.get(FIGHT_STARTED);
    }

    public boolean canStartDialogueClient() {
        return !isFightStartedClientSafe() && getBossAnimation() != ANIMATION_DEATH_KNEEL;
    }

    private void setFightStarted(boolean started) {
        fightStarted = started;
        entityData.set(FIGHT_STARTED, started);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.11D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        SpawnGroupData spawnGroupData, CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);

        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ETERNAL_FIRE_BLADE.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        setCanPickUpLoot(false);
        setHealth(getMaxHealth());

        return data;
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && deathSequenceStarted) {
            tickAnimationTimer();
            tickDeathSequence();
            return;
        }

        super.tick();

        if (level().isClientSide()) {
            return;
        }

        tickAnimationTimer();
        ensureRelicEquipped();
        bossEvent.setProgress(getHealth() / getMaxHealth());
        spawnAtmosphere();

        LivingEntity target = getTarget();
        if (!fightStarted) {
            if (target != null) {
                setTarget(null);
                setLastHurtByMob(null);
            }
            tickPassiveDialogue();
            return;
        }

        updatePhase();
        tickFinalPhaseHealing();
        tickActionSounds();
        if (tickProjectileBlock(target)) {
            tickArenaPressure();
            spawnCombatParticles();
            return;
        }

        tickCombatMemory(target);
        tickCommanderMovement();
        tickAbilities();
        tickArenaPressure();
        clearBlockingBlocks();
        spawnCombatParticles();
    }

    private void tickPassiveDialogue() {
        getNavigation().stop();
        setTarget(null);
        setLastHurtByMob(null);
        setDeltaMovement(getDeltaMovement().multiply(0.10D, 1.0D, 0.10D));
        if (tickCount % 55 == 0) {
            playBreathSound();
        }
        if (dialogueMode && getBossAnimation() == ANIMATION_IDLE && tickCount % 80 == 0) {
            startBossAnimation(ANIMATION_GUARD_OBSERVE, 28);
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!(entity instanceof LivingEntity living) || deathSequenceStarted) {
            return false;
        }

        confidence = clampConfidence(confidence + 2);
        repeatedPatternMemory = Math.min(120, repeatedPatternMemory + 1);
        performBasicSwordAttack(living);
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (deathSequenceStarted) {
            return false;
        }

        LivingEntity responsibleAttacker = getResponsibleLivingAttacker(source);
        Entity directAttacker = source.getDirectEntity();
        if (shouldPerfectGuardIncomingDamage(responsibleAttacker, directAttacker)) {
            guardIncomingAttack(responsibleAttacker, directAttacker, true);
            return false;
        }

        if (EternalFireEffect.isFireDamage(source)) {
            return false;
        }

        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= 0.8F;
        }

        if (tryBlockMeleeHit(source)) {
            return false;
        }

        if (finalPhaseHealingTicks > 0) {
            amount *= 0.55F;
            if (source.getEntity() instanceof LivingEntity attacker) {
                pushAway(attacker, 0.25D);
            }
        }

        if (amount >= getHealth()) {
            startDeathSequence(source);
            return false;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof Player) {
            aggressionMemory = Math.min(200, aggressionMemory + 8);
            confidence = clampConfidence(confidence - (amount >= 8.0F ? 5 : 2));
            if (source.getDirectEntity() instanceof Projectile) {
                projectileMemory = Math.min(160, projectileMemory + 10);
            } else {
                registerFacetankPressure(source.getEntity(), amount);
            }
        }
        if (hurt && getBossAnimation() == ANIMATION_IDLE && fightStarted && facetankPressure < 3) {
            startBossAnimation(amount >= 8.0F ? ANIMATION_STAGGER : ANIMATION_HURT, amount >= 8.0F ? 16 : 9);
        }
        return hurt;
    }

    private LivingEntity getResponsibleLivingAttacker(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity living) {
            return living;
        }

        if (source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity living) {
            return living;
        }

        return null;
    }

    private boolean shouldPerfectGuardIncomingDamage(LivingEntity attacker, Entity directAttacker) {
        if (attacker == null || attacker == this || deathSequenceStarted) {
            return false;
        }

        boolean projectile = directAttacker instanceof Projectile;
        boolean playerDriven = attacker instanceof Player;
        if (!playerDriven) {
            return false;
        }

        if (!fightStarted) {
            return true;
        }

        if (!projectile) {
            return false;
        }

        return isDistantOrHighThreat(attacker) || siegePressureTicks > 20;
    }

    private void guardIncomingAttack(LivingEntity attacker, Entity directAttacker, boolean perfectGuard) {
        if (attacker != null) {
            setTarget(attacker);
            setLastHurtByMob(attacker);
            getNavigation().stop();
            getLookControl().setLookAt(attacker, 45.0F, 45.0F);
            lookAt(attacker, 45.0F, 45.0F);
        }

        if (!fightStarted) {
            introTicks = Math.max(introTicks, 1);
        }

        projectileMemory = Math.min(200, projectileMemory + (perfectGuard ? 14 : 6));
        siegePressureTicks = Math.min(20 * 16, siegePressureTicks + (perfectGuard ? 18 : 8));
        combatStyle = CombatStyle.GUARDIAN;
        confidence = clampConfidence(confidence + 4);
        projectileBlockCooldown = Math.max(projectileBlockCooldown, perfectGuard ? 8 : 5);
        startBossAnimation(ANIMATION_PROJECTILE_BLOCK, 19);
        playSound(SoundEvents.SHIELD_BLOCK, 1.0F, perfectGuard ? 0.42F : 0.55F);
        playSound(SoundEvents.ANVIL_HIT, perfectGuard ? 0.55F : 0.35F, 0.66F);
        spawnGuardSparks();

        if (directAttacker instanceof Projectile projectile && projectile.isAlive()) {
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, projectile.getX(), projectile.getY(), projectile.getZ(), 10, 0.18D, 0.18D, 0.18D, 0.10D);
            }
            projectile.discard();
        }
    }

    private boolean isDistantOrHighThreat(LivingEntity attacker) {
        double horizontalDistanceSqr = new Vec3(attacker.getX() - getX(), 0.0D, attacker.getZ() - getZ()).lengthSqr();
        double heightDifference = attacker.getY() - getY();
        return horizontalDistanceSqr > 100.0D || heightDifference > 4.5D || (heightDifference > 2.5D && horizontalDistanceSqr > 36.0D);
    }

    @Override
    public void die(DamageSource source) {
        startDeathSequence(source);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.NETHERITE_BLOCK_STEP;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.NETHERITE_BLOCK_STEP, 0.85F, 0.75F + random.nextFloat() * 0.1F);
        playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 0.18F, 0.65F + random.nextFloat() * 0.1F);
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (isFinalPhase()) {
            return;
        }

        super.knockback(strength, x, z);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("intro_line", introLine);
        tag.putBoolean("fight_started", fightStarted);
        tag.putBoolean("dialogue_started", dialogueStarted);
        tag.putBoolean("dialogue_mode", dialogueMode);
        tag.putBoolean("first_eternal_fire_line", firstEternalFireLine);
        tag.putBoolean("phase_two_line", phaseTwoLine);
        tag.putBoolean("final_phase_line", finalPhaseLine);
        tag.putBoolean("death_sequence_started", deathSequenceStarted);
        tag.putBoolean("relic_dropped", relicDropped);
        tag.putBoolean("final_phase_healing_used", finalPhaseHealingUsed);
        tag.putInt("intro_ticks", introTicks);
        tag.putInt("death_sequence_ticks", deathSequenceTicks);
        tag.putInt("final_phase_healing_ticks", finalPhaseHealingTicks);
        tag.putInt("charge_cooldown", chargeCooldown);
        tag.putInt("circular_slash_cooldown", circularSlashCooldown);
        tag.putInt("flame_leap_cooldown", flameLeapCooldown);
        tag.putString("combat_style", combatStyle.name());
        tag.putInt("confidence", confidence);
        tag.putInt("style_study_ticks", styleStudyTicks);
        tag.putInt("retreat_memory", retreatMemory);
        tag.putInt("left_dodge_memory", leftDodgeMemory);
        tag.putInt("right_dodge_memory", rightDodgeMemory);
        tag.putInt("aggression_memory", aggressionMemory);
        tag.putInt("projectile_memory", projectileMemory);
        tag.putInt("repeated_pattern_memory", repeatedPatternMemory);
        tag.putInt("melee_block_chance", meleeBlockChance);
        tag.putInt("melee_block_cooldown", meleeBlockCooldown);
        tag.putInt("block_counter_attack_ticks", blockCounterAttackTicks);
        tag.putInt("anti_facetank_cooldown", antiFacetankCooldown);
        tag.putInt("facetank_pressure", facetankPressure);
        tag.putInt("revenge_attack_ticks", revengeAttackTicks);
        tag.putInt("last_basic_attack_animation", lastBasicAttackAnimation);
        tag.putInt("water_camp_ticks", waterCampTicks);
        tag.putInt("water_punish_cooldown", waterPunishCooldown);
        tag.putInt("siege_pressure_ticks", siegePressureTicks);
        tag.putInt("siege_leap_cooldown", siegeLeapCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        introLine = tag.getBoolean("intro_line");
        setFightStarted(tag.getBoolean("fight_started"));
        dialogueStarted = tag.getBoolean("dialogue_started");
        dialogueMode = tag.getBoolean("dialogue_mode");
        firstEternalFireLine = tag.getBoolean("first_eternal_fire_line");
        phaseTwoLine = tag.getBoolean("phase_two_line");
        finalPhaseLine = tag.getBoolean("final_phase_line");
        deathSequenceStarted = tag.getBoolean("death_sequence_started");
        relicDropped = tag.getBoolean("relic_dropped");
        finalPhaseHealingUsed = tag.getBoolean("final_phase_healing_used");
        introTicks = tag.getInt("intro_ticks");
        deathSequenceTicks = tag.getInt("death_sequence_ticks");
        finalPhaseHealingTicks = tag.getInt("final_phase_healing_ticks");
        chargeCooldown = tag.getInt("charge_cooldown");
        circularSlashCooldown = tag.getInt("circular_slash_cooldown");
        flameLeapCooldown = tag.getInt("flame_leap_cooldown");
        confidence = tag.contains("confidence") ? tag.getInt("confidence") : 50;
        styleStudyTicks = tag.getInt("style_study_ticks");
        retreatMemory = tag.getInt("retreat_memory");
        leftDodgeMemory = tag.getInt("left_dodge_memory");
        rightDodgeMemory = tag.getInt("right_dodge_memory");
        aggressionMemory = tag.getInt("aggression_memory");
        projectileMemory = tag.getInt("projectile_memory");
        repeatedPatternMemory = tag.getInt("repeated_pattern_memory");
        meleeBlockChance = tag.getInt("melee_block_chance");
        meleeBlockCooldown = tag.getInt("melee_block_cooldown");
        blockCounterAttackTicks = tag.getInt("block_counter_attack_ticks");
        antiFacetankCooldown = tag.getInt("anti_facetank_cooldown");
        facetankPressure = tag.getInt("facetank_pressure");
        revengeAttackTicks = tag.getInt("revenge_attack_ticks");
        lastBasicAttackAnimation = tag.contains("last_basic_attack_animation")
                ? tag.getInt("last_basic_attack_animation")
                : ANIMATION_VERTICAL_SLASH;
        waterCampTicks = tag.getInt("water_camp_ticks");
        waterPunishCooldown = tag.getInt("water_punish_cooldown");
        siegePressureTicks = tag.getInt("siege_pressure_ticks");
        siegeLeapCooldown = tag.getInt("siege_leap_cooldown");
        if (tag.contains("combat_style")) {
            try {
                combatStyle = CombatStyle.valueOf(tag.getString("combat_style"));
            } catch (IllegalArgumentException ignored) {
                combatStyle = CombatStyle.STUDYING;
            }
        }

        if (hasCustomName()) {
            bossEvent.setName(getDisplayName());
        }

        if (deathSequenceStarted) {
            setFightStarted(false);
            setTarget(null);
            setNoAi(true);
            setInvulnerable(true);
            finalPhaseHealingTicks = 0;
            bossEvent.setProgress(0.0F);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        if (recentlyHit || source.getEntity() instanceof Player) {
            spawnAtLocation(new ItemStack(ModItems.ETERNAL_FIRE_BLADE.get()));
        }
    }

    @Override
    public boolean shouldDropExperience() {
        return true;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || deathSequenceStarted) {
            return super.mobInteract(player, hand);
        }

        if (fightStarted || isFightStartedClientSafe()) {
            return super.mobInteract(player, hand);
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            startIntroDialogue(serverPlayer);
        }
        return InteractionResult.CONSUME;
    }

    private void startIntroDialogue(ServerPlayer player) {
        if (fightStarted || deathSequenceStarted || dialogueMode) {
            return;
        }

        dialogueStarted = true;
        introLine = true;
        introTicks = Math.max(introTicks, 40);
        setTarget(null);
        setLastHurtByMob(null);
        getNavigation().stop();
        getLookControl().setLookAt(player, 40.0F, 40.0F);
        lookAt(player, 40.0F, 40.0F);

        String startNode = DialogueMemory.getBoolean(player, "ash_knight_defeated") ? "skip_prompt" : "start";
        DialogueManager.startDialogue(player, this, "ash_knight_intro", startNode);
    }

    private void tickEntrance(LivingEntity target) {
        introTicks++;
        getNavigation().stop();
        setDeltaMovement(getDeltaMovement().multiply(0.15D, 1.0D, 0.15D));
        getLookControl().setLookAt(target, 40.0F, 40.0F);
        lookAt(target, 40.0F, 40.0F);

        if (introTicks % 8 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.15D, getZ(), 6, 0.7D, 0.6D, 0.7D, 0.005D);
        }

        if (introTicks == 20) {
            playBreathSound();
        }

        if (dialogueMode) {
            if (introTicks % 30 == 0) {
                startBossAnimation(ANIMATION_GUARD_OBSERVE, 26);
            }
            return;
        }

        if (dialogueStarted) {
            return;
        }

        if (introTicks >= 40 && target instanceof ServerPlayer player) {
            dialogueStarted = true;
            introLine = true;
            String startNode = DialogueMemory.getBoolean(player, "ash_knight_defeated") ? "skip_prompt" : "start";
            DialogueManager.startDialogue(player, this, "ash_knight_intro", startNode);
        }
    }

    private void tickActionSounds() {
        if (tickCount % 92 == 0) {
            playBreathSound();
        }

        if (getDeltaMovement().horizontalDistanceSqr() > 0.01D && tickCount % 18 == 0) {
            playSound(SoundEvents.CHAIN_STEP, 0.28F, 0.55F + random.nextFloat() * 0.08F);
            playSound(SoundEvents.FLINTANDSTEEL_USE, 0.12F, 0.65F + random.nextFloat() * 0.12F);
        }
    }

    private void playBreathSound() {
        playSound(SoundEvents.ANVIL_LAND, 0.16F, 0.38F);
        playSound(SoundEvents.FIRE_AMBIENT, 0.22F, 0.62F + random.nextFloat() * 0.08F);
    }

    private void playSwordDrawSound() {
        playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 0.95F, 0.42F);
        playSound(SoundEvents.FIRECHARGE_USE, 0.5F, 0.55F);
        playSound(SoundEvents.CHAIN_HIT, 0.35F, 0.55F);
    }

    private void spawnAtmosphere() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int ashCount = isFinalPhase() ? 12 : isPhaseTwo() ? 8 : 5;
        if (tickCount % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.ASH, getX(), getY() + 2.2D, getZ(), ashCount, 5.0D, 1.3D, 5.0D, 0.01D);
        }

        if (tickCount % 9 == 0) {
            int emberCount = isFinalPhase() ? 7 : isPhaseTwo() ? 4 : 2;
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, getX(), getY() + 1.1D, getZ(), emberCount, 2.5D, 0.8D, 2.5D, 0.015D);
        }

        if (tickCount % 16 == 0) {
            int smokeCount = isFinalPhase() ? 8 : isPhaseTwo() ? 5 : 3;
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.2D, getZ(), smokeCount, 3.2D, 0.2D, 3.2D, 0.01D);
        }
    }

    private void spawnCombatParticles() {
        if (!(level() instanceof ServerLevel serverLevel) || tickCount % 8 != 0) {
            return;
        }

        int count = isFinalPhase() ? 10 : isPhaseTwo() ? 6 : 3;
        serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.1D, getZ(), count, 1.4D, 0.9D, 1.4D, 0.01D);

        if (getDeltaMovement().horizontalDistanceSqr() > 0.012D) {
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.45D, getZ(), isFinalPhase() ? 5 : 3, 0.5D, 0.25D, 0.5D, 0.01D);
        }
    }

    private void startDeathSequence(DamageSource source) {
        if (deathSequenceStarted) {
            return;
        }

        deathSequenceStarted = true;
        setFightStarted(false);
        deathSequenceTicks = 0;
        finalPhaseHealingTicks = 0;
        chargeAttackTicks = 0;
        leapImpactTicks = 0;
        blockCounterAttackTicks = 0;
        revengeAttackTicks = 0;
        basicAttackCooldown = 9999;
        combatRecoveryTicks = 9999;
        setTarget(null);
        setLastHurtByMob(null);
        removeEffect(MobEffects.REGENERATION);
        removeEffect(MobEffects.DAMAGE_RESISTANCE);
        extinguishFire();
        setHealth(1.0F);
        setInvulnerable(true);
        setNoAi(true);
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        bossEvent.setProgress(0.0F);
        startBossAnimation(ANIMATION_DEATH_KNEEL, 105);
        sayToNearbyPlayers("Finalmente... alguem suportou o peso desta espada...");
        playSound(SoundEvents.IRON_GOLEM_DEATH, 0.8F, 0.45F);
        playSound(SoundEvents.FIRE_EXTINGUISH, 0.7F, 0.55F);
        if (source.getEntity() instanceof ServerPlayer player) {
            CombatParticipationTracker.recordDamage(this, player);
        }
        for (ServerPlayer player : CombatParticipationTracker.getParticipants(this)) {
            DialogueMemory.setBoolean(player, "ash_knight_defeated", true);
            DialogueMemory.setBoolean(player, "ash_knight_intro_seen", true);
        }
        PlayerExperienceManager.rewardBossKill(this, CombatParticipationTracker.getParticipants(this), "cavaleiro_das_cinzas");
    }

    public void setDialogueMode(boolean dialogueMode) {
        this.dialogueMode = dialogueMode;
        if (dialogueMode) {
            setTarget(null);
            setLastHurtByMob(null);
            getNavigation().stop();
            combatRecoveryTicks = 9999;
            basicAttackCooldown = 9999;
            chargeAttackTicks = 0;
            leapImpactTicks = 0;
        } else if (!fightStarted) {
            combatRecoveryTicks = 0;
            basicAttackCooldown = 0;
        }
    }

    public void beginCombatAfterDialogue(ServerPlayer player) {
        dialogueMode = false;
        dialogueStarted = true;
        BossScalingManager.applyOnce(this, 350.0D, 16.0D);
        setFightStarted(true);
        combatRecoveryTicks = 10;
        basicAttackCooldown = 12;
        if (player != null && player.isAlive()) {
            setTarget(player);
            setLastHurtByMob(player);
            getLookControl().setLookAt(player, 45.0F, 45.0F);
            lookAt(player, 45.0F, 45.0F);
        }
        if (getBossAnimation() == ANIMATION_IDLE) {
            playSwordDrawSound();
            startBossAnimation(ANIMATION_DRAW_SWORD, 42);
        }
    }

    public void playDialogueAnimation(String animation, int durationTicks) {
        int duration = durationTicks <= 0 ? 35 : durationTicks;
        int animationId = switch (animation == null ? "" : animation) {
            case "draw_sword" -> ANIMATION_DRAW_SWORD;
            case "turn_to_player", "sword_rest", "look_player" -> ANIMATION_GUARD_OBSERVE;
            case "phase_transition" -> ANIMATION_PHASE_TRANSITION;
            case "death_kneel" -> ANIMATION_DEATH_KNEEL;
            default -> ANIMATION_GUARD_OBSERVE;
        };

        if (animationId == ANIMATION_DRAW_SWORD) {
            playSwordDrawSound();
        }
        startBossAnimation(animationId, duration);
    }

    private void tickDeathSequence() {
        deathSequenceTicks++;
        setTarget(null);
        setLastHurtByMob(null);
        setHealth(1.0F);
        setNoAi(true);
        setInvulnerable(true);
        finalPhaseHealingTicks = 0;
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;

        if (level() instanceof ServerLevel serverLevel) {
            if (deathSequenceTicks % 5 == 0) {
                serverLevel.sendParticles(ParticleTypes.ASH, getX(), getY() + 1.0D, getZ(), 18, 0.8D, 0.9D, 0.8D, 0.025D);
                serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.8D, getZ(), 8, 0.7D, 0.6D, 0.7D, 0.015D);
            }

            if (deathSequenceTicks == 38) {
                playSound(SoundEvents.ITEM_BREAK, 0.55F, 0.45F);
                playSound(SoundEvents.CHAIN_BREAK, 0.45F, 0.55F);
            }

            if (deathSequenceTicks == 70 && !relicDropped) {
                relicDropped = true;
                spawnAtLocation(new ItemStack(ModItems.ETERNAL_FIRE_BLADE.get()));
                playSound(SoundEvents.NETHERITE_BLOCK_PLACE, 0.75F, 0.45F);
            }

            if (deathSequenceTicks >= 105) {
                serverLevel.sendParticles(ParticleTypes.ASH, getX(), getY() + 1.0D, getZ(), 45, 1.0D, 1.2D, 1.0D, 0.05D);
                bossEvent.removeAllPlayers();
                discard();
            }
        }
    }

    private void tickCommanderMovement() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || chargeAttackTicks > 0 || leapImpactTicks > 0) {
            return;
        }

        if (projectileBlockCooldown > 0) {
            projectileBlockCooldown--;
        }

        if (meleeBlockCooldown > 0) {
            meleeBlockCooldown--;
        }

        if (antiFacetankCooldown > 0) {
            antiFacetankCooldown--;
        }

        if (revengeAttackTicks > 0) {
            revengeAttackTicks--;
            if (revengeAttackTicks == 0 && target.distanceToSqr(this) <= 25.0D) {
                if (isCommittedToAction()) {
                    revengeAttackTicks = 2;
                } else {
                    performRevengeAttack(target);
                    return;
                }
            }
        }

        if (tickCount % 20 == 0) {
            facetankPressure = Math.max(0, facetankPressure - 1);
        }

        if (waterPunishCooldown > 0) {
            waterPunishCooldown--;
        }

        if (siegeLeapCooldown > 0) {
            siegeLeapCooldown--;
        }

        if (blockCounterAttackTicks > 0) {
            blockCounterAttackTicks--;
            if (blockCounterAttackTicks == 0 && target.distanceToSqr(this) <= 18.0D && combatRecoveryTicks <= 0) {
                performCounterAttack(target);
                return;
            }
        }

        if (basicAttackCooldown > 0) {
            basicAttackCooldown--;
        }

        if (combatRecoveryTicks > 0) {
            combatRecoveryTicks--;
        }

        if (feintCooldown > 0) {
            feintCooldown--;
        }

        if (opportunityCooldown > 0) {
            opportunityCooldown--;
        }

        if (falseFatigueCooldown > 0) {
            falseFatigueCooldown--;
        }

        if (tacticalPauseTicks > 0) {
            tacticalPauseTicks--;
            getNavigation().stop();
            getLookControl().setLookAt(target, 45.0F, 45.0F);
            lookAt(target, 45.0F, 45.0F);
            setDeltaMovement(getDeltaMovement().multiply(0.10D, 1.0D, 0.10D));
            if (tacticalPauseTicks == 1 && distanceToSqr(target) <= 20.25D) {
                performBasicSwordAttack(target);
            }
            return;
        }

        getLookControl().setLookAt(target, 35.0F, 35.0F);
        lookAt(target, 35.0F, 35.0F);

        if (movementDecisionTicks-- <= 0) {
            movementDecisionTicks = 26 + random.nextInt(22);
            strafeDirection = random.nextBoolean() ? 1 : -1;
        }

        if (shouldDodgeProjectile()) {
            movementDecisionTicks = 18;
            strafeDirection *= -1;
        }

        if (target.isInWater() && !isInWater()) {
            chargeAfterWater = true;
            waterCampTicks++;
            circleWaterEdge(target);
            if (tryPunishWaterCamping(target)) {
                return;
            }
            return;
        }
        waterCampTicks = Math.max(0, waterCampTicks - 3);

        if (chargeAfterWater && chargeCooldown <= 0) {
            chargeAfterWater = false;
            chargeAt(target);
            return;
        }

        double distance = distanceTo(target);
        if (trySiegeApproach(target, distance)) {
            return;
        }

        if (tryObserveAndGuardAtRange(target, distance)) {
            return;
        }

        if (tryOpportunityPunish(target, distance)) {
            return;
        }

        if (tryFalseFatigue(target, distance)) {
            return;
        }

        if (tryAntiFacetankStomp(target, distance)) {
            return;
        }

        double attackReach = getAdaptiveAttackReach();
        if (distance <= attackReach && basicAttackCooldown <= 0 && combatRecoveryTicks <= 0) {
            performBasicSwordAttack(target);
            return;
        }

        Vec3 toTarget = target.position().subtract(position());
        Vec3 forward = horizontalOrLook(toTarget);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x).scale(strafeDirection);

        double idealMin = combatStyle == CombatStyle.DUELIST ? 2.75D : 3.0D;
        double idealMax = combatStyle == CombatStyle.EXECUTOR ? 3.35D : isFinalPhase() ? 3.55D : 4.0D;
        double forwardPressure = getForwardPressure();
        double sidePressure = combatStyle == CombatStyle.GUARDIAN ? 0.15D : isFinalPhase() ? 0.14D : isPhaseTwo() ? 0.125D : 0.11D;
        double speed = getAdaptiveMovementSpeed();

        Vec3 desired = side.scale(sidePressure);
        if (distance > idealMax) {
            desired = desired.add(forward.scale(speed));
        } else if (distance < idealMin) {
            desired = desired.add(forward.scale(-0.16D));
        } else {
            desired = desired.add(forward.scale(forwardPressure));
        }

        if (!getSensing().hasLineOfSight(target)) {
            desired = desired.add(forward.scale(0.16D));
            if (tickCount % 10 == 0) {
                getNavigation().moveTo(target, 1.15D);
            }
        } else {
            getNavigation().stop();
        }

        if (combatRecoveryTicks > 0) {
            desired = desired.scale(0.45D);
        }

        applyCommanderMovement(desired);
    }

    private void tickCombatMemory(LivingEntity target) {
        if (target == null || !target.isAlive() || !fightStarted) {
            return;
        }

        styleStudyTicks++;

        if (chargeCooldown > 0) {
            chargeCooldown--;
        }
        if (circularSlashCooldown > 0) {
            circularSlashCooldown--;
        }
        if (flameLeapCooldown > 0) {
            flameLeapCooldown--;
        }

        double distance = distanceTo(target);
        updateSiegePressure(target, distance);

        if (distance < 2.6D) {
            closeRangeTicks++;
            farRangeTicks = Math.max(0, farRangeTicks - 2);
            confidence = clampConfidence(confidence - 1);
        } else if (distance > 9.5D) {
            farRangeTicks++;
            closeRangeTicks = Math.max(0, closeRangeTicks - 1);
        } else {
            closeRangeTicks = Math.max(0, closeRangeTicks - 1);
            farRangeTicks = Math.max(0, farRangeTicks - 1);
        }

        if (hasLastTargetPosition) {
            Vec3 movement = new Vec3(target.getX() - lastTargetX, 0.0D, target.getZ() - lastTargetZ);
            if (movement.lengthSqr() > 0.003D) {
                Vec3 awayFromBoss = target.position().subtract(position());
                awayFromBoss = new Vec3(awayFromBoss.x, 0.0D, awayFromBoss.z);
                if (awayFromBoss.lengthSqr() > 0.0001D) {
                    awayFromBoss = awayFromBoss.normalize();
                    Vec3 side = new Vec3(-awayFromBoss.z, 0.0D, awayFromBoss.x);
                    double retreat = movement.normalize().dot(awayFromBoss);
                    double lateral = movement.normalize().dot(side);
                    if (retreat > 0.55D) {
                        retreatMemory = Math.min(240, retreatMemory + 3);
                    } else {
                        retreatMemory = Math.max(0, retreatMemory - 1);
                    }

                    if (lateral > 0.45D) {
                        rightDodgeMemory = Math.min(160, rightDodgeMemory + 2);
                        leftDodgeMemory = Math.max(0, leftDodgeMemory - 1);
                    } else if (lateral < -0.45D) {
                        leftDodgeMemory = Math.min(160, leftDodgeMemory + 2);
                        rightDodgeMemory = Math.max(0, rightDodgeMemory - 1);
                    }
                }
            }
        }

        if (target instanceof Player player && player.isUsingItem()) {
            passivePressureTicks += 8;
        } else if (distance > 7.0D && getSensing().hasLineOfSight(target)) {
            passivePressureTicks++;
        } else {
            passivePressureTicks = Math.max(0, passivePressureTicks - 2);
        }

        retreatMemory = Math.max(0, retreatMemory - (tickCount % 20 == 0 ? 1 : 0));
        projectileMemory = Math.max(0, projectileMemory - (tickCount % 30 == 0 ? 1 : 0));
        repeatedPatternMemory = Math.max(0, repeatedPatternMemory - (tickCount % 40 == 0 ? 1 : 0));

        if (styleStudyTicks == 20 * 22 || (styleStudyTicks > 20 * 22 && styleStudyTicks % (20 * 14) == 0)) {
            chooseCombatStyle();
        }

        lastTargetX = target.getX();
        lastTargetZ = target.getZ();
        hasLastTargetPosition = true;
    }

    private void updateSiegePressure(LivingEntity target, double distance) {
        double heightDifference = target.getY() - getY();
        boolean highGround = heightDifference > 4.5D;
        boolean farAndThreatening = distance > 11.0D && (projectileMemory > 8 || passivePressureTicks > 20 * 6);
        boolean lineBlocked = !getSensing().hasLineOfSight(target);

        if (highGround || farAndThreatening || (lineBlocked && distance > 7.0D)) {
            siegePressureTicks = Math.min(20 * 18, siegePressureTicks + (highGround ? 4 : 2));
            combatStyle = CombatStyle.GUARDIAN;
        } else if (distance < 5.0D && heightDifference < 2.5D) {
            siegePressureTicks = Math.max(0, siegePressureTicks - 5);
        } else {
            siegePressureTicks = Math.max(0, siegePressureTicks - 1);
        }
    }

    private void chooseCombatStyle() {
        CombatStyle previous = combatStyle;
        if (retreatMemory + projectileMemory + passivePressureTicks / 6 > aggressionMemory + closeRangeTicks / 4 + 30) {
            combatStyle = CombatStyle.EXECUTOR;
        } else if (aggressionMemory + closeRangeTicks / 3 > retreatMemory + 24) {
            combatStyle = CombatStyle.DUELIST;
        } else if (projectileMemory + repeatedPatternMemory + Math.max(leftDodgeMemory, rightDodgeMemory) > 36) {
            combatStyle = CombatStyle.GUARDIAN;
        } else {
            combatStyle = CombatStyle.STUDYING;
        }

        if (previous != combatStyle && fightStarted && getBossAnimation() == ANIMATION_IDLE) {
            startBossAnimation(ANIMATION_GUARD_OBSERVE, 24);
            playBreathSound();
        }
    }

    private boolean tryOpportunityPunish(LivingEntity target, double distance) {
        if (!(target instanceof Player player) || opportunityCooldown > 0 || combatRecoveryTicks > 0 || observationTicks > 0) {
            return false;
        }

        if (!player.isUsingItem()) {
            return false;
        }

        opportunityCooldown = 80;
        confidence = clampConfidence(confidence + 4);
        if (distance > 4.5D && chargeCooldown <= 60) {
            chargeAt(target);
            chargeCooldown = Math.max(80, CHARGE_COOLDOWN - (isPhaseTwo() ? 50 : 20));
        } else if (distance <= 4.5D) {
            performBasicSwordAttack(target);
        } else if (flameLeapCooldown <= 80) {
            flameLeap(target);
            flameLeapCooldown = Math.max(120, FLAME_LEAP_COOLDOWN - (isPhaseTwo() ? 45 : 15));
        }
        return true;
    }

    private boolean tryFalseFatigue(LivingEntity target, double distance) {
        // Em modelo blocado, essa leitura parecia falha de animação em vez de blefe.
        // Mantido desativado até existir uma animação dedicada bem teatral.
        if (areSubtleFeintsDisabled()) {
            return false;
        }

        if (falseFatigueCooldown > 0 || tacticalPauseTicks > 0 || combatStyle == CombatStyle.EXECUTOR || confidence < 62) {
            return false;
        }

        if (distance < 3.0D || distance > 5.2D || random.nextInt(180) != 0) {
            return false;
        }

        falseFatigueCooldown = 20 * 18;
        tacticalPauseTicks = 18 + random.nextInt(10);
        startBossAnimation(ANIMATION_GUARD_OBSERVE, tacticalPauseTicks + 8);
        playBreathSound();
        return true;
    }

    private double getAdaptiveAttackReach() {
        if (closeRangeTicks > 30) {
            return 2.85D;
        }
        if (combatStyle == CombatStyle.DUELIST) {
            return 3.10D;
        }
        return 3.25D;
    }

    private double getForwardPressure() {
        double pressure = isFinalPhase() ? 0.08D : isPhaseTwo() ? 0.06D : 0.045D;
        if (combatStyle == CombatStyle.EXECUTOR) {
            pressure += 0.05D;
        } else if (combatStyle == CombatStyle.GUARDIAN) {
            pressure -= 0.015D;
        }

        if (passivePressureTicks > 20 * 30) {
            pressure += 0.08D;
        }
        if (confidence > 70) {
            pressure += 0.025D;
        } else if (confidence < 35) {
            pressure -= 0.025D;
        }
        return Math.max(0.015D, pressure);
    }

    private double getAdaptiveMovementSpeed() {
        double speed = isFinalPhase() ? 0.29D : isPhaseTwo() ? 0.245D : 0.205D;
        if (combatStyle == CombatStyle.EXECUTOR) {
            speed += 0.045D;
        } else if (combatStyle == CombatStyle.GUARDIAN) {
            speed -= 0.015D;
        }
        if (passivePressureTicks > 20 * 30) {
            speed += 0.045D;
        }
        return Math.max(0.16D, speed);
    }

    private int clampConfidence(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private boolean tryObserveAndGuardAtRange(LivingEntity target, double distance) {
        boolean hasLineOfSight = getSensing().hasLineOfSight(target);
        boolean veryFar = distance >= (isFinalPhase() ? 13.0D : isPhaseTwo() ? 14.0D : 15.0D);
        if (!hasLineOfSight || !veryFar || target.isInWater() || combatRecoveryTicks > 0) {
            observationTicks = 0;
            return false;
        }

        observationTicks++;
        getNavigation().stop();
        getLookControl().setLookAt(target, 45.0F, 45.0F);
        lookAt(target, 45.0F, 45.0F);
        setDeltaMovement(getDeltaMovement().multiply(0.08D, 1.0D, 0.08D));

        if (getBossAnimation() == ANIMATION_IDLE || getBossAnimation() == ANIMATION_HURT) {
            startBossAnimation(ANIMATION_GUARD_OBSERVE, 26);
        }

        if (level() instanceof ServerLevel serverLevel && observationTicks % 8 == 0) {
            serverLevel.sendParticles(ParticleTypes.ASH, getX(), getY() + 1.5D, getZ(), 5, 0.45D, 0.45D, 0.45D, 0.01D);
        }

        if (chargeCooldown <= 0 && observationTicks >= 24) {
            observationTicks = 0;
            chargeAt(target);
            chargeCooldown = Math.max(80, CHARGE_COOLDOWN - (isPhaseTwo() ? 45 : 0));
        }

        return true;
    }

    private boolean trySiegeApproach(LivingEntity target, double distance) {
        if (!fightStarted || deathSequenceStarted || target.isInWater() || chargeAttackTicks > 0 || leapImpactTicks > 0) {
            return false;
        }

        double heightDifference = target.getY() - getY();
        boolean highGround = heightDifference > 4.5D;
        boolean farArcher = distance > 11.0D && projectileMemory > 10;
        boolean lineBlocked = !getSensing().hasLineOfSight(target) && distance > 7.0D;
        if (!highGround && !farArcher && !lineBlocked && siegePressureTicks < 20 * 5) {
            return false;
        }

        getNavigation().stop();
        getLookControl().setLookAt(target, 45.0F, 45.0F);
        lookAt(target, 45.0F, 45.0F);

        if (level() instanceof ServerLevel serverLevel && tickCount % 6 == 0) {
            serverLevel.sendParticles(ParticleTypes.ASH, getX(), getY() + 1.4D, getZ(), 8, 0.7D, 0.5D, 0.7D, 0.018D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.6D, getZ(), 5, 0.45D, 0.25D, 0.45D, 0.012D);
        }

        if (getBossAnimation() == ANIMATION_IDLE && tickCount % 18 == 0) {
            startBossAnimation(ANIMATION_GUARD_OBSERVE, 24);
            playSound(SoundEvents.SHIELD_BLOCK, 0.45F, 0.52F);
        }

        Vec3 toTarget = target.position().subtract(position());
        Vec3 forward = horizontalOrLook(toTarget);
        applyCommanderMovement(forward.scale(highGround ? 0.11D : 0.16D));

        boolean shouldLeap = siegeLeapCooldown <= 0
                && flameLeapCooldown <= 80
                && siegePressureTicks >= (highGround ? 20 * 3 : 20 * 6)
                && distance <= 24.0D;
        if (shouldLeap) {
            siegeLeapAt(target, highGround);
            siegeLeapCooldown = highGround ? 20 * 5 : 20 * 7;
            flameLeapCooldown = Math.max(flameLeapCooldown, 20 * 5);
            siegePressureTicks = Math.max(20, siegePressureTicks / 2);
        }

        return true;
    }

    private boolean tryBlockMeleeHit(DamageSource source) {
        if (!fightStarted || deathSequenceStarted || meleeBlockCooldown > 0 || isCommittedToAction()) {
            return false;
        }

        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        if (!(attacker instanceof LivingEntity livingAttacker) || direct instanceof Projectile || attacker == this) {
            return false;
        }

        if (distanceToSqr(attacker) > 20.25D) {
            return false;
        }

        meleeBlockChance = Math.min(95, meleeBlockChance + 16);
        repeatedPatternMemory = Math.min(160, repeatedPatternMemory + 6);

        int effectiveChance = meleeBlockChance;
        if (facetankPressure >= 3) {
            effectiveChance += 18;
        }
        if (combatStyle == CombatStyle.GUARDIAN) {
            effectiveChance += 10;
        }
        if (repeatedPatternMemory > 45) {
            effectiveChance += 10;
        }
        if (confidence < 35) {
            effectiveChance += 8;
        }
        if (livingAttacker instanceof Player player && player.getAttackStrengthScale(0.5F) < 0.85F) {
            effectiveChance += 8;
        }

        effectiveChance = Math.min(96, effectiveChance);
        if (random.nextInt(100) >= effectiveChance) {
            return false;
        }

        meleeBlockChance = Math.max(20, meleeBlockChance - 35);
        meleeBlockCooldown = 10;
        blockCounterAttackTicks = 4;
        confidence = clampConfidence(confidence + 6);
        combatStyle = combatStyle == CombatStyle.STUDYING ? CombatStyle.GUARDIAN : combatStyle;
        getNavigation().stop();
        lookAt(attacker, 45.0F, 45.0F);
        getLookControl().setLookAt(attacker, 45.0F, 45.0F);
        startBossAnimation(ANIMATION_PROJECTILE_BLOCK, 19);
        playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.55F);
        playSound(SoundEvents.ANVIL_HIT, 0.55F, 0.75F);
        spawnGuardSparks();

        Vec3 push = position().subtract(attacker.position());
        Vec3 horizontal = horizontalOrLook(push).scale(0.20D);
        applyCommanderMovement(horizontal);
        if (attacker instanceof LivingEntity living) {
            living.knockback(0.35D, getX() - living.getX(), getZ() - living.getZ());
        }
        return true;
    }

    private void performCounterAttack(LivingEntity target) {
        startBossAnimation(ANIMATION_VERTICAL_SLASH, getAttackAnimationDuration(ANIMATION_VERTICAL_SLASH));
        swing(InteractionHand.MAIN_HAND, true);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F;
        target.hurt(damageSources().mobAttack(this), damage);
        applyEternalFire(target, 1);
        target.knockback(0.85D, getX() - target.getX(), getZ() - target.getZ());
        combatRecoveryTicks = 7;
        basicAttackCooldown = 6;
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 0.95F, 0.58F);
        playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 0.85F, 0.75F);
    }

    private void registerFacetankPressure(Entity attacker, float amount) {
        if (!(attacker instanceof LivingEntity livingAttacker) || distanceToSqr(attacker) > 18.0D) {
            return;
        }

        facetankPressure = Math.min(12, facetankPressure + (amount >= 7.0F ? 2 : 1));
        closeRangeTicks = Math.min(90, closeRangeTicks + 12);
        repeatedPatternMemory = Math.min(160, repeatedPatternMemory + 8);
        combatStyle = CombatStyle.DUELIST;

        if (facetankPressure >= 3 && revengeAttackTicks <= 0) {
            revengeAttackTicks = facetankPressure >= 6 ? 2 : 5;
            getLookControl().setLookAt(livingAttacker, 45.0F, 45.0F);
            lookAt(livingAttacker, 45.0F, 45.0F);
        }
    }

    private void performRevengeAttack(LivingEntity target) {
        if (facetankPressure >= 6 && antiFacetankCooldown <= 0) {
            if (tryAntiFacetankStomp(target, distanceTo(target))) {
                return;
            }
        }

        int animation = facetankPressure >= 5 ? ANIMATION_CIRCULAR_SLASH : ANIMATION_VERTICAL_SLASH;
        startBossAnimation(animation, getAttackAnimationDuration(animation));
        swing(InteractionHand.MAIN_HAND, true);

        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * (facetankPressure >= 5 ? 0.92F : 0.82F);
        target.hurt(damageSources().mobAttack(this), damage);
        applyEternalFire(target, 1);
        pushAway(target, facetankPressure >= 5 ? 0.9D : 0.55D);

        facetankPressure = Math.max(0, facetankPressure - 3);
        basicAttackCooldown = 5;
        combatRecoveryTicks = animation == ANIMATION_CIRCULAR_SLASH ? 10 : 6;
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, animation == ANIMATION_CIRCULAR_SLASH ? 0.52F : 0.62F);
    }

    private boolean tryAntiFacetankStomp(LivingEntity target, double distance) {
        if (antiFacetankCooldown > 0 || distance > 3.15D || combatRecoveryTicks > 0 || isCommittedToAction()) {
            return false;
        }

        if (closeRangeTicks < 30 && facetankPressure < 5) {
            return false;
        }

        antiFacetankCooldown = isFinalPhase() ? 55 : isPhaseTwo() ? 70 : 90;
        facetankPressure = Math.max(0, facetankPressure - 4);
        closeRangeTicks = 0;
        startBossAnimation(ANIMATION_FLAME_IMPACT, 16);
        combatRecoveryTicks = 10;
        playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.55F);
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 0.9F, 0.62F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.25D, getZ(), 28, 1.4D, 0.15D, 1.4D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.45D, getZ(), 18, 1.2D, 0.25D, 1.2D, 0.035D);
        }

        AABB area = getBoundingBox().inflate(3.0D);
        for (Entity entity : level().getEntities(this, area, entity -> entity instanceof LivingEntity && entity != this)) {
            LivingEntity living = (LivingEntity) entity;
            if (!living.isAlive()) {
                continue;
            }
            living.hurt(damageSources().mobAttack(this), 6.0F);
            applyEternalFire(living, 1);
            pushAway(living, 1.05D);
            living.push(0.0D, 0.35D, 0.0D);
        }
        return true;
    }

    private void performBasicSwordAttack(LivingEntity target) {
        if (tryFeint(target)) {
            return;
        }

        int animation = chooseBasicAttackAnimation(target);
        startBossAnimation(animation, getAttackAnimationDuration(animation));
        swing(InteractionHand.MAIN_HAND, true);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (combatStyle == CombatStyle.DUELIST && aggressionMemory > 30) {
            damage *= 1.08F;
        }
        applyEternalFire(target, 1);
        target.hurt(damageSources().mobAttack(this), damage);
        pushAway(target, closeRangeTicks > 25 ? 0.55D : 0.28D);
        basicAttackCooldown = getAdaptiveBasicCooldown(animation);
        combatRecoveryTicks = animation == ANIMATION_CIRCULAR_SLASH ? 12 : animation == ANIMATION_VERTICAL_SLASH ? 8 : 6;
        playSound(SoundEvents.PLAYER_ATTACK_STRONG, 0.9F, animation == ANIMATION_VERTICAL_SLASH ? 0.48F : 0.58F);
    }

    private boolean tryFeint(LivingEntity target) {
        // A finta curta não comunica bem em Minecraft: visualmente parece corte bugado/cancelado.
        // Por enquanto o boss ganha profundidade pelos ataques distintos, bloqueios e decisões.
        if (areSubtleFeintsDisabled()) {
            return false;
        }

        if (feintCooldown > 0 || combatStyle == CombatStyle.EXECUTOR || confidence < 45 || random.nextInt(100) > 13) {
            return false;
        }

        feintCooldown = 20 * 7;
        basicAttackCooldown = 10;
        combatRecoveryTicks = 4;
        startBossAnimation(random.nextBoolean() ? ANIMATION_VERTICAL_SLASH : ANIMATION_BASIC_SLASH, 10);
        getNavigation().stop();
        Vec3 away = position().subtract(target.position());
        away = horizontalOrLook(away).scale(0.15D);
        applyCommanderMovement(away);
        playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 0.55F, 0.55F);
        return true;
    }

    private int chooseBasicAttackAnimation(LivingEntity target) {
        double distance = distanceTo(target);
        if (facetankPressure >= 5) {
            return rememberBasicAttack(ANIMATION_CIRCULAR_SLASH);
        }

        if (closeRangeTicks > 24 || distance < 2.35D) {
            return rememberBasicAttack(lastBasicAttackAnimation == ANIMATION_VERTICAL_SLASH ? ANIMATION_BASIC_SLASH : ANIMATION_VERTICAL_SLASH);
        }

        if (rightDodgeMemory > leftDodgeMemory + 18 || leftDodgeMemory > rightDodgeMemory + 18) {
            return rememberBasicAttack(ANIMATION_CIRCULAR_SLASH);
        }

        if (retreatMemory > 36 || combatStyle == CombatStyle.EXECUTOR) {
            return rememberBasicAttack(lastBasicAttackAnimation == ANIMATION_VERTICAL_SLASH ? ANIMATION_BASIC_SLASH : ANIMATION_VERTICAL_SLASH);
        }

        if (combatStyle == CombatStyle.DUELIST && random.nextBoolean()) {
            return rememberBasicAttack(lastBasicAttackAnimation == ANIMATION_VERTICAL_SLASH ? ANIMATION_BASIC_SLASH : ANIMATION_VERTICAL_SLASH);
        }

        if (lastBasicAttackAnimation == ANIMATION_BASIC_SLASH) {
            return rememberBasicAttack(ANIMATION_VERTICAL_SLASH);
        }
        if (lastBasicAttackAnimation == ANIMATION_VERTICAL_SLASH && random.nextInt(3) == 0) {
            return rememberBasicAttack(ANIMATION_CIRCULAR_SLASH);
        }
        return rememberBasicAttack(ANIMATION_BASIC_SLASH);
    }

    private int rememberBasicAttack(int animation) {
        lastBasicAttackAnimation = animation;
        return animation;
    }

    private int getAdaptiveBasicCooldown(int animation) {
        int base = isFinalPhase() ? 9 : isPhaseTwo() ? 11 : 13;
        if (combatStyle == CombatStyle.EXECUTOR) {
            base -= 2;
        } else if (combatStyle == CombatStyle.GUARDIAN) {
            base += 1;
        }
        if (facetankPressure >= 3) {
            base -= 3;
        }
        if (animation == ANIMATION_CIRCULAR_SLASH) {
            base += 4;
        }
        return Math.max(6, base);
    }

    private int getAttackAnimationDuration(int animation) {
        if (animation == ANIMATION_CIRCULAR_SLASH) {
            return 32;
        }
        if (animation == ANIMATION_VERTICAL_SLASH) {
            return 24;
        }
        return 21;
    }

    private boolean areSubtleFeintsDisabled() {
        return true;
    }

    private void circleWaterEdge(LivingEntity target) {
        Vec3 toTarget = target.position().subtract(position());
        Vec3 forward = horizontalOrLook(toTarget);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x).scale(strafeDirection);

        double distance = distanceTo(target);
        Vec3 desired = side.scale(0.18D);
        if (distance > 7.0D) {
            desired = desired.add(forward.scale(0.22D));
        } else if (distance < 4.5D) {
            desired = desired.add(forward.scale(-0.16D));
        }

        getNavigation().stop();
        applyCommanderMovement(desired);
    }

    private boolean tryPunishWaterCamping(LivingEntity target) {
        if (waterPunishCooldown > 0 || waterCampTicks < 20 * 4) {
            return false;
        }

        waterPunishCooldown = 20 * 7;
        waterCampTicks = 20;
        projectileMemory = Math.min(160, projectileMemory + 18);
        combatStyle = CombatStyle.GUARDIAN;
        confidence = clampConfidence(confidence + 8);
        startBossAnimation(ANIMATION_FLAME_IMPACT, 18);
        getNavigation().stop();
        getLookControl().setLookAt(target, 45.0F, 45.0F);
        lookAt(target, 45.0F, 45.0F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, target.getX(), target.getY() + 0.25D, target.getZ(), 36, 1.0D, 0.35D, 1.0D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 0.35D, target.getZ(), 20, 0.75D, 0.2D, 0.75D, 0.03D);
            serverLevel.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 0.15D, target.getZ(), 8, 0.65D, 0.15D, 0.65D, 0.05D);
        }

        target.hurt(damageSources().mobAttack(this), isFinalPhase() ? 8.0F : isPhaseTwo() ? 6.0F : 4.0F);
        Vec3 away = target.position().subtract(position());
        Vec3 knock = horizontalOrLook(away);
        target.knockback(1.35D, -knock.x, -knock.z);
        target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.28D, 0.0D));
        playSound(SoundEvents.LAVA_EXTINGUISH, 1.1F, 0.65F);
        playSound(SoundEvents.BLAZE_SHOOT, 0.75F, 0.55F);
        return true;
    }

    private boolean shouldDodgeProjectile() {
        AABB area = getBoundingBox().inflate(4.0D);
        for (Projectile projectile : level().getEntitiesOfClass(Projectile.class, area)) {
            if (!projectile.isAlive()) {
                continue;
            }

            Entity owner = projectile.getOwner();
            if (owner == this) {
                continue;
            }

            Vec3 projectileMotion = projectile.getDeltaMovement();
            Vec3 toBoss = position().subtract(projectile.position());
            if (projectileMotion.lengthSqr() > 0.01D && projectileMotion.normalize().dot(toBoss.normalize()) > 0.55D) {
                projectileMemory = Math.min(160, projectileMemory + 2);
                return true;
            }
        }

        return false;
    }

    private void spawnGuardSparks() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.65D, getZ(), 8, 0.35D, 0.45D, 0.35D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.35D, getZ(), 4, 0.25D, 0.25D, 0.25D, 0.015D);
        }
    }

    private boolean tickProjectileBlock(LivingEntity target) {
        if (target == null || !target.isAlive() || projectileBlockCooldown > 0 || !fightStarted || deathSequenceStarted) {
            return false;
        }

        boolean siegeThreat = isDistantOrHighThreat(target) || siegePressureTicks > 20;
        Projectile projectile = findIncomingProjectile(target.isInWater() || siegeThreat ? 14.0D : 8.0D);
        if (projectile == null) {
            return false;
        }

        projectileMemory = Math.min(160, projectileMemory + 4);
        float blockChance;
        if (siegeThreat) {
            blockChance = 1.0F;
        } else if (target.isInWater()) {
            blockChance = isCommittedToAction() ? 0.70F : 0.92F;
        } else {
            blockChance = isCommittedToAction() ? 0.25F : 0.50F;
        }
        if (random.nextFloat() > blockChance) {
            projectileBlockCooldown = target.isInWater() ? 4 : 8;
            return false;
        }

        projectileBlockCooldown = siegeThreat ? 6 : target.isInWater() ? 10 : 20;
        observationTicks = 0;
        if (siegeThreat) {
            siegePressureTicks = Math.min(20 * 18, siegePressureTicks + 12);
            combatStyle = CombatStyle.GUARDIAN;
        }
        getNavigation().stop();
        lookAt(projectile, 45.0F, 45.0F);
        getLookControl().setLookAt(projectile, 45.0F, 45.0F);
        setDeltaMovement(getDeltaMovement().multiply(0.15D, 1.0D, 0.15D));
        startBossAnimation(ANIMATION_PROJECTILE_BLOCK, 19);
        playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.45F);
        playSound(SoundEvents.ANVIL_HIT, 0.45F, 0.65F);
        spawnGuardSparks();

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, projectile.getX(), projectile.getY(), projectile.getZ(), 10, 0.16D, 0.16D, 0.16D, 0.10D);
        }

        projectile.discard();
        return true;
    }

    private Projectile findIncomingProjectile(double radius) {
        AABB area = getBoundingBox().inflate(radius);
        Projectile best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Projectile projectile : level().getEntitiesOfClass(Projectile.class, area)) {
            if (!isDangerousIncomingProjectile(projectile)) {
                continue;
            }

            double distance = projectile.distanceToSqr(this);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = projectile;
            }
        }

        return best;
    }

    private boolean isDangerousIncomingProjectile(Projectile projectile) {
        if (!projectile.isAlive() || projectile.getOwner() == this) {
            return false;
        }

        Vec3 projectileMotion = projectile.getDeltaMovement();
        if (projectileMotion.lengthSqr() <= 0.01D) {
            return false;
        }

        Vec3 toBoss = position().add(0.0D, getBbHeight() * 0.55D, 0.0D).subtract(projectile.position());
        if (toBoss.lengthSqr() <= 0.0001D) {
            return false;
        }

        return projectileMotion.normalize().dot(toBoss.normalize()) > 0.62D;
    }

    private boolean isCommittedToAction() {
        int animation = getBossAnimation();
        return chargeAttackTicks > 0
                || leapImpactTicks > 0
                || animation == ANIMATION_BASIC_SLASH
                || animation == ANIMATION_VERTICAL_SLASH
                || animation == ANIMATION_CIRCULAR_SLASH
                || animation == ANIMATION_FLAME_LEAP
                || animation == ANIMATION_FLAME_IMPACT
                || animation == ANIMATION_CHARGE;
    }

    private Vec3 horizontalOrLook(Vec3 vector) {
        Vec3 horizontal = new Vec3(vector.x, 0.0D, vector.z);
        if (horizontal.lengthSqr() < 0.0001D) {
            horizontal = new Vec3(getLookAngle().x, 0.0D, getLookAngle().z);
        }

        if (horizontal.lengthSqr() < 0.0001D) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }

        return horizontal.normalize();
    }

    private void applyCommanderMovement(Vec3 desiredHorizontalMovement) {
        Vec3 current = getDeltaMovement();
        Vec3 blended = new Vec3(
                current.x * 0.25D + desiredHorizontalMovement.x * 0.75D,
                current.y,
                current.z * 0.25D + desiredHorizontalMovement.z * 0.75D
        );
        setDeltaMovement(blended);
        hasImpulse = true;
    }

    private void tickAbilities() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        if (chargeAttackTicks > 0) {
            tickChargeAttack(target);
            return;
        }

        if (leapImpactTicks > 0) {
            leapImpactTicks--;
            if (leapImpactTicks == 0 || onGround()) {
                leapImpactTicks = 0;
                flameImpact();
            }
            return;
        }

        int cooldownReduction = isPhaseTwo() ? 40 : 0;

        if (observationTicks > 0) {
            return;
        }

        double distanceSqr = distanceToSqr(target);
        if (chargeCooldown <= 0 && shouldPunishWithCharge(target, distanceSqr)) {
            chargeAfterWater = false;
            chargeAt(target);
            chargeCooldown = Math.max(80, CHARGE_COOLDOWN - cooldownReduction - (combatStyle == CombatStyle.EXECUTOR ? 25 : 0));
            return;
        }

        if (flameLeapCooldown <= 0 && shouldPunishWithLeap(target, distanceSqr)) {
            flameLeap(target);
            flameLeapCooldown = Math.max(120, FLAME_LEAP_COOLDOWN - cooldownReduction - (combatStyle == CombatStyle.EXECUTOR ? 20 : 0));
            return;
        }

        if (circularSlashCooldown <= 0 && shouldUseCircularSlash(target, distanceSqr)) {
            circularSlash();
            circularSlashCooldown = Math.max(55, CIRCULAR_SLASH_COOLDOWN - cooldownReduction);
            return;
        }

        if (combatStyle == CombatStyle.GUARDIAN && confidence < 40 && distanceSqr <= 25.0D && random.nextInt(120) == 0) {
            startBossAnimation(ANIMATION_GUARD_OBSERVE, 22);
            combatRecoveryTicks = 8;
        }
    }

    private boolean shouldPunishWithCharge(LivingEntity target, double distanceSqr) {
        if (chargeAfterWater) {
            return true;
        }

        if (distanceSqr > 64.0D && (combatStyle == CombatStyle.EXECUTOR || retreatMemory > 35 || passivePressureTicks > 20 * 14)) {
            return true;
        }

        if (target instanceof Player player && player.isUsingItem() && distanceSqr > 16.0D) {
            return true;
        }

        return distanceSqr > 100.0D && retreatMemory > 18;
    }

    private boolean shouldPunishWithLeap(LivingEntity target, double distanceSqr) {
        if (distanceSqr < 36.0D || distanceSqr > 196.0D) {
            return false;
        }

        if (retreatMemory > 60 || passivePressureTicks > 20 * 22) {
            return true;
        }

        return combatStyle == CombatStyle.EXECUTOR && random.nextInt(70) == 0;
    }

    private boolean shouldUseCircularSlash(LivingEntity target, double distanceSqr) {
        if (distanceSqr > 36.0D) {
            return false;
        }

        if (closeRangeTicks > 28) {
            return true;
        }

        if (rightDodgeMemory > leftDodgeMemory + 15 || leftDodgeMemory > rightDodgeMemory + 15) {
            return true;
        }

        return combatStyle == CombatStyle.DUELIST && distanceSqr <= 20.25D && random.nextInt(85) == 0;
    }

    private void ensureRelicEquipped() {
        if (getMainHandItem().isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ETERNAL_FIRE_BLADE.get()));
            setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }

    }

    private void startBossAnimation(int animation, int durationTicks) {
        entityData.set(BOSS_ANIMATION, animation);
        entityData.set(BOSS_ANIMATION_TICKS, durationTicks);
    }

    private void tickAnimationTimer() {
        int ticks = entityData.get(BOSS_ANIMATION_TICKS);
        if (ticks <= 0) {
            if (entityData.get(BOSS_ANIMATION) != ANIMATION_IDLE) {
                entityData.set(BOSS_ANIMATION, ANIMATION_IDLE);
            }
            return;
        }

        entityData.set(BOSS_ANIMATION_TICKS, ticks - 1);
        if (ticks - 1 <= 0) {
            entityData.set(BOSS_ANIMATION, ANIMATION_IDLE);
        }
    }

    private void chargeAt(LivingEntity target) {
        Vec3 direction = target.position().subtract(position()).normalize();
        double distance = distanceTo(target);
        boolean extendedCharge = distance > 9.0D;
        int extraWindup = extendedCharge ? (int) Math.min(24.0D, Math.max(8.0D, (distance - 9.0D) * 2.2D)) : 0;
        int dashTicks = extendedCharge ? 28 : 20;

        chargeDirectionX = direction.x;
        chargeDirectionZ = direction.z;
        chargeWindupTicks = 20 + extraWindup;
        chargeAttackTicks = chargeWindupTicks + dashTicks;
        chargeDashTicks = dashTicks;
        chargeDashTicksTotal = dashTicks;
        chargeDashSpeed = getChargeSpeedForDistance(distance, extendedCharge);
        chargeDamageDone = false;
        combatRecoveryTicks = extendedCharge ? 12 : 8;
        startBossAnimation(ANIMATION_CHARGE, chargeAttackTicks);
        getNavigation().stop();
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        setDeltaMovement(getDeltaMovement().multiply(0.15D, 1.0D, 0.15D));
        playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 0.85F, 0.38F);
        playSound(SoundEvents.FIRECHARGE_USE, 0.45F, 0.45F);
    }

    private double getChargeSpeedForDistance(double distance, boolean extendedCharge) {
        double baseSpeed = isFinalPhase() ? 0.62D : isPhaseTwo() ? 0.56D : 0.46D;
        if (!extendedCharge) {
            return baseSpeed;
        }

        return baseSpeed + Math.min(0.30D, (distance - 9.0D) * 0.035D);
    }

    private void adjustChargeDirectionToward(LivingEntity target) {
        if (chargeDashTicksTotal <= 0 || target == null || !target.isAlive()) {
            return;
        }

        Vec3 desired = target.position().subtract(position());
        desired = new Vec3(desired.x, 0.0D, desired.z);
        if (desired.lengthSqr() < 0.0001D) {
            return;
        }

        desired = desired.normalize();
        Vec3 current = new Vec3(chargeDirectionX, 0.0D, chargeDirectionZ);
        if (current.lengthSqr() < 0.0001D) {
            current = desired;
        } else {
            current = current.normalize();
        }

        double progress = 1.0D - (chargeDashTicks / (double) chargeDashTicksTotal);
        double turnRate = isFinalPhase() ? 0.18D : isPhaseTwo() ? 0.14D : 0.11D;
        if (progress > 0.55D) {
            turnRate *= 0.45D;
        }
        if (progress > 0.78D) {
            turnRate *= 0.20D;
        }

        Vec3 adjusted = current.scale(1.0D - turnRate).add(desired.scale(turnRate));
        if (adjusted.lengthSqr() < 0.0001D) {
            return;
        }

        adjusted = adjusted.normalize();
        chargeDirectionX = adjusted.x;
        chargeDirectionZ = adjusted.z;
    }

    private void tickChargeAttack(LivingEntity target) {
        chargeAttackTicks--;
        getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (level() instanceof ServerLevel serverLevel && tickCount % 2 == 0) {
            double yOffset = chargeAttackTicks > chargeWindupTicks ? 0.35D : 0.65D;
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + yOffset, getZ(), 7, 0.45D, 0.15D, 0.45D, 0.018D);
        }

        if (chargeAttackTicks == chargeWindupTicks) {
            playSound(SoundEvents.RAVAGER_ROAR, 0.7F, 0.55F);
        }

        if (chargeAttackTicks > chargeWindupTicks) {
            setDeltaMovement(getDeltaMovement().multiply(0.12D, 1.0D, 0.12D));
            return;
        }

        chargeDashTicks = Math.max(0, chargeDashTicks - 1);
        adjustChargeDirectionToward(target);
        setDeltaMovement(chargeDirectionX * chargeDashSpeed, getDeltaMovement().y, chargeDirectionZ * chargeDashSpeed);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.65D, getZ(), 8, 0.35D, 0.35D, 0.35D, 0.01D);
        }

        if (!chargeDamageDone && distanceToSqr(target) <= 12.25D) {
            chargeDamageDone = true;
            swing(InteractionHand.MAIN_HAND, true);
            target.hurt(damageSources().mobAttack(this), 12.0F);
            target.knockback(2.0D, getX() - target.getX(), getZ() - target.getZ());
            applyEternalFire(target, 2);
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.25F, 0.55F);
            playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 0.65F);
        }

        if (chargeAttackTicks <= 0) {
            chargeAttackTicks = 0;
            combatRecoveryTicks = 6;
            setDeltaMovement(getDeltaMovement().multiply(0.35D, 1.0D, 0.35D));
        }
    }

    private void circularSlash() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        startBossAnimation(ANIMATION_CIRCULAR_SLASH, getAttackAnimationDuration(ANIMATION_CIRCULAR_SLASH));
        combatRecoveryTicks = 12;
        serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.0D, getZ(), 70, 3.0D, 0.35D, 3.0D, 0.025D);
        serverLevel.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.45D, getZ(), 12, 2.0D, 0.2D, 2.0D, 0.04D);
        swing(InteractionHand.MAIN_HAND, true);
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.3F, 0.45F);
        playSound(SoundEvents.ANVIL_HIT, 0.65F, 0.6F);
        playSound(SoundEvents.BLAZE_SHOOT, 0.45F, 0.75F);

        AABB area = getBoundingBox().inflate(5.0D);
        for (Entity entity : level().getEntities(this, area, entity -> entity instanceof LivingEntity && entity != this)) {
            LivingEntity living = (LivingEntity) entity;
            if (!living.isAlive()) {
                continue;
            }

            living.hurt(damageSources().mobAttack(this), 10.0F);
            applyEternalFire(living, 1);
            pushAway(living, 0.65D);
        }
    }

    private void flameLeap(LivingEntity target) {
        Vec3 direction = target.position().subtract(position()).normalize();
        startBossAnimation(ANIMATION_FLAME_LEAP, 30);
        combatRecoveryTicks = 8;
        setDeltaMovement(direction.x * 0.95D, 0.9D, direction.z * 0.95D);
        leapImpactTicks = 26;
        playSound(SoundEvents.BLAZE_SHOOT, 0.9F, 0.65F);
        playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 0.55F, 0.55F);
    }

    private void siegeLeapAt(LivingEntity target, boolean highGround) {
        Vec3 toTarget = target.position().subtract(position());
        Vec3 horizontal = horizontalOrLook(toTarget);
        double horizontalDistance = Math.sqrt(new Vec3(toTarget.x, 0.0D, toTarget.z).lengthSqr());
        double heightDifference = Math.max(0.0D, target.getY() - getY());
        double horizontalSpeed = Math.min(1.25D, 0.72D + horizontalDistance * 0.035D);
        double verticalSpeed = highGround ? Math.min(1.85D, 1.0D + heightDifference * 0.075D) : 0.95D;

        startBossAnimation(ANIMATION_FLAME_LEAP, 34);
        combatRecoveryTicks = 10;
        leapImpactTicks = highGround ? 32 : 26;
        setDeltaMovement(horizontal.x * horizontalSpeed, verticalSpeed, horizontal.z * horizontalSpeed);
        hasImpulse = true;
        playSound(SoundEvents.RAVAGER_ROAR, 0.75F, 0.48F);
        playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.55F);
        playSound(SoundEvents.ANVIL_LAND, 0.7F, 0.55F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.45D, getZ(), 22, 0.75D, 0.25D, 0.75D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.65D, getZ(), 28, 0.65D, 0.35D, 0.65D, 0.05D);
        }
    }

    private void flameImpact() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        startBossAnimation(ANIMATION_FLAME_IMPACT, 18);
        combatRecoveryTicks = 12;
        serverLevel.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.2D, getZ(), 25, 2.5D, 0.4D, 2.5D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.2D, getZ(), 55, 3.0D, 0.5D, 3.0D, 0.04D);
        playSound(SoundEvents.GENERIC_EXPLODE, 0.75F, 0.7F);
        playSound(SoundEvents.LAVA_EXTINGUISH, 0.8F, 0.75F);

        AABB area = getBoundingBox().inflate(4.0D);
        for (Entity entity : level().getEntities(this, area, entity -> entity instanceof LivingEntity && entity != this)) {
            LivingEntity living = (LivingEntity) entity;
            living.hurt(damageSources().mobAttack(this), 8.0F);
            applyEternalFire(living, 1);
            pushAway(living, 1.25D);
        }
    }

    private void applyEternalFire(LivingEntity target, int stacks) {
        HeatManager.addHeat(this, 1);
        EternalFireEffect.addStacks(target, stacks);

        if (!firstEternalFireLine && target instanceof Player) {
            firstEternalFireLine = true;
            sayToNearbyPlayers("Agora carregue o mesmo fardo que eu.");
        }
    }

    private void updatePhase() {
        if (isPhaseTwo()) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.132D);
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(18.4D);

            if (!phaseTwoLine) {
                phaseTwoLine = true;
                startBossAnimation(ANIMATION_PHASE_TRANSITION, 45);
                playSound(SoundEvents.BLASTFURNACE_FIRE_CRACKLE, 1.0F, 0.55F);
                playSound(SoundEvents.FIRECHARGE_USE, 0.75F, 0.45F);
                sayToNearbyPlayers("Então... você suporta as chamas.");
            }
        }

        if (isFinalPhase()) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.150D);
            getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);

            if (!finalPhaseLine) {
                finalPhaseLine = true;
                startBossAnimation(ANIMATION_PHASE_TRANSITION, 50);
                playSound(SoundEvents.ANVIL_LAND, 0.9F, 0.35F);
                playSound(SoundEvents.BLAZE_SHOOT, 0.7F, 0.42F);
                sayToNearbyPlayers("Mesmo reduzido a cinzas... continuarei lutando.");
                startFinalPhaseHealing();
            }
        }
    }

    private void startFinalPhaseHealing() {
        if (finalPhaseHealingUsed) {
            return;
        }

        finalPhaseHealingUsed = true;
        finalPhaseHealingTicks = 20 * 8;
        setSecondsOnFire(10);
        addEffect(new MobEffectInstance(MobEffects.REGENERATION, finalPhaseHealingTicks, 1, false, true, true));
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, finalPhaseHealingTicks, 0, false, true, true));
        heal(28.0F);
        playSound(SoundEvents.FIRECHARGE_USE, 1.1F, 0.35F);
        playSound(SoundEvents.BLASTFURNACE_FIRE_CRACKLE, 1.2F, 0.5F);
    }

    private void tickFinalPhaseHealing() {
        if (finalPhaseHealingTicks <= 0) {
            return;
        }

        finalPhaseHealingTicks--;
        setSecondsOnFire(2);

        if (tickCount % 20 == 0) {
            heal(3.0F);
        }

        if (level() instanceof ServerLevel serverLevel && tickCount % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.2D, getZ(), 12, 0.9D, 1.0D, 0.9D, 0.035D);
            serverLevel.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.8D, getZ(), 3, 0.45D, 0.6D, 0.45D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.4D, getZ(), 6, 0.9D, 1.0D, 0.9D, 0.02D);
        }
    }

    private void tickArenaPressure() {
        if (!isFinalPhase() || tickCount % 40 != 0) {
            return;
        }

        BlockPos pos = blockPosition().offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
        BlockPos firePos = level().getBlockState(pos).isAir() ? pos : pos.above();

        if (level().getBlockState(firePos).isAir() && Blocks.FIRE.defaultBlockState().canSurvive(level(), firePos)) {
            level().setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
        }
    }

    private void keepDistanceFromWaterTargets() {
        LivingEntity target = getTarget();
        if (target != null && target.isInWater() && !isInWater()) {
            getNavigation().stop();
            getLookControl().setLookAt(target, 30.0F, 30.0F);
            setDeltaMovement(getDeltaMovement().multiply(0.2D, 1.0D, 0.2D));
        }
    }

    private void clearBlockingBlocks() {
        if (!(level() instanceof ServerLevel serverLevel) || tickCount % 5 != 0) {
            return;
        }

        LivingEntity target = getTarget();
        Vec3 lookDirection = target != null
                ? target.position().subtract(position()).normalize()
                : getLookAngle().normalize();

        BlockPos center = blockPosition().offset(
                Math.round((float) lookDirection.x),
                0,
                Math.round((float) lookDirection.z)
        );

        int changedBlocks = 0;
        int maxChangedBlocks = chargeAttackTicks > 0 ? 4 : 2;

        for (int y = 0; y <= 2 && changedBlocks < maxChangedBlocks; y++) {
            for (int x = -1; x <= 1 && changedBlocks < maxChangedBlocks; x++) {
                for (int z = -1; z <= 1 && changedBlocks < maxChangedBlocks; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (tryClearBlockingBlock(serverLevel, pos)) {
                        changedBlocks++;
                    }
                }
            }
        }
    }

    private boolean tryClearBlockingBlock(ServerLevel serverLevel, BlockPos pos) {
        BlockState state = serverLevel.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.WATER)) {
            return false;
        }

        if (state.getDestroySpeed(serverLevel, pos) < 0.0F || serverLevel.getBlockEntity(pos) != null) {
            return false;
        }

        boolean flammable = false;
        for (Direction direction : Direction.values()) {
            if (state.isFlammable(serverLevel, pos, direction)) {
                flammable = true;
                break;
            }
        }

        if (flammable) {
            serverLevel.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
            serverLevel.levelEvent(1501, pos, 0);
            return true;
        }

        serverLevel.destroyBlock(pos, false, this);
        serverLevel.levelEvent(2001, pos, Block.getId(state));
        return true;
    }

    private void spawnAmbientAshes() {
        if (!(level() instanceof ServerLevel serverLevel) || tickCount % 8 != 0) {
            return;
        }

        int count = isPhaseTwo() ? 8 : 3;
        serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.1D, getZ(), count, 1.4D, 0.9D, 1.4D, 0.01D);

        if (isFinalPhase()) {
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.0D, getZ(), 4, 1.5D, 0.8D, 1.5D, 0.01D);
        }
    }

    private void pushAway(LivingEntity target, double strength) {
        Vec3 direction = target.position().subtract(position()).normalize();
        target.push(direction.x * strength, 0.35D, direction.z * strength);
    }

    private void sayToNearbyPlayers(String message) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Component component = Component.literal("<Cavaleiro das Cinzas> " + message);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(this) <= 64.0D * 64.0D) {
                player.sendSystemMessage(component);
            }
        }
    }

    private boolean isPhaseTwo() {
        return getHealth() <= getMaxHealth() * 0.5F;
    }

    private boolean isFinalPhase() {
        return getHealth() <= getMaxHealth() * 0.15F;
    }

}
