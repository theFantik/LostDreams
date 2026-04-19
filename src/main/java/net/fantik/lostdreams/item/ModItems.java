package net.fantik.lostdreams.item;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.entity.ModEntities;
import net.fantik.lostdreams.item.custom.NullBerryItem;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LostDreams.MOD_ID);

    public static final DeferredItem<Item> PILLOW = ITEMS.register("pillow",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NULL_PILLOW = ITEMS.register("null_pillow",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BUG_ANTENNA = ITEMS.register("bug_antenna",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LUCID_ESSENCE = ITEMS.register("lucid_essence", () -> new Item(new Item.Properties()) );

    // функциональное

    public static final DeferredItem<Item> DREAM_CATCHER = ITEMS.register("dream_catcher",
            () -> new Item(new Item.Properties()));


    // яички

    public static final DeferredItem<Item> NULL_BUG_SPAWN_EGG = ITEMS.register("null_bug_spawn_egg",
            () -> new SpawnEggItem(ModEntities.NULL_BUG.get(), 0xffffff, 0xffffff, new Item.Properties()));

    public static final DeferredItem<Item> LUCID_WASTE_SPAWN_EGG = ITEMS.register("lucid_waste_spawn_egg",
            () -> new SpawnEggItem(ModEntities.LUCID_WASTE.get(), 0xffffff, 0xffffff, new Item.Properties()));

    // еда

    public static final DeferredItem<Item> NULL_BERRY =
            ITEMS.register("null_berry",
                    () -> new NullBerryItem(new Item.Properties()));




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
