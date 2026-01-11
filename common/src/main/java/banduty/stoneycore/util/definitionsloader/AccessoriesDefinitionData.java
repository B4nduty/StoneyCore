package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Map;

public record AccessoriesDefinitionData(double armor, double toughness, String armorSlot, double hungerDrainMultiplier,
                                        Map<String, Double> deflectChance, double weight, ResourceLocation visoredHelmet) {
    public static final Codec<AccessoriesDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(AccessoriesDefinitionData::armor),
            Codec.DOUBLE.optionalFieldOf("toughness", 0.0).forGetter(AccessoriesDefinitionData::toughness),
            Codec.STRING.optionalFieldOf("armorSlot", "").forGetter(AccessoriesDefinitionData::armorSlot),
            Codec.DOUBLE.optionalFieldOf("hungerDrainMultiplier", 0.0).forGetter(AccessoriesDefinitionData::hungerDrainMultiplier),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("deflectChance", Map.of()).forGetter(AccessoriesDefinitionData::deflectChance),
            Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(AccessoriesDefinitionData::weight),
            ResourceLocation.CODEC.optionalFieldOf("visoredHelmet", new ResourceLocation("", "")).forGetter(AccessoriesDefinitionData::visoredHelmet)
    ).apply(instance, AccessoriesDefinitionData::new));

    public EquipmentSlot getArmorSlot() {
        if (armorSlot.isEmpty()) return null;
        try {
            return EquipmentSlot.valueOf(armorSlot.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}