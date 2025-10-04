package banduty.stoneycore.util.data.playerdata;

import banduty.stoneycore.util.data.keys.SCKey;

public class PDKeys {
    // Stamina
    public static final SCKey<Boolean> STAMINA_BLOCKED = SCKey.bool("staminaBlocked");
    public static final SCKey<Double> STAMINA_VALUE_SAVED = SCKey.dbl("staminaValueSaved");
    public static final SCKey<Integer> STAMINA_USE_TIME = SCKey.integer("staminaUseTime");

    // Gameplay
    public static final SCKey<Long> BLOCK_START_TICK = SCKey.lng("blockStartTick");
    public static final SCKey<Integer> RECHARGE_TIME = SCKey.integer("rechargeTime");
    public static final SCKey<Integer> SWALLOWTAIL_ARROW_COUNT = SCKey.integer("swallowtailArrowCount");

    // Lands
    public static final SCKey<Boolean> LAND_EXPANDED = SCKey.bool("landExpanded");

    // Extra Data
    public static final SCKey<Boolean> FIRST_JOIN = SCKey.bool("firstJoin");
}
