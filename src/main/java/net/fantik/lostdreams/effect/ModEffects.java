package net.fantik.lostdreams.effect;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, LostDreams.MOD_ID);

    public static final DeferredHolder<MobEffect, AfterDreamingEffect> AFTER_DREAMING =
            MOB_EFFECTS.register("after_dreaming",
                    () -> new AfterDreamingEffect());

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
