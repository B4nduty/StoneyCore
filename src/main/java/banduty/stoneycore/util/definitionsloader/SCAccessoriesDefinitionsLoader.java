package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class SCAccessoriesDefinitionsLoader implements IdentifiableResourceReloadListener {

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
                    JsonObject json = new Gson().fromJson(new InputStreamReader(stream), JsonObject.class);

                    double armor = 0;
                    if (json.has("armor")) {
                        armor = json.get("armor").getAsDouble();
                    }

                    double toughness = 0;
                    if (json.has("toughness")) {
                        toughness = json.get("toughness").getAsDouble();
                    }

                    double hungerDrainAddition = 0;
                    if (json.has("hungerDrainAddition")) {
                        hungerDrainAddition = json.get("hungerDrainAddition").getAsDouble();
                    }

                    Identifier attributeId = Identifier.of(id.getNamespace(), id.getPath().substring("definitions/accessories/".length(), id.getPath().length() - 5));
                    DEFINITIONS.put(attributeId, new DefinitionData(armor, toughness, hungerDrainAddition));
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
        return DEFINITIONS.getOrDefault(definitionId, new DefinitionData(0, 0, 0));
    }

    public static boolean containsItem(Item item) {
        Identifier itemId = Registries.ITEM.getId(item);
        Identifier definitionId = Identifier.of(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.containsKey(definitionId);
    }

    public record DefinitionData(double armor, double toughness, double hungerDrainAddition) {}
}