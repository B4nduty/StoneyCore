package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCDamageCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class WeaponDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Map<Identifier, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final Identifier RELOAD_LISTENER_ID = new Identifier(StoneyCore.MOD_ID, "weapon_definitions_loader");

    @Override
    public Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager resourceManager,
                                          Profiler prepareProfiler, Profiler applyProfiler,
                                          Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            DEFINITIONS.clear();

            Map<Identifier, Resource> resources = resourceManager.findResources("definitions/weapon", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    com.google.gson.JsonElement element = com.google.gson.JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<WeaponDefinition> result = WeaponDefinition.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(weaponDef -> {
                                Identifier weaponId = Identifier.of(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/weapon/".length(), id.getPath().length() - 5)
                                );

                                EnumSet<Usage> usage = EnumSet.noneOf(Usage.class);
                                MeleeData meleeData = null;
                                RangedData rangedData = null;
                                AmmoData ammoData = null;

                                if (weaponDef.melee != null) {
                                    usage.add(Usage.MELEE);
                                    meleeData = weaponDef.melee;
                                }
                                if (weaponDef.ranged != null) {
                                    usage.add(Usage.RANGED);
                                    rangedData = weaponDef.ranged;
                                }
                                if (weaponDef.ammo != null) {
                                    usage.add(Usage.AMMO);
                                    ammoData = weaponDef.ammo;
                                }

                                DEFINITIONS.put(weaponId, new DefinitionData(usage, meleeData, rangedData, ammoData));
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {}, applyExecutor);
    }

    public static DefinitionData getData(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return getData(stack.getItem());
    }

    public static DefinitionData getData(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return DEFINITIONS.getOrDefault(id, new DefinitionData(EnumSet.noneOf(Usage.class), null, null, null));
    }

    public static boolean containsItem(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return containsItem(stack.getItem());
    }

    public static boolean containsItem(Item item) {
        return DEFINITIONS.containsKey(Registries.ITEM.getId(item));
    }

    public static boolean isMelee(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return isMelee(stack.getItem());
    }

    public static boolean isRanged(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return isRanged(stack.getItem());
    }

    public static boolean isAmmo(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return isAmmo(stack.getItem());
    }

    public static boolean isMelee(Item item) {
        DefinitionData data = getData(item);
        return data.melee() != null && data.usage().contains(Usage.MELEE);
    }

    public static boolean isRanged(Item item) {
        DefinitionData data = getData(item);
        return data.ranged() != null && data.usage().contains(Usage.RANGED);
    }

    public static boolean isAmmo(Item item) {
        DefinitionData data = getData(item);
        return data.ammo() != null && data.usage().contains(Usage.AMMO);
    }

    public record WeaponDefinition(MeleeData melee, RangedData ranged, AmmoData ammo) {
        public static final Codec<WeaponDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MeleeData.CODEC.optionalFieldOf("melee").forGetter(wd -> Optional.ofNullable(wd.melee)),
                RangedData.CODEC.optionalFieldOf("ranged").forGetter(wd -> Optional.ofNullable(wd.ranged)),
                AmmoData.CODEC.optionalFieldOf("ammo").forGetter(wd -> Optional.ofNullable(wd.ammo))
        ).apply(instance, (melee, ranged, ammo) -> new WeaponDefinition(melee.orElse(null), ranged.orElse(null), ammo.orElse(null))));
    }

    public record DefinitionData(EnumSet<Usage> usage, MeleeData melee, RangedData ranged, AmmoData ammo) {}

    public record MeleeData(
            Map<String, Float> damage,
            Map<String, Double> radius,
            int[] piercingAnimation,
            int animation,
            SCDamageCalculator.DamageType onlyDamageType,
            double deflectChance,
            double bonusKnockback
    ) {
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

    public record RangedData(
            float baseDamage,
            SCDamageCalculator.DamageType damageType,
            int maxUseTime,
            float speed,
            float divergence,
            int rechargeTime,
            boolean needsFlintAndSteel,
            UseAction useAction,
            Map<String, AmmoRequirementData> ammoRequirement,
            SoundEvent soundEvent
    ) {

        private static final Codec<UseAction> USE_ACTION_CODEC = Codec.STRING.xmap(
                str -> UseAction.valueOf(str.toUpperCase()),
                UseAction::name
        );

        private static final Codec<SoundEvent> SOUND_EVENT_CODEC = Identifier.CODEC.xmap(
                Registries.SOUND_EVENT::get,
                Registries.SOUND_EVENT::getId
        );

        public static final Codec<RangedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("baseDamage", 0f).forGetter(RangedData::baseDamage),
                SCDamageCalculator.DamageType.CODEC.optionalFieldOf("damageType").forGetter(rd -> Optional.ofNullable(rd.damageType)),
                Codec.INT.optionalFieldOf("maxUseTime", 0).forGetter(RangedData::maxUseTime),
                Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(RangedData::speed),
                Codec.FLOAT.optionalFieldOf("divergence", 0f).forGetter(RangedData::divergence),
                Codec.INT.optionalFieldOf("rechargeTime", 0).forGetter(RangedData::rechargeTime),
                Codec.BOOL.optionalFieldOf("needsFlintAndSteel", false).forGetter(RangedData::needsFlintAndSteel),
                USE_ACTION_CODEC.optionalFieldOf("useAction", UseAction.NONE).forGetter(RangedData::useAction),
                Codec.unboundedMap(Codec.STRING, AmmoRequirementData.CODEC).optionalFieldOf("ammoRequirement", Map.of()).forGetter(RangedData::ammoRequirement),
                SOUND_EVENT_CODEC.optionalFieldOf("soundEvent").forGetter(rd -> Optional.ofNullable(rd.soundEvent))
        ).apply(instance, (baseDamage, damageType, maxUseTime, speed, divergence, rechargeTime, needsFlintAndSteel, useAction, ammoRequirement, soundEvent) ->
                new RangedData(baseDamage, damageType.orElse(null), maxUseTime, speed, divergence, rechargeTime, needsFlintAndSteel, useAction, ammoRequirement, soundEvent.orElse(null))));
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

    public enum Usage {
        MELEE, RANGED, AMMO
    }
}