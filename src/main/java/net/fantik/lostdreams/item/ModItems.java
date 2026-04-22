package net.fantik.lostdreams.item;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.entity.ModEntities;
import net.fantik.lostdreams.item.custom.NullBerryItem;
import net.fantik.lostdreams.item.custom.NullSeedItem;
import net.fantik.lostdreams.item.custom.ZirconFertilizerItem;
import net.minecraft.core.Direction;
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



    public static final DeferredItem<Item> NULL_SEED =
            ITEMS.register("null_seed",
                    () -> new NullSeedItem(new Item.Properties()));

    public static final DeferredItem<Item> ZIRCON =
            ITEMS.register("zircon",
                    () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ZIRCON_FERTILIZER =
            ITEMS.register("zircon_fertilizer",
                    () -> new ZirconFertilizerItem(new Item.Properties()
                            .stacksTo(16)));

    public static final DeferredItem<StandingAndWallBlockItem> ZIRCON_TORCH_ITEM =
            ITEMS.register("zircon_torch",
                    () -> new StandingAndWallBlockItem(
                            ModBlocks.ZIRCON_TORCH.get(),
                            ModBlocks.ZIRCON_WALL_TORCH.get(),
                            new Item.Properties(), Direction.DOWN));





    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
