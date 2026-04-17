package net.fantik.lostdreams.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.fantik.lostdreams.LostDreams;

import java.util.stream.Stream;

public class NullZoneBiomeSource extends BiomeSource {
    // Используем RegistryCodecs для получения списка или полной базы биомов
    public static final MapCodec<NullZoneBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(s -> s.allowedBiomes)
            ).apply(instance, NullZoneBiomeSource::new));

    private final Holder.Reference<Biome> plains;
    private final Holder.Reference<Biome> caves;
    private final Holder.Reference<Biome> clouds;
    private final net.minecraft.core.HolderSet<Biome> allowedBiomes;

    public NullZoneBiomeSource(net.minecraft.core.HolderSet<Biome> allowedBiomes) {
        this.allowedBiomes = allowedBiomes;

        // Поиск конкретных биомов внутри предоставленного набора
        this.plains = findBiome(allowedBiomes, "null_zone_plains");
        this.caves = findBiome(allowedBiomes, "null_zone_caves");
        this.clouds = findBiome(allowedBiomes, "null_clouds");
    }

    private Holder.Reference<Biome> findBiome(net.minecraft.core.HolderSet<Biome> set, String name) {
        ResourceLocation target = ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, name);
        return set.stream()
                .filter(holder -> holder.unwrapKey().isPresent() && holder.unwrapKey().get().location().equals(target))
                .findFirst()
                .flatMap(Holder::unwrapKey)
                .map(key -> {
                    // Здесь нам нужно вернуть Reference.
                    // В норме они уже должны быть в реестре, раз попали в HolderSet
                    return (Holder.Reference<Biome>) set.stream()
                            .filter(h -> h.is(key))
                            .findFirst()
                            .orElseThrow();
                })
                .orElseThrow(() -> new IllegalStateException("Missing biome: " + target));
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return allowedBiomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        // Конвертация в реальные координаты (1 unit = 4 blocks)
        int realY = y << 2;

        if (realY >= 190) {
            return clouds;
        } else if (realY <= 60) {
            return caves;
        } else {
            return plains;
        }
    }
}