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
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AccessoriesDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final ResourceLocation RELOAD_LISTENER_ID =
            new ResourceLocation(StoneyCore.MOD_ID, "accessories_definitions_loader");

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
            AccessoriesDefinitionsStorage.clearDefinitions();

            Map<ResourceLocation, Resource> resources = resourceManager.listResources("definitions/accessories",
                    id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.open()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<AccessoriesDefinitionData> result = AccessoriesDefinitionData.CODEC.parse(JsonOps.INSTANCE, element);
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
                                    def = new AccessoriesDefinitionData(
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
                                        id.getPath().substring("definitions/accessories/".length(),
                                                id.getPath().length() - 5)
                                );
                                AccessoriesDefinitionsStorage.addDefinition(definitionId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOG.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {
            StoneyCore.LOG.info("Loaded {} accessory definitions", AccessoriesDefinitionsStorage.DEFINITIONS.size());
        }, applyExecutor);
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