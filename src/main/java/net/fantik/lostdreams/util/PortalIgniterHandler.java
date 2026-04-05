package net.fantik.lostdreams.util;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PortalIgniterHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PortalTeleporter.tickCooldown(player);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        var item = event.getItemStack().getItem();

        if (!(item instanceof FlintAndSteelItem) && item != Items.FIRE_CHARGE) return;

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        PortalDefinition def = PortalRegistry.findByFrame(clickedState.getBlock());
        if (def == null) return;

        for (var dir : net.minecraft.core.Direction.values()) {
            BlockPos adjacent = clickedPos.relative(dir);

            // Используем наш новый безопасный Optional
            Optional<PortalShape> shapeOpt = PortalShape.find(level, adjacent, def);

            if (shapeOpt.isPresent()) {
                // Отменяем ванильное событие (чтобы огонь не появился локально)
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);

                // Создаем портал только на сервере
                if (!level.isClientSide()) {
                    shapeOpt.get().fill();

                    if (item instanceof FlintAndSteelItem && !event.getEntity().isCreative()) {
                        event.getItemStack().hurtAndBreak(1, event.getEntity(),
                                event.getEntity().getEquipmentSlotForItem(event.getItemStack()));
                    }
                }
                return;
            }
        }
    }
}