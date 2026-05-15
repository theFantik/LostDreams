package net.fantik.lostdreams.network;

import net.fantik.lostdreams.LostDreams;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::onRegisterPayloads);
    }

    private static void onRegisterPayloads(
            RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(LostDreams.MOD_ID);

        registrar.playToClient(
                GlowingBlocksPacket.TYPE,
                GlowingBlocksPacket.STREAM_CODEC,
                GlowingBlocksPacket::handle
        );
    }
}
