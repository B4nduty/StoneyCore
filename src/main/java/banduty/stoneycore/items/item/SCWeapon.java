package banduty.stoneycore.items.item;

import banduty.stoneycore.util.SCDamageCalculator;

public interface SCWeapon {
    double[] getRadiusValues();

    float[] getAttackDamageValues();

    int[] getPiercingAnimation();

    int getAnimation();

    SCDamageCalculator.DamageType getOnlyDamageType();
}