package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.NullZoneDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

import java.util.Random;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SleepEventHandler {

    private static final Random RANDOM = new Random();
    private static final int SLOW_FALLING_DURATION_TICKS = 20 * 20;

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (NullZoneDimension.isNullZone(player.level())) return;
        if (RANDOM.nextFloat() >= 0.45f) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel nullZone = server.getLevel(NullZoneDimension.NULL_ZONE_KEY);
        if (nullZone == null) {
            LostDreams.LOGGER.warn("Null Zone dimension not found! Check your dimension JSON.");
            return;
        }

        // Фикс "the bed is occupied": сбрасываем состояние сна до телепортации
        // stopSleeping() помечает кровать как свободную на сервере
        player.stopSleeping();

        BlockPos safePos = findSafeSpawnPos(nullZone, player.blockPosition());

        LostDreams.LOGGER.info("Teleporting {} to Null Zone at {}", player.getName().getString(), safePos);

        player.teleportTo(
                nullZone,
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );

        player.addEffect(new MobEffectInstance(
                MobEffects.SLOW_FALLING,
                SLOW_FALLING_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));
    }

    private static BlockPos findSafeSpawnPos(ServerLevel level, BlockPos referencePos) {
        int x = referencePos.getX();
        int z = referencePos.getZ();

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos candidate = new BlockPos(x, surfaceY, z);
        if (isSafeForPlayer(level, candidate)) return candidate;

        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
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