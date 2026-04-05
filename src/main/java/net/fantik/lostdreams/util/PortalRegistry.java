package net.fantik.lostdreams.util;



import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Глобальный реестр всех порталов мода.
 * Регистрируй порталы в LostDreams.java или ModSetupHandler.java.
 */
public class PortalRegistry {

    private static final List<PortalDefinition> PORTALS = new ArrayList<>();

    public static void register(PortalDefinition def) {
        PORTALS.add(def);
    }

    public static List<PortalDefinition> getAll() {
        return Collections.unmodifiableList(PORTALS);
    }

    /** Найти определение портала по блоку рамки */
    public static PortalDefinition findByFrame(Block frameBlock) {
        for (PortalDefinition def : PORTALS) {
            if (def.getFrameBlock() == frameBlock) return def;
        }
        return null;
    }

    /** Найти определение портала по блоку портала */
    public static PortalDefinition findByPortalBlock(Block portalBlock) {
        for (PortalDefinition def : PORTALS) {
            if (def.getPortalBlock() == portalBlock) return def;
        }
        return null;
    }
}