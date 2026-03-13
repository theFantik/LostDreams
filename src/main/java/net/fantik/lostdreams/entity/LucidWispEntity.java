package net.fantik.lostdreams.entity;

import net.fantik.lostdreams.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LucidWispEntity extends PathfinderMob {

    public LucidWispEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WispAttackGoal(this));
        this.goalSelector.addGoal(2, new WispIdleWanderGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (this.level().isClientSide && this.level().getGameTime() % 3 == 0) {
            this.level().addParticle(
                    new net.minecraft.core.particles.DustParticleOptions(
                            new org.joml.Vector3f(0.96f, 0.87f, 0.70f), 1.0f
                    ),
                    this.getX() + (random.nextDouble() - 0.5) * 0.5,
                    this.getY() + random.nextDouble() * 0.5,
                    this.getZ() + (random.nextDouble() - 0.5) * 0.5,
                    0, 0.02, 0
            );
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (source.getEntity() instanceof Player player) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    30 * 20,
                    0,
                    false,
                    true,
                    true
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Цель: атака и движение к игроку через velocity
    // -----------------------------------------------------------------------
    static class WispAttackGoal extends Goal {
        private final LucidWispEntity wisp;
        private int attackCooldown = 0;
        private int strafeTick = 0;
        private boolean strafingClockwise = false;
        private boolean strafingBackwards = false;

        WispAttackGoal(LucidWispEntity wisp) {
            this.wisp = wisp;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return wisp.getTarget() != null && wisp.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return wisp.getTarget() != null && wisp.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = wisp.getTarget();
            if (target == null) return;

            double dx = target.getX() - wisp.getX();
            double dy = target.getY() + 1.0 - wisp.getY();
            double dz = target.getZ() - wisp.getZ();
            double distSq = wisp.distanceToSqr(target);
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Поворачиваем лицом к цели
            float yaw = (float)(Math.toDegrees(Math.atan2(-dx, dz)));
            wisp.setYRot(yaw);
            wisp.yBodyRot = yaw;
            wisp.yHeadRot = yaw;

            // Страйф
            strafeTick++;
            if (strafeTick >= 20) {
                strafeTick = 0;
                strafingClockwise = !strafingClockwise;
                if (wisp.random.nextInt(4) == 0) strafingBackwards = !strafingBackwards;
            }

            // Движение к цели
            double speed = 0.18;
            if (dist > 0.1) {
                Vec3 velocity = new Vec3(dx / dist * speed, dy / dist * speed, dz / dist * speed);
                wisp.setDeltaMovement(wisp.getDeltaMovement().scale(0.5).add(velocity));
            }

            // Атака при сближении
            if (--attackCooldown <= 0 && distSq < 3.5 * 3.5) {
                attackCooldown = 20;
                wisp.doHurtTarget(target);
            }

            // Ускорение если далеко
            if (distSq > 16 * 16 && dist > 0.1) {
                Vec3 fastVelocity = new Vec3(dx / dist * speed * 2, dy / dist * speed * 2, dz / dist * speed * 2);
                wisp.setDeltaMovement(wisp.getDeltaMovement().scale(0.3).add(fastVelocity));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Цель: блуждание когда нет цели — активное движение во всех направлениях
    // -----------------------------------------------------------------------
    static class WispIdleWanderGoal extends Goal {
        private final LucidWispEntity wisp;
        private Vec3 targetPos = null;
        private int wanderCooldown = 0;

        WispIdleWanderGoal(LucidWispEntity wisp) {
            this.wisp = wisp;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return wisp.getTarget() == null && --wanderCooldown <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            if (wisp.getTarget() != null) return false;
            if (targetPos == null) return false;
            double dist = wisp.position().distanceTo(targetPos);
            return dist > 1.5;
        }




        @Override
        public void start() {
            // Выбираем случайную точку в радиусе 8 блоков по всем осям
            double x = wisp.getX() + (wisp.random.nextFloat() * 2 - 1) * 8;
            double y = wisp.getY() + (wisp.random.nextFloat() * 2 - 1) * 4;
            double z = wisp.getZ() + (wisp.random.nextFloat() * 2 - 1) * 8;

            // Ограничиваем Y чтобы не вылетал за пределы
            y = Math.max(wisp.level().getMinBuildHeight() + 2, Math.min(wisp.level().getMaxBuildHeight() - 2, y));

            targetPos = new Vec3(x, y, z);
            wanderCooldown = 40 + wisp.random.nextInt(40); // пауза 2-4 секунды между блужданиями
        }

        @Override
        public void stop() {
            targetPos = null;
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            double dx = targetPos.x - wisp.getX();
            double dy = targetPos.y - wisp.getY();
            double dz = targetPos.z - wisp.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist > 0.1) {
                double speed = 0.12;
                Vec3 velocity = new Vec3(dx / dist * speed, dy / dist * speed, dz / dist * speed);
                wisp.setDeltaMovement(wisp.getDeltaMovement().scale(0.5).add(velocity));

                // Поворачиваем в сторону движения
                float yaw = (float)(Math.toDegrees(Math.atan2(-dx, dz)));
                wisp.setYRot(yaw);
                wisp.yBodyRot = yaw;
            }
        }
    }
    protected SoundEvent getAmbientSound() {
        return ModSounds.LUCID_WISP_AMBIENT.get();
    }


    public int getAmbientSoundInterval() {
        return 80; // каждые 4 секунды
    }
}