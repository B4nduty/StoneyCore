package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class AccessoriesDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<Identifier, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final Identifier RELOAD_LISTENER_ID = new Identifier(StoneyCore.MOD_ID, "accessories_definitions_loader");

    @Override
    public Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            DEFINITIONS.clear();

            Map<Identifier, Resource> resources = resourceManager.findResources("definitions/accessories", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);

                    double armor = 0;
                    if (json.has("armor")) {
                        armor = json.get("armor").getAsDouble();
                    }

                    double toughness = 0;
                    if (json.has("toughness")) {
                        toughness = json.get("toughness").getAsDouble();
                    }

                    String armorSlot = "";
                    if (json.has("armorSlot")) {
                        armorSlot = json.get("armorSlot").getAsString().toUpperCase();
                        Set<EquipmentSlot> ARMOR_SLOTS = EnumSet.of(
                                EquipmentSlot.HEAD,
                                EquipmentSlot.CHEST,
                                EquipmentSlot.LEGS,
                                EquipmentSlot.FEET
                        );

                        boolean valid = false;
                        for (EquipmentSlot slot : ARMOR_SLOTS) {
                            if (slot.name().equals(armorSlot)) {
                                valid = true;
                                break;
                            }
                        }

                        if (!valid && !armorSlot.isBlank()) {
                            StoneyCore.LOGGER.error(
                                    "Invalid armorSlot '{}' in {}. Expected one of {}. This item will not protect any armor slot.",
                                    armorSlot,
                                    id,
                                    ARMOR_SLOTS
                            );
                            armorSlot = "";
                        }
                    }

                    double hungerDrainMultiplier = 0;
                    if (json.has("hungerDrainMultiplier")) {
                        hungerDrainMultiplier = json.get("hungerDrainMultiplier").getAsDouble();
                    }

                    Map<String, Double> deflectChance = new ConcurrentHashMap<>();
                    if (json.has("deflectChance") && json.get("deflectChance").isJsonObject()) {
                        JsonObject deflectJson = json.getAsJsonObject("deflectChance");
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : deflectJson.entrySet()) {
                            String key = entry.getKey();
                            double value = entry.getValue().getAsDouble();
                            deflectChance.put(key, value);
                        }
                    }

                    double weight = 0;
                    if (json.has("weight")) {
                        weight = json.get("weight").getAsDouble();
                    }

                    Identifier attributeId = Identifier.of(id.getNamespace(), id.getPath().substring("definitions/accessories/".length(), id.getPath().length() - 5));
                    DEFINITIONS.put(attributeId, new DefinitionData(armor, toughness, armorSlot, hungerDrainMultiplier, deflectChance, weight));
                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {
        }, applyExecutor);
    }

    public static DefinitionData getData(ItemStack itemStack) {
        if (itemStack == null) itemStack = ItemStack.EMPTY;
        return getData(itemStack.getItem());
    }

    public static DefinitionData getData(Item item) {
        Identifier itemId = Registries.ITEM.getId(item);
        Identifier definitionId = Identifier.of(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.getOrDefault(definitionId, new DefinitionData(0, 0, "",0, null, 0));
    }

    public static boolean containsItem(ItemStack itemStack) {
        if (itemStack == null) itemStack = ItemStack.EMPTY;
        return containsItem(itemStack.getItem());
    }

    public static boolean containsItem(Item item) {
        Identifier itemId = Registries.ITEM.getId(item);
        Identifier definitionId = Identifier.of(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.containsKey(definitionId);
    }

    public record DefinitionData(double armor, double toughness, String armorSlot, double hungerDrainMultiplier, Map<String, Double> deflectChance, double weight) {}
}