package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SiegeEngineDefinitionData(double playerSpeed, double horseSpeed, double knockback, double baseDamage,
                                        int baseReload, float projectileSpeed, float accuracyMultiplier,
                                        DamageSourceConfig damageConfig) {
    public static final SiegeEngineDefinitionData DEFAULT = new SiegeEngineDefinitionData(
            0.05, 0.1, 265.0, 25.0, 90, 140.0f, 1, DamageSourceConfig.DEFAULT
    );

    public static final Codec<SiegeEngineDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("playerSpeed", 0.05).forGetter(SiegeEngineDefinitionData::playerSpeed),
            Codec.DOUBLE.optionalFieldOf("horseSpeed", 0.1).forGetter(SiegeEngineDefinitionData::horseSpeed),
            Codec.DOUBLE.optionalFieldOf("knockback", 0.0).forGetter(SiegeEngineDefinitionData::knockback),
            Codec.DOUBLE.optionalFieldOf("baseDamage", 25.0).forGetter(SiegeEngineDefinitionData::baseDamage),
            Codec.INT.optionalFieldOf("baseReload", 90).forGetter(SiegeEngineDefinitionData::baseReload),
            Codec.FLOAT.optionalFieldOf("projectileSpeed", 140.0f).forGetter(SiegeEngineDefinitionData::projectileSpeed),
            Codec.FLOAT.optionalFieldOf("accuracyMultiplier", 1.2f).forGetter(SiegeEngineDefinitionData::accuracyMultiplier),
            DamageSourceConfig.CODEC.optionalFieldOf("damageConfig", DamageSourceConfig.DEFAULT).forGetter(SiegeEngineDefinitionData::damageConfig)
    ).apply(instance, SiegeEngineDefinitionData::new));
}

