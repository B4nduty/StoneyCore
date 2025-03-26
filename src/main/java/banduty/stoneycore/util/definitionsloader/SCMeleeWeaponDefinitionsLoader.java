package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCDamageCalculator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class SCMeleeWeaponDefinitionsLoader implements IdentifiableResourceReloadListener {

    private static final Map<Identifier, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    private static final Identifier RELOAD_LISTENER_ID = new Identifier(StoneyCore.MOD_ID, "melee_weapon_definitions_loader");

    @Override
    public Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            DEFINITIONS.clear();

            Map<Identifier, Resource> resources = resourceManager.findResources("definitions/melee_weapon", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonObject json = new Gson().fromJson(new InputStreamReader(stream), JsonObject.class);

                    Map<String, Float> damage = new HashMap<>();
                    if (json.has("damage")) {
                        JsonObject attributesJson = json.getAsJsonObject("damage");
                        for (Map.Entry<String, JsonElement> entry : attributesJson.entrySet()) {
                            damage.put(entry.getKey().toUpperCase(), entry.getValue().getAsFloat());
                        }
                    }

                    Map<String, Double> radius = new HashMap<>();
                    if (json.has("radius")) {
                        JsonObject attributesJson = json.getAsJsonObject("radius");
                        for (Map.Entry<String, JsonElement> entry : attributesJson.entrySet()) {
                            radius.put(entry.getKey(), entry.getValue().getAsDouble());
                        }
                    }

                    int[] piercingAnimation = new int[0];
                    if (json.has("piercingAnimation")) {
                        JsonElement piercingAnimationElement = json.get("piercingAnimation");
                        if (piercingAnimationElement.isJsonArray()) {
                            JsonArray piercingAnimationArray = piercingAnimationElement.getAsJsonArray();
                            piercingAnimation = new int[piercingAnimationArray.size()];
                            for (int i = 0; i < piercingAnimationArray.size(); i++) {
                                piercingAnimation[i] = piercingAnimationArray.get(i).getAsInt();
                            }
                        } else if (piercingAnimationElement.isJsonPrimitive()) {
                            piercingAnimation = new int[] { piercingAnimationElement.getAsInt() };
                        }
                    }

                    int animation = 0;
                    if (json.has("animation")) {
                        animation = json.get("animation").getAsInt();
                    }

                    SCDamageCalculator.DamageType onlyDamageType = null;
                    if (json.has("onlyDamageType")) {
                        String value = json.get("onlyDamageType").getAsString();
                        onlyDamageType = SCDamageCalculator.DamageType.valueOf(value.toUpperCase());
                    }

                    Identifier attributeId = Identifier.of(id.getNamespace(), id.getPath().substring("definitions/melee_weapon/".length(), id.getPath().length() - 5));
                    DEFINITIONS.put(attributeId, new DefinitionData(damage, radius, piercingAnimation, animation, onlyDamageType));
                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {
        }, applyExecutor);
    }

    public static DefinitionData getData(Item item) {
        Identifier itemId = Registries.ITEM.getId(item);
        Identifier definitionId = Identifier.of(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.getOrDefault(definitionId, new DefinitionData(null, null, null, 0, null));
    }

    public static boolean containsItem(Item item) {
        Identifier itemId = Registries.ITEM.getId(item);
        Identifier definitionId = Identifier.of(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.containsKey(definitionId);
    }

    public record DefinitionData(Map<String, Float> damage, Map<String, Double> radius, int[] piercingAnimation,
                                 int animation, SCDamageCalculator.DamageType onlyDamageType) {}
}