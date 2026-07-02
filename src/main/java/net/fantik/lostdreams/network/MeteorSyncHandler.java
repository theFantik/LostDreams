package net.fantik.lostdreams.network;

import net.fantik.lostdreams.entity.MeteorMountEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MeteorSyncHandler {

    public static void handle(MeteorSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            Entity entity = serverPlayer.serverLevel().getEntity(packet.entityId());
            if (!(entity instanceof MeteorMountEntity meteor)) return;

            boolean isCurrentPassenger = meteor.hasPassenger(serverPlayer);

            // Доверяем текущему пассажиру
            if (isCurrentPassenger) {
                applySync(meteor, packet);
                return;
            }

            // Доверяем недавнему наезднику в грейс-период:
            // - пакет спешивания (isDismounting)
            // - или пакеты падения (isFalling) пока грейс не истёк
            boolean isRecentRider = serverPlayer.getUUID().equals(meteor.getLastRiderUUID());
            boolean inGrace       = meteor.isInDismountGracePeriod();

            if (isRecentRider && inGrace && (packet.isDismounting() || packet.isFalling())) {
                applySync(meteor, packet);

                if (packet.isDismounting()) {
                    // Уведомляем сущность: клиент только что спрыгнул с этой скоростью.
                    // Сущность запомнит флаг и НЕ будет сама считать физику,
                    // пока приходят isFalling-пакеты.
                    meteor.onClientDismountSync(packet.velX(), packet.velY(), packet.velZ());
                }
                return;
            }

            // Все остальные — игнорируем
        });
    }

    // -------------------------------------------------------
    private static void applySync(MeteorMountEntity meteor, MeteorSyncPacket packet) {
        double distSq = meteor.distanceToSqr(packet.x(), packet.y(), packet.z());
        // Отсекаем читеров / телепорты (100 блоков за тик — явная аномалия)
        if (distSq > 100.0 * 100.0) return;

        meteor.moveTo(packet.x(), packet.y(), packet.z(), packet.yRot(), packet.xRot());
        meteor.setDeltaMovement(packet.velX(), packet.velY(), packet.velZ());
    }
}
