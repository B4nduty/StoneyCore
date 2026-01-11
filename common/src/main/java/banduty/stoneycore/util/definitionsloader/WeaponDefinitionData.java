package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.util.SCDamageCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.UseAnim;

import java.util.*;

public record WeaponDefinitionData(EnumSet<Usage> usage, MeleeData melee, RangedData ranged, AmmoData ammo) {
    public enum Usage { MELEE, RANGED, AMMO }

    public record MeleeData(Map<String, Float> damage, Map<String, Double> radius, int[] piercingAnimation,
                            int animation, SCDamageCalculator.DamageType onlyDamageType, double deflectChance, double bonusKnockback) {
        public static final Codec<MeleeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.FLOAT).xmap(
                        map -> {
                            Map<String, Float> upperCaseMap = new HashMap<>();
                            for (Map.Entry<String, Float> entry : map.entrySet()) {
                                upperCaseMap.put(entry.getKey().toUpperCase(), entry.getValue());
                            }
                            return upperCaseMap;
                        },
                        map -> map
                ).optionalFieldOf("damage", Map.of()).forGetter(MeleeData::damage),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("radius", Map.of()).forGetter(MeleeData::radius),
                Codec.INT.listOf().xmap(
                        list -> list.stream().mapToInt(i -> i).toArray(),
                        array -> Arrays.stream(array).boxed().toList()
                ).optionalFieldOf("piercingAnimation", new int[0]).forGetter(MeleeData::piercingAnimation),
                Codec.INT.optionalFieldOf("animation", 0).forGetter(MeleeData::animation),
                SCDamageCalculator.DamageType.CODEC.optionalFieldOf("onlyDamageType").forGetter(md -> Optional.ofNullable(md.onlyDamageType)),
                Codec.DOUBLE.optionalFieldOf("deflectChance", 0.0).forGetter(MeleeData::deflectChance),
                Codec.DOUBLE.optionalFieldOf("bonusKnockback", 0.0).forGetter(MeleeData::bonusKnockback)
        ).apply(instance, (damage, radius, piercingAnimation, animation, onlyDamageType, deflectChance, bonusKnockback) ->
                new MeleeData(damage, radius, piercingAnimation, animation, onlyDamageType.orElse(null), deflectChance, bonusKnockback)));
    }

    public record RangedData(String id, float baseDamage, SCDamageCalculator.DamageType damageType, int maxUseTime,
                             float speed, float divergence, int rechargeTime, boolean needsFlintAndSteel, UseAnim useAnim,
                             Map<String, AmmoRequirementData> ammoRequirement, SoundEvent soundEvent) {

        private static final Codec<UseAnim> USE_ACTION_CODEC =
                Codec.STRING.xmap(str -> UseAnim.valueOf(str.toUpperCase()), UseAnim::name);

        private static final Codec<SoundEvent> SOUND_EVENT_CODEC =
                ResourceLocation.CODEC.xmap(BuiltInRegistries.SOUND_EVENT::get, BuiltInRegistries.SOUND_EVENT::getKey);

        public static final Codec<RangedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("id", "bow").forGetter(RangedData::id),
                Codec.FLOAT.optionalFieldOf("baseDamage", 0f).forGetter(RangedData::baseDamage),
                SCDamageCalculator.DamageType.CODEC.optionalFieldOf("damageType").forGetter(rd -> Optional.ofNullable(rd.damageType)),
                Codec.INT.optionalFieldOf("maxUseTime", 0).forGetter(RangedData::maxUseTime),
                Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(RangedData::speed),
                Codec.FLOAT.optionalFieldOf("divergence", 0f).forGetter(RangedData::divergence),
                Codec.INT.optionalFieldOf("rechargeTime", 0).forGetter(RangedData::rechargeTime),
                Codec.BOOL.optionalFieldOf("needsFlintAndSteel", false).forGetter(RangedData::needsFlintAndSteel),
                USE_ACTION_CODEC.optionalFieldOf("useAnim", UseAnim.NONE).forGetter(RangedData::useAnim),
                Codec.unboundedMap(Codec.STRING, AmmoRequirementData.CODEC).optionalFieldOf("ammoRequirement", Map.of()).forGetter(RangedData::ammoRequirement),
                SOUND_EVENT_CODEC.optionalFieldOf("soundEvent").forGetter(rd -> Optional.ofNullable(rd.soundEvent))
        ).apply(instance, (id, baseDamage, damageType, maxUseTime, speed, divergence, rechargeTime, needsFlintAndSteel, useAction, ammoRequirement, soundEvent) ->
                new RangedData(id, baseDamage, damageType.orElse(null), maxUseTime, speed, divergence, rechargeTime, needsFlintAndSteel, useAction, ammoRequirement, soundEvent.orElse(null))));
    }

    public record AmmoData(double deflectChance) {
        public static final Codec<AmmoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("deflectChance", 0.0).forGetter(AmmoData::deflectChance)
        ).apply(instance, AmmoData::new));
    }

    public record AmmoRequirementData(HashSet<String> itemIds, int amount) {
        public static final Codec<AmmoRequirementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new).fieldOf("items").forGetter(AmmoRequirementData::itemIds),
                Codec.INT.fieldOf("amount").forGetter(AmmoRequirementData::amount)
        ).apply(instance, AmmoRequirementData::new));
    }

    public record WeaponDefinition(MeleeData melee, RangedData ranged, AmmoData ammo) {
        public static final Codec<WeaponDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MeleeData.CODEC.optionalFieldOf("melee").forGetter(wd -> Optional.ofNullable(wd.melee)),
                RangedData.CODEC.optionalFieldOf("ranged").forGetter(wd -> Optional.ofNullable(wd.ranged)),
                AmmoData.CODEC.optionalFieldOf("ammo").forGetter(wd -> Optional.ofNullable(wd.ammo))
        ).apply(instance, (melee, ranged, ammo) -> new WeaponDefinition(melee.orElse(null), ranged.orElse(null), ammo.orElse(null))));
    }
}