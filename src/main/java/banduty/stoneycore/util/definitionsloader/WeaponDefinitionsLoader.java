package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCDamageCalculator;
import com.google.gson.*;
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
    private static final Gson GSON = new Gson();
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
                    JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);

                    boolean hasMelee = json.has("melee");
                    boolean hasRanged = json.has("ranged");
                    boolean hasAmmo = json.has("ammo");

                    EnumSet<Usage> usage = EnumSet.noneOf(Usage.class);
                    if (hasMelee) usage.add(Usage.MELEE);
                    if (hasRanged) usage.add(Usage.RANGED);
                    if (hasAmmo) usage.add(Usage.AMMO);

                    // --- MELEE ---
                    MeleeData meleeData = null;
                    if (hasMelee) {
                        JsonObject meleeJson = json.getAsJsonObject("melee");

                        Map<String, Float> damage = new HashMap<>();
                        if (meleeJson.has("damage")) {
                            for (Map.Entry<String, JsonElement> e : meleeJson.getAsJsonObject("damage").entrySet()) {
                                damage.put(e.getKey().toUpperCase(), e.getValue().getAsFloat());
                            }
                        }

                        Map<String, Double> radius = new HashMap<>();
                        if (meleeJson.has("radius")) {
                            for (Map.Entry<String, JsonElement> e : meleeJson.getAsJsonObject("radius").entrySet()) {
                                radius.put(e.getKey(), e.getValue().getAsDouble());
                            }
                        }

                        int[] piercingAnimation = new int[0];
                        if (meleeJson.has("piercingAnimation")) {
                            JsonElement el = meleeJson.get("piercingAnimation");
                            if (el.isJsonArray()) {
                                JsonArray array = el.getAsJsonArray();
                                piercingAnimation = new int[array.size()];
                                for (int i = 0; i < array.size(); i++) piercingAnimation[i] = array.get(i).getAsInt();
                            } else if (el.isJsonPrimitive()) {
                                piercingAnimation = new int[]{el.getAsInt()};
                            }
                        }

                        int animation = meleeJson.has("animation") ? meleeJson.get("animation").getAsInt() : 0;

                        SCDamageCalculator.DamageType onlyDamageType = null;
                        if (meleeJson.has("onlyDamageType"))
                            onlyDamageType = SCDamageCalculator.DamageType.valueOf(meleeJson.get("onlyDamageType").getAsString().toUpperCase());

                        double deflectChance = meleeJson.has("deflectChance") ? meleeJson.get("deflectChance").getAsDouble() : 0;

                        double bonusKnockback = meleeJson.has("bonusKnockback") ? meleeJson.get("bonusKnockback").getAsDouble() : 0;

                        meleeData = new MeleeData(damage, radius, piercingAnimation, animation, onlyDamageType, deflectChance, bonusKnockback);
                    }

                    // --- RANGED ---
                    RangedData rangedData = null;
                    if (hasRanged) {
                        JsonObject rangedJson = json.getAsJsonObject("ranged");

                        float baseDamage = rangedJson.has("baseDamage") ? rangedJson.get("baseDamage").getAsFloat() : 0f;
                        SCDamageCalculator.DamageType damageType = rangedJson.has("damageType")
                                ? SCDamageCalculator.DamageType.valueOf(rangedJson.get("damageType").getAsString().toUpperCase())
                                : null;
                        int maxUseTime = rangedJson.has("maxUseTime") ? rangedJson.get("maxUseTime").getAsInt() : 0;
                        float speed = rangedJson.has("speed") ? rangedJson.get("speed").getAsFloat() : 0f;
                        float divergence = rangedJson.has("divergence") ? rangedJson.get("divergence").getAsFloat() : 0f;
                        int rechargeTime = rangedJson.has("rechargeTime") ? rangedJson.get("rechargeTime").getAsInt() : 0;
                        boolean needsFlintAndSteel = rangedJson.has("needsFlintAndSteel") && rangedJson.get("needsFlintAndSteel").getAsBoolean();
                        UseAction useAction = rangedJson.has("useAction")
                                ? UseAction.valueOf(rangedJson.get("useAction").getAsString().toUpperCase())
                                : UseAction.NONE;

                        Map<String, AmmoRequirementData> ammoRequirement = new HashMap<>();
                        if (rangedJson.has("ammoRequirement")) {
                            JsonObject ammoJson = rangedJson.getAsJsonObject("ammoRequirement");
                            for (Map.Entry<String, JsonElement> entry : ammoJson.entrySet()) {
                                JsonArray arr = entry.getValue().getAsJsonArray();

                                Set<String> itemIds = new HashSet<>();
                                int amount = 1;

                                for (int i = 0; i < arr.size(); i++) {
                                    if (arr.get(i).isJsonPrimitive()) {
                                        if (arr.get(i).getAsJsonPrimitive().isString()) {
                                            itemIds.add(arr.get(i).getAsString());
                                        } else if (arr.get(i).getAsJsonPrimitive().isNumber()) {
                                            amount = arr.get(i).getAsInt();
                                        }
                                    }
                                }

                                ammoRequirement.put(entry.getKey(), new AmmoRequirementData(itemIds, amount));
                            }
                        }

                        SoundEvent soundEvent = null;
                        if (rangedJson.has("soundEvent")) {
                            String soundEventId = rangedJson.get("soundEvent").getAsString();
                            soundEvent = Registries.SOUND_EVENT.get(new Identifier(soundEventId));
                        }

                        rangedData = new RangedData(baseDamage, damageType, maxUseTime, speed, divergence, rechargeTime,
                                needsFlintAndSteel, useAction, ammoRequirement, soundEvent);
                    }

                    // --- AMMO ---
                    AmmoData ammoData = null;
                    if (hasAmmo) {
                        JsonObject ammoJson = json.getAsJsonObject("ammo");

                        double deflectChance = ammoJson.has("deflectChance")
                                ? ammoJson.get("deflectChance").getAsDouble()
                                : 0.0;

                        ammoData = new AmmoData(deflectChance);
                    }

                    // --- SAVE DEFINITION ---
                    Identifier attributeId = Identifier.of(
                            id.getNamespace(),
                            id.getPath().substring("definitions/weapon/".length(), id.getPath().length() - 5)
                    );
                    DEFINITIONS.put(attributeId, new DefinitionData(usage, meleeData, rangedData, ammoData));

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

    public record DefinitionData(EnumSet<Usage> usage, MeleeData melee, RangedData ranged, AmmoData ammo) {}
    public record MeleeData(Map<String, Float> damage, Map<String, Double> radius, int[] piercingAnimation,
                            int animation, SCDamageCalculator.DamageType onlyDamageType, double deflectChance,
                            double bonusKnockback) {}
    public record RangedData(float baseDamage, SCDamageCalculator.DamageType damageType, int maxUseTime, float speed,
                             float divergence, int rechargeTime, boolean needsFlintAndSteel, UseAction useAction,
                             Map<String, AmmoRequirementData> ammoRequirement, SoundEvent soundEvent) {}
    public record AmmoData(double deflectChance) {}
    public record AmmoRequirementData(Set<String> itemIds, int amount) {}

    public enum Usage {
        MELEE, RANGED, AMMO
    }
}