package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record ArmorDefinitionData(Map<String, Double> damageResistance, float deflectChance, double weight) {
    public static final Codec<ArmorDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(String::toUpperCase, s -> s), Codec.DOUBLE)
                    .optionalFieldOf("damageResistance", Map.of())
                    .forGetter(ArmorDefinitionData::damageResistance),
            Codec.FLOAT.optionalFieldOf("deflectChance", 0.0f)
                    .forGetter(ArmorDefinitionData::deflectChance),
            Codec.DOUBLE.optionalFieldOf("weight", 0.0)
                    .forGetter(ArmorDefinitionData::weight)
    ).apply(instance, ArmorDefinitionData::new));
}