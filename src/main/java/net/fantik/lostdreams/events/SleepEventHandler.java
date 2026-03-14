package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.item.ModItems;
import net.fantik.lostdreams.world.dimension.NullZoneDimension;
import net.fantik.lostdreams.world.dimension.SkyBlockDimension;
import net.fantik.lostdreams.world.dimension.SkyBlockIslandGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SleepEventHandler {

    private static final Set<UUID> TELEPORTING = new HashSet<>();
    private static final Set<UUID> CURSING = new HashSet<>();

    private static final int SLOW_FALLING_DURATION_TICKS = 20 * 20;
    private static final String NBT_KEY = "lostdreams_cursed_bed";
    private static final String NBT_KEY_NULL = "lostdreams_null_cursed_bed";

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // Блокировка кровати в Null Zone
        if (NullZoneDimension.isNullZone(event.getEntity().level())) {
            BlockPos pos = event.getPos();
            if (event.getEntity().level().getBlockState(pos).getBlock() instanceof BedBlock) {
                if (event.getEntity() instanceof ServerPlayer player) {
                    player.displayClientMessage(
                            Component.literal("§7Sleeping within a dream is troublesome..."), true
                    );
                }
                event.setCanceled(true);
                return;
            }
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (NullZoneDimension.isNullZone(player.level())) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        // --- NULL PILLOW -> SkyBlock (работает из любого измерения кроме Null Zone) ---
        if (player.getMainHandItem().is(ModItems.NULL_PILLOW.get())) {
            BlockPos clickedPos = event.getPos();
            BlockState bedState = player.level().getBlockState(clickedPos);
            if (!(bedState.getBlock() instanceof BedBlock)) return;

            if (bedState.getValue(BedBlock.PART) == BedPart.FOOT) {
                event.setCanceled(true);
                Direction facing = bedState.getValue(BedBlock.FACING);
                BlockPos headPos = clickedPos.relative(facing);
                BlockPos existing = getNullCursedBed(player);
                if (existing != null && existing.equals(headPos)) {
                    player.displayClientMessage(Component.literal("§cThis bed is already cursed!"), true);
                }
                return;
            }

            event.setCanceled(true);

            if (CURSING.contains(player.getUUID())) return;
            CURSING.add(player.getUUID());
            player.getServer().execute(() -> CURSING.remove(player.getUUID()));

            BlockPos existing = getNullCursedBed(player);
            if (existing != null && existing.equals(clickedPos)) {
                player.displayClientMessage(Component.literal("§cThis bed is already cursed!"), true);
                return;
            }

            setNullCursedBed(player, clickedPos);
            // Снимаем обычное проклятие
            player.getPersistentData().remove(NBT_KEY);

            if (!player.isCreative()) {
                player.getMainHandItem().shrink(1);
            }

            spawnTeleportParticles((ServerLevel) player.level(), clickedPos);
            return;
        }

        // --- PILLOW -> Null Zone (не работает из SkyBlock) ---
        if (!player.getMainHandItem().is(ModItems.PILLOW.get())) return;
        if (SkyBlockDimension.isSkyBlock(player.level())) return;

        BlockPos clickedPos = event.getPos();
        BlockState bedState = player.level().getBlockState(clickedPos);
        if (!(bedState.getBlock() instanceof BedBlock)) return;

        if (bedState.getValue(BedBlock.PART) == BedPart.FOOT) {
            event.setCanceled(true);
            Direction facing = bedState.getValue(BedBlock.FACING);
            BlockPos headPos = clickedPos.relative(facing);
            BlockPos existing = getCursedBed(player);
            if (existing != null && existing.equals(headPos)) {
                player.displayClientMessage(Component.literal("§cThis bed is already cursed!"), true);
            }
            return;
        }

        event.setCanceled(true);

        if (CURSING.contains(player.getUUID())) return;
        CURSING.add(player.getUUID());
        player.getServer().execute(() -> CURSING.remove(player.getUUID()));

        BlockPos existing = getCursedBed(player);
        if (existing != null && existing.equals(clickedPos)) {
            player.displayClientMessage(Component.literal("§cThis bed is already cursed!"), true);
            return;
        }

        setCursedBed(player, clickedPos);
        // Снимаем null проклятие
        player.getPersistentData().remove(NBT_KEY_NULL);

        if (!player.isCreative()) {
            player.getMainHandItem().shrink(1);
        }

        spawnTeleportParticles((ServerLevel) player.level(), clickedPos);
        LostDreams.LOGGER.info("{} cursed bed at {}", player.getName().getString(), clickedPos);
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // В Null Zone — ничего не делаем
        if (NullZoneDimension.isNullZone(player.level())) return;

        // В SkyBlock — просто пропускаем ночь, никакой телепортации
        if (SkyBlockDimension.isSkyBlock(player.level())) return;

        if (TELEPORTING.contains(player.getUUID())) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        // --- Проверка null_pillow (SkyBlock) ---
        BlockPos nullCursedBed = getNullCursedBed(player);
        if (nullCursedBed != null && isPlayerSleepingAt(player, nullCursedBed)) {
            TELEPORTING.add(player.getUUID());
            try {
                player.stopSleeping();
                teleportToSkyBlock(player, server);
            } finally {
                TELEPORTING.remove(player.getUUID());
            }
            return;
        }

        // --- Проверка pillow (Null Zone) ---
        ServerLevel nullZone = server.getLevel(NullZoneDimension.NULL_ZONE_KEY);
        if (nullZone == null) {
            LostDreams.LOGGER.warn("Null Zone dimension not found!");
            return;
        }

        boolean shouldTeleport;
        BlockPos cursedBed = getCursedBed(player);

        if (cursedBed != null && isPlayerSleepingAt(player, cursedBed)) {
            shouldTeleport = true;
        } else {
            shouldTeleport = player.getRandom().nextFloat() < 0.4f;
        }

        if (!shouldTeleport) return;

        TELEPORTING.add(player.getUUID());
        try {
            player.stopSleeping();
            teleportToNullZone(player, nullZone);
        } finally {
            TELEPORTING.remove(player.getUUID());
        }
    }

    private static boolean isPlayerSleepingAt(ServerPlayer player, BlockPos cursedBed) {
        return player.getSleepingPos().map(pos -> {
            if (pos.equals(cursedBed)) return true;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (pos.relative(dir).equals(cursedBed)) return true;
            }
            return false;
        }).orElse(false);
    }

    // --- NBT: pillow (Null Zone) ---
    private static void setCursedBed(ServerPlayer player, BlockPos pos) {
        CompoundTag data = player.getPersistentData();
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        data.put(NBT_KEY, tag);
    }

    private static BlockPos getCursedBed(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(NBT_KEY)) return null;
        CompoundTag tag = data.getCompound(NBT_KEY);
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    // --- NBT: null_pillow (SkyBlock) ---
    private static void setNullCursedBed(ServerPlayer player, BlockPos pos) {
        CompoundTag data = player.getPersistentData();
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        data.put(NBT_KEY_NULL, tag);
    }

    private static BlockPos getNullCursedBed(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(NBT_KEY_NULL)) return null;
        CompoundTag tag = data.getCompound(NBT_KEY_NULL);
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    // --- Телепортация ---
    private static void teleportToNullZone(ServerPlayer player, ServerLevel nullZone) {
        BlockPos safePos = findSafeSpawnPos(nullZone, player.blockPosition());
        LostDreams.LOGGER.info("Teleporting {} to Null Zone at {}", player.getName().getString(), safePos);
        player.teleportTo(nullZone,
                safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
                SLOW_FALLING_DURATION_TICKS, 0, false, true, true));
    }

    private static void teleportToSkyBlock(ServerPlayer player, MinecraftServer server) {
        ServerLevel skyblock = server.getLevel(SkyBlockDimension.SKYBLOCK_KEY);
        if (skyblock == null) {
            LostDreams.LOGGER.warn("SkyBlock dimension not found!");
            return;
        }

        BlockPos islandOrigin = SkyBlockIslandGenerator.getIslandOrigin(player);
        SkyBlockIslandGenerator.generateIsland(skyblock, islandOrigin);

        player.teleportTo(skyblock,
                islandOrigin.getX() + 0.5,
                islandOrigin.getY() + 1,
                islandOrigin.getZ() + 0.5,
                player.getYRot(), player.getXRot());

        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
                SLOW_FALLING_DURATION_TICKS, 0, false, true, true));
    }

    // --- Частицы ---
    private static void spawnTeleportParticles(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.6;
        double cz = pos.getZ() + 0.5;

        for (int i = 0; i < 40; i++) {
            double angle = i * (Math.PI * 2 / 20);
            double radius = 0.5 + (i / 40.0) * 0.4;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            double dy = (i / 40.0) * 1.2;
            level.sendParticles(ParticleTypes.PORTAL, cx + dx, cy + dy, cz + dz, 1, 0, 0, 0, 0.05);
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy + 0.5, cz, 8, 0.3, 0.2, 0.3, 0.01);
        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.5, cz, 20, 0.4, 0.4, 0.4, 0.1);
    }

    // --- Поиск безопасной позиции (Null Zone) ---
    private static BlockPos findSafeSpawnPos(ServerLevel level, BlockPos referencePos) {
        int x = referencePos.getX();
        int z = referencePos.getZ();

        for (int y = 110; y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isSafeForPlayer(level, pos)) return pos;
        }

        return new BlockPos(x, 64, z);
    }

    private static boolean isSafeForPlayer(ServerLevel level, BlockPos feetPos) {
        boolean groundSolid = !level.getBlockState(feetPos.below()).isAir();
        boolean feetClear = level.getBlockState(feetPos).isAir();
        boolean headClear = level.getBlockState(feetPos.above()).isAir();
        return groundSolid && feetClear && headClear;
    }
}