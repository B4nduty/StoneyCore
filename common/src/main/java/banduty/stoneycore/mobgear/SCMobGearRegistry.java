package banduty.stoneycore.mobgear;

import banduty.stoneycore.mobgear.data.MobGearArmorData;
import banduty.stoneycore.mobgear.data.MobGearAttachmentData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class SCMobGearRegistry {
    private record GearInfo(Item item, Set<ResourceLocation> mobs) {
        private boolean appliesTo(ResourceLocation mobId) {
            return mobs.isEmpty() || mobs.contains(mobId);
        }
    }

    private static volatile List<GearInfo> WEAPONS = List.of();
    private static volatile Map<EquipmentSlot, List<GearInfo>> ARMOR = Map.of();
    private static volatile Map<EquipmentSlot, List<GearInfo>> ATTACHMENTS = Map.of();

    private static final Map<EntityType<?>, List<Item>> WEAPON_CACHE = new ConcurrentHashMap<>();
    private static final Map<EquipmentSlot, Map<EntityType<?>, List<Item>>> ARMOR_CACHE = new EnumMap<>(EquipmentSlot.class);
    private static final Map<EquipmentSlot, Map<EntityType<?>, List<Item>>> ATTACHMENT_CACHE = new EnumMap<>(EquipmentSlot.class);

    private SCMobGearRegistry() {
    }

    public static void applyDatapackData(Map<ResourceLocation, List<ResourceLocation>> weaponData,
                                         Map<ResourceLocation, MobGearArmorData> armorData,
                                         Map<ResourceLocation, MobGearAttachmentData> attachmentData) {
        WEAPONS = buildList(weaponData);
        ARMOR = buildSlotMap(armorData, MobGearArmorData::slot, MobGearArmorData::mobs);
        ATTACHMENTS = buildSlotMap(attachmentData, MobGearAttachmentData::slot, MobGearAttachmentData::mobs);

        WEAPON_CACHE.clear();
        ARMOR_CACHE.clear();
        ATTACHMENT_CACHE.clear();
    }

    public static boolean hasWeapons(Mob mob) {
        return !resolveWeapons(mob.getType()).isEmpty();
    }

    public static boolean hasArmor(EquipmentSlot slot, Mob mob) {
        return !resolveSlot(ARMOR, ARMOR_CACHE, slot, mob.getType()).isEmpty();
    }

    public static boolean hasAttachments(EquipmentSlot slot, Mob mob) {
        return !resolveSlot(ATTACHMENTS, ATTACHMENT_CACHE, slot, mob.getType()).isEmpty();
    }

    public static Item getRandomWeapon(Mob mob, RandomSource random) {
        return pickRandom(resolveWeapons(mob.getType()), random);
    }

    public static Item getRandomArmor(EquipmentSlot slot, Mob mob, RandomSource random) {
        return pickRandom(resolveSlot(ARMOR, ARMOR_CACHE, slot, mob.getType()), random);
    }

    public static Item getRandomAttachment(EquipmentSlot slot, Mob mob, RandomSource random) {
        return pickRandom(resolveSlot(ATTACHMENTS, ATTACHMENT_CACHE, slot, mob.getType()), random);
    }

    public static List<Item> getAttachments(EquipmentSlot slot, Mob mob) {
        return resolveSlot(ATTACHMENTS, ATTACHMENT_CACHE, slot, mob.getType());
    }

    private static List<GearInfo> buildList(Map<ResourceLocation, List<ResourceLocation>> data) {
        List<GearInfo> list = new ArrayList<>();
        data.forEach((itemId, mobs) -> resolveItem(itemId)
                .ifPresent(item -> list.add(new GearInfo(item, Set.copyOf(mobs)))));
        return List.copyOf(list);
    }

    private static <T> Map<EquipmentSlot, List<GearInfo>> buildSlotMap(Map<ResourceLocation, T> data,
                                                                       Function<T, EquipmentSlot> slotGetter,
                                                                       Function<T, List<ResourceLocation>> mobsGetter) {
        Map<EquipmentSlot, List<GearInfo>> grouped = new EnumMap<>(EquipmentSlot.class);

        data.forEach((itemId, entry) -> resolveItem(itemId).ifPresent(item -> {
            EquipmentSlot slot = slotGetter.apply(entry);
            grouped.computeIfAbsent(slot, s -> new ArrayList<>())
                    .add(new GearInfo(item, Set.copyOf(mobsGetter.apply(entry))));
        }));

        grouped.replaceAll((slot, list) -> List.copyOf(list));
        return Map.copyOf(grouped);
    }

    private static Optional<Item> resolveItem(ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id);
    }

    private static List<Item> resolveWeapons(EntityType<?> type) {
        return WEAPON_CACHE.computeIfAbsent(type, t -> resolve(WEAPONS, t));
    }

    private static List<Item> resolveSlot(Map<EquipmentSlot, List<GearInfo>> registry,
                                          Map<EquipmentSlot, Map<EntityType<?>, List<Item>>> cache,
                                          EquipmentSlot slot, EntityType<?> type) {
        List<GearInfo> entries = registry.get(slot);
        if (entries == null || entries.isEmpty()) return List.of();

        return cache.computeIfAbsent(slot, s -> new ConcurrentHashMap<>())
                .computeIfAbsent(type, t -> resolve(entries, t));
    }

    private static List<Item> resolve(List<GearInfo> entries, EntityType<?> type) {
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        List<Item> resolved = new ArrayList<>();
        for (GearInfo entry : entries) {
            if (entry.appliesTo(typeId)) resolved.add(entry.item());
        }
        return resolved.isEmpty() ? List.of() : resolved;
    }

    private static Item pickRandom(List<Item> candidates, RandomSource random) {
        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }
}