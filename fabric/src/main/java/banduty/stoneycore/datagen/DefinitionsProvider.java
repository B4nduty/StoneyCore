package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCDamageCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * This class provides a framework for generating JSON definitions that are loaded by
 * StoneyCore's various definition loaders at runtime. Extend one of the inner classes
 * to generate definitions for a specific system.
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * public class MyAccessoriesProvider extends DefinitionsProvider.Accessories {
 *     public MyAccessoriesProvider(PackOutput output) {
 *         super(output);
 *     }
 *
 *     @Override
 *     protected void generateDefinitions(BiConsumer<Item, DefinitionEntry> consumer) {
 *         consumer.accept(my_custom_item,
 *             Builder.create()
 *                 .armor(1.0)
 *                 .toughness(0.2)
 *                 .build()
 *         );
 *     }
 * }
 * }
 * </pre>
 */
public abstract class DefinitionsProvider implements DataProvider {
    protected final PackOutput output;
    protected final String subDirectory;

    protected DefinitionsProvider(PackOutput output, String subDirectory) {
        this.output = output;
        this.subDirectory = subDirectory;
    }

    @Override
    public abstract CompletableFuture<?> run(CachedOutput writer);

    @Override
    public abstract String getName();

    protected ResourceLocation id(String path) {
        return new ResourceLocation(StoneyCore.MOD_ID, path);
    }

    protected ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    // ACCESSORIES DEFINITIONS
    public abstract static class Accessories extends DefinitionsProvider {
        public Accessories(PackOutput output) {
            super(output, "definitions/accessories");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput writer) {
            Map<ResourceLocation, DefinitionEntry> entries = new TreeMap<>();

            generateDefinitions((item, definition) -> {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                entries.put(id, definition);
            });

            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceLocation, DefinitionEntry> entry : entries.entrySet()) {
                var jsonResult = DefinitionEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue());
                if (jsonResult.error().isPresent()) {
                    throw new IllegalStateException("Failed to encode definition: " + jsonResult.error().get().message());
                }

                var jsonElement = jsonResult.result().get();
                ResourceLocation filePath = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        "definitions/accessories/" + entry.getKey().getPath() + ".json"
                );

                futures.add(DataProvider.saveStable(writer, jsonElement,
                        output.getOutputFolder().resolve("data/" + filePath.getNamespace() + "/" + filePath.getPath())));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        @Override
        public String getName() {
            return "Accessories Definitions";
        }

        public interface ItemDefinitionConsumer extends BiConsumer<Item, DefinitionEntry> {
            default void accept(DefinitionEntry definition, Item... items) {
                for (Item item : items) {
                    accept(item, definition);
                }
            }
        }

        protected abstract void generateDefinitions(ItemDefinitionConsumer consumer);

