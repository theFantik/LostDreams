package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.item.ModItems;
import net.fantik.lostdreams.world.dimension.NullZoneDimension;
import net.fantik.lostdreams.world.dimension.SkyBlockDimension;
import net.fantik.lostdreams.world.dimension.SkyBlockIslandGenerator;
import net.fantik.lostdreams.world.dimension.SurrealAsteroidsDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    private static final String CURSE_TAG = "lostdreams_bed_curse";
    private static final String CURSE_TYPE_NULL = "null_zone";
    private static final String CURSE_TYPE_LUCID = "lucid";

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (NullZoneDimension.isNullZone(event.getEntity().level()) || SurrealAsteroidsDimension.isSurrealAsteroids(event.getEntity().level())) {
            BlockPos pos = event.getPos();
            if (event.getEntity().level().getBlockState(pos).getBlock() instanceof BedBlock) {
                if (event.getEntity() instanceof ServerPlayer player) {
                    player.displayClientMessage(
                            Component.translatable("message.lostdreams.sleep_warning").withStyle(style -> style.withColor(0xC82323)), true
                    );
                }
                event.setCanceled(true);
                return;
            }
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        boolean isPillow = player.getMainHandItem().is(ModItems.PILLOW.get());
        boolean isNullPillow = player.getMainHandItem().is(ModItems.NULL_PILLOW.get());

        if (!isPillow && !isNullPillow) return;

        BlockPos clickedPos = event.getPos();
        BlockState bedState = player.level().getBlockState(clickedPos);
        if (!(bedState.getBlock() instanceof BedBlock)) return;

        event.setCanceled(true);

        BlockPos headPos = clickedPos;
        BlockPos footPos = clickedPos;
        Direction facing = bedState.getValue(BedBlock.FACING);

        if (bedState.getValue(BedBlock.PART) == BedPart.FOOT) {
            headPos = clickedPos.relative(facing);
        } else {
            footPos = clickedPos.relative(facing.getOpposite());
        }

        BlockEntity headBE = player.level().getBlockEntity(headPos);
        BlockEntity footBE = player.level().getBlockEntity(footPos);

        if ((headBE != null && headBE.getPersistentData().contains(CURSE_TAG)) ||
                (footBE != null && footBE.getPersistentData().contains(CURSE_TAG))) {
            player.displayClientMessage(Component.literal("§cThis bed is already cursed!"), true);
            return;
        }

        if (CURSING.contains(player.getUUID())) return;
        CURSING.add(player.getUUID());
        player.getServer().execute(() -> CURSING.remove(player.getUUID()));

        String curseType = isNullPillow ? CURSE_TYPE_LUCID : CURSE_TYPE_NULL;

        if (isPillow && SkyBlockDimension.isSkyBlock(player.level())) {
            return;
        }

        if (headBE != null) headBE.getPersistentData().putString(CURSE_TAG, curseType);
        if (footBE != null) footBE.getPersistentData().putString(CURSE_TAG, curseType);

        if (!player.isCreative()) {
            player.getMainHandItem().shrink(1);
        }

        if (isNullPillow) {
            spawnAsteroidsParticles((ServerLevel) player.level(), clickedPos);
        } else {
            spawnNullZoneParticles((ServerLevel) player.level(), clickedPos);
            LostDreams.LOGGER.info("{} cursed bed at {}", player.getName().getString(), clickedPos);
        }
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (NullZoneDimension.isNullZone(player.level())) return;
        if (SkyBlockDimension.isSkyBlock(player.level())) return;
        if (SurrealAsteroidsDimension.isSurrealAsteroids(player.level())) return;

        if (TELEPORTING.contains(player.getUUID())) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        player.getSleepingPos().ifPresent(sleepPos -> {
            BlockEntity be = player.level().getBlockEntity(sleepPos);
            String curseType = "";

            if (be != null && be.getPersistentData().contains(CURSE_TAG)) {
                curseType = be.getPersistentData().getString(CURSE_TAG);
            }

            TELEPORTING.add(player.getUUID());
            try {
                if (CURSE_TYPE_LUCID.equals(curseType)) {
                    player.stopSleeping();
                    float roll = player.getRandom().nextFloat();

                    // ИСПРАВЛЕНО: Отложенная телепортация
                    server.execute(() -> {
                        if (roll < 0.30f) {
                            teleportToSkyBlock(player, server);
                        } else if (roll < 0.60f) {
                            teleportToSurrealAsteroids(player, server);
                        }
                    });
                    return;
                }

                ServerLevel nullZone = server.getLevel(NullZoneDimension.NULL_ZONE_KEY);
                if (nullZone == null) {
                    LostDreams.LOGGER.warn("Null Zone dimension not found!");
                    return;
                }

                boolean shouldTeleportToNullZone = false;

                if (CURSE_TYPE_NULL.equals(curseType)) {
                    shouldTeleportToNullZone = true;
                } else {
                    if (player.getRandom().nextFloat() < 0.4f) {
                        shouldTeleportToNullZone = true;
                    }
                }

                if (shouldTeleportToNullZone) {
                    player.stopSleeping();
                    // ИСПРАВЛЕНО: Отложенная телепортация
                    server.execute(() -> {
                        teleportToNullZone(player, nullZone);
                    });
                }

            } finally {
                TELEPORTING.remove(player.getUUID());
            }
        });
    }

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
        skyblock.getChunk(islandOrigin.getX() >> 4, islandOrigin.getZ() >> 4);
        SkyBlockIslandGenerator.generateIsland(skyblock, islandOrigin, true);

        player.teleportTo(skyblock,
                islandOrigin.getX() + 0.5,
                islandOrigin.getY() + 1,
                islandOrigin.getZ() + 0.5,
                player.getYRot(), player.getXRot());

        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
                SLOW_FALLING_DURATION_TICKS, 0, false, true, true));
    }

    private static void teleportToSurrealAsteroids(ServerPlayer player, MinecraftServer server) {
        ServerLevel asteroids = server.getLevel(SurrealAsteroidsDimension.SURREAL_ASTEROIDS_KEY);
        if (asteroids == null) {
            LostDreams.LOGGER.warn("Surreal Asteroids dimension not found!");
            return;
        }

        BlockPos spawnPos = findAsteroidSurface(asteroids, player.blockPosition());

        player.teleportTo(asteroids,
                spawnPos.getX() + 0.5,
                spawnPos.getY() + 1,
                spawnPos.getZ() + 0.5,
                player.getYRot(), player.getXRot());

        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
                SLOW_FALLING_DURATION_TICKS * 3, 0, false, true, true));
    }

    private static BlockPos findAsteroidSurface(ServerLevel level, BlockPos reference) {
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = reference.getX() + level.getRandom().nextInt(64) - 32;
            int z = reference.getZ() + level.getRandom().nextInt(64) - 32;
            for (int y = 150; y >= -32; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!level.getBlockState(pos).isAir() &&
                        level.getBlockState(pos.above()).isAir()) {
                    return pos;
                }
            }
        }
        return new BlockPos(reference.getX(), 80, reference.getZ());
    }

    private static void spawnNullZoneParticles(ServerLevel level, BlockPos pos) {
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

    private static void spawnAsteroidsParticles(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.6;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.ASH, cx, cy + 0.5, cz, 40, 0.5, 0.5, 0.5, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy + 0.5, cz, 15, 0.4, 0.2, 0.4, 0.03);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, cx, cy + 0.5, cz, 30, 0.6, 0.6, 0.6, 0.05);
        level.sendParticles(ParticleTypes.FLAME, cx, cy + 0.2, cz, 10, 0.3, 0.1, 0.3, 0.02);
    }

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