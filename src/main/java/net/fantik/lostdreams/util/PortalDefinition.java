package net.fantik.lostdreams.util;



import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Описание одного типа портала.
 * Регистрируй новые порталы через PortalRegistry.register(...)
 *
 * Пример:
 * PortalRegistry.register(PortalDefinition.builder()
 *     .frame(ModBlocks.NULL_STONE)
 *     .portalBlock(ModBlocks.NULL_ZONE_PORTAL)
 *     .destination(NullZoneDimension.NULL_ZONE_KEY)
 *     .minWidth(2).minHeight(3)
 *     .build());
 */
public class PortalDefinition {

    private final Block frameBlock;
    private final Block portalBlock;
    private final ResourceKey<Level> destination;
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;

    private PortalDefinition(Builder builder) {
        this.frameBlock    = builder.frameBlock;
        this.portalBlock   = builder.portalBlock;
        this.destination   = builder.destination;
        this.minWidth      = builder.minWidth;
        this.minHeight     = builder.minHeight;
        this.maxWidth      = builder.maxWidth;
        this.maxHeight     = builder.maxHeight;
    }

    public Block getFrameBlock()          { return frameBlock; }
    public Block getPortalBlock()         { return portalBlock; }
    public ResourceKey<Level> getDestination() { return destination; }
    public int getMinWidth()              { return minWidth; }
    public int getMinHeight()             { return minHeight; }
    public int getMaxWidth()              { return maxWidth; }
    public int getMaxHeight()             { return maxHeight; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Block frameBlock;
        private Block portalBlock;
        private ResourceKey<Level> destination;
        private int minWidth  = 2;
        private int minHeight = 3;
        private int maxWidth  = 21;
        private int maxHeight = 21;

        public Builder frame(Block block)                        { this.frameBlock  = block; return this; }
        public Builder frame(DeferredBlock<? extends Block> b)   { return frame(b.get()); }
        public Builder portalBlock(Block block)                   { this.portalBlock = block; return this; }
        public Builder portalBlock(DeferredBlock<? extends Block> b) { return portalBlock(b.get()); }
        public Builder destination(ResourceKey<Level> key)        { this.destination = key; return this; }
        public Builder minWidth(int w)                            { this.minWidth  = w; return this; }
        public Builder minHeight(int h)                           { this.minHeight = h; return this; }
        public Builder maxWidth(int w)                            { this.maxWidth  = w; return this; }
        public Builder maxHeight(int h)                           { this.maxHeight = h; return this; }

        public PortalDefinition build() {
            if (frameBlock  == null) throw new IllegalStateException("Portal frame block not set");
            if (portalBlock == null) throw new IllegalStateException("Portal block not set");
            if (destination == null) throw new IllegalStateException("Portal destination not set");
            return new PortalDefinition(this);
        }
    }
}