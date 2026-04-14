package net.fantik.lostdreams.block.custom;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneratorTier {

    private final String name; // Добавлено для сохранения в NBT
    private final List<WeightedDrop> drops;
    private final int totalWeight;

    private GeneratorTier(String name, List<WeightedDrop> drops) {
        this.name = name;
        this.drops = drops;
        this.totalWeight = drops.stream().mapToInt(d -> d.weight).sum();
    }

    public String name() {
        return name;
    }

    /**
     * Выбирает случайный дроп.
     * ИСПРАВЛЕНО: тип random изменен на RandomSource для совместимости с Minecraft
     */
    public ItemStack roll(RandomSource random, int bonusItems) {
        if (drops.isEmpty()) return ItemStack.EMPTY;

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;

        for (WeightedDrop drop : drops) {
            cumulative += drop.weight;
            if (roll < cumulative) {
                int amount = drop.minAmount + (drop.maxAmount > drop.minAmount ? random.nextInt(drop.maxAmount - drop.minAmount + 1) : 0);
                amount += bonusItems;
                amount = Math.min(amount, drop.item.getDefaultMaxStackSize());
                return new ItemStack(drop.item, amount);
            }
        }

        WeightedDrop first = drops.get(0);
        return new ItemStack(first.item, first.minAmount + bonusItems);
    }

    // -----------------------------------------------------------------------
    // Регистрация и Готовые тиры
    // -----------------------------------------------------------------------

    public static final GeneratorTier BASIC = new Builder("BASIC")
            .add(Items.COAL,     50, 1, 3)
            .add(Items.RAW_IRON, 15, 1, 3)
            .add(Items.STRING,   35, 1, 4)
            .build();

    public static final GeneratorTier ADVANCED = new Builder("ADVANCED")
            .add(Items.RAW_GOLD,   25, 1, 2)
            .add(Items.RAW_COPPER, 30, 1, 4)
            .add(Items.REDSTONE,   25, 2, 5)
            .add(Items.LAPIS_LAZULI, 20, 1, 3)
            .build();

    public static final GeneratorTier ELITE = new Builder("ELITE")
            .add(Items.DIAMOND,   20, 1, 2)
            .add(Items.EMERALD,   15, 1, 1)
            .add(Items.RAW_GOLD,  30, 1, 3)
            .add(Items.AMETHYST_SHARD, 35, 2, 4)
            .build();

    // Карта для быстрого поиска тира по имени (для загрузки из NBT)
    private static final Map<String, GeneratorTier> TIERS = Map.of(
            "BASIC", BASIC,
            "ADVANCED", ADVANCED,
            "ELITE", ELITE
    );

    public static GeneratorTier valueOf(String name) {
        return TIERS.getOrDefault(name, BASIC);
    }

    // -----------------------------------------------------------------------
    // Внутренние классы
    // -----------------------------------------------------------------------

    private record WeightedDrop(Item item, int weight, int minAmount, int maxAmount) {}

    public static class Builder {
        private final String name;
        private final List<WeightedDrop> drops = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder add(Item item, int weight, int minAmount, int maxAmount) {
            drops.add(new WeightedDrop(item, weight, minAmount, maxAmount));
            return this;
        }

        public GeneratorTier build() {
            if (drops.isEmpty()) throw new IllegalStateException("GeneratorTier must have at least one drop");
            return new GeneratorTier(name, drops);
        }
    }
}