package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class ArmorDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<Identifier, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final Identifier RELOAD_LISTENER_ID = new Identifier(StoneyCore.MOD_ID, "armor_definitions_loader");

    @Override
    public Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            DEFINITIONS.clear();

            Map<Identifier, Resource> resources = resourceManager.findResources("definitions/armor", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);

                    Map<String, Double> damage = new HashMap<>();
                    if (json.has("damageResistance")) {
                        JsonObject attributesJson = json.getAsJsonObject("damageResistance");
                        for (Map.Entry<String, JsonElement> entry : attributesJson.entrySet()) {
                            damage.put(entry.getKey().toUpperCase(), entry.getValue().getAsDouble());
                        }
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

                    Identifier attributeId = Identifier.of(id.getNamespace(), id.getPath().substring("definitions/armor/".length(), id.getPath().length() - 5));
                    DEFINITIONS.put(attributeId, new DefinitionData(damage, deflectChance));
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
        return DEFINITIONS.getOrDefault(definitionId, new DefinitionData(null, null));
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

    public record DefinitionData(Map<String, Double> damageResistance, Map<String, Double> deflectChance) {}
}