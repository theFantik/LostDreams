package net.fantik.lostdreams.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class PortalTeleporter {

    public static final String COOLDOWN_KEY = "portal_cooldown";
    private static final int COOLDOWN_TICKS = 80;

    // 32 блока — идеальный баланс: не лагает и достаточно далеко ищет
    private static final int SEARCH_RADIUS = 32;

    public static void teleport(ServerPlayer player, ServerLevel fromLevel, PortalDefinition def) {
        int cooldown = player.getPersistentData().getInt(COOLDOWN_KEY);
        if (cooldown > 0) return;

        // Определяем куда телепортировать
        ResourceKey<Level> targetKey;
        if (fromLevel.dimension().equals(def.getDestination())) {
            targetKey = Level.OVERWORLD;
        } else {
            targetKey = def.getDestination();
        }

        ServerLevel toLevel = fromLevel.getServer().getLevel(targetKey);
        if (toLevel == null || fromLevel == toLevel) return;

        BlockPos playerPos = player.blockPosition();

        // 1. Ищем существующий портал
        BlockPos destination = findExistingPortal(toLevel, playerPos, def);

        // 2. Если не нашли - создаем новый
        if (destination == null) {
            destination = createPortal(toLevel, playerPos, def);
        }

        if (destination == null) return;

        // Телепортируем прямо в блок портала
        player.teleportTo(toLevel,
                destination.getX() + 0.5,
                destination.getY(), // Убрал +1, чтобы не кидало на верхнюю рамку
                destination.getZ() + 0.5,
                player.getYRot(),
                player.getXRot());

        player.getPersistentData().putInt(COOLDOWN_KEY, COOLDOWN_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
    }

    public static void tickCooldown(ServerPlayer player) {
        int cooldown = player.getPersistentData().getInt(COOLDOWN_KEY);
        if (cooldown > 0) {
            player.getPersistentData().putInt(COOLDOWN_KEY, cooldown - 1);
        }
    }

    private static BlockPos findExistingPortal(ServerLevel level, BlockPos near, PortalDefinition def) {
        // Vanilla-метод: ищет от центра по спирали, очень быстро и не пропускает блоки
        Optional<BlockPos> closest = BlockPos.findClosestMatch(near, SEARCH_RADIUS, SEARCH_RADIUS, pos ->
                level.getBlockState(pos).is(def.getPortalBlock())
        );
        return closest.orElse(null);
    }

    private static BlockPos createPortal(ServerLevel level, BlockPos near, PortalDefinition def) {
        BlockPos target = findSafeSpot(level, near, def);

        // Если вообще нет места (мы в сплошном камне), строим прямо там, где стоим
        if (target == null) {
            target = near;
        }

        int w = def.getMinWidth();
        int h = def.getMinHeight();
        BlockState frame = def.getFrameBlock().defaultBlockState();
        BlockState portalState = def.getPortalBlock().defaultBlockState();

        // Пол и потолок
        for (int x = -1; x <= w; x++) {
            level.setBlockAndUpdate(target.offset(x, -1, 0), frame);
            level.setBlockAndUpdate(target.offset(x, h, 0), frame);
        }
        // Боковые стенки
        for (int y = 0; y < h; y++) {
            level.setBlockAndUpdate(target.offset(-1, y, 0), frame);
            level.setBlockAndUpdate(target.offset(w, y, 0), frame);
        }


        // Заполняем внутренность
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // ИЗМЕНЕНО: Вместо setBlockAndUpdate используем setBlock с флагом 18
                level.setBlock(target.offset(x, y, 0), portalState, 18);
            }
        }

        return target;
    }

    private static BlockPos findSafeSpot(ServerLevel level, BlockPos near, PortalDefinition def) {
        int w = def.getMinWidth();
        int h = def.getMinHeight();

        // Ищем сверху вниз, проверяем каждый блок (без прыжков)
        for (int dy = 16; dy >= -16; dy--) {
            for (int dx = -16; dx <= 16; dx++) {
                for (int dz = -16; dz <= 16; dz++) {
                    BlockPos checkPos = near.offset(dx, dy, dz);
                    if (isAreaClearForPortal(level, checkPos, w, h)) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    // Проверяем, влезет ли портал ЦЕЛИКОМ в эту точку
    private static boolean isAreaClearForPortal(ServerLevel level, BlockPos pos, int width, int height) {
        for (int x = -1; x <= width; x++) {
            for (int y = -1; y <= height; y++) {
                BlockPos currentPos = pos.offset(x, y, 0);
                BlockState state = level.getBlockState(currentPos);

                if (y == -1) {
                    // Под порталом должен быть прочный блок (не воздух и не вода)
                    if (state.isAir() || !state.getFluidState().isEmpty()) {
                        return false;
                    }
                } else {
                    // Тело портала и рамка должны заменять воздух или траву (replaceable)
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}