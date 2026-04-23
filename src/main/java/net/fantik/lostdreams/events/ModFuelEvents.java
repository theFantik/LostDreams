package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.item.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID)
public class ModFuelEvents {

    @SubscribeEvent
    public static void addFuel(FurnaceFuelBurnTimeEvent event) {

        // если в печь вставили zircon
        if (event.getItemStack().is(ModItems.ZIRCON.get())) {
            event.setBurnTime(2000); // 10 предметов
        }

    }
}