package net.fantik.lostdreams.sound;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, LostDreams.MOD_ID);

    // ===== Звуки моба =====
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

    // ===== Звуки блока null_ground =====
    public static final DeferredHolder<SoundEvent, SoundEvent> NULL_GROUND_BREAK =
            SOUND_EVENTS.register("block.null_ground.break",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.parse("lostdreams:block.null_ground.break")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NULL_GROUND_STEP =
            SOUND_EVENTS.register("block.null_ground.step",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.parse("lostdreams:block.null_ground.step")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NULL_GROUND_PLACE =
            SOUND_EVENTS.register("block.null_ground.place",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.parse("lostdreams:block.null_ground.place")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NULL_GROUND_HIT =
            SOUND_EVENTS.register("block.null_ground.hit",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.parse("lostdreams:block.null_ground.hit")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NULL_GROUND_FALL =
            SOUND_EVENTS.register("block.null_ground.fall",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.parse("lostdreams:block.null_ground.fall")));

    // Ленивая инициализация — SoundType создаётся только при первом обращении,
    // когда SoundEvent уже точно зарегистрированы
    private static SoundType nullGroundSoundType = null;

    public static SoundType getNullGroundSoundType() {
        if (nullGroundSoundType == null) {
            nullGroundSoundType = new SoundType(
                    1.0F, 1.0F,
                    NULL_GROUND_BREAK.get(),
                    NULL_GROUND_STEP.get(),
                    NULL_GROUND_PLACE.get(),
                    NULL_GROUND_HIT.get(),
                    NULL_GROUND_FALL.get()
            );
        }
        return nullGroundSoundType;
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}