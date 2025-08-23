package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.LandTypeRegistry;
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

public class LandDefinitionsLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static final Identifier RELOAD_LISTENER_ID =
            new Identifier(StoneyCore.MOD_ID, "land_definitions_loader");

    @Override
    public Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager resourceManager,
                                          Profiler prepareProfiler, Profiler applyProfiler,
                                          Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            LandTypeRegistry.clearOverrides();

            Map<Identifier, Resource> resources =
                    resourceManager.findResources("definitions/lands", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);

                    int baseRadius = json.has("base_radius") ? json.get("base_radius").getAsInt() : 0;

                    Map<Item, Integer> itemsToExpand = new ConcurrentHashMap<>();
                    if (json.has("items_to_expand") && json.get("items_to_expand").isJsonObject()) {
                        JsonObject expandJson = json.getAsJsonObject("items_to_expand");
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : expandJson.entrySet()) {
                            Item item = Registries.ITEM.get(new Identifier(entry.getKey()));
                            int value = entry.getValue().getAsInt();
                            itemsToExpand.put(item, value);
                        }
                    }

                    String expandFormula = json.has("expand_formula") ? json.get("expand_formula").getAsString() : "";

                    Identifier landId = new Identifier(id.getNamespace(),
                            id.getPath().substring("definitions/lands/".length(), id.getPath().length() - 5));

                    LandTypeRegistry.applyOverride(landId, new LandValues(baseRadius, itemsToExpand, expandFormula));

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load land definition from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {
        }, applyExecutor);
    }

    public record LandValues(int baseRadius, Map<Item, Integer> itemsToExpand, String expandFormula) { }
}