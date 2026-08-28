package banduty.stoneycore.mobgear;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class SCMobGearRegistry {

    private record GearEntry<T>(Supplier<T> item, Set<EntityType<?>> allowedMobs) {
        private boolean appliesTo(EntityType<?> type) {
            return allowedMobs.isEmpty() || allowedMobs.contains(type);
        }
    }

    private static final List<GearEntry<? extends Item>> WEAPONS = new ArrayList<>();
    private static final Map<EquipmentSlot, List<GearEntry<? extends Item>>> ARMOR = new EnumMap<>(EquipmentSlot.class);
    private static final Map<EquipmentSlot, List<GearEntry<? extends Item>>> ATTACHMENTS = new EnumMap<>(EquipmentSlot.class);

    private static final Map<EntityType<?>, List<Item>> WEAPON_CACHE = new HashMap<>();
    private static final Map<EquipmentSlot, Map<EntityType<?>, List<Item>>> ARMOR_CACHE = new EnumMap<>(EquipmentSlot.class);
    private static final Map<EquipmentSlot, Map<EntityType<?>, List<Item>>> ATTACHMENT_CACHE = new EnumMap<>(EquipmentSlot.class);

    private SCMobGearRegistry() {
    }

    public static void registerWeapon(Supplier<? extends Item> weapon, EntityType<?>... allowedMobs) {
        registerWeapon(weapon, toSet(allowedMobs));
    }

    public static void registerWeapon(Supplier<? extends Item> weapon, Collection<EntityType<?>> allowedMobs) {
        WEAPONS.add(new GearEntry<>(weapon, toSet(allowedMobs)));
        WEAPON_CACHE.clear();
    }

    public static void registerArmor(EquipmentSlot slot, Supplier<? extends Item> armorPiece,
                                     EntityType<?>... allowedMobs) {
        registerArmor(slot, armorPiece, toSet(allowedMobs));
    }

    public static void registerArmor(EquipmentSlot slot, Supplier<? extends Item> armorPiece,
                                     Collection<EntityType<?>> allowedMobs) {
        ARMOR.computeIfAbsent(slot, s -> new ArrayList<>()).add(new GearEntry<>(armorPiece, toSet(allowedMobs)));
        clearSlotCache(ARMOR_CACHE, slot);
    }

    public static void registerArmorSet(Supplier<? extends Item> helmet,
                                        Supplier<? extends Item> chestplate,
                                        Supplier<? extends Item> leggings,
                                        Supplier<? extends Item> boots,
                                        EntityType<?>... allowedMobs) {
        registerArmorSet(helmet, chestplate, leggings, boots, toSet(allowedMobs));
    }

    public static void registerArmorSet(Supplier<? extends Item> helmet,
                                        Supplier<? extends Item> chestplate,
                                        Supplier<? extends Item> leggings,
                                        Supplier<? extends Item> boots,
                                        Collection<EntityType<?>> allowedMobs) {
        if (helmet != null) registerArmor(EquipmentSlot.HEAD, helmet, allowedMobs);
        if (chestplate != null) registerArmor(EquipmentSlot.CHEST, chestplate, allowedMobs);
        if (leggings != null) registerArmor(EquipmentSlot.LEGS, leggings, allowedMobs);
        if (boots != null) registerArmor(EquipmentSlot.FEET, boots, allowedMobs);
    }

    public static void registerAttachment(EquipmentSlot slot, Supplier<? extends Item> attachment,
                                          EntityType<?>... allowedMobs) {
        registerAttachment(slot, attachment, toSet(allowedMobs));
    }

    public static void registerAttachment(EquipmentSlot slot, Supplier<? extends Item> attachment,
                                          Collection<EntityType<?>> allowedMobs) {
        ATTACHMENTS.computeIfAbsent(slot, s -> new ArrayList<>()).add(new GearEntry<>(attachment, toSet(allowedMobs)));
        clearSlotCache(ATTACHMENT_CACHE, slot);
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

    private static List<Item> resolveWeapons(EntityType<?> type) {
        return WEAPON_CACHE.computeIfAbsent(type, t -> resolve(WEAPONS, t));
    }

    private static List<Item> resolveSlot(Map<EquipmentSlot, List<GearEntry<? extends Item>>> registry,
                                          Map<EquipmentSlot, Map<EntityType<?>, List<Item>>> cache,
                                          EquipmentSlot slot, EntityType<?> type) {
        List<GearEntry<? extends Item>> entries = registry.get(slot);
        if (entries == null || entries.isEmpty()) return List.of();

        return cache.computeIfAbsent(slot, s -> new HashMap<>())
                .computeIfAbsent(type, t -> resolve(entries, t));
    }

    private static List<Item> resolve(List<GearEntry<? extends Item>> entries, EntityType<?> type) {
        List<Item> resolved = new ArrayList<>();
        for (GearEntry<? extends Item> entry : entries) {
            if (entry.appliesTo(type)) resolved.add(entry.item().get());
        }
        return resolved.isEmpty() ? List.of() : resolved;
    }

    private static Item pickRandom(List<Item> candidates, RandomSource random) {
        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }

    private static void clearSlotCache(Map<EquipmentSlot, Map<EntityType<?>, List<Item>>> cache, EquipmentSlot slot) {
        Map<EntityType<?>, List<Item>> perType = cache.get(slot);
        if (perType != null) perType.clear();
    }

    private static Set<EntityType<?>> toSet(EntityType<?>... types) {
        if (types == null || types.length == 0) return Collections.emptySet();
        return new HashSet<>(List.of(types));
    }

    private static Set<EntityType<?>> toSet(Collection<EntityType<?>> types) {
        if (types == null || types.isEmpty()) return Collections.emptySet();
        return new HashSet<>(types);
    }
}