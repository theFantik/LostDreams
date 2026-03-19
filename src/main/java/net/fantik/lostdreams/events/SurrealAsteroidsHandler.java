package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.SurrealAsteroidsDimension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SurrealAsteroidsHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!SurrealAsteroidsDimension.isSurrealAsteroids(player.level())) return;

        Vec3 velocity = player.getDeltaMovement();

        // Низкая гравитация — применяем обратную силу к падению
        if (velocity.y < 0) {
            // Добавляем положительное ускорение чтобы компенсировать гравитацию
            double newY = velocity.y + 0.06; // гравитация ~0.08 в тик, компенсируем 75%
            if (newY > 0) newY = 0; // не даём улететь вверх от этого
            player.setDeltaMovement(velocity.x, newY, velocity.z);
        }

        // Замедляем урон от падения
        player.fallDistance *= 0.3f;

        // Применяем изменения немедленно
        player.hurtMarked = true;
    }
}