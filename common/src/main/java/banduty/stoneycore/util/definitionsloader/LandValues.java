package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;

public record LandValues(int baseRadius, Map<Item, Integer> itemsToExpand, String expandFormula, int maxAllies,
                         double spawnChance, int maxVisitorsPerLand) {
    public static final Codec<LandValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("base_radius", 0)
                    .forGetter(LandValues::baseRadius),
            Codec.unboundedMap(ResourceLocation.CODEC.xmap(BuiltInRegistries.ITEM::get, BuiltInRegistries.ITEM::getKey), Codec.INT)
                    .optionalFieldOf("items_to_expand", Map.of())
                    .forGetter(LandValues::itemsToExpand),
            Codec.STRING.optionalFieldOf("expand_formula", "")
                    .forGetter(LandValues::expandFormula),
            Codec.INT.optionalFieldOf("maxAllies", -1)
                    .forGetter(LandValues::maxAllies),
            Codec.DOUBLE.optionalFieldOf("spawnChance", 0.15d)
                    .forGetter(LandValues::spawnChance),
            Codec.INT.optionalFieldOf("maxVisitorsPerLand", 3)
                    .forGetter(LandValues::maxVisitorsPerLand)
    ).apply(instance, LandValues::new));
}