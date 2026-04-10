package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.block.custom.DreamGeneratorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

import java.util.*;

@EventBusSubscriber(modid = LostDreams.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        // Ночь успешно пропущена — проверяем всех спящих прямо сейчас
        // В этот момент игроки ещё спят, так что getSleepingPos() работает
        for (Player player : serverLevel.players()) {
            if (!player.isSleeping()) continue;

            player.getSleepingPos().ifPresent(bedPos -> {
                BlockPos underBed = bedPos.below();
                if (serverLevel.getBlockState(underBed).is(ModBlocks.DREAM_GENERATOR.get())) {
                    DreamGeneratorBlock.generateResources(serverLevel, underBed);
                }
            });
        }
    }
}