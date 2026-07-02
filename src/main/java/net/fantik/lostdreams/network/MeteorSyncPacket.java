package net.fantik.lostdreams.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Пакет: Клиент → Сервер
 * Назначение: синхронизация позиции и скорости сущности-транспорта.
 * Отправляется каждые N тиков во время полёта И в момент спешивания.
 *
 * isDismounting = true  → финальный пакет при спешивании (сервер запускает
 *                         свою физику с этими начальными условиями)
 * isFalling     = true  → клиент сообщает, что он уже спешился и
 *                         продолжает падать; сервер применяет позицию напрямую
 */
public record MeteorSyncPacket(
        int entityId,
        double x, double y, double z,
        double velX, double velY, double velZ,
        float yRot, float xRot,
        boolean isDismounting,
        boolean isFalling
) implements CustomPacketPayload {

    public static final Type<MeteorSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lostdreams", "meteor_sync"));

    public static final StreamCodec<FriendlyByteBuf, MeteorSyncPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeVarInt(pkt.entityId());
                        buf.writeDouble(pkt.x());
                        buf.writeDouble(pkt.y());
                        buf.writeDouble(pkt.z());
                        buf.writeDouble(pkt.velX());
                        buf.writeDouble(pkt.velY());
                        buf.writeDouble(pkt.velZ());
                        buf.writeFloat(pkt.yRot());
                        buf.writeFloat(pkt.xRot());
                        buf.writeBoolean(pkt.isDismounting());
                        buf.writeBoolean(pkt.isFalling());
                    },
                    buf -> new MeteorSyncPacket(
                            buf.readVarInt(),
                            buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readFloat(), buf.readFloat(),
                            buf.readBoolean(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
