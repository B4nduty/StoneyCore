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
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ArmorAttachmentDefinitionsLoader extends SimplePreparableReloadListener<Void> {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ArmorAttachmentDefinitionsLoader());
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        ArmorAttachmentDefinitionsStorage.clearDefinitions();

        Map<ResourceLocation, Resource> resources = resourceManager.listResources("definitions/attachments",
                id -> id.getPath().endsWith(".json"));

        resources.forEach((id, resource) -> {
            try (InputStream stream = resource.open()) {
                JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                DataResult<ArmorAttachmentDefinitionData> result = ArmorAttachmentDefinitionData.CODEC.parse(JsonOps.INSTANCE, element);
                result.resultOrPartial(StoneyCore.LOG::error)
                        .ifPresent(def -> {
                            String armorSlot = def.armorSlot().toUpperCase();
                            if (!armorSlot.isEmpty() && !isValidArmorSlot(armorSlot)) {
                                StoneyCore.LOG.error(
                                        "Invalid armorSlot '{}' in {}. Expected one of {}. This item will not protect any armor slot.",
                                        armorSlot, id, EnumSet.of(
                                                EquipmentSlot.HEAD,
                                                EquipmentSlot.CHEST,
                                                EquipmentSlot.LEGS,
                                                EquipmentSlot.FEET
                                        )
                                );
                                def = new ArmorAttachmentDefinitionData(
                                        def.armor(),
                                        def.toughness(),
                                        "",
                                        def.hungerDrainMultiplier(),
                                        def.deflectChance(),
                                        def.weight(),
                                        def.visoredHelmet()
                                );
                            }

                            ResourceLocation definitionId = ResourceLocation.tryBuild(
                                    id.getNamespace(),
                                    id.getPath().substring("definitions/attachments/".length(),
                                            id.getPath().length() - 5)
                            );
                            ArmorAttachmentDefinitionsStorage.addDefinition(definitionId, def);
                        });

            } catch (Exception e) {
                StoneyCore.LOG.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
            }
        });

        StoneyCore.LOG.debug("Loaded {} armor attachment definitions", ArmorAttachmentDefinitionsStorage.DEFINITIONS.size());
    }

    private boolean isValidArmorSlot(String slot) {
        Set<EquipmentSlot> valid = EnumSet.of(
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        );
        return valid.stream().anyMatch(s -> s.name().equals(slot));
    }
}