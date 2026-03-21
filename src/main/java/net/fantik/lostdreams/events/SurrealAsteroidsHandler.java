package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.SurrealAsteroidsDimension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SurrealAsteroidsHandler {

    // Значения гравитации
    private static final double NORMAL_GRAVITY = 0.08; // ваниль
    private static final double LOW_GRAVITY = 0.005;   // космос

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
        if (gravity == null) return;

        // Проверяем измерение
        if (SurrealAsteroidsDimension.isSurrealAsteroids(player.level())) {

            // Устанавливаем слабую гравитацию
            if (gravity.getBaseValue() != LOW_GRAVITY) {
                gravity.setBaseValue(LOW_GRAVITY);
            }

            // Убираем урон от падения
            player.fallDistance = 0f;

        } else {

            // Возвращаем обычную гравитацию
            if (gravity.getBaseValue() != NORMAL_GRAVITY) {
                gravity.setBaseValue(NORMAL_GRAVITY);
            }
        }
    }
}