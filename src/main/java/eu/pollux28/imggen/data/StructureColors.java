package eu.pollux28.imggen.data;

import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;

public enum StructureColors {
    PILLAGER_OUTPOST(0xB5DB88, ConfiguredStructureFeatures.PILLAGER_OUTPOST),
    MINESHAFT(0x056621, ConfiguredStructureFeatures.MINESHAFT),
    MINESHAFT_MESA(0xD94515, ConfiguredStructureFeatures.MINESHAFT_MESA),
    MANSION(0x40511A, ConfiguredStructureFeatures.MANSION),
    JUNGLE_PYRAMID(0x537B09, ConfiguredStructureFeatures.JUNGLE_PYRAMID),
    DESERT_PYRAMID(0xFFBC40, ConfiguredStructureFeatures.DESERT_PYRAMID),
    IGLOO(0x243F36, ConfiguredStructureFeatures.IGLOO),
    SHIPWRECK(0x000090, ConfiguredStructureFeatures.SHIPWRECK),
    SHIPWRECK_BEACHED (0xFADE55, ConfiguredStructureFeatures.SHIPWRECK_BEACHED),
    SWAMP_HUT(0x07F9B2, ConfiguredStructureFeatures.SWAMP_HUT),
    STRONGHOLD(0x606060, ConfiguredStructureFeatures.STRONGHOLD),
    MONUMENT(0x000070, ConfiguredStructureFeatures.MONUMENT),
    OCEAN_RUIN_COLD(0x202070, ConfiguredStructureFeatures.OCEAN_RUIN_COLD),
    OCEAN_RUIN_WARM(0x0000AC, ConfiguredStructureFeatures.OCEAN_RUIN_WARM),
    FORTRESS(0x5e3830, ConfiguredStructureFeatures.FORTRESS),
    NETHER_FOSSIL(0x49907B, ConfiguredStructureFeatures.NETHER_FOSSIL),
    END_CITY(0x8080FF, ConfiguredStructureFeatures.END_CITY),
    BURIED_TREASURE(0xFAF0C0, ConfiguredStructureFeatures.BURIED_TREASURE),
    BASTION_REMNANT(0x403636, ConfiguredStructureFeatures.BASTION_REMNANT),
    VILLAGE_PLAINS(0x8DB360, ConfiguredStructureFeatures.VILLAGE_PLAINS),
    VILLAGE_DESERT(0xFA9418, ConfiguredStructureFeatures.VILLAGE_DESERT),
    VILLAGE_SAVANNA(0xBDB25F, ConfiguredStructureFeatures.VILLAGE_SAVANNA),
    VILLAGE_SNOWY(0xFFFFFF, ConfiguredStructureFeatures.VILLAGE_SNOWY),
    VILLAGE_TAIGA(0x0B6659, ConfiguredStructureFeatures.VILLAGE_TAIGA),
    RUINED_PORTAL(0xDD0808, ConfiguredStructureFeatures.RUINED_PORTAL),
    RUINED_PORTAL_DESERT(0xD25F12, ConfiguredStructureFeatures.RUINED_PORTAL_DESERT),
    RUINED_PORTAL_JUNGLE(0x2C4205, ConfiguredStructureFeatures.RUINED_PORTAL_JUNGLE),
    RUINED_PORTAL_SWAMP(0x2FFFDA, ConfiguredStructureFeatures.RUINED_PORTAL_SWAMP),
    RUINED_PORTAL_MOUNTAIN(0x789878, ConfiguredStructureFeatures.RUINED_PORTAL_MOUNTAIN),
    RUINED_PORTAL_OCEAN(0x202038, ConfiguredStructureFeatures.RUINED_PORTAL_OCEAN),
    RUINED_PORTAL_NETHER(0xbf3b3b, ConfiguredStructureFeatures.RUINED_PORTAL_NETHER),
    NONE(0x000000,null);
    final int RGB;
    final ConfiguredStructureFeature<?,?> configuredStructureFeature;

    StructureColors(int RGB, ConfiguredStructureFeature<?,?> configuredStructureFeature) {
        this.RGB = RGB;
        this.configuredStructureFeature = configuredStructureFeature;
    }
    public ConfiguredStructureFeature<?,?> getConfiguredStructureFeature() {
        return configuredStructureFeature;
    }
    public int getRGB() {
        return RGB;
    }

}
