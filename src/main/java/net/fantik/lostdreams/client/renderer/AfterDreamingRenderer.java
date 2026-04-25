package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = LostDreams.MOD_ID, value = Dist.CLIENT)
public class AfterDreamingRenderer {

    private static final ResourceLocation SHADER_LOCATION =
            ResourceLocation.fromNamespaceAndPath(
                    LostDreams.MOD_ID, "shaders/post/after_dreaming.json");

    @Nullable
    private static PostChain postChain = null;

    private static boolean shaderLoaded = false;
    private static boolean shaderActive = false;

    // Переменные для отслеживания изменения размера окна
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private static void loadShader() {
        if (shaderLoaded) return;
        shaderLoaded = true;

        RenderSystem.recordRenderCall(() -> {
            try {
                Minecraft mc = Minecraft.getInstance();

                postChain = new PostChain(
                        mc.getTextureManager(),
                        mc.getResourceManager(),
                        mc.getMainRenderTarget(),
                        SHADER_LOCATION
                );

                // Запоминаем изначальный размер окна
                lastWidth = mc.getWindow().getWidth();
                lastHeight = mc.getWindow().getHeight();
                postChain.resize(lastWidth, lastHeight);

                LostDreams.LOGGER.info("AfterDreaming shader loaded");
            } catch (Exception e) {
                LostDreams.LOGGER.error("Failed to load after_dreaming shader", e);
                postChain = null;
            }
        });
    }

    private static void unloadShader() {
        if (postChain != null) {
            postChain.close();
            postChain = null;
        }
        shaderLoaded = false;
        shaderActive = false;
        // Сбрасываем кэш размеров
        lastWidth = -1;
        lastHeight = -1;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        boolean hasEffect = player.hasEffect(ModEffects.AFTER_DREAMING);

        if (hasEffect && !shaderLoaded) {
            loadShader();
            return;
        }

        if (!hasEffect && shaderActive) {
            unloadShader();
            return;
        }

        if (!hasEffect || postChain == null) return;

        shaderActive = true;

        // --- ИСПРАВЛЕНИЕ ИСКАЖЕНИЙ (RESIZE) ---
        // Проверяем, изменился ли размер окна с прошлого кадра
        int currentWidth = mc.getWindow().getWidth();
        int currentHeight = mc.getWindow().getHeight();
        if (currentWidth != lastWidth || currentHeight != lastHeight) {
            postChain.resize(currentWidth, currentHeight);
            lastWidth = currentWidth;
            lastHeight = currentHeight;
        }

        // --- ПЛАВНОЕ ВЫКЛЮЧЕНИЕ ---
        MobEffectInstance effectInstance = player.getEffect(ModEffects.AFTER_DREAMING);
        int duration = effectInstance.getDuration(); // Время в тиках (20 тиков = 1 секунда)
        int amplifier = effectInstance.getAmplifier();

        float baseIntensity = 1.0f + amplifier * 0.5f;
        float fadeMultiplier = 1.0f;

        // Если осталась 1 секунда (20 тиков) или меньше, начинаем плавно опускать множитель до нуля
        if (duration <= 40) {
            fadeMultiplier = duration / 40.0f;
        }

        // Рассчитываем целевые значения интенсивности
        float targetSaturation = Math.min(baseIntensity * 2.5f, 10.0f);
        float targetContrast = 1.0f + baseIntensity * 0.8f;

        // Интерполируем: (Нормальное значение 1.0) + (Разница * fadeMultiplier)
        // Когда fadeMultiplier = 0.0, шейдер вернет обычные цвета (Saturation: 1.0, Contrast: 1.0)
        float currentSaturation = 1.0f + (targetSaturation - 1.0f) * fadeMultiplier;
        float currentContrast = 1.0f + (targetContrast - 1.0f) * fadeMultiplier;

        postChain.setUniform("Saturation", currentSaturation);
        postChain.setUniform("Contrast", currentContrast);

        // 🔥 Запуск пост-эффекта
        RenderSystem.disableBlend();
        postChain.process(event.getPartialTick().getGameTimeDeltaPartialTick(true));
        mc.getMainRenderTarget().bindWrite(false);
    }
}