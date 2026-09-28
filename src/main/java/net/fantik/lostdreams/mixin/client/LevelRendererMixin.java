package net.fantik.lostdreams.mixin.client;

import com.mojang.blaze3d.vertex.*;
import net.fantik.lostdreams.LostDreams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    private static final ResourceLocation CHESS_DIMENSION =
            ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "chess_dimension");

    private static boolean isChessDimension() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null
                && mc.level.dimension().location().equals(CHESS_DIMENSION);
    }

    // -----------------------------------------------------------------------
    // Убираем чёрный нижний диск неба
    // -----------------------------------------------------------------------

    @Redirect(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V",
                    ordinal = 2
            )
    )
    private void lostdreams$removeChessDarkSky(VertexBuffer buffer,
                                               Matrix4f modelView,
                                               Matrix4f projection,
                                               ShaderInstance shader) {
        if (!isChessDimension()) {
            buffer.drawWithShader(modelView, projection, shader);
        }
    }

    // -----------------------------------------------------------------------
    // Размер солнца / луны
    // -----------------------------------------------------------------------

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 30.0F))
    private float lostdreams$chessSunSize(float value) {
        return isChessDimension() ? 10.0F : value;
    }

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float lostdreams$chessMoonSize(float value) {
        return isChessDimension() ? 40.0F : value;
    }


}