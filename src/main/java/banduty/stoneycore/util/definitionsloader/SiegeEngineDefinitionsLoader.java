package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.EntityType;
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

public class SiegeEngineDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Map<Identifier, SiegeEngineDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final Identifier RELOAD_LISTENER_ID =
            new Identifier(StoneyCore.MOD_ID, "siege_engine_definitions_loader");

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
            DEFINITIONS.clear();

            Map<Identifier, Resource> resources =
                    resourceManager.findResources("definitions/siege_engines", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<SiegeEngineDefinitionData> result =
                            SiegeEngineDefinitionData.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(def -> {
                                Identifier siegeEngineId = Identifier.of(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/siege_engines/".length(), id.getPath().length() - 5)
                                );
                                DEFINITIONS.put(siegeEngineId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load siege engine definition from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {
            StoneyCore.LOGGER.info("Loaded {} siege engine definitions", DEFINITIONS.size());
        }, applyExecutor);
    }

    public static SiegeEngineDefinitionData getData(EntityType<?> entityType) {
        Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
        Identifier definitionId = Identifier.of(entityId.getNamespace(), entityId.getPath());
        return DEFINITIONS.getOrDefault(definitionId, SiegeEngineDefinitionData.DEFAULT);
    }

    public static boolean containsEntity(EntityType<?> entityType) {
        Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
        Identifier definitionId = Identifier.of(entityId.getNamespace(), entityId.getPath());
        return DEFINITIONS.containsKey(definitionId);
    }

    public record SiegeEngineDefinitionData(
            double playerSpeed,
            double horseSpeed,
            double knockback,
            double baseDamage,
            int baseReload,
            float projectileSpeed,
            float accuracyMultiplier
    ) {
        public static final SiegeEngineDefinitionData DEFAULT = new SiegeEngineDefinitionData(
                0.05, 0.1, 265.0, 25.0, 90, 140.0f, 1
        );

        public static final Codec<SiegeEngineDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("playerSpeed", 0.05).forGetter(SiegeEngineDefinitionData::playerSpeed),
                Codec.DOUBLE.optionalFieldOf("horseSpeed", 0.1).forGetter(SiegeEngineDefinitionData::horseSpeed),
                Codec.DOUBLE.optionalFieldOf("knockback", 0.0).forGetter(SiegeEngineDefinitionData::knockback),
                Codec.DOUBLE.optionalFieldOf("baseDamage", 25.0).forGetter(SiegeEngineDefinitionData::baseDamage),
                Codec.INT.optionalFieldOf("baseReload", 90).forGetter(SiegeEngineDefinitionData::baseReload),
                Codec.FLOAT.optionalFieldOf("projectileSpeed", 140.0f).forGetter(SiegeEngineDefinitionData::projectileSpeed),
                Codec.FLOAT.optionalFieldOf("accuracyMultiplier", 1.2f).forGetter(SiegeEngineDefinitionData::accuracyMultiplier)
        ).apply(instance, SiegeEngineDefinitionData::new));
    }
}