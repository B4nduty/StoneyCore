package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class ArmorDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Map<Identifier, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final Identifier RELOAD_LISTENER_ID =
            new Identifier(StoneyCore.MOD_ID, "armor_definitions_loader");

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
                    resourceManager.findResources("definitions/armor", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<DefinitionData> result =
                            DefinitionData.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(def -> {
                                Identifier attributeId = Identifier.of(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/armor/".length(), id.getPath().length() - 5)
                                );
                                DEFINITIONS.put(attributeId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load armor definition from {}: {}", id, e.getMessage(), e);
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
        return DEFINITIONS.getOrDefault(definitionId,
                new DefinitionData(Map.of(), Map.of(), 0));
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