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

public class SCWeaponDefinitionsLoader implements IdentifiableResourceReloadListener {

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
                    JsonObject json = new Gson().fromJson(new InputStreamReader(stream), JsonObject.class);

                    Usage usage = json.has("usage")
                            ? Usage.fromString(json.get("usage").getAsString())
                            : Usage.MELEE;

                    // --- MELEE ---
                    MeleeData meleeData = null;
                    if (json.has("melee") || usage == Usage.MELEE || usage == Usage.BOTH) {
                        JsonObject meleeJson = json.has("melee") ? json.getAsJsonObject("melee") : json;

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

                        meleeData = new MeleeData(damage, radius, piercingAnimation, animation, onlyDamageType, deflectChance);
                    }

                    // --- RANGED ---
                    RangedData rangedData = null;
                    if (usage == Usage.RANGED && !json.has("ranged")) {
                        float baseDamage = json.has("baseDamage") ? json.get("baseDamage").getAsFloat() : 0f;
                        SCDamageCalculator.DamageType damageType = json.has("damageType")
                                ? SCDamageCalculator.DamageType.valueOf(json.get("damageType").getAsString().toUpperCase())
                                : null;
                        int maxUseTime = json.has("maxUseTime") ? json.get("maxUseTime").getAsInt() : 0;
                        float speed = json.has("speed") ? json.get("speed").getAsFloat() : 0f;
                        float divergence = json.has("divergence") ? json.get("divergence").getAsFloat() : 0f;
                        int rechargeTime = json.has("rechargeTime") ? json.get("rechargeTime").getAsInt() : 0;
                        boolean needsFlintAndSteel = json.has("needsFlintAndSteel") && json.get("needsFlintAndSteel").getAsBoolean();
                        UseAction useAction = json.has("useAction")
                                ? UseAction.valueOf(json.get("useAction").getAsString().toUpperCase())
                                : UseAction.NONE;

                        Map<String, AmmoRequirementData> ammoRequirement = new HashMap<>();
                        if (json.has("ammoRequirement")) {
                            JsonObject ammoJson = json.getAsJsonObject("ammoRequirement");
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
                        if (json.has("soundEvent")) {
                            String soundEventId = json.get("soundEvent").getAsString();
                            soundEvent = Registries.SOUND_EVENT.get(new Identifier(soundEventId));
                        }

                        rangedData = new RangedData(baseDamage, damageType, maxUseTime, speed, divergence, rechargeTime,
                                needsFlintAndSteel, useAction, ammoRequirement, soundEvent);
                    }

                    // --- AMMO ---
                    AmmoData ammoData = null;
                    if (json.has("usage") && Usage.fromString(json.get("usage").getAsString()) == Usage.AMMO) {
                        double deflectChance = json.has("deflectChance")
                                ? json.get("deflectChance").getAsDouble()
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
        return DEFINITIONS.getOrDefault(id, new DefinitionData(Usage.NONE, null, null, null));
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

    public static boolean isBoth(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return isBoth(stack.getItem());
    }

    public static boolean isAmmo(ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        return isAmmo(stack.getItem());
    }

    public static boolean isMelee(Item item) {
        DefinitionData data = getData(item);
        return data.melee() != null && (data.usage() == Usage.MELEE || data.usage() == Usage.BOTH);
    }

    public static boolean isRanged(Item item) {
        DefinitionData data = getData(item);
        return data.ranged() != null && (data.usage() == Usage.RANGED || data.usage() == Usage.BOTH);
    }

    public static boolean isBoth(Item item) {
        if (getData(item) == null || getData(item).ammo() == null || getData(item).ranged() == null) {
            return false;
        }
        return getData(item).usage() == Usage.BOTH;
    }

    public static boolean isAmmo(Item item) {
        DefinitionData data = getData(item);
        return data.ammo() != null && data.usage() == Usage.AMMO;
    }

    public record DefinitionData(Usage usage, MeleeData melee, RangedData ranged, AmmoData ammo) {}
    public record MeleeData(Map<String, Float> damage, Map<String, Double> radius, int[] piercingAnimation,
                            int animation, SCDamageCalculator.DamageType onlyDamageType, double deflectChance) {}
    public record RangedData(float baseDamage, SCDamageCalculator.DamageType damageType, int maxUseTime, float speed,
                             float divergence, int rechargeTime, boolean needsFlintAndSteel, UseAction useAction,
                             Map<String, AmmoRequirementData> ammoRequirement, SoundEvent soundEvent) {}
    public record AmmoData(double deflectChance) {}
    public record AmmoRequirementData(Set<String> itemIds, int amount) {}

    public enum Usage {
        NONE, MELEE, RANGED, BOTH, AMMO;

        public static Usage fromString(String value) {
            try {
                return Usage.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return MELEE;
            }
        }
    }
}