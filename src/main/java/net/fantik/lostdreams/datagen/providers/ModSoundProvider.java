package net.fantik.lostdreams.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fantik.lostdreams.LostDreams;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModSoundProvider implements DataProvider {

    private final PackOutput output;
    // ключ — имя события (без modid), значение — список путей к файлам
    private final Map<String, List<String>> sounds = new LinkedHashMap<>();

    public ModSoundProvider(PackOutput output) {
        this.output = output;
    }

    private void registerSounds() {
        // Моб: null_bug
        add("entity.null_bug.ambient", "lostdreams:entity/null_bug/ambient");
        add("entity.null_bug.hurt",    "lostdreams:entity/null_bug/hurt");
        add("entity.null_bug.death",   "lostdreams:entity/null_bug/death");

        // Моб: lucid_wisp
        add("entity.lucid_waste.ambient", "lostdreams:entity/lucid_waste/ambient");

        // Блок: null_ground
        add("block.null_ground.break", "lostdreams:block/null_ground/break");
        add("block.null_ground.step",  "lostdreams:block/null_ground/step");
        add("block.null_ground.place", "lostdreams:block/null_ground/place");
        add("block.null_ground.hit",   "lostdreams:block/null_ground/hit");
        add("block.null_ground.fall",  "lostdreams:block/null_ground/fall");

        add("block.surreal_glowcrystal.break", "lostdreams:block/surreal_glowcrystal/break");
        add("block.surreal_glowcrystal.step", "lostdreams:block/surreal_glowcrystal/step");
        add("block.surreal_glowcrystal.place", "lostdreams:block/surreal_glowcrystal/place");
    }

    // Добавить событие с одним или несколькими звуками
    private void add(String event, String... paths) {
        sounds.put(event, List.of(paths));
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        registerSounds();

        JsonObject root = new JsonObject();

        for (var entry : sounds.entrySet()) {
            JsonObject eventObj = new JsonObject();
            JsonArray array = new JsonArray();
            for (String path : entry.getValue()) {
                array.add(path);
            }
            eventObj.add("sounds", array);
            root.add(entry.getKey(), eventObj);
        }

        Path path = output
                .getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(LostDreams.MOD_ID)
                .resolve("sounds.json");

        return DataProvider.saveStable(cache, root, path);
    }

    @Override
    public @NotNull String getName() {
        return "LostDreams Sound Provider";
    }
}