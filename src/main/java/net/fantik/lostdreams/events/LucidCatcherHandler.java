package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.item.ModItems;
import net.fantik.lostdreams.network.GlowingBlocksPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class LucidCatcherHandler {

    private static final int RANGE = 48;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameTime() % 10 != 0) return;

        boolean holding = player.getMainHandItem().is(ModItems.LUCID_CATCHER.get())
                || player.getOffhandItem().is(ModItems.LUCID_CATCHER.get());

        if (!holding) return;

        // -------------------------------------------------------
        // 1. Свечение живых существ
        // -------------------------------------------------------
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(RANGE),
                e -> !(e instanceof Player)
        );

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    15,
                    0,
                    false,
                    false,
                    false
            ));
        }

        // -------------------------------------------------------
        // 2. Свечение блок-энтити
        // -------------------------------------------------------
        List<BlockPos> glowPositions =
                getBlockEntitiesInRange(player, RANGE);

        if (!glowPositions.isEmpty()) {
            PacketDistributor.sendToPlayer(player,
                    new GlowingBlocksPacket(glowPositions));
        }
    }

    private static List<BlockPos> getBlockEntitiesInRange(
            ServerPlayer player, int range) {

        List<BlockPos> result = new ArrayList<>();
        BlockPos center = player.blockPosition();
        long rangeSq = (long) range * range;

        // Итерируем блоки в кубе, фильтруем по сфере
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-range, -range, -range),
                center.offset(range, range, range))) {

            if (pos.distSqr(center) > rangeSq) continue;

            if (player.level().getBlockEntity(pos) != null) {
                result.add(pos.immutable());
            }
        }

        return result;
    }
}
