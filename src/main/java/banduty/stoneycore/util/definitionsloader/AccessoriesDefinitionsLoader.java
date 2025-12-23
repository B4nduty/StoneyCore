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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class AccessoriesDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Map<ResourceLocation, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final ResourceLocation RELOAD_LISTENER_ID = new ResourceLocation(StoneyCore.MOD_ID, "accessories_definitions_loader");

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

            Map<ResourceLocation, Resource> resources = resourceManager.listResources("definitions/accessories",
                    id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.open()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<DefinitionData> result = DefinitionData.CODEC.parse(JsonOps.INSTANCE, element);
                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(def -> {
                                String armorSlot = def.armorSlot().toUpperCase();
                                if (!armorSlot.isEmpty() && !isValidArmorSlot(armorSlot)) {
                                    StoneyCore.LOGGER.error(
                                            "Invalid armorSlot '{}' in {}. Expected one of {}. This item will not protect any armor slot.",
                                            armorSlot, id, EnumSet.of(
                                                    EquipmentSlot.HEAD,
                                                    EquipmentSlot.CHEST,
                                                    EquipmentSlot.LEGS,
                                                    EquipmentSlot.FEET
                                            )
                                    );
                                    def = new DefinitionData(
                                            def.armor(),
                                            def.toughness(),
                                            "",
                                            def.hungerDrainMultiplier(),
                                            def.deflectChance(),
                                            def.weight(),
                                            def.visoredHelmet()
                                    );
                                }

                                ResourceLocation attributeId = ResourceLocation.tryBuild(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/accessories/".length(), id.getPath().length() - 5)
                                );
                                DEFINITIONS.put(attributeId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {
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

    public static DefinitionData getData(ItemStack itemStack) {
        if (itemStack == null) itemStack = ItemStack.EMPTY;
        return getData(itemStack.getItem());
    }

    public static DefinitionData getData(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        ResourceLocation definitionId = ResourceLocation.tryBuild(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.getOrDefault(definitionId,
                new DefinitionData(0, 0, "", 0, Map.of(), 0, new ResourceLocation("", "")));
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
            double armor,
            double toughness,
            String armorSlot,
            double hungerDrainMultiplier,
            Map<String, Double> deflectChance,
            double weight,
            ResourceLocation visoredHelmet
    ) {
        public static final Codec<DefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(DefinitionData::armor),
                Codec.DOUBLE.optionalFieldOf("toughness", 0.0).forGetter(DefinitionData::toughness),
                Codec.STRING.optionalFieldOf("armorSlot", "").forGetter(DefinitionData::armorSlot),
                Codec.DOUBLE.optionalFieldOf("hungerDrainMultiplier", 0.0).forGetter(DefinitionData::hungerDrainMultiplier),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("deflectChance", Map.of()).forGetter(DefinitionData::deflectChance),
                Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(DefinitionData::weight),
                ResourceLocation.CODEC.optionalFieldOf("visoredHelmet", new ResourceLocation("", "")).forGetter(DefinitionData::visoredHelmet)
        ).apply(instance, DefinitionData::new));
    }
}