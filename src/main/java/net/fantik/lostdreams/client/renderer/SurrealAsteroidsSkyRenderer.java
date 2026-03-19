package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.SurrealAsteroidsDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SurrealAsteroidsSkyRenderer {

    // Звёзды — генерируем один раз
    private static List<Star> stars = null;

    private record Star(float x, float y, float z, float r, float g, float b, float size) {}

    private static List<Star> generateStars() {
        List<Star> result = new ArrayList<>();
        RandomSource random = RandomSource.create(12345L);

        // 2000 звёзд разных цветов
        for (int i = 0; i < 2000; i++) {
            float x = (random.nextFloat() * 2 - 1) * 100;
            float y = (random.nextFloat() * 2 - 1) * 100;
            float z = (random.nextFloat() * 2 - 1) * 100;

            // Нормализуем на сферу
            float len = (float) Math.sqrt(x*x + y*y + z*z);
            x /= len; y /= len; z /= len;

            // Цвета звёзд — белые, голубые, жёлтые, красные, фиолетовые
            float[] color = randomStarColor(random);
            float size = 0.3f + random.nextFloat() * 0.7f;

            result.add(new Star(x * 90, y * 90, z * 90, color[0], color[1], color[2], size));
        }
        return result;
    }

    private static float[] randomStarColor(RandomSource random) {
        return switch (random.nextInt(6)) {
            case 0 -> new float[]{1.0f, 1.0f, 1.0f};         // белая
            case 1 -> new float[]{0.6f, 0.8f, 1.0f};         // голубая
            case 2 -> new float[]{1.0f, 1.0f, 0.6f};         // жёлтая
            case 3 -> new float[]{1.0f, 0.4f, 0.4f};         // красная
            case 4 -> new float[]{0.8f, 0.4f, 1.0f};         // фиолетовая
            default -> new float[]{0.4f, 1.0f, 0.8f};        // бирюзовая
        };
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!SurrealAsteroidsDimension.isSurrealAsteroids(mc.level)) return;

        if (stars == null) stars = generateStars();

        // Чёрный фон
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = event.getPoseStack().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Рисуем каждую звезду как маленький квадрат
        for (Star star : stars) {
            // Мерцание — небольшая анимация яркости
            long time = mc.level.getGameTime();
            float twinkle = 0.7f + 0.3f * (float) Math.sin(time * 0.05 + star.x * 10);
            float alpha = twinkle;

            float s = star.size * 0.15f;
            float r = star.r * twinkle;
            float g = star.g * twinkle;
            float b = star.b * twinkle;

            buffer.addVertex(matrix, star.x - s, star.y - s, star.z).setColor(r, g, b, alpha);
            buffer.addVertex(matrix, star.x + s, star.y - s, star.z).setColor(r, g, b, alpha);
            buffer.addVertex(matrix, star.x + s, star.y + s, star.z).setColor(r, g, b, alpha);
            buffer.addVertex(matrix, star.x - s, star.y + s, star.z).setColor(r, g, b, alpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}