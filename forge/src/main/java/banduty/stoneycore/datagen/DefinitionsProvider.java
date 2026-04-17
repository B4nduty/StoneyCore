package banduty.stoneycore.datagen;

import banduty.stoneycore.combat.melee.SCDamageType;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
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

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

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

    public abstract static class Accessories extends DefinitionsProvider {
        public Accessories(PackOutput output) { super(output, "definitions/accessories"); }

        @Override
        public CompletableFuture<?> run(CachedOutput writer) {
            Map<ResourceLocation, DefinitionEntry> entries = new TreeMap<>();
            generateDefinitions((item, definition) -> entries.put(BuiltInRegistries.ITEM.getKey(item), definition));
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceLocation, DefinitionEntry> entry : entries.entrySet()) {
                Path path = output.getOutputFolder().resolve("data/" + entry.getKey().getNamespace() + "/definitions/accessories/" + entry.getKey().getPath() + ".json");
                futures.add(DataProvider.saveStable(writer, DefinitionEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {}), path));
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        @Override public String getName() { return "Accessories Definitions"; }

        protected abstract void generateDefinitions(AccessoriesConsumer consumer);

        @FunctionalInterface
        public interface AccessoriesConsumer {
            void accept(Item item, DefinitionEntry definition);

            default void accept(DefinitionEntry definition, Item... items) {
                for (Item item : items) {
                    accept(item, definition);
                }
            }
        }

        public record DefinitionEntry(double armor, double toughness, String armorSlot, double hungerDrainMultiplier, double deflectChance, double weight, ResourceLocation visoredHelmet) {
            public static final Codec<DefinitionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(DefinitionEntry::armor),
                    Codec.DOUBLE.optionalFieldOf("toughness", 0.0).forGetter(DefinitionEntry::toughness),
                    Codec.STRING.optionalFieldOf("armorSlot", "").forGetter(DefinitionEntry::armorSlot),
                    Codec.DOUBLE.optionalFieldOf("hungerDrainMultiplier", 0.0).forGetter(DefinitionEntry::hungerDrainMultiplier),
                    Codec.DOUBLE.optionalFieldOf("deflectChance", 0.0).forGetter(DefinitionEntry::deflectChance),
                    Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(DefinitionEntry::weight),
                    ResourceLocation.CODEC.optionalFieldOf("visoredHelmet", new ResourceLocation("", "")).forGetter(DefinitionEntry::visoredHelmet)
            ).apply(instance, DefinitionEntry::new));
        }

        public static class Builder {
            private double armor, toughness, hunger, deflect, weight;
            private String slot = "";
            private ResourceLocation visor = new ResourceLocation("", "");
            public static Builder create() { return new Builder(); }
            public Builder armor(double a, double t) { this.armor = a; this.toughness = t; return this; }
            public Builder weight(double w) { this.weight = w; return this; }
            public Builder slot(String s) { this.slot = s; return this; }
            public Builder hunger(double h) { this.hunger = h; return this; }
            public Builder deflect(double d) { this.deflect = d; return this; }
            public Builder visor(ResourceLocation v) { this.visor = v; return this; }
            public DefinitionEntry build() { return new DefinitionEntry(armor, toughness, slot, hunger, deflect, weight, visor); }
        }
    }

    public abstract static class Armor extends DefinitionsProvider {
        public Armor(PackOutput output) { super(output, "definitions/armor"); }

        @Override
        public CompletableFuture<?> run(CachedOutput writer) {
            Map<ResourceLocation, DefinitionEntry> entries = new TreeMap<>();
            generateDefinitions((item, definition) -> entries.put(BuiltInRegistries.ITEM.getKey(item), definition));
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceLocation, DefinitionEntry> entry : entries.entrySet()) {
                Path path = output.getOutputFolder().resolve("data/" + entry.getKey().getNamespace() + "/definitions/armor/" + entry.getKey().getPath() + ".json");
                futures.add(DataProvider.saveStable(writer, DefinitionEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {}), path));
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        @Override public String getName() { return "Armor Definitions"; }

        protected abstract void generateDefinitions(ArmorConsumer consumer);

        @FunctionalInterface
        public interface ArmorConsumer {
            void accept(Item item, DefinitionEntry definition);

            default void accept(DefinitionEntry definition, Item... items) {
                for (Item item : items) {
                    accept(item, definition);
                }
            }
        }

        public record DefinitionEntry(Map<String, Double> damageResistance, Map<String, Double> deflectChance, double weight) {
            public static final Codec<DefinitionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("damageResistance", Map.of()).forGetter(DefinitionEntry::damageResistance),
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("deflectChance", Map.of()).forGetter(DefinitionEntry::deflectChance),
                    Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(DefinitionEntry::weight)
            ).apply(instance, DefinitionEntry::new));
        }

        public static class Builder {
            private final Map<String, Double> res = new HashMap<>(), deflect = new HashMap<>();
            private double weight;
            public static Builder create() { return new Builder(); }
            public Builder damageResistance(double slash, double bludg, double pierce) {
                res.put("SLASHING", slash); res.put("BLUDGEONING", bludg); res.put("PIERCING", pierce); return this;
            }
            public Builder weight(double w) { this.weight = w; return this; }
            public Builder deflectChance(double chance, EntityType<?>... types) {
                for (EntityType<?> t : types) deflect.put(BuiltInRegistries.ENTITY_TYPE.getKey(t).toString(), chance);
                return this;
            }
            public DefinitionEntry build() { return new DefinitionEntry(res, deflect, weight); }
        }
    }

    public abstract static class Weapon extends DefinitionsProvider {
        public Weapon(PackOutput output) { super(output, "definitions/weapon"); }

        @Override
        public CompletableFuture<?> run(CachedOutput writer) {
            Map<ResourceLocation, WeaponDefinitionData> entries = new TreeMap<>();
            generateDefinitions((item, definition) -> entries.put(BuiltInRegistries.ITEM.getKey(item), definition));
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceLocation, WeaponDefinitionData> entry : entries.entrySet()) {
                Path path = output.getOutputFolder().resolve("data/" + entry.getKey().getNamespace() + "/definitions/weapon/" + entry.getKey().getPath() + ".json");
                futures.add(DataProvider.saveStable(writer, WeaponDefinitionData.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {}), path));
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        @Override public String getName() { return "Weapon Definitions"; }

        protected abstract void generateDefinitions(WeaponConsumer consumer);

        @FunctionalInterface
        public interface WeaponConsumer {
            void accept(Item item, WeaponDefinitionData definition);

            default void accept(WeaponDefinitionData definition, Item... items) {
                for (Item item : items) {
                    accept(item, definition);
                }
            }
        }

        public static class Builder {
            private final EnumSet<WeaponDefinitionData.Usage> usage = EnumSet.noneOf(WeaponDefinitionData.Usage.class);
            private WeaponDefinitionData.MeleeData melee;
            private WeaponDefinitionData.RangedData ranged;
            private WeaponDefinitionData.AmmoData ammo;

            public static Builder create() { return new Builder(); }

            public Builder melee(MeleeBuilder builder) {
                this.melee = builder.build();
                this.usage.add(WeaponDefinitionData.Usage.MELEE);
                return this;
            }

            public Builder ranged(RangedBuilder builder) {
                this.ranged = builder.build();
                this.usage.add(WeaponDefinitionData.Usage.RANGED);
                return this;
            }

            public Builder ammo(double deflect) {
                this.ammo = new WeaponDefinitionData.AmmoData(deflect);
                this.usage.add(WeaponDefinitionData.Usage.AMMO);
                return this;
            }

            public WeaponDefinitionData build() { return new WeaponDefinitionData(usage, melee, ranged, ammo); }
        }

        public static class MeleeBuilder {
            private final Map<String, Map<String, Float>> damage = new HashMap<>();
            private final Map<String, Double> radius = new HashMap<>();
            private int anim = 0;
            private int[] pierceAnim = new int[0];
            private double deflect, knockback;

            public static MeleeBuilder create() { return new MeleeBuilder(); }
            public MeleeBuilder slashingDamage(float... values) {
                return applyDamageType(SCDamageType.SLASHING, values);
            }

            public MeleeBuilder bludgeoningDamage(float... values) {
                return applyDamageType(SCDamageType.BLUDGEONING, values);
            }

            public MeleeBuilder piercingDamage(float... values) {
                return applyDamageType(SCDamageType.PIERCING, values);
            }
            private MeleeBuilder applyDamageType(SCDamageType type, float... values) {
                for (int i = 0; i < values.length && i < 5; i++) {
                    damage(type, i, values[i]);
                }
                return this;
            }
            public void damage(SCDamageType type, int level, float val) {
                damage.computeIfAbsent(type.name(), k -> new HashMap<>()).put("level_" + level, val);
            }
            public MeleeBuilder levelRadii(double l0, double l1, double l2, double l3, double l4) {
                radius(0, l0); radius(1, l1); radius(2, l2); radius(3, l3); radius(4, l4);
                return this;
            }
            public void radius(int level, double blocks) { radius.put("level_" + level, blocks);}
            public MeleeBuilder knockback(double k) { this.knockback = k; return this; }
            public MeleeBuilder deflect(double d) { this.deflect = d; return this; }
            public MeleeBuilder anim(int i) { this.anim = i; return this; }
            public MeleeBuilder pierceAnim(int... i) { this.pierceAnim = i; return this; }
            public WeaponDefinitionData.MeleeData build() { return new WeaponDefinitionData.MeleeData(damage, radius, pierceAnim, anim, deflect, knockback); }
        }

        public static class RangedBuilder {
            private String id = "bow";
            private SCDamageType type = SCDamageType.PIERCING;
            private float damage, speed, divergence;
            private int maxUseTime, rechargeTime;
            private UseAnim useAnim;
            private boolean needsFlintAndSteel;
            private final Map<String, WeaponDefinitionData.AmmoRequirementData> ammoRequirement = new HashMap<>();
            private SoundEvent soundEvent;
            public static RangedBuilder create(String id) { RangedBuilder b = new RangedBuilder(); b.id = id; return b; }
            public RangedBuilder projectile(float damage, SCDamageType type, float speed, float divergence) { this.damage = damage; this.type = type; this.speed = speed; this.divergence = divergence; return this; }
            public RangedBuilder maxUseTime(int m) { this.maxUseTime = m; return this; }
            public RangedBuilder rechargeTime(int k) { this.rechargeTime = k; return this; }
            public RangedBuilder useAnim(UseAnim u) { this.useAnim = u; return this; }
            public RangedBuilder needsFlintAndSteel(boolean n) { this.needsFlintAndSteel = n; return this; }
            public RangedBuilder ammoRequirement(String ammoType, WeaponDefinitionData.AmmoRequirementData requirement) {
                this.ammoRequirement.put(ammoType, requirement);
                return this;
            }

            public RangedBuilder ammoRequirement(String ammoType, Item item, int amount) {
                HashSet<String> items = new HashSet<>();
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                items.add(itemId.toString());
                return ammoRequirement(ammoType, new WeaponDefinitionData.AmmoRequirementData(items, amount));
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
                return ammoRequirement(ammoType, new WeaponDefinitionData.AmmoRequirementData(items, amount));
            }
            public RangedBuilder soundEvent(SoundEvent s) { this.soundEvent = s; return this; }
            public WeaponDefinitionData.RangedData build() { return new WeaponDefinitionData.RangedData(id, damage, type, maxUseTime, speed, divergence, rechargeTime, needsFlintAndSteel, useAnim, ammoRequirement, soundEvent); }
        }
    }

    public abstract static class Land extends DefinitionsProvider {
        public Land(PackOutput output) { super(output, "definitions/lands"); }
        @Override public CompletableFuture<?> run(CachedOutput writer) {return CompletableFuture.completedFuture(null); }
        protected abstract void generateDefinitions(BiConsumer<DefinitionEntry, List<Item>> consumer);
        @Override public String getName() { return "Land Definitions"; }
        public record DefinitionEntry(int baseRadius, Map<Item, Integer> itemsToExpand, String expandFormula, int maxAllies) {
            public static final Codec<DefinitionEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.INT.fieldOf("base_radius").forGetter(DefinitionEntry::baseRadius),
                    Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Codec.INT).fieldOf("items_to_expand").forGetter(DefinitionEntry::itemsToExpand),
                    Codec.STRING.fieldOf("expand_formula").forGetter(DefinitionEntry::expandFormula),
                    Codec.INT.fieldOf("maxAllies").forGetter(DefinitionEntry::maxAllies)
            ).apply(i, DefinitionEntry::new));
        }
    }

    public abstract static class SiegeEngine extends DefinitionsProvider {
        public SiegeEngine(PackOutput output) { super(output, "definitions/siege_engines"); }
        @Override public CompletableFuture<?> run(CachedOutput writer) {return CompletableFuture.completedFuture(null); }
        protected abstract void generateDefinitions(BiConsumer<DefinitionEntry, List<Item>> consumer);
        @Override public String getName() { return "Siege Engine Definitions"; }
        public record DefinitionEntry(double playerSpeed, double horseSpeed, double baseDamage, int baseReload) {
            public static final Codec<DefinitionEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.DOUBLE.fieldOf("playerSpeed").forGetter(DefinitionEntry::playerSpeed),
                    Codec.DOUBLE.fieldOf("horseSpeed").forGetter(DefinitionEntry::horseSpeed),
                    Codec.DOUBLE.fieldOf("baseDamage").forGetter(DefinitionEntry::baseDamage),
                    Codec.INT.fieldOf("baseReload").forGetter(DefinitionEntry::baseReload)
            ).apply(i, DefinitionEntry::new));
        }
    }
}