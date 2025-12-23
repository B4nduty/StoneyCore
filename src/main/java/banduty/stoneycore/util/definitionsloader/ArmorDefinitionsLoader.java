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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class ArmorDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Map<ResourceLocation, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final ResourceLocation RELOAD_LISTENER_ID =
            new ResourceLocation(StoneyCore.MOD_ID, "armor_definitions_loader");

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
                    resourceManager.listResources("definitions/armor", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.open()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<DefinitionData> result =
                            DefinitionData.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(def -> {
                                ResourceLocation attributeId = ResourceLocation.tryBuild(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/armor/".length(), id.getPath().length() - 5)
                                );
                                DEFINITIONS.put(attributeId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load armor definition from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {
        }, applyExecutor);
    }

    public static DefinitionData getData(ItemStack itemStack) {
        if (itemStack == null) itemStack = ItemStack.EMPTY;
        return getData(itemStack.getItem());
    }

    public static DefinitionData getData(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        ResourceLocation definitionId = ResourceLocation.tryBuild(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.getOrDefault(definitionId,
                new DefinitionData(Map.of(), Map.of(), 0));
    }

    public static boolean containsItem(ItemStack itemStack) {
        if (itemStack == null) itemStack = ItemStack.EMPTY;
        return containsItem(itemStack.getItem());
    }

    public static boolean containsItem(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        ResourceLocation definitionId = ResourceLocation.tryBuild(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.containsKey(definitionId);
    }

    public record DefinitionData(
            Map<String, Double> damageResistance,
            Map<String, Double> deflectChance,
            double weight
    ) {
        public static final Codec<DefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING.xmap(String::toUpperCase, s -> s), Codec.DOUBLE)
                        .optionalFieldOf("damageResistance", Map.of())
                        .forGetter(DefinitionData::damageResistance),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                        .optionalFieldOf("deflectChance", Map.of())
                        .forGetter(DefinitionData::deflectChance),
                Codec.DOUBLE.optionalFieldOf("weight", 0.0)
                        .forGetter(DefinitionData::weight)
        ).apply(instance, DefinitionData::new));
    }
}