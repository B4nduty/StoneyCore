package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

public record DamageSourceConfig(Map<String, Boolean> entityDamageSources, Map<String, Boolean> itemDamageSources, Map<String, Boolean> damageTypeSources) {
    public static final DamageSourceConfig DEFAULT = new DamageSourceConfig(
            Map.of(
                    "minecraft:player", false,
                    "minecraft:vindicator", true,
                    "*", false
            ),
            Map.of(
                    "minecraft:wooden_axe", true,
                    "minecraft:stone_axe", true,
                    "minecraft:iron_axe", true,
                    "minecraft:golden_axe", true,
                    "minecraft:diamond_axe", true,
                    "minecraft:netherite_axe", true,
                    "*", false
            ),
            Map.of(
                    "projectile", false,
                    "explosion", true,
                    "*", false
            )
    );

    public static final Codec<DamageSourceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("entityDamageSources", new HashMap<>()).forGetter(DamageSourceConfig::entityDamageSources),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("itemDamageSources", new HashMap<>()).forGetter(DamageSourceConfig::itemDamageSources),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("damageTypeSources", new HashMap<>()).forGetter(DamageSourceConfig::damageTypeSources)
    ).apply(instance, DamageSourceConfig::new));

    public boolean canEntityDamage(String entityId) {
        if (entityDamageSources.containsKey(entityId)) {
            return entityDamageSources.get(entityId);
        }
        return entityDamageSources.getOrDefault("*", false);
    }

    public boolean canItemDamage(String itemId) {
        if (itemDamageSources.containsKey(itemId)) {
            return itemDamageSources.get(itemId);
        }
        return itemDamageSources.getOrDefault("*", false);
    }

    public boolean canDamageTypeDamage(String damageType) {
        if (damageTypeSources.containsKey(damageType)) {
            return damageTypeSources.get(damageType);
        }
        return damageTypeSources.getOrDefault("*", false);
    }
}
