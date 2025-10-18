package banduty.stoneycore.util.data.itemdata;

import banduty.stoneycore.util.data.keys.SCKey;

public class INBTKeys {
    // Weapon
    public static final SCKey<Boolean> BLUDGEONING = SCKey.bool("bludgeoning");
    public static final SCKey<Boolean> IGNITED = SCKey.bool("ignited");
    public static final SCKey<Boolean> FROM_RANGED_WEAPON = SCKey.bool("from_ranged_weapon");

    // Armor
    public static final SCKey<Boolean> VISOR_OPEN = SCKey.bool("visorOpen");

    // Armor with Banner
    public static final SCKey<String> PATTERN = SCKey.str("Pattern");
    public static final SCKey<Integer> COLOR = SCKey.integer("Color");
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String PATTERNS = "Patterns";
    public static final SCKey<Float> DYE_COLOR_R = SCKey.flt("dyeColorR");
    public static final SCKey<Float> DYE_COLOR_G = SCKey.flt("dyeColorG");
    public static final SCKey<Float> DYE_COLOR_B = SCKey.flt("dyeColorB");
}