        public record DefinitionEntry(
                double armor,
                double toughness,
                String armorSlot,
                double hungerDrainMultiplier,
                double deflectChance,
                double weight,
                ResourceLocation visoredHelmet
        ) {
            public static final Codec<DefinitionEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(DefinitionEntry::armor),
                            Codec.DOUBLE.optionalFieldOf("toughness", 0.0).forGetter(DefinitionEntry::toughness),
                            Codec.STRING.optionalFieldOf("armorSlot", "").forGetter(DefinitionEntry::armorSlot),
                            Codec.DOUBLE.optionalFieldOf("hungerDrainMultiplier", 0.0).forGetter(DefinitionEntry::hungerDrainMultiplier),
                            Codec.DOUBLE.optionalFieldOf("deflectChance", 0.0).forGetter(DefinitionEntry::deflectChance),
                            Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(DefinitionEntry::weight),
                            ResourceLocation.CODEC.optionalFieldOf("visoredHelmet", new ResourceLocation("", "")).forGetter(DefinitionEntry::visoredHelmet)
                    ).apply(instance, DefinitionEntry::new)
            );
        }

        public static class Builder {
            private double armor = 0.0;
            private double toughness = 0.0;
            private String armorSlot = "";
            private double hungerDrainMultiplier = 0.0;
            private double deflectChance = 0.0;
            private double weight = 0.0;
            private ResourceLocation visoredHelmet = new ResourceLocation("", "");

            private Builder() {}

            public static Builder create() {
                return new Builder();
            }

            public Builder armor(double armor) {
                this.armor = armor;
                return this;
            }

            public Builder toughness(double toughness) {
                this.toughness = toughness;
                return this;
            }

            public Builder armorSlot(String armorSlot) {
                this.armorSlot = armorSlot;
                return this;
            }

            public Builder armorSlotHead() {
                return armorSlot("HEAD");
            }

            public Builder armorSlotChest() {
                return armorSlot("CHEST");
            }

            public Builder armorSlotLegs() {
                return armorSlot("LEGS");
            }

            public Builder armorSlotFeet() {
                return armorSlot("FEET");
            }

            public Builder hungerDrainMultiplier(double multiplier) {
                this.hungerDrainMultiplier = multiplier;
                return this;
            }

            public Builder deflectChance(double chance) {
                this.deflectChance = chance;
                return this;
            }

            public Builder weight(double weight) {
                this.weight = weight;
                return this;
            }

            public Builder visoredHelmet(ResourceLocation helmetId) {
                this.visoredHelmet = helmetId;
                return this;
            }

            public Builder visoredHelmet(String namespace, String path) {
                return visoredHelmet(new ResourceLocation(namespace, path));
            }

            public DefinitionEntry build() {
                return new DefinitionEntry(armor, toughness, armorSlot, hungerDrainMultiplier,
                        deflectChance, weight, visoredHelmet);
            }
        }
    }

    // ARMOR DEFINITIONS
    public abstract static class Armor extends DefinitionsProvider {
        public Armor(PackOutput output) {
            super(output, "definitions/armor");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput writer) {
            Map<ResourceLocation, DefinitionEntry> entries = new TreeMap<>();

            generateDefinitions((item, definition) -> {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                entries.put(id, definition);
            });

            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceLocation, DefinitionEntry> entry : entries.entrySet()) {
                var jsonResult = DefinitionEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue());
                if (jsonResult.error().isPresent()) {
                    throw new IllegalStateException("Failed to encode definition: " + jsonResult.error().get().message());
                }

                var jsonElement = jsonResult.result().get();
                ResourceLocation filePath = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        "definitions/armor/" + entry.getKey().getPath() + ".json"
                );

                futures.add(DataProvider.saveStable(writer, jsonElement,
                        output.getOutputFolder().resolve("data/" + filePath.getNamespace() + "/" + filePath.getPath())));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        @Override
        public String getName() {
            return "Armor Definitions";
        }

        protected abstract void generateDefinitions(BiConsumer<Item, DefinitionEntry> consumer);

        public record DefinitionEntry(
                Map<String, Double> damageResistance,
                Map<String, Double> deflectChance,
                double weight
        ) {
            public static final Codec<DefinitionEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.unboundedMap(Codec.STRING.xmap(String::toUpperCase, s -> s), Codec.DOUBLE)
                                    .optionalFieldOf("damageResistance", Map.of())
                                    .forGetter(DefinitionEntry::damageResistance),
                            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                                    .optionalFieldOf("deflectChance", Map.of())
                                    .forGetter(DefinitionEntry::deflectChance),
                            Codec.DOUBLE.optionalFieldOf("weight", 0.0)
                                    .forGetter(DefinitionEntry::weight)
                    ).apply(instance, DefinitionEntry::new)
            );
        }

        public static class Builder {
            private double slashingResistance = 0.0;
            private double bludgeoningResistance = 0.0;
            private double piercingResistance = 0.0;
            private Map<String, Double> additionalResistances = new HashMap<>();
            private Map<String, Double> deflectChance = new HashMap<>();
            private double weight = 0.0;

            private Builder() {}

            public static Builder create() {
                return new Builder();
            }

            public Builder damageResistance(double slashing, double bludgeoning, double piercing) {
                this.slashingResistance = slashing;
                this.bludgeoningResistance = bludgeoning;
                this.piercingResistance = piercing;
                return this;
            }

            public Builder slashingResistance(double resistance) {
                this.slashingResistance = resistance;
                return this;
            }

            public Builder bludgeoningResistance(double resistance) {
                this.bludgeoningResistance = resistance;
                return this;
            }

            public Builder piercingResistance(double resistance) {
                this.piercingResistance = resistance;
                return this;
            }

            public Builder damageResistance(String damageType, double resistance) {
                this.additionalResistances.put(damageType.toUpperCase(), resistance);
                return this;
            }

            public Builder deflectChance(String damageType, double chance) {
                this.deflectChance.put(damageType, chance);
                return this;
            }

            public Builder deflectChance(EntityType<?> entityType, double chance) {
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                this.deflectChance.put(entityId.toString(), chance);
                return this;
            }

            public Builder deflectChance(double chance, EntityType<?>... entityTypes) {
                for (EntityType<?> entityType : entityTypes) {
                    deflectChance(entityType, chance);
                }
                return this;
            }

            public Builder weight(double weight) {
                this.weight = weight;
                return this;
            }

            public DefinitionEntry build() {
                Map<String, Double> allResistances = new HashMap<>(additionalResistances);

                if (slashingResistance != 0.0) {
                    allResistances.put("SLASHING", slashingResistance);
                }
                if (bludgeoningResistance != 0.0) {
                    allResistances.put("BLUDGEONING", bludgeoningResistance);
                }
                if (piercingResistance != 0.0) {
                    allResistances.put("PIERCING", piercingResistance);
                }

                return new DefinitionEntry(allResistances, deflectChance, weight);
            }
        }
    }

    // LAND DEFINITIONS
    public abstract static class Land extends DefinitionsProvider {
        public Land(PackOutput output) {
            super(output, "definitions/lands");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput writer) {
            Map<ResourceLocation, DefinitionEntry> entries = new TreeMap<>();

            generateDefinitions((item, definition) -> {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                entries.put(id, definition);
            });

            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceLocation, DefinitionEntry> entry : entries.entrySet()) {
                var jsonResult = DefinitionEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue());
                if (jsonResult.error().isPresent()) {
                    throw new IllegalStateException("Failed to encode definition: " + jsonResult.error().get().message());
                }

                var jsonElement = jsonResult.result().get();
                ResourceLocation filePath = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        "definitions/lands/" + entry.getKey().getPath() + ".json"
                );

                futures.add(DataProvider.saveStable(writer, jsonElement,
                        output.getOutputFolder().resolve("data/" + filePath.getNamespace() + "/" + filePath.getPath())));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        @Override
        public String getName() {
            return "Land Definitions";
        }

        protected abstract void generateDefinitions(BiConsumer<Item, DefinitionEntry> consumer);

        public record DefinitionEntry(
                int baseRadius,
                Map<Item, Integer> itemsToExpand,
                String expandFormula,
                int maxAllies
        ) {
            public static final Codec<DefinitionEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.INT.optionalFieldOf("base_radius", 0)
                                    .forGetter(DefinitionEntry::baseRadius),
                            Codec.unboundedMap(ResourceLocation.CODEC.xmap(BuiltInRegistries.ITEM::get, BuiltInRegistries.ITEM::getKey), Codec.INT)
                                    .optionalFieldOf("items_to_expand", Map.of())
                                    .forGetter(DefinitionEntry::itemsToExpand),
                            Codec.STRING.optionalFieldOf("expand_formula", "")
                                    .forGetter(DefinitionEntry::expandFormula),
                            Codec.INT.optionalFieldOf("maxAllies", -1)
                                    .forGetter(DefinitionEntry::maxAllies)
                    ).apply(instance, DefinitionEntry::new)
            );
        }

        public static class Builder {
            private int baseRadius = 0;
            private Map<Item, Integer> itemsToExpand = new HashMap<>();
            private String expandFormula = "";
            private int maxAllies = -1;

            private Builder() {}

            public static Builder create() {
                return new Builder();
            }

            public Builder baseRadius(int baseRadius) {
                this.baseRadius = baseRadius;
                return this;
            }

            public Builder itemToExpand(Item item, int amount) {
                this.itemsToExpand.put(item, amount);
                return this;
            }

            public Builder itemToExpand(ResourceLocation itemId, int amount) {
                this.itemsToExpand.put(BuiltInRegistries.ITEM.get(itemId), amount);
                return this;
            }

            public Builder expandFormula(String formula) {
                this.expandFormula = formula;
                return this;
            }

            public Builder maxAllies(int maxAllies) {
                this.maxAllies = maxAllies;
                return this;
            }

            public DefinitionEntry build() {
                return new DefinitionEntry(baseRadius, itemsToExpand, expandFormula, maxAllies);
            }
        }
    }

    // SIEGE ENGINE DEFINITIONS
    public abstract static class SiegeEngine extends DefinitionsProvider {
        public SiegeEngine(PackOutput output) {
            super(output, "definitions/siege_engines");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput writer) {
            Map<ResourceLocation, DefinitionEntry> entries = new TreeMap<>();

            generateDefinitions((item, definition) -> {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                entries.put(id, definition);
            });

            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceLocation, DefinitionEntry> entry : entries.entrySet()) {
                var jsonResult = DefinitionEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue());
                if (jsonResult.error().isPresent()) {
                    throw new IllegalStateException("Failed to encode definition: " + jsonResult.error().get().message());
                }

                var jsonElement = jsonResult.result().get();
                ResourceLocation filePath = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        "definitions/siege_engines/" + entry.getKey().getPath() + ".json"
                );

                futures.add(DataProvider.saveStable(writer, jsonElement,
                        output.getOutputFolder().resolve("data/" + filePath.getNamespace() + "/" + filePath.getPath())));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        @Override
        public String getName() {
            return "Siege Engine Definitions";
        }

        protected abstract void generateDefinitions(BiConsumer<Item, DefinitionEntry> consumer);

        public record DefinitionEntry(
                double playerSpeed,
                double horseSpeed,
                double knockback,
                double baseDamage,
                int baseReload,
                float projectileSpeed,
                float accuracyMultiplier
        ) {
            public static final Codec<DefinitionEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.DOUBLE.optionalFieldOf("playerSpeed", 0.05).forGetter(DefinitionEntry::playerSpeed),
                            Codec.DOUBLE.optionalFieldOf("horseSpeed", 0.1).forGetter(DefinitionEntry::horseSpeed),
                            Codec.DOUBLE.optionalFieldOf("knockback", 0.0).forGetter(DefinitionEntry::knockback),
                            Codec.DOUBLE.optionalFieldOf("baseDamage", 25.0).forGetter(DefinitionEntry::baseDamage),
                            Codec.INT.optionalFieldOf("baseReload", 90).forGetter(DefinitionEntry::baseReload),
                            Codec.FLOAT.optionalFieldOf("projectileSpeed", 140.0f).forGetter(DefinitionEntry::projectileSpeed),
                            Codec.FLOAT.optionalFieldOf("accuracyMultiplier", 1.2f).forGetter(DefinitionEntry::accuracyMultiplier)
                    ).apply(instance, DefinitionEntry::new)
            );
        }

        public static class Builder {
            private double playerSpeed = 0.05;
            private double horseSpeed = 0.1;
            private double knockback = 0.0;
            private double baseDamage = 25.0;
            private int baseReload = 90;
            private float projectileSpeed = 140.0f;
            private float accuracyMultiplier = 1.2f;

            private Builder() {}

            public static Builder create() {
                return new Builder();
            }

            public Builder playerSpeed(double playerSpeed) {
                this.playerSpeed = playerSpeed;
                return this;
            }

            public Builder horseSpeed(double horseSpeed) {
                this.horseSpeed = horseSpeed;
                return this;
            }

            public Builder knockback(double knockback) {
                this.knockback = knockback;
                return this;
            }

            public Builder baseDamage(double baseDamage) {
                this.baseDamage = baseDamage;
                return this;
            }

            public Builder baseReload(int baseReload) {
                this.baseReload = baseReload;
                return this;
            }

            public Builder projectileSpeed(float projectileSpeed) {
                this.projectileSpeed = projectileSpeed;
                return this;
            }

            public Builder accuracyMultiplier(float accuracyMultiplier) {
                this.accuracyMultiplier = accuracyMultiplier;
                return this;
            }

            public DefinitionEntry build() {
                return new DefinitionEntry(playerSpeed, horseSpeed, knockback, baseDamage,
                        baseReload, projectileSpeed, accuracyMultiplier);
            }
        }
    }

    // WEAPON DEFINITIONS
    public abstract static class Weapon extends DefinitionsProvider {
        public Weapon(PackOutput output) {
            super(output, "definitions/weapon");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput writer) {
            Map<ResourceLocation, WeaponEntry> entries = new TreeMap<>();

            generateDefinitions((item, definition) -> {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                entries.put(id, definition);
            });

            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceLocation, WeaponEntry> entry : entries.entrySet()) {
                var jsonResult = WeaponEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue());
                if (jsonResult.error().isPresent()) {
                    throw new IllegalStateException("Failed to encode definition: " + jsonResult.error().get().message());
                }

                var jsonElement = jsonResult.result().get();
                ResourceLocation filePath = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        "definitions/weapon/" + entry.getKey().getPath() + ".json"
                );

                futures.add(DataProvider.saveStable(writer, jsonElement,
                        output.getOutputFolder().resolve("data/" + filePath.getNamespace() + "/" + filePath.getPath())));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        @Override
        public String getName() {
            return "Weapon Definitions";
        }

        public interface ItemDefinitionConsumer extends BiConsumer<Item, WeaponEntry> {
            default void accept(WeaponEntry weaponEntry, Item... items) {
                for (Item item : items) {
                    accept(item, weaponEntry);
                }
            }
        }

        protected abstract void generateDefinitions(ItemDefinitionConsumer consumer);

        public static final class WeaponBuilder {
            private MeleeData melee = null;
            private RangedData ranged = null;
            private AmmoData ammo = null;

            private WeaponBuilder() {}

            public static WeaponBuilder melee(MeleeData melee) {
                WeaponBuilder builder = new WeaponBuilder();
                builder.melee = melee;
                return builder;
            }

            public static WeaponBuilder ranged(RangedData ranged) {
                WeaponBuilder builder = new WeaponBuilder();
                builder.ranged = ranged;
                return builder;
            }

            public static WeaponBuilder ammo(AmmoData ammo) {
                WeaponBuilder builder = new WeaponBuilder();
                builder.ammo = ammo;
                return builder;
            }

            public static WeaponBuilder meleeRanged(MeleeData melee, RangedData ranged) {
                WeaponBuilder builder = new WeaponBuilder();
                builder.melee = melee;
                builder.ranged = ranged;
                return builder;
            }

            public static WeaponBuilder rangedWithAmmo(RangedData ranged, AmmoData ammo) {
                WeaponBuilder builder = new WeaponBuilder();
                builder.ranged = ranged;
                builder.ammo = ammo;
                return builder;
            }

            public static WeaponBuilder all(MeleeData melee, RangedData ranged, AmmoData ammo) {
                WeaponBuilder builder = new WeaponBuilder();
                builder.melee = melee;
                builder.ranged = ranged;
                builder.ammo = ammo;
                return builder;
            }

            public WeaponEntry build() {
                return new WeaponEntry(melee, ranged, ammo);
            }
        }

        public record WeaponEntry(MeleeData melee, RangedData ranged, AmmoData ammo) {
            public static final Codec<WeaponEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            MeleeData.CODEC.optionalFieldOf("melee").forGetter(wd -> Optional.ofNullable(wd.melee)),
                            RangedData.CODEC.optionalFieldOf("ranged").forGetter(wd -> Optional.ofNullable(wd.ranged)),
                            AmmoData.CODEC.optionalFieldOf("ammo").forGetter(wd -> Optional.ofNullable(wd.ammo))
                    ).apply(instance, (melee, ranged, ammo) -> new WeaponEntry(melee.orElse(null), ranged.orElse(null), ammo.orElse(null)))
            );
        }

        public static final class MeleeBuilder {
            private Map<String, Float> damage = new HashMap<>();
            private Map<String, Double> radius = new HashMap<>();
            private int[] piercingAnimation = new int[0];
            private int animation = 0;
            private SCDamageCalculator.DamageType onlyDamageType = null;
            private double deflectChance = 0.0;
            private double bonusKnockback = 0.0;

            private MeleeBuilder() {}

            public static MeleeBuilder create() {
                return new MeleeBuilder();
            }

            public MeleeBuilder damage(String damageType, float amount) {
                this.damage.put(damageType.toUpperCase(), amount);
                return this;
            }

            public MeleeBuilder damage(float slashing, float bludgeoning, float piercing) {
                this.damage.put("SLASHING", slashing);
                this.damage.put("BLUDGEONING", bludgeoning);
                this.damage.put("PIERCING", piercing);
                return this;
            }

            public MeleeBuilder radius(String direction, double radius) {
                this.radius.put(direction, radius);
                return this;
            }

            public MeleeBuilder levelRadii(double level0, double level1, double level2, double level3, double level4) {
                this.radius.put("level_0", level0);
                this.radius.put("level_1", level1);
                this.radius.put("level_2", level2);
                this.radius.put("level_3", level3);
                this.radius.put("level_4", level4);
                return this;
            }

            public MeleeBuilder piercingAnimation(int... frames) {
                this.piercingAnimation = frames;
                return this;
            }

            public MeleeBuilder animation(int animation) {
                this.animation = animation;
                return this;
            }

            public MeleeBuilder onlyDamageType(SCDamageCalculator.DamageType damageType) {
                this.onlyDamageType = damageType;
                return this;
            }

            public MeleeBuilder deflectChance(double chance) {
                this.deflectChance = chance;
                return this;
            }

            public MeleeBuilder bonusKnockback(double knockback) {
                this.bonusKnockback = knockback;
                return this;
            }

            public MeleeData build() {
                return new MeleeData(damage, radius, piercingAnimation, animation, onlyDamageType, deflectChance, bonusKnockback);
            }
        }

        public static final class RangedBuilder {
            private String id = "bow";
            private float baseDamage = 0f;
            private SCDamageCalculator.DamageType damageType = null;
            private int maxUseTime = 0;
            private float speed = 0f;
            private float divergence = 0f;
            private int rechargeTime = 0;
            private boolean needsFlintAndSteel = false;
            private UseAnim useAnim = UseAnim.NONE;
            private Map<String, AmmoRequirementData> ammoRequirement = new HashMap<>();
            private SoundEvent soundEvent = null;

            private RangedBuilder() {}

            public static RangedBuilder create() {
                return new RangedBuilder();
            }

            public RangedBuilder id(String id) {
                this.id = id;
                return this;
            }

            public RangedBuilder baseDamage(float damage) {
                this.baseDamage = damage;
                return this;
            }

            public RangedBuilder damageType(SCDamageCalculator.DamageType damageType) {
                this.damageType = damageType;
                return this;
            }

            public RangedBuilder maxUseTime(int ticks) {
                this.maxUseTime = ticks;
                return this;
            }

            public RangedBuilder speed(float speed) {
                this.speed = speed;
                return this;
            }

            public RangedBuilder divergence(float divergence) {
                this.divergence = divergence;
                return this;
            }

            public RangedBuilder rechargeTime(int seconds) {
                this.rechargeTime = seconds;
                return this;
            }

            public RangedBuilder needsFlintAndSteel(boolean needs) {
                this.needsFlintAndSteel = needs;
                return this;
            }

            public RangedBuilder useAnim(UseAnim anim) {
                this.useAnim = anim;
                return this;
            }

            public RangedBuilder ammoRequirement(String ammoType, AmmoRequirementData requirement) {
                this.ammoRequirement.put(ammoType, requirement);
                return this;
            }

            public RangedBuilder ammoRequirement(String ammoType, Item item, int amount) {
                HashSet<String> items = new HashSet<>();
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                items.add(itemId.toString());
                return ammoRequirement(ammoType, new AmmoRequirementData(items, amount));
            }

            public RangedBuilder ammoRequirement(String ammoType, int amount, Item... items) {
                return ammoRequirement(ammoType, Arrays.asList(items), amount);
            }

            public RangedBuilder ammoRequirement(String ammoType, List<Item> itemList, int amount) {
                HashSet<String> items = new HashSet<>();
                for (Item item : itemList) {
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                    items.add(itemId.toString());
                }
                return ammoRequirement(ammoType, new AmmoRequirementData(items, amount));
            }

            public RangedBuilder soundEvent(SoundEvent sound) {
                this.soundEvent = sound;
                return this;
            }

            public RangedBuilder soundEvent(ResourceLocation soundId) {
                this.soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundId);
                return this;
            }

            public RangedData build() {
                return new RangedData(id, baseDamage, damageType, maxUseTime, speed, divergence,
                        rechargeTime, needsFlintAndSteel, useAnim, ammoRequirement, soundEvent);
            }
        }

        public static final class AmmoBuilder {
            private double deflectChance = 0.0;

            private AmmoBuilder() {}

            public static AmmoBuilder create() {
                return new AmmoBuilder();
            }

            public AmmoBuilder deflectChance(double chance) {
                this.deflectChance = chance;
                return this;
            }

            public AmmoData build() {
                return new AmmoData(deflectChance);
            }
        }

        public record MeleeData(
                Map<String, Float> damage,
                Map<String, Double> radius,
                int[] piercingAnimation,
                int animation,
                SCDamageCalculator.DamageType onlyDamageType,
                double deflectChance,
                double bonusKnockback
        ) {
            public static final Codec<MeleeData> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
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
                            new MeleeData(damage, radius, piercingAnimation, animation, onlyDamageType.orElse(null), deflectChance, bonusKnockback))
            );
        }

        public record RangedData(
                String id,
                float baseDamage,
                SCDamageCalculator.DamageType damageType,
                int maxUseTime,
                float speed,
                float divergence,
                int rechargeTime,
                boolean needsFlintAndSteel,
                UseAnim useAnim,
                Map<String, AmmoRequirementData> ammoRequirement,
                SoundEvent soundEvent
        ) {
            private static final Codec<UseAnim> USE_ACTION_CODEC =
                    Codec.STRING.xmap(str -> UseAnim.valueOf(str.toUpperCase()), UseAnim::name);

            private static final Codec<SoundEvent> SOUND_EVENT_CODEC =
                    ResourceLocation.CODEC.xmap(BuiltInRegistries.SOUND_EVENT::get, BuiltInRegistries.SOUND_EVENT::getKey);

            public static final Codec<RangedData> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
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
                            new RangedData(id, baseDamage, damageType.orElse(null), maxUseTime, speed, divergence, rechargeTime, needsFlintAndSteel, useAction, ammoRequirement, soundEvent.orElse(null)))
            );
        }

        public record AmmoData(double deflectChance) {
            public static final Codec<AmmoData> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.DOUBLE.optionalFieldOf("deflectChance", 0.0).forGetter(AmmoData::deflectChance)
                    ).apply(instance, AmmoData::new)
            );
        }

        public record AmmoRequirementData(HashSet<String> itemIds, int amount) {
            public static final Codec<AmmoRequirementData> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new).fieldOf("items").forGetter(AmmoRequirementData::itemIds),
                            Codec.INT.fieldOf("amount").forGetter(AmmoRequirementData::amount)
                    ).apply(instance, AmmoRequirementData::new)
            );
        }

        public enum Usage {
            MELEE,
            RANGED,
            AMMO
        }
    }
}