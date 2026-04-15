package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.custom.DreamGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

import java.util.List;

@EventBusSubscriber(modid = LostDreams.MOD_ID)
public class ModEvents {

    private static final String CURSE_TAG        = "lostdreams_bed_curse";
    private static final String CURSE_TYPE_NULL  = "null_zone";
    private static final String CURSE_TYPE_LUCID = "lucid";

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        // --- Игроки ---
        for (Player player : serverLevel.players()) {
            if (!player.isSleeping()) continue;
            player.getSleepingPos().ifPresent(bedPos ->
                    tryGenerateForBed(serverLevel, bedPos, getPlayerBonus(serverLevel, bedPos)));
        }

        // --- Жители ---
        // Ищем всех жителей которые спят в этом мире
        List<Villager> villagers = serverLevel.getEntitiesOfClass(
                Villager.class,
                new AABB(serverLevel.getWorldBorder().getMinX(),
                        serverLevel.getMinBuildHeight(),
                        serverLevel.getWorldBorder().getMinZ(),
                        serverLevel.getWorldBorder().getMaxX(),
                        serverLevel.getMaxBuildHeight(),
                        serverLevel.getWorldBorder().getMaxZ()),
                Villager::isSleeping
        );

        for (Villager villager : villagers) {
            villager.getSleepingPos().ifPresent(bedPos ->
                    // Жители не дают бонус — базовый дроп без подушки
                    tryGenerateForBed(serverLevel, bedPos, 0));
        }
    }

    /**
     * Пытается найти генератор под кроватью и сгенерировать ресурсы.
     */
    private static void tryGenerateForBed(ServerLevel level, BlockPos bedPos, int bonus) {
        BlockState bedState = level.getBlockState(bedPos);
        if (!(bedState.getBlock() instanceof BedBlock)) return;

        // Находим обе части кровати
        Direction facing = bedState.getValue(BedBlock.FACING);
        BlockPos otherHalfPos = bedState.getValue(BedBlock.PART) == BedPart.HEAD
                ? bedPos.relative(facing.getOpposite())
                : bedPos.relative(facing);

        // Ищем генератор под любой из двух частей
        DreamGeneratorBlockEntity generator = findGenerator(level, bedPos.below());
        if (generator == null) generator = findGenerator(level, otherHalfPos.below());
        if (generator == null) return;

        generator.generateResources(bonus);
    }

    /**
     * Определяет бонус игрока от типа проклятия кровати.
     */
    private static int getPlayerBonus(ServerLevel level, BlockPos bedPos) {
        BlockEntity bedBE = level.getBlockEntity(bedPos);
        if (bedBE == null || !bedBE.getPersistentData().contains(CURSE_TAG)) return 0;

        String curseType = bedBE.getPersistentData().getString(CURSE_TAG);
        if (CURSE_TYPE_LUCID.equals(curseType)) return 2;
        if (CURSE_TYPE_NULL.equals(curseType)) return 1;
        return 0;
    }

    private static DreamGeneratorBlockEntity findGenerator(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof DreamGeneratorBlockEntity gen ? gen : null;
    }
}