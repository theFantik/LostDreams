package net.fantik.lostdreams.sound;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, LostDreams.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> NULL_BUG_AMBIENT =
            SOUND_EVENTS.register("entity.null_bug.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.parse("lostdreams:entity.null_bug.ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NULL_BUG_HURT =
            SOUND_EVENTS.register("entity.null_bug.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.parse("lostdreams:entity.null_bug.hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NULL_BUG_DEATH =
            SOUND_EVENTS.register("entity.null_bug.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.parse("lostdreams:entity.null_bug.death")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}