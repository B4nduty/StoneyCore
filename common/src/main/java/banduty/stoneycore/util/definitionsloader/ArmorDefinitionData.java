package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record ArmorDefinitionData(Map<String, Double> damageResistance, Map<String, Double> deflectChance, double weight) {
    public static final Codec<ArmorDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(String::toUpperCase, s -> s), Codec.DOUBLE)
                    .optionalFieldOf("damageResistance", Map.of())
                    .forGetter(ArmorDefinitionData::damageResistance),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                    .optionalFieldOf("deflectChance", Map.of())
                    .forGetter(ArmorDefinitionData::deflectChance),
            Codec.DOUBLE.optionalFieldOf("weight", 0.0)
                    .forGetter(ArmorDefinitionData::weight)
    ).apply(instance, ArmorDefinitionData::new));
}