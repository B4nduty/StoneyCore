package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class AccessoriesDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final Map<Identifier, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();
    private static final Identifier RELOAD_LISTENER_ID = new Identifier(StoneyCore.MOD_ID, "accessories_definitions_loader");

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

            Map<Identifier, Resource> resources = resourceManager.findResources("definitions/accessories",
                    id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<DefinitionData> result = DefinitionData.CODEC.parse(JsonOps.INSTANCE, element);
                    result.resultOrPartial(StoneyCore.LOGGER::error)
                            .ifPresent(def -> {
                                // Validate armorSlot manually since Codec can’t enforce enum membership
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
                                            def.weight()
                                    );
                                }

                                Identifier attributeId = Identifier.of(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/accessories/".length(), id.getPath().length() - 5)
                                );
                                DEFINITIONS.put(attributeId, def);
                            });

                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {
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
        Identifier itemId = Registries.ITEM.getId(item);
        Identifier definitionId = Identifier.of(itemId.getNamespace(), itemId.getPath());
        return DEFINITIONS.getOrDefault(definitionId,
                new DefinitionData(0, 0, "", 0, Map.of(), 0));
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
            double armor,
            double toughness,
            String armorSlot,
            double hungerDrainMultiplier,
            Map<String, Double> deflectChance,
            double weight
    ) {
        public static final Codec<DefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(DefinitionData::armor),
                Codec.DOUBLE.optionalFieldOf("toughness", 0.0).forGetter(DefinitionData::toughness),
                Codec.STRING.optionalFieldOf("armorSlot", "").forGetter(DefinitionData::armorSlot),
                Codec.DOUBLE.optionalFieldOf("hungerDrainMultiplier", 0.0).forGetter(DefinitionData::hungerDrainMultiplier),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("deflectChance", Map.of()).forGetter(DefinitionData::deflectChance),
                Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(DefinitionData::weight)
        ).apply(instance, DefinitionData::new));
    }
}