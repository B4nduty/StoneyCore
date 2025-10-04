package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.LandTypeRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.concurrent.Executor;

public class LandDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Identifier RELOAD_LISTENER_ID =
            new Identifier(StoneyCore.MOD_ID, "land_definitions_loader");

    @Override
    public Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer,
                                          ResourceManager resourceManager,
                                          Profiler prepareProfiler,
                                          Profiler applyProfiler,
                                          Executor prepareExecutor,
                                          Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            LandTypeRegistry.clearOverrides();

            Map<Identifier, Resource> resources =
                    resourceManager.findResources("definitions/lands", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<LandValues> result =
                            LandValues.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(def -> {
                                Identifier landId = new Identifier(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/lands/".length(), id.getPath().length() - 5)
                                );
                                LandTypeRegistry.applyOverride(landId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load land definition from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {
        }, applyExecutor);
    }

    public record LandValues(
            int baseRadius,
            Map<Item, Integer> itemsToExpand,
            String expandFormula,
            int maxAllies
    ) {
        public static final Codec<LandValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("base_radius", 0)
                        .forGetter(LandValues::baseRadius),
                Codec.unboundedMap(Identifier.CODEC.xmap(Registries.ITEM::get, Registries.ITEM::getId), Codec.INT)
                        .optionalFieldOf("items_to_expand", Map.of())
                        .forGetter(LandValues::itemsToExpand),
                Codec.STRING.optionalFieldOf("expand_formula", "")
                        .forGetter(LandValues::expandFormula),
                Codec.INT.optionalFieldOf("maxAllies", -1)
                        .forGetter(LandValues::maxAllies)
        ).apply(instance, LandValues::new));
    }
}