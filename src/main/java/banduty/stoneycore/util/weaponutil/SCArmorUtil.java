package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.items.armor.SCUnderArmorItem;

public class SCArmorUtil {
    public static double getResistance(ResistanceType type, SCUnderArmorItem scUnderArmorItem) {
        return switch (type) {
            case SLASHING -> scUnderArmorItem.slashingResistance();
            case BLUDGEONING -> scUnderArmorItem.bludgeoningResistance();
            case PIERCING -> scUnderArmorItem.piercingResistance();
        };
    }

    public enum ResistanceType {
        SLASHING, BLUDGEONING, PIERCING
    }
}
