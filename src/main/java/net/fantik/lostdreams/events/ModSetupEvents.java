package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModSetupEvents {
    @SubscribeEvent
    public static void registerSigns(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN,
                ModBlocks.NULL_SIGN.get(),
                ModBlocks.NULL_WALL_SIGN.get());

        event.modify(BlockEntityType.HANGING_SIGN,
                ModBlocks.NULL_HANGING_SIGN.get(),
                ModBlocks.NULL_WALL_HANGING_SIGN.get());
    }
}
