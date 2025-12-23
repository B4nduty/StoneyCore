package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class SiegeEngineDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Map<ResourceLocation, SiegeEngineDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final ResourceLocation RELOAD_LISTENER_ID =
            new ResourceLocation(StoneyCore.MOD_ID, "siege_engine_definitions_loader");

    @Override
    public ResourceLocation getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier synchronizer,
                                                   @NotNull ResourceManager resourceManager,
                                                   @NotNull ProfilerFiller prepareProfiler,
                                                   @NotNull ProfilerFiller applyProfiler,
                                                   @NotNull Executor prepareExecutor,
                                                   @NotNull Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            DEFINITIONS.clear();

            Map<ResourceLocation, Resource> resources =
                    resourceManager.listResources("definitions/siege_engines", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.open()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<SiegeEngineDefinitionData> result =
                            SiegeEngineDefinitionData.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(def -> {
                                ResourceLocation siegeEngineId = ResourceLocation.tryBuild(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/siege_engines/".length(), id.getPath().length() - 5)
                                );
                                DEFINITIONS.put(siegeEngineId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load siege engine definition from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {
            StoneyCore.LOGGER.info("Loaded {} siege engine definitions", DEFINITIONS.size());
        }, applyExecutor);
    }

    public static SiegeEngineDefinitionData getData(EntityType<?> entityType) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        ResourceLocation definitionId = ResourceLocation.tryBuild(entityId.getNamespace(), entityId.getPath());
        return DEFINITIONS.getOrDefault(definitionId, SiegeEngineDefinitionData.DEFAULT);
    }

    public static boolean containsEntity(EntityType<?> entityType) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        ResourceLocation definitionId = ResourceLocation.tryBuild(entityId.getNamespace(), entityId.getPath());
        return DEFINITIONS.containsKey(definitionId);
    }

    public record DamageSourceConfig(
            Map<String, Boolean> entityDamageSources,
            Map<String, Boolean> itemDamageSources,
            Map<String, Boolean> damageTypeSources
    ) {
        public static final DamageSourceConfig DEFAULT = new DamageSourceConfig(
                Map.of(
                        "minecraft:player", false,
                        "minecraft:vindicator", true,
                        "*", false  // Wildcard for all entities not specified
                ),
                Map.of(
                        "minecraft:wooden_axe", true,
                        "minecraft:stone_axe", true,
                        "minecraft:iron_axe", true,
                        "minecraft:golden_axe", true,
                        "minecraft:diamond_axe", true,
                        "minecraft:netherite_axe", true,
                        "*", false  // Wildcard for all items not specified
                ),
                Map.of(
                        "projectile", false,
                        "explosion", true,
                        "*", false  // Wildcard for all damage types not specified
                )
        );

        public static final Codec<DamageSourceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("entityDamageSources", new HashMap<>()).forGetter(DamageSourceConfig::entityDamageSources),
                Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("itemDamageSources", new HashMap<>()).forGetter(DamageSourceConfig::itemDamageSources),
                Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("damageTypeSources", new HashMap<>()).forGetter(DamageSourceConfig::damageTypeSources)
        ).apply(instance, DamageSourceConfig::new));

        public boolean canEntityDamage(String entityId) {
            if (entityDamageSources.containsKey(entityId)) {
                return entityDamageSources.get(entityId);
            }
            return entityDamageSources.getOrDefault("*", false);
        }

        public boolean canItemDamage(String itemId) {
            if (itemDamageSources.containsKey(itemId)) {
                return itemDamageSources.get(itemId);
            }
            return itemDamageSources.getOrDefault("*", false);
        }

        public boolean canDamageTypeDamage(String damageType) {
            if (damageTypeSources.containsKey(damageType)) {
                return damageTypeSources.get(damageType);
            }
            return damageTypeSources.getOrDefault("*", false);
        }
    }

    public record SiegeEngineDefinitionData(
            double playerSpeed,
            double horseSpeed,
            double knockback,
            double baseDamage,
            int baseReload,
            float projectileSpeed,
            float accuracyMultiplier,
            DamageSourceConfig damageConfig
    ) {
        public static final SiegeEngineDefinitionData DEFAULT = new SiegeEngineDefinitionData(
                0.05, 0.1, 265.0, 25.0, 90, 140.0f, 1, DamageSourceConfig.DEFAULT
        );

        public static final Codec<SiegeEngineDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("playerSpeed", 0.05).forGetter(SiegeEngineDefinitionData::playerSpeed),
                Codec.DOUBLE.optionalFieldOf("horseSpeed", 0.1).forGetter(SiegeEngineDefinitionData::horseSpeed),
                Codec.DOUBLE.optionalFieldOf("knockback", 0.0).forGetter(SiegeEngineDefinitionData::knockback),
                Codec.DOUBLE.optionalFieldOf("baseDamage", 25.0).forGetter(SiegeEngineDefinitionData::baseDamage),
                Codec.INT.optionalFieldOf("baseReload", 90).forGetter(SiegeEngineDefinitionData::baseReload),
                Codec.FLOAT.optionalFieldOf("projectileSpeed", 140.0f).forGetter(SiegeEngineDefinitionData::projectileSpeed),
                Codec.FLOAT.optionalFieldOf("accuracyMultiplier", 1.2f).forGetter(SiegeEngineDefinitionData::accuracyMultiplier),
                DamageSourceConfig.CODEC.optionalFieldOf("damageConfig", DamageSourceConfig.DEFAULT).forGetter(SiegeEngineDefinitionData::damageConfig)
        ).apply(instance, SiegeEngineDefinitionData::new));
    }
}