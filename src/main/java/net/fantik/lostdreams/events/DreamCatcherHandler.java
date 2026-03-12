package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class DreamCatcherHandler {

    private static final int RANGE = 32;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameTime() % 10 != 0) return;

        boolean holding = player.getMainHandItem().is(ModItems.DREAM_CATCHER.get())
                || player.getOffhandItem().is(ModItems.DREAM_CATCHER.get());

        if (!holding) return;

        // Находим всех мобов в радиусе и накладываем свечение
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(RANGE),
                e -> !(e instanceof Player)
        );

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    15, // чуть больше 10 тиков — эффект не мигает
                    0,
                    false,
                    false,
                    false
            ));
        }
    }
}