package net.fantik.lostdreams.entity;

import net.fantik.lostdreams.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class MeteorEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
            SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_CHARGE =
            SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.INT);

    private static final double FLY_SPEED     = 0.55;
    //private static final float  EXPLOSION_RADIUS = 2.5f;
    private static final double HIT_DISTANCE_SQ  = 1.5 * 1.5;
    private static final int    CHARGE_TICKS  = 30;
    private static final double SEARCH_RADIUS = 48.0;
    private static final int    MAX_FLY_TICKS = 400;

    private int  explosionCount = 0;
    private boolean exploded    = false;
    private int  flyTicks       = 0;

    public MeteorEntity(EntityType<? extends MeteorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;      // ← летит сквозь блоки, не падает на пол
        this.setNoGravity(true);    // ← нет гравитации

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, SEARCH_RADIUS)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTIVE, false);
        builder.define(DATA_CHARGE, 0);
    }

    public boolean isActive()            { return this.entityData.get(DATA_ACTIVE); }
    private void setActive(boolean v)    { this.entityData.set(DATA_ACTIVE, v); }
    public int  getCharge()              { return this.entityData.get(DATA_CHARGE); }
    private void setCharge(int v)        { this.entityData.set(DATA_CHARGE, v); }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setActive(tag.getBoolean("Active"));
        this.explosionCount = tag.getInt("ExplosionCount");
        this.flyTicks = tag.getInt("FlyTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Active", this.isActive());
        tag.putInt("ExplosionCount", this.explosionCount);
        tag.putInt("FlyTicks", this.flyTicks);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeteorChaseGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, Player.class, true));
    }

    // -----------------------------------------------------------------------
    // Tick
    // -----------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            spawnClientParticles();
            return;
        }

        if (exploded) {
            this.discard();
            return;
        }

        if (!isActive()) {
            tickCharging();
        } else {
            tickFlying();
        }
    }

    // -----------------------------------------------------------------------
    // Зарядка
    // -----------------------------------------------------------------------

    private void tickCharging() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        int charge = getCharge() + 1;
        setCharge(charge);

        // Смотрим и поворачиваем тело к цели
        faceTarget(target);

        // Лёгкое "дрожание" на месте
        if (charge % 3 == 0) {
            this.setDeltaMovement(
                    (this.random.nextDouble() - 0.5) * 0.05,
                    (this.random.nextDouble() - 0.5) * 0.05,
                    (this.random.nextDouble() - 0.5) * 0.05
            );
            this.move(MoverType.SELF, this.getDeltaMovement());
        }

        ServerLevel sl = (ServerLevel) this.level();
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 0.5, this.getZ(),
                3, 0.3, 0.3, 0.3, 0.02);

        if (charge % 10 == 0) {
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE,
                    0.3f + charge * 0.01f, 2.0f - charge * 0.03f);
        }

        if (charge >= CHARGE_TICKS) {
            setCharge(0);
            setActive(true);
            flyTicks = 0;
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 1.5f, 0.5f);
        }
    }

    // -----------------------------------------------------------------------
    // Полёт
    // -----------------------------------------------------------------------

    private void tickFlying() {
        flyTicks++;

        if (flyTicks > MAX_FLY_TICKS) {
            this.discard();
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            Player nearest = this.level().getNearestPlayer(this, SEARCH_RADIUS);
            if (nearest != null) { this.setTarget(nearest); target = nearest; }
            else { this.discard(); return; }
        }

        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
        Vec3 myPos     = this.position().add(0, this.getBbHeight() / 2.0, 0);
        Vec3 direction = targetPos.subtract(myPos);
        double distSq  = direction.lengthSqr();

        if (distSq <= HIT_DISTANCE_SQ) {
            triggerExplosion(target);
            return;
        }

        // Поворачиваем тело к цели во время полёта
        faceTarget(target);

        Vec3 vel = direction.normalize().scale(FLY_SPEED);
        if (flyTicks < 10) vel = vel.scale(flyTicks / 10.0);

        this.setDeltaMovement(vel);
        this.move(MoverType.SELF, this.getDeltaMovement());

        ServerLevel sl = (ServerLevel) this.level();
        sl.sendParticles(ParticleTypes.LARGE_SMOKE,
                this.getX(), this.getY() + 0.5, this.getZ(),
                2, 0.15, 0.15, 0.15, 0.01);

        if (flyTicks % 10 == 0) {
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.HOSTILE, 0.5f, 1.8f);
        }
    }

    // -----------------------------------------------------------------------
    // Поворот тела к цели
    // -----------------------------------------------------------------------

    private void faceTarget(LivingEntity target) {
        Vec3 toTarget = target.position().add(0, target.getBbHeight() / 2.0, 0)
                .subtract(this.position().add(0, this.getBbHeight() / 2.0, 0));

        double hDist = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        float yaw   = (float)(Mth.atan2(toTarget.z, toTarget.x) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float)(-(Mth.atan2(toTarget.y, hDist) * (180.0 / Math.PI)));

        this.setYRot(yaw);
        this.yBodyRot    = yaw;   // тело
        this.yHeadRot    = yaw;   // голова
        this.setXRot(pitch);
    }

    // -----------------------------------------------------------------------
    // Взрыв
    // -----------------------------------------------------------------------

    private void triggerExplosion(LivingEntity target) {
        ServerLevel sl = (ServerLevel) this.level();
        explosionCount++;

        // Урон только игроку — фиксированный, без взрывного AOE
        target.hurt(this.damageSources().mobAttack(this), 6.0f);

        // Визуальный взрыв — только частицы, без реального Explosion
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY(), this.getZ(), 30, 0.5, 0.5, 0.5, 0.3);

        this.level().playSound(null, this.blockPosition(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0f, 0.8f);

        if (explosionCount >= 2) {
            exploded = true;
        } else {
            // Первый взрыв — отлетаем и перезаряжаемся
            setActive(false);
            setCharge(0);
            flyTicks = 0;

            Vec3 away = this.position().subtract(target.position()).normalize().scale(4.0);
            this.setPos(this.getX() + away.x, this.getY() + 2.0, this.getZ() + away.z);

            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0f, 0.5f);
        }
    }

    // -----------------------------------------------------------------------
    // Клиентские частицы
    // -----------------------------------------------------------------------

    private void spawnClientParticles() {
        if (!isActive()) {
            // Зарядка — чёрные частицы
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.SQUID_INK,
                        this.getX() + (random.nextDouble() - 0.5) * 0.5,
                        this.getY() + random.nextDouble(),
                        this.getZ() + (random.nextDouble() - 0.5) * 0.5,
                        0, 0.05, 0);
            }
        } else {
            // Полёт — кастомная частица
            Vec3 vel = this.getDeltaMovement();
            this.level().addParticle(ModParticles.NULL_PARTICLE.get(),
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    -vel.x * 0.5, -vel.y * 0.5, -vel.z * 0.5);
        }
    }

    // -----------------------------------------------------------------------
    // Переопределения
    // -----------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isActive()) return false; // неуязвим во время полёта
        return super.hurt(source, amount);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround,
                                   net.minecraft.world.level.block.state.BlockState state,
                                   BlockPos pos) {}

    @Override public boolean isOnFire()  { return false; }
    @Override public boolean fireImmune(){ return true; }

    @Override
    public Entity.MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    // -----------------------------------------------------------------------
    // Goal
    // -----------------------------------------------------------------------

    static class MeteorChaseGoal extends Goal {

        private final MeteorEntity meteor;

        MeteorChaseGoal(MeteorEntity meteor) {
            this.meteor = meteor;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        }

        @Override public boolean canUse() {
            LivingEntity t = meteor.getTarget();
            return t != null && t.isAlive() && !meteor.exploded;
        }
        @Override public boolean canContinueToUse() { return !meteor.exploded; }
        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override public void start() {
            meteor.setActive(false);
            meteor.setCharge(0);
            meteor.flyTicks = 0;
        }

        @Override public void stop() {
            meteor.setDeltaMovement(Vec3.ZERO);
            meteor.setActive(false);
            meteor.setCharge(0);
        }

        @Override public void tick() {}
    }
}