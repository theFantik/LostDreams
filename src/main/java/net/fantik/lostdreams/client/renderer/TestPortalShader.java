package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fantik.lostdreams.LostDreams;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

public final class TestPortalShader {

    private TestPortalShader() {
    }

    public static ShaderInstance SHADER;


    public static final RenderType PORTAL = RenderType.create(
            LostDreams.MOD_ID + ":test_portal",

            DefaultVertexFormat.POSITION_TEX,

            com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,

            256,

            false,
            false,

            RenderType.CompositeState.builder()

                    .setShaderState(
                            new RenderStateShard.ShaderStateShard(
                                    () -> SHADER
                            )
                    )

                    .setTransparencyState(
                            RenderStateShard.TRANSLUCENT_TRANSPARENCY
                    )

                    .setDepthTestState(
                            RenderStateShard.LEQUAL_DEPTH_TEST
                    )

                    .setCullState(
                            RenderStateShard.NO_CULL
                    )

                    .setLightmapState(
                            RenderStateShard.NO_LIGHTMAP
                    )

                    .setOverlayState(
                            RenderStateShard.NO_OVERLAY
                    )

                    .setWriteMaskState(
                            RenderStateShard.COLOR_DEPTH_WRITE
                    )

                    .createCompositeState(false)
    );
}