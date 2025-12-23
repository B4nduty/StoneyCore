package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCDamageCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class WeaponDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Map<ResourceLocation, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final ResourceLocation RELOAD_LISTENER_ID = new ResourceLocation(StoneyCore.MOD_ID, "weapon_definitions_loader");

    @Override
    public ResourceLocation getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier synchronizer,
                                                   @NotNull ResourceManager resourceManager,
                                                   @NotNull ProfilerFiller prepareProfiler,
                                                   @NotNull ProfilerFiller applyProfiler,
                                                   @NotNull Executor prepareExecutor,
                                                   @NotNull Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            DEFINITIONS.clear();

            Map<ResourceLocation, Resource> resources = resourceManager.listResources("definitions/weapon", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.open()) {
                    com.google.gson.JsonElement element = com.google.gson.JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<WeaponDefinition> result = WeaponDefinition.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(weaponDef -> {
                                ResourceLocation weaponId = ResourceLocation.tryBuild(
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
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {}, applyExecutor);
    }

    public static DefinitionData getData(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return getData(stack.getItem());
    }

    public static DefinitionData getData(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return DEFINITIONS.getOrDefault(id, new DefinitionData(EnumSet.noneOf(Usage.class), null, null, null));
    }

    public static boolean containsItem(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return containsItem(stack.getItem());
    }

    public static boolean containsItem(Item item) {
        return DEFINITIONS.containsKey(BuiltInRegistries.ITEM.getKey(item));
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

    public enum Usage {
        MELEE, RANGED, AMMO
    }
}