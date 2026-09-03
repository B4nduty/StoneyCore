package banduty.stoneycore.mobgear;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.mobgear.data.MobGearArmorData;
import banduty.stoneycore.mobgear.data.MobGearAttachmentData;
import banduty.stoneycore.mobgear.data.MobGearWeaponData;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class MobGearDataReader {
    private static final String WEAPON_PATH = "mob_gear/weapon";
    private static final String ARMOR_PATH = "mob_gear/armor";
    private static final String ATTACHMENT_PATH = "mob_gear/attachment";

    public record Result(Map<ResourceLocation, List<ResourceLocation>> weapons,
                         Map<ResourceLocation, MobGearArmorData> armor,
                         Map<ResourceLocation, MobGearAttachmentData> attachments) {
    }

    @FunctionalInterface
    private interface SlotDataFactory<S, T> {
        T create(S slot, List<ResourceLocation> mobs, boolean replace);
    }

    private MobGearDataReader() {
    }

    public static Result readAll(ResourceManager resourceManager) {
        Map<ResourceLocation, List<ResourceLocation>> weapons = readMobsOnly(
                resourceManager, WEAPON_PATH, MobGearWeaponData.CODEC,
                MobGearWeaponData::mobs, MobGearWeaponData::replace);

        Map<ResourceLocation, MobGearArmorData> armor = readWithSlot(
                resourceManager, ARMOR_PATH, MobGearArmorData.CODEC,
                MobGearArmorData::mobs, MobGearArmorData::replace, MobGearArmorData::slot,
                MobGearArmorData::new);

        Map<ResourceLocation, MobGearAttachmentData> attachments = readWithSlot(
                resourceManager, ATTACHMENT_PATH, MobGearAttachmentData.CODEC,
                MobGearAttachmentData::mobs, MobGearAttachmentData::replace, MobGearAttachmentData::slot,
                MobGearAttachmentData::new);

        return new Result(weapons, armor, attachments);
    }

    private static <T> Map<ResourceLocation, List<ResourceLocation>> readMobsOnly(
            ResourceManager manager, String path, Codec<T> codec,
            Function<T, List<ResourceLocation>> mobsGetter, Function<T, Boolean> replaceGetter) {

        Map<ResourceLocation, List<ResourceLocation>> result = new HashMap<>();
        Map<ResourceLocation, List<Resource>> stacks =
                manager.listResourceStacks(path, id -> id.getPath().endsWith(".json"));

        stacks.forEach((fileId, resources) -> {
            ResourceLocation itemId = toItemId(fileId, path);
            if (itemId == null) return;

            List<ResourceLocation> mobs = new ArrayList<>();
            for (Resource resource : resources) {
                parse(resource, codec, fileId).ifPresent(data -> {
                    if (replaceGetter.apply(data)) mobs.clear();
                    mobs.addAll(mobsGetter.apply(data));
                });
            }
            result.put(itemId, List.copyOf(mobs));
        });

        return result;
    }

    private static <T, S> Map<ResourceLocation, T> readWithSlot(
            ResourceManager manager, String path, Codec<T> codec,
            Function<T, List<ResourceLocation>> mobsGetter, Function<T, Boolean> replaceGetter,
            Function<T, S> slotGetter, SlotDataFactory<S, T> factory) {

        Map<ResourceLocation, T> result = new HashMap<>();
        Map<ResourceLocation, List<Resource>> stacks =
                manager.listResourceStacks(path, id -> id.getPath().endsWith(".json"));

        stacks.forEach((fileId, resources) -> {
            ResourceLocation itemId = toItemId(fileId, path);
            if (itemId == null) return;

            List<ResourceLocation> mobs = new ArrayList<>();
            Object[] slotHolder = new Object[1];

            for (Resource resource : resources) {
                parse(resource, codec, fileId).ifPresent(data -> {
                    if (replaceGetter.apply(data)) mobs.clear();
                    mobs.addAll(mobsGetter.apply(data));
                    slotHolder[0] = slotGetter.apply(data);
                });
            }

            if (slotHolder[0] != null) {
                @SuppressWarnings("unchecked")
                S slot = (S) slotHolder[0];
                result.put(itemId, factory.create(slot, List.copyOf(mobs), false));
            }
        });

        return result;
    }

    private static ResourceLocation toItemId(ResourceLocation fileId, String basePath) {
        String path = fileId.getPath();
        int prefixLength = basePath.length() + 1;
        int suffixLength = ".json".length();
        if (path.length() <= prefixLength + suffixLength) return null;

        String itemPath = path.substring(prefixLength, path.length() - suffixLength);
        return ResourceLocation.tryBuild(fileId.getNamespace(), itemPath);
    }

    private static <T> Optional<T> parse(Resource resource, Codec<T> codec, ResourceLocation fileId) {
        try (InputStream stream = resource.open()) {
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            DataResult<T> result = codec.parse(JsonOps.INSTANCE, element);
            return result.resultOrPartial(error ->
                    StoneyCore.LOG.error("Failed to parse mob gear data {}: {}", fileId, error));
        } catch (Exception e) {
            StoneyCore.LOG.error("Failed to read mob gear data {}: {}", fileId, e.getMessage(), e);
            return Optional.empty();
        }
    }
}