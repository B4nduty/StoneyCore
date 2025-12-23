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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LandDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final ResourceLocation RELOAD_LISTENER_ID =
            new ResourceLocation(StoneyCore.MOD_ID, "land_definitions_loader");

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
            LandTypeRegistry.clearOverrides();

            Map<ResourceLocation, Resource> resources =
                    resourceManager.listResources("definitions/lands", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.open()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<LandValues> result =
                            LandValues.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(def -> {
                                ResourceLocation landId = new ResourceLocation(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/lands/".length(), id.getPath().length() - 5)
                                );
                                LandTypeRegistry.applyOverride(landId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load land definition from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {
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
                Codec.unboundedMap(ResourceLocation.CODEC.xmap(BuiltInRegistries.ITEM::get, BuiltInRegistries.ITEM::getKey), Codec.INT)
                        .optionalFieldOf("items_to_expand", Map.of())
                        .forGetter(LandValues::itemsToExpand),
                Codec.STRING.optionalFieldOf("expand_formula", "")
                        .forGetter(LandValues::expandFormula),
                Codec.INT.optionalFieldOf("maxAllies", -1)
                        .forGetter(LandValues::maxAllies)
        ).apply(instance, LandValues::new));
    }
}