package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.block.custom.DreamGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID)
public class ModEvents {

    private static final String CURSE_TAG        = "lostdreams_bed_curse";
    private static final String CURSE_TYPE_NULL      = "null_zone";
    private static final String CURSE_TYPE_LUCID = "lucid";

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        for (Player player : serverLevel.players()) {
            if (!player.isSleeping()) continue;

            player.getSleepingPos().ifPresent(bedPos -> {
                BlockState bedState = serverLevel.getBlockState(bedPos);
                if (!(bedState.getBlock() instanceof BedBlock)) return;

                // ИСПРАВЛЕНО: Находим вторую половину кровати
                Direction facing = bedState.getValue(BedBlock.FACING);
                BlockPos otherHalfPos = bedState.getValue(BedBlock.PART) == BedPart.HEAD
                        ? bedPos.relative(facing.getOpposite())
                        : bedPos.relative(facing);

                // ИСПРАВЛЕНО: Проверяем блок под обеими частями кровати
                DreamGeneratorBlockEntity generator = findGenerator(serverLevel, bedPos.below());
                if (generator == null) {
                    generator = findGenerator(serverLevel, otherHalfPos.below());
                }

                if (generator != null) {
                    BlockEntity bedBE = serverLevel.getBlockEntity(bedPos);
                    int bonus = 0;

                    if (bedBE != null && bedBE.getPersistentData().contains(CURSE_TAG)) {
                        String curseType = bedBE.getPersistentData().getString(CURSE_TAG);
                        if (CURSE_TYPE_LUCID.equals(curseType)) {
                            bonus = 2;
                        } else if (CURSE_TYPE_NULL.equals(curseType)) {
                            bonus = 1;
                        }
                    }

                    generator.generateResources(bonus);
                }
            });
        }
    }

    // ИСПРАВЛЕНО: Вспомогательный метод для удобного поиска генератора
    private static DreamGeneratorBlockEntity findGenerator(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof DreamGeneratorBlockEntity gen) {
            return gen;
        }

        return null;
    }
}