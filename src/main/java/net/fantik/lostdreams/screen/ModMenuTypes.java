package net.fantik.lostdreams.screen;

import net.fantik.lostdreams.LostDreams;

import net.fantik.lostdreams.block.custom.DreamGeneratorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, LostDreams.MOD_ID);

    public static final Supplier<MenuType<DreamGeneratorMenu>> DREAM_GENERATOR_MENU =
            MENU_TYPES.register("dream_generator",
                    () -> new MenuType<>(DreamGeneratorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}