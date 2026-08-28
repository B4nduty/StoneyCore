package banduty.stoneycore.mobgear;

import banduty.stoneycore.data.SCDataComponents;
import banduty.stoneycore.definitions.ArmorAttachmentSlotDefinitionData;
import banduty.stoneycore.definitions.ArmorAttachmentSlotDefinitionsStorage;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SCMobGearHandler {
    private static final float DYE_CHANCE = 0.5F;

    private static final float BASE_WEAPON_CHANCE = 2.0F;

    private static final float BASE_ARMOR_CHANCE = 2.0F;

    private static final float PER_SLOT_CHANCE = 1.0F;

    private static final float ATTACHMENT_CHANCE = 0.8F;

    private static final int ATTACHMENT_SLOT_PASSES = 2;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    private SCMobGearHandler() {
    }

    public static void tryEquipRandomGear(Mob mob, RandomSource random, DifficultyInstance difficulty) {
        float difficultyMultiplier = difficulty.getSpecialMultiplier();
        if (difficultyMultiplier <= 0.0F) return;

        int color = randomColor(random);

        tryEquipWeapon(mob, random, color, difficultyMultiplier);
        tryEquipArmor(mob, random, color, difficultyMultiplier);
    }

    private static void tryEquipWeapon(Mob mob, RandomSource random, int color, float difficultyMultiplier) {
        if (!SCMobGearRegistry.hasWeapons(mob)) return;
        if (!mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) return;
        if (random.nextFloat() >= BASE_WEAPON_CHANCE * difficultyMultiplier) return;

        Item weapon = SCMobGearRegistry.getRandomWeapon(mob, random);
        if (weapon == null) return;

        ItemStack weaponStack = new ItemStack(weapon);
        tryDye(weaponStack, random, color);

        mob.setItemSlot(EquipmentSlot.MAINHAND, weaponStack);
    }

    private static void tryEquipArmor(Mob mob, RandomSource random, int color, float difficultyMultiplier) {
        if (random.nextFloat() >= BASE_ARMOR_CHANCE * difficultyMultiplier) return;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!SCMobGearRegistry.hasArmor(slot, mob)) continue;
            if (!mob.getItemBySlot(slot).isEmpty()) continue;
            if (random.nextFloat() >= PER_SLOT_CHANCE) continue;

            Item armorPiece = SCMobGearRegistry.getRandomArmor(slot, mob, random);
            if (armorPiece == null) continue;

            ItemStack stack = new ItemStack(armorPiece);

            tryDye(stack, random, color);

            if (armorPiece instanceof SCUnderArmor underArmor) {
                tryLayerAttachments(underArmor, stack, mob, random, color, difficultyMultiplier);
                underArmor.rebuildAttachmentAttributes(stack);
            }

            mob.setItemSlot(slot, stack);
        }
    }

    private static void tryLayerAttachments(SCUnderArmor underArmor, ItemStack stack, Mob mob,
                                            RandomSource random, int color, float difficultyMultiplier) {
        ArmorItem.Type armorType = underArmor.getType();

        List<ArmorAttachmentSlotDefinitionData> slotDefs =
                ArmorAttachmentSlotDefinitionsStorage.getSlotsForArmorType(armorType);
        if (slotDefs.isEmpty()) return;

        Map<String, List<Item>> candidatesBySlot =
                groupCandidatesBySlot(SCMobGearRegistry.getAttachments(armorType.getSlot(), mob), armorType);
        if (candidatesBySlot.isEmpty()) return;

        UnderArmorContents contents = stack.getOrDefault(
                SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY);
        UnderArmorContents.Mutable mutable = new UnderArmorContents.Mutable(contents);

        boolean changed = false;

        for (int pass = 0; pass < ATTACHMENT_SLOT_PASSES; pass++) {
            boolean changedThisPass = false;

            for (ArmorAttachmentSlotDefinitionData slotDef : slotDefs) {
                String slotName = slotDef.slot();
                if (slotName == null || slotName.isEmpty()) continue;

                List<Item> candidates = candidatesBySlot.get(slotName);
                if (candidates == null || candidates.isEmpty()) continue;

                if (isAttachmentSlotFilled(mutable, slotName, armorType)) continue;
                if (!isRequiredSlotSatisfied(slotDef, mutable, armorType)) continue;
                if (random.nextFloat() >= ATTACHMENT_CHANCE * difficultyMultiplier) continue;

                Item chosen = candidates.get(random.nextInt(candidates.size()));

                try {
                    ItemStack chosenStack = new ItemStack(chosen);
                    tryDye(chosenStack, random, color);
                    if (mutable.tryInsert(chosenStack, null, stack) != null) {
                        changed = true;
                        changedThisPass = true;
                    }
                } catch (NullPointerException ignored) {
                    // Skip this one attachment; everything else already rolled still applies.
                }
            }

            if (!changedThisPass) break;
        }

        if (changed) {
            UnderArmorContents newContents = mutable.toImmutable();
            if (newContents.isEmpty()) {
                stack.remove(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
            } else {
                stack.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), newContents);
            }
        }
    }

    private static Map<String, List<Item>> groupCandidatesBySlot(List<Item> registered, ArmorItem.Type armorType) {
        Map<String, List<Item>> grouped = new HashMap<>();

        for (Item candidate : registered) {
            ArmorAttachmentSlotDefinitionData def =
                    ArmorAttachmentSlotDefinitionsStorage.getData(new ItemStack(candidate), armorType);
            String slotName = def.slot();
            if (slotName == null || slotName.isEmpty()) continue;

            grouped.computeIfAbsent(slotName, s -> new ArrayList<>()).add(candidate);
        }

        return grouped;
    }

    private static boolean isAttachmentSlotFilled(UnderArmorContents.Mutable mutable, String slotName,
                                                  ArmorItem.Type armorType) {
        for (ItemStack existing : mutable.toImmutable().attachments()) {
            ArmorAttachmentSlotDefinitionData existingDef =
                    ArmorAttachmentSlotDefinitionsStorage.getData(existing, armorType);
            if (existingDef != null && slotName.equals(existingDef.slot())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRequiredSlotSatisfied(ArmorAttachmentSlotDefinitionData slotDef,
                                                   UnderArmorContents.Mutable mutable, ArmorItem.Type armorType) {
        String requiredSlot = slotDef.requiredSlot();
        if (requiredSlot == null || requiredSlot.isEmpty()) return true;

        return isAttachmentSlotFilled(mutable, requiredSlot, armorType);
    }

    private static void tryDye(ItemStack stack, RandomSource random, int color) {
        if (!stack.is(ItemTags.DYEABLE)) return;
        if (random.nextFloat() >= DYE_CHANCE) return;

        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
    }

    private static int randomColor(RandomSource random) {
        int rgb = (random.nextInt(256) << 16) | (random.nextInt(256) << 8) | random.nextInt(256);
        return 0xFF000000 | rgb;
    }
}