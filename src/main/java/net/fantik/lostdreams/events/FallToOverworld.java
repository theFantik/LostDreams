package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.NullZoneDimension;
import net.fantik.lostdreams.world.dimension.SkyBlockDimension;
import net.fantik.lostdreams.world.dimension.SurrealAsteroidsDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class FallToOverworld {

    private static final double FALL_THRESHOLD = -63.0;
    private static final int SLOW_FALLING_DURATION_TICKS = 3 * 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!NullZoneDimension.isNullZone(player.level()) && !SkyBlockDimension.isSkyBlock(player.level()) && !SurrealAsteroidsDimension.isSurrealAsteroids(player.level())) return;
        if (player.getY() >= FALL_THRESHOLD) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        // Всегда возвращаем в оверворлд — respawn point только там имеет смысл
        BlockPos spawnPos = player.getRespawnPosition();
        ServerLevel spawnLevel = server.getLevel(player.getRespawnDimension());

        // Если respawn point не в оверворлде (например в SkyBlock) — используем мировой спавн
        if (spawnPos == null || spawnLevel == null ||
                !spawnLevel.dimension().equals(Level.OVERWORLD)) {
            spawnPos = overworld.getSharedSpawnPos();
            spawnLevel = overworld;
        }

        BlockPos safePos = findSafePos(spawnLevel, spawnPos);

        LostDreams.LOGGER.info(
                "Player {} fell below -63, returning to spawn at {}",
                player.getName().getString(), safePos
        );

        player.addEffect(new MobEffectInstance(
                MobEffects.SLOW_FALLING,
                SLOW_FALLING_DURATION_TICKS,
                0, false, false, true
        ));

        player.setDeltaMovement(
                player.getDeltaMovement().x,
                0.0,
                player.getDeltaMovement().z
        );

        player.teleportTo(
                spawnLevel,
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );

        player.resetFallDistance();
    }

    private static BlockPos findSafePos(ServerLevel level, BlockPos reference) {
        int x = reference.getX();
        int z = reference.getZ();

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos candidate = new BlockPos(x, surfaceY, z);
        if (isSafe(level, candidate)) return candidate;

        for (int r = 1; r <= 5; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int sy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz);
                    BlockPos pos = new BlockPos(x + dx, sy, z + dz);
                    if (isSafe(level, pos)) return pos;
                }
            }
        }

        return candidate;
    }

    private static boolean isSafe(ServerLevel level, BlockPos feetPos) {
        boolean groundSolid = !level.getBlockState(feetPos.below()).isAir();
        boolean feetClear = level.getBlockState(feetPos).isAir();
        boolean headClear = level.getBlockState(feetPos.above()).isAir();
        return groundSolid && feetClear && headClear;
    }
}