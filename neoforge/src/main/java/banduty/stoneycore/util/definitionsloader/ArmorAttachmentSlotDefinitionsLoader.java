package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ArmorAttachmentSlotDefinitionsLoader extends SimplePreparableReloadListener<Void> {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ArmorAttachmentSlotDefinitionsLoader());
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
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

        StoneyCore.LOG.debug("Loaded {} armor attachment slots definitions", ArmorAttachmentSlotDefinitionsStorage.DEFINITIONS.size());
    }
}