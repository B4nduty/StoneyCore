package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ArmorAttachmentSlotDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final ResourceLocation RELOAD_LISTENER_ID =
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "attachments_slots_definitions_loader");

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
            ArmorAttachmentSlotDefinitionsStorage.clearDefinitions();

            Map<ResourceLocation, Resource> resources = resourceManager.listResources("attachments/slots",
                    id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.open()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<ArmorAttachmentSlotDefinitionData> result = ArmorAttachmentSlotDefinitionData.CODEC.parse(JsonOps.INSTANCE, element);
                    result.resultOrPartial(StoneyCore.LOG::error)
                            .ifPresent(ArmorAttachmentSlotDefinitionsStorage::mergeAndAddDefinition);

                } catch (Exception e) {
                    StoneyCore.LOG.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {
            StoneyCore.LOG.debug("Loaded {} armor attachment slots definitions", ArmorAttachmentSlotDefinitionsStorage.DEFINITIONS.size());
        }, applyExecutor);
    }
}