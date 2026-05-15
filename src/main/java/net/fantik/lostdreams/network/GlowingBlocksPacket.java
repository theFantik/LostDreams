package net.fantik.lostdreams.network;

import io.netty.buffer.ByteBuf;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.BlockGlowRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record GlowingBlocksPacket(List<BlockPos> positions) implements CustomPacketPayload {

    public static final Type<GlowingBlocksPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    LostDreams.MOD_ID, "glowing_blocks"));

    public static final StreamCodec<ByteBuf, GlowingBlocksPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new,
                            BlockPos.STREAM_CODEC),
                    GlowingBlocksPacket::positions,
                    GlowingBlocksPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Вызывается на клиенте
    public static void handle(GlowingBlocksPacket packet,
                              IPayloadContext context) {
        context.enqueueWork(() ->
                BlockGlowRenderer.updateGlowingBlocks(packet.positions())
        );
    }
}
