package banduty.stoneycore.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record ArmorDefinitionData(Map<String, Double> deflectChance, double weight) {
    public static final Codec<ArmorDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(String::toUpperCase, s -> s), Codec.DOUBLE)
                    .optionalFieldOf("deflectChance", Map.of())
                    .forGetter(ArmorDefinitionData::deflectChance),
            Codec.DOUBLE.optionalFieldOf("weight", 0.0)
                    .forGetter(ArmorDefinitionData::weight)
    ).apply(instance, ArmorDefinitionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorDefinitionData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
}