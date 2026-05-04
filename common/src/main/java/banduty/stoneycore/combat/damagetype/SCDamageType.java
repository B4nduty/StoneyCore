package banduty.stoneycore.combat.damagetype;

import com.mojang.serialization.Codec;

public enum SCDamageType {
    SLASHING,
    PIERCING,
    BLUDGEONING;

    public static final Codec<SCDamageType> CODEC = Codec.STRING.xmap(
            str -> SCDamageType.valueOf(str.toUpperCase()),
            SCDamageType::name
    );
}