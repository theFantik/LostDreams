package net.fantik.lostdreams.item;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LostDreams.MOD_ID);

    public static final DeferredItem<Item> PILLOW = ITEMS.register("pillow",
            () -> new Item(new Item.Properties()));

    // Жучьи усики — дроп с null_bug
    public static final DeferredItem<Item> BUG_ANTENNA = ITEMS.register("bug_antenna",
            () -> new Item(new Item.Properties()));

    // Яйцо призыва null_bug (цвета: основной тёмно-серый, пятна оранжевые)
    public static final DeferredItem<Item> NULL_BUG_SPAWN_EGG = ITEMS.register("null_bug_spawn_egg",
            () -> new SpawnEggItem(ModEntities.NULL_BUG.get(), 0xffffff, 0xffffff, new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
