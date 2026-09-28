package net.fantik.lostdreams.entity;

import net.fantik.lostdreams.item.ModItems;
import net.fantik.lostdreams.network.MeteorSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MeteorMountEntity extends Entity {

    // =========================================================
    //  SYNCHED DATA
    // =========================================================
    private static final EntityDataAccessor<Boolean> DATA_HAS_RIDER =
            SynchedEntityData.defineId(MeteorMountEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     * Синхронизированный флаг буста: клиент читает его чтобы применить
     * множитель скорости. Сервер устанавливает его при активации.
     */
    private static final EntityDataAccessor<Boolean> DATA_BOOSTED =
            SynchedEntityData.defineId(MeteorMountEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     * Синхронизированный остаток тиков буста — нужен клиенту для отображения
     * (например, в HUD или партиклах). Обновляется каждый тик на сервере.
     */
    private static final EntityDataAccessor<Integer> DATA_BOOST_TICKS =
            SynchedEntityData.defineId(MeteorMountEntity.class, EntityDataSerializers.INT);

    // =========================================================
    //  КОНСТАНТЫ
    // =========================================================
    private static final double FLY_SPEED         = 0.6;
    private static final double VERTICAL_SPEED    = 0.4;
    private static final double SPRINT_MULTIPLIER = 1.8;
    private static final double MAX_SPEED         = 1.5;
    private static final double FRICTION          = 0.85;

    /** Множитель скорости при активном бусте (+30%) */
    private static final double BOOST_MULTIPLIER  = 1.3;
    /** Множитель максимальной скорости при бусте */
    private static final double BOOST_MAX_SPEED   = MAX_SPEED * BOOST_MULTIPLIER;
    /** Длительность буста в тиках (15 секунд × 20 тиков) */
    private static final int    BOOST_DURATION_TICKS = 15 * 20; // 300

    /** Как часто (тики) клиент шлёт позицию во время полёта */
    private static final int SYNC_INTERVAL_TICKS      = 3;
    /** Учащённая синхронизация после спешивания */
    private static final int FAST_SYNC_INTERVAL_TICKS = 1;

    /** Сколько тиков клиент предсказывает падение и игнорирует lerpTo */
    private static final int CLIENT_FALL_SYNC_DURATION = 40;
    /** Сколько тиков сервер принимает пакеты от lastRider после спешивания */
    private static final int GRACE_PERIOD_TICKS = 35;

    /** Скорость поворота модели (градусов/тик) */
    private static final float  ROTATION_SPEED             = 10f;
    /** Ниже этого квадрата скорости не вращаемся по вектору движения */
    private static final double MIN_SPEED_FOR_ROTATION_SQ  = 0.0025;

    // =========================================================
    //  ИНТЕРПОЛЯЦИЯ (только клиент)
    // =========================================================
    private int    lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private double lerpYRot, lerpXRot;

    // =========================================================
    //  ОБЩЕЕ СОСТОЯНИЕ
    // =========================================================
    /** UUID последнего/текущего наездника */
    private UUID lastRiderUUID;

    // =========================================================
    //  КЛИЕНТСКОЕ СОСТОЯНИЕ
    // =========================================================
    private boolean wasLocalPlayerControllingLastTick = false;
    private int     syncTimer                         = 0;
    private boolean dismountPacketSent                = false;
    /** Сколько тиков клиент ещё предсказывает падение и шлёт isFalling-пакеты */
    private int     clientFallSyncTicksRemaining      = 0;

    // =========================================================
    //  СЕРВЕРНОЕ СОСТОЯНИЕ
    // =========================================================
    /** Сколько тиков сервер принимает пакеты от lastRider */
    private int     serverGraceTicks            = 0;
    /** Сервер ждёт isFalling-пакеты (физику не считает сам) */
    private boolean serverWaitingForClientFall  = false;

    // =========================================================
    //  КОНСТРУКТОР
    // =========================================================
    public MeteorMountEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    // =========================================================
    //  SYNCHED DATA
    // =========================================================
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HAS_RIDER,   false);
        builder.define(DATA_BOOSTED,     false);
        builder.define(DATA_BOOST_TICKS, 0);
    }

    public boolean hasRider() {
        return this.entityData.get(DATA_HAS_RIDER);
    }

    private void setHasRider(boolean value) {
        this.entityData.set(DATA_HAS_RIDER, value);
    }

    /** Активен ли бует прямо сейчас (читается на клиенте и сервере) */
    public boolean isBoosted() {
        return this.entityData.get(DATA_BOOSTED);
    }

    /** Сколько тиков осталось до конца буста (0 = неактивен) */
    public int getBoostTicksRemaining() {
        return this.entityData.get(DATA_BOOST_TICKS);
    }

    // Вызывается ТОЛЬКО на сервере
    private void setBoosted(boolean value) {
        this.entityData.set(DATA_BOOSTED, value);
    }

    // Вызывается ТОЛЬКО на сервере
    private void setBoostTicksRemaining(int ticks) {
        this.entityData.set(DATA_BOOST_TICKS, ticks);
    }

    // =========================================================
    //  СОХРАНЕНИЕ (буст не сохраняем — он временный)
    // =========================================================
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    // =========================================================
    //  ФИЗИЧЕСКИЕ СВОЙСТВА
    // =========================================================
    @Override public boolean canBeCollidedWith()      { return true; }
    @Override public boolean canCollideWith(Entity e) { return true; }
    @Override public boolean isPushable()             { return true; }
    @Override public boolean isPickable()             { return true; }
    @Override public boolean isOnFire()               { return false; }
    @Override public boolean fireImmune()             { return true; }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected MovementEmission getMovementEmission() { return MovementEmission.NONE; }

    @Override public boolean shouldRiderSit()          { return true; }
    @Override public boolean canAddPassenger(Entity p) { return this.getPassengers().isEmpty(); }

    // =========================================================
    //  УРОН / ВЗАИМОДЕЙСТВИЕ
    // =========================================================
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (!this.level().isClientSide && !this.isRemoved()) {
            boolean creative = source.getEntity() instanceof Player p && p.isCreative();
            if (!creative) this.spawnAtLocation(ModItems.METEOR_MOUNT_ITEM.get());
            this.discard();
        }
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        Level level = this.level();

        // ── Посадка (сервер, метеор пустой, игрок не держит meteor_core) ────────
        if (!level.isClientSide && this.getPassengers().isEmpty()) {
            ItemStack heldItem = player.getItemInHand(hand);

            // Если игрок держит meteor_core — не садимся, уходим в ветку буста ниже
            if (!heldItem.is(ModItems.METEOR_CORE.get())) {
                player.startRiding(this);
                level.playSound(null, this.blockPosition(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0f, 0.8f);
                return InteractionResult.SUCCESS;
            }
        }

        // ── Активация буста (сервер, игрок — пассажир, держит meteor_core) ──────
        if (!level.isClientSide
                && this.hasPassenger(player)
                && hand == InteractionHand.MAIN_HAND) {

            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.is(ModItems.METEOR_CORE.get())) {
                activateBoost(player);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * Активирует (или продлевает) бует на сервере.
     * Вызывается только на серверной стороне.
     */
    private void activateBoost(Player player) {
        int currentTicks = getBoostTicksRemaining();

        if (currentTicks > 0) {
            // Бует уже активен — сообщаем игроку, не тратим предмет повторно
            player.displayClientMessage(
                    Component.translatable("entity.lostdreams.meteor_mount.boost_already_active"),
                    true // actionbar
            );
            return;
        }

        // Запускаем бует
        setBoosted(true);
        setBoostTicksRemaining(BOOST_DURATION_TICKS);

        // Звук активации
        this.level().playSound(null, this.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 0.6f, 1.4f);

        // Сообщение игроку
        player.displayClientMessage(
                Component.translatable("entity.lostdreams.meteor_mount.boost_activated"),
                true
        );

        // Расход предмета (убираем 1 штуку из стака)
        // Если не хотите тратить предмет — удалите эти 3 строки
        if (!player.isCreative()) {
            player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
        }
    }

    // =========================================================
    //  ПАССАЖИР
    // =========================================================
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() { return getRider(); }

    @Nullable
    private Player getRider() {
        if (this.getPassengers().isEmpty()) return null;
        Entity first = this.getPassengers().get(0);
        return first instanceof Player p ? p : null;
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction callback) {
        if (this.hasPassenger(passenger)) {
            double offsetY = this.getBbHeight() * 0.5;
            callback.accept(passenger, this.getX(), this.getY() + offsetY, this.getZ());
        }
    }

    // =========================================================
    //  API ДЛЯ HANDLER-А
    // =========================================================
    @Nullable
    public UUID getLastRiderUUID() { return lastRiderUUID; }

    public boolean isInDismountGracePeriod() { return serverGraceTicks > 0; }

    public void onClientDismountSync(double velX, double velY, double velZ) {
        this.setDeltaMovement(velX, velY, velZ);
        this.hasImpulse = true;
        this.serverWaitingForClientFall = true;
    }

    public void onClientFallSync() {
        // Грейс-период уже тикает — метод оставлен для явности вызова из Handler-а.
    }

    // =========================================================
    //  ИНТЕРПОЛЯЦИЯ (lerpTo)
    // =========================================================
    @Override
    public void lerpTo(double x, double y, double z,
                       float yRot, float xRot, int steps) {
        if (this.isControlledByLocalInstance()) return;
        if (this.clientFallSyncTicksRemaining > 0) return;

        this.lerpX     = x;
        this.lerpY     = y;
        this.lerpZ     = z;
        this.lerpYRot  = yRot;
        this.lerpXRot  = xRot;
        this.lerpSteps = steps;
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        if (this.clientFallSyncTicksRemaining > 0) return;
        this.setDeltaMovement(x, y, z);
        this.hasImpulse = true;
    }

    private void tickLerp() {
        if (this.lerpSteps > 0) {
            double t = 1.0 / this.lerpSteps;
            this.setPos(
                    Mth.lerp(t, this.getX(), this.lerpX),
                    Mth.lerp(t, this.getY(), this.lerpY),
                    Mth.lerp(t, this.getZ(), this.lerpZ)
            );
            this.setYRot((float) Mth.lerp(t, this.getYRot(), this.lerpYRot));
            this.setXRot((float) Mth.lerp(t, this.getXRot(), this.lerpXRot));
            this.lerpSteps--;
        }
    }

    // =========================================================
    //  ГЛАВНЫЙ ТИК
    // =========================================================
    @Override
    public void tick() {
        super.tick();

        Player rider     = getRider();
        boolean isRidden = rider != null;

        if (isRidden) {
            this.lastRiderUUID = rider.getUUID();
        }

        if (!this.level().isClientSide) {
            // --- СЕРВЕР ---
            if (this.hasRider() && !isRidden) {
                this.serverGraceTicks           = GRACE_PERIOD_TICKS;
                this.serverWaitingForClientFall = false;
            }
            setHasRider(isRidden);
            tickBoostServer(); // тикаем бует ДО tickServerSide
            tickServerSide(isRidden);
        } else {
            // --- КЛИЕНТ ---
            tickClientSide(rider, isRidden);
        }
    }

    // =========================================================
    //  ТИК БУСТА (только сервер)
    // =========================================================
    /**
     * Уменьшает счётчик буста каждый тик и сбрасывает флаг когда время вышло.
     * Вызывается только на сервере — SynchedEntityData сама доставит изменения клиенту.
     */
    private void tickBoostServer() {
        int ticks = getBoostTicksRemaining();
        if (ticks <= 0) return;

        ticks--;
        setBoostTicksRemaining(ticks);

        if (ticks == 0) {
            setBoosted(false);
            // Уведомляем пассажира об окончании буста
            Player rider = getRider();
            if (rider != null) {
                rider.displayClientMessage(
                        Component.translatable("entity.lostdreams.meteor_mount.boost_expired"),
                        true
                );
            }
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 0.5f, 1.2f);
        }
    }

    // =========================================================
    //  КЛИЕНТСКИЙ ТИК
    // =========================================================
    private void tickClientSide(Player rider, boolean isRiddenNow) {
        tickClientParticles();

        boolean isLocalPlayerRiding = isControlledByLocalInstance() && isRiddenNow;
        boolean justDismounted      = wasLocalPlayerControllingLastTick && !isLocalPlayerRiding;

        // ── Спешивание ──────────────────────────────────────────────────────────
        if (justDismounted && !dismountPacketSent) {
            sendSyncPacket(true, false);
            dismountPacketSent               = true;
            this.lerpSteps                  = 0;
            this.clientFallSyncTicksRemaining = CLIENT_FALL_SYNC_DURATION;
            this.syncTimer                  = 0;
        }

        if (isLocalPlayerRiding) {
            dismountPacketSent = false;
        }

        // ── Локальное управление ────────────────────────────────────────────────
        if (isLocalPlayerRiding) {
            syncTimer++;
            if (syncTimer >= SYNC_INTERVAL_TICKS) {
                syncTimer = 0;
                sendSyncPacket(false, false);
            }
            tickRiding(rider);

        } else if (clientFallSyncTicksRemaining > 0) {
            // ── Предсказание падения ─────────────────────────────────────────────
            clientFallSyncTicksRemaining--;

            syncTimer++;
            if (syncTimer >= FAST_SYNC_INTERVAL_TICKS) {
                syncTimer = 0;
                sendSyncPacket(false, true);
            }
            applyFallPhysics();

        } else {
            // ── Зритель ──────────────────────────────────────────────────────────
            tickLerp();
        }

        wasLocalPlayerControllingLastTick = isLocalPlayerRiding;
    }

    // =========================================================
    //  СЕРВЕРНЫЙ ТИК
    // =========================================================
    private void tickServerSide(boolean isRiddenNow) {
        if (isRiddenNow) {
            spawnTrailParticles();
            return;
        }

        if (serverGraceTicks > 0) {
            serverGraceTicks--;
            if (serverWaitingForClientFall) return;
            return;
        }

        serverWaitingForClientFall = false;
        applyFallPhysics();
    }

    // =========================================================
    //  ФИЗИКА ПАДЕНИЯ
    // =========================================================
    private void applyFallPhysics() {
        if (!this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
        } else {
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(vel.x * 0.5, 0, vel.z * 0.5);
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        updateRotationToVelocity(null);
    }

    // =========================================================
    //  ФИЗИКА ПОЛЁТА (клиент, управляемый игроком)
    // =========================================================
    private void tickRiding(Player rider) {
        Vec3 vel = this.getDeltaMovement();

        Vec3 desired = calculateDesiredVelocity(rider);

        if (desired.lengthSqr() > 1e-6) {
            vel = vel.lerp(desired, 0.25);
        } else {
            vel = vel.scale(FRICTION);
        }

        // Лимит скорости зависит от состояния буста
        double currentMaxSpeed = isBoosted() ? BOOST_MAX_SPEED : MAX_SPEED;
        if (vel.length() > currentMaxSpeed) {
            vel = vel.normalize().scale(currentMaxSpeed);
        }

        this.setDeltaMovement(vel);
        this.move(MoverType.SELF, this.getDeltaMovement());
        updateRotationToVelocity(rider);
    }

    // =========================================================
    //  ВРАЩЕНИЕ МОДЕЛИ
    // =========================================================
    private void updateRotationToVelocity(@Nullable Player rider) {
        Vec3 vel = this.getDeltaMovement();
        double horizSq = vel.x * vel.x + vel.z * vel.z;

        if (vel.lengthSqr() > MIN_SPEED_FOR_ROTATION_SQ) {
            float targetYaw   = (float) (Mth.atan2(-vel.x, vel.z) * Mth.RAD_TO_DEG);
            float targetPitch = (float) (-Mth.atan2(vel.y, Math.sqrt(horizSq)) * Mth.RAD_TO_DEG);
            this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw,   ROTATION_SPEED));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, ROTATION_SPEED));
        } else if (rider != null) {
            this.setYRot(Mth.approachDegrees(this.getYRot(), rider.getYRot(),         ROTATION_SPEED));
            this.setXRot(Mth.approachDegrees(this.getXRot(), rider.getXRot() * 0.5f, ROTATION_SPEED));
        }
    }

    // =========================================================
    //  ЖЕЛАЕМАЯ СКОРОСТЬ
    // =========================================================
    private Vec3 calculateDesiredVelocity(Player rider) {
        float yRot = rider.getYRot();
        float xRot = rider.getXRot();
        Vec3 look  = calculateLookDirection(yRot, xRot);

        boolean forward  = rider.zza > 0;
        boolean backward = rider.zza < 0;
        boolean left     = rider.xxa > 0;
        boolean right    = rider.xxa < 0;
        boolean up       = !rider.onGround() && rider.getDeltaMovement().y > 0.05;
        boolean down     = rider.isShiftKeyDown() && !rider.isSuppressingBounce();
        boolean sprint   = rider.isSprinting();

        // Применяем множитель буста к базовым скоростям
        double boostFactor = isBoosted() ? BOOST_MULTIPLIER : 1.0;
        double flySpeed    = FLY_SPEED * boostFactor;
        double vertSpeed   = VERTICAL_SPEED * boostFactor;

        Vec3 desired = Vec3.ZERO;
        if (forward)  desired = desired.add(look.scale(sprint ? flySpeed * SPRINT_MULTIPLIER : flySpeed));
        if (backward) desired = desired.add(look.scale(-flySpeed * 0.5));
        if (left || right) {
            Vec3 strafe = calculateStrafeDirection(yRot);
            desired = desired.add(strafe.scale((left ? 1.0 : -1.0) * flySpeed * 0.7));
        }
        if (up)   desired = desired.add(0,  vertSpeed, 0);
        if (down) desired = desired.add(0, -vertSpeed, 0);

        return desired;
    }

    // =========================================================
    //  ОТПРАВКА ПАКЕТОВ
    // =========================================================
    private void sendSyncPacket(boolean isDismounting, boolean isFalling) {
        Vec3 vel = this.getDeltaMovement();
        PacketDistributor.sendToServer(new MeteorSyncPacket(
                this.getId(),
                this.getX(), this.getY(), this.getZ(),
                vel.x, vel.y, vel.z,
                this.getYRot(), this.getXRot(),
                isDismounting,
                isFalling
        ));
    }

    // =========================================================
    //  ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =========================================================
    private Vec3 calculateLookDirection(float yRot, float xRot) {
        float yRad = yRot * Mth.DEG_TO_RAD;
        float xRad = xRot * Mth.DEG_TO_RAD;
        return new Vec3(
                -Mth.sin(yRad) * Mth.cos(xRad),
                -Mth.sin(xRad),
                Mth.cos(yRad) * Mth.cos(xRad)
        ).normalize();
    }

    private Vec3 calculateStrafeDirection(float yRot) {
        float yRad = (yRot - 90.0f) * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yRad), 0, Mth.cos(yRad)).normalize();
    }

    private void spawnTrailParticles() { /* серверные частицы */ }
    private void tickClientParticles() { /* клиентские частицы */ }
}
