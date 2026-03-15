package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.SkyBlockDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class SkyBlockSpawnHandler {

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(EntityType.SHEEP, SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) ->
                        canSpawnPeaceful(level, pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(EntityType.COW, SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) ->
                        canSpawnPeaceful(level, pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(EntityType.CHICKEN, SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) ->
                        canSpawnPeaceful(level, pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean canSpawnPeaceful(ServerLevelAccessor level, BlockPos pos) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (!SkyBlockDimension.isSkyBlock(serverLevel)) return false;
        }

        // Блок под ногами должен быть твёрдым
        if (level.getBlockState(pos.below()).isAir()) return false;

        // Только днём
        long time = level.dayTime() % 24000;
        return time < 12000;
    }
}