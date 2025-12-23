package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

public class ItemTooltipHandler implements ItemTooltipCallback {
    @Override
    public void getTooltip(ItemStack stack, TooltipFlag tooltipFlag, List<Component> lines) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Component attackDamage = Component.translatable("attribute.name.generic.attack_damage");
        if (WeaponDefinitionsLoader.containsItem(stack)) lines.removeIf(line -> line.contains(attackDamage));

        for (LandType landType : LandTypeRegistry.getAll()) {
            if (stack.getItem() == landType.coreItem() || (stack.getTag() != null && stack.getTag().contains(BuiltInRegistries.ITEM.getKey(landType.coreItem()).getPath()))) {
                lines.add(Component.translatable("component.tooltip.stoneycore.coreItem").withStyle(ChatFormatting.GOLD));
                break;
            }
        }

        if (stack.is(SCTags.HIDE_NAME_TAG.getTag())) lines.add(Component.translatable("component.tooltip.stoneycore.hideNameTag").withStyle(ChatFormatting.BLUE));
        if (stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) lines.add(Component.translatable("component.tooltip.stoneycore.freezing").withStyle(ChatFormatting.BLUE));

        if (WeaponDefinitionsLoader.isRanged(stack)) {
            double baseDamage = WeaponDefinitionsLoader.getData(stack).ranged().baseDamage();
            lines.add(Component.translatable("component.tooltip.stoneycore.baseDamage", baseDamage).withStyle(ChatFormatting.GREEN));
        }

        if (WeaponDefinitionsLoader.isMelee(stack)) {
            double bonusKnockback = WeaponDefinitionsLoader.getData(stack).melee().bonusKnockback();
            if (bonusKnockback != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.bonusKnockback", bonusKnockback).withStyle(ChatFormatting.AQUA));
            }
            double deflectChance = WeaponDefinitionsLoader.getData(stack).melee().deflectChance();
            if (deflectChance > 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", (int) (deflectChance * 100)).withStyle(ChatFormatting.AQUA));
            }
        }

        if (WeaponDefinitionsLoader.isAmmo(stack)) {
            double deflectChance = WeaponDefinitionsLoader.getData(stack).ammo().deflectChance();
            if (deflectChance != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", (int) (deflectChance * 100)).withStyle(ChatFormatting.AQUA));
            }
        }

        if (stack.getItem() instanceof ArmorItem armorItem && ArmorDefinitionsLoader.containsItem(armorItem)) {
            double slashing = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.SLASHING, armorItem) * 100;
            double bludgeoning = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.BLUDGEONING, armorItem) * 100;
            double piercing = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.PIERCING, armorItem) * 100;

            if (slashing != 0) lines.add(Component.translatable("component.tooltip.stoneycore.slashingResistance", slashing).withStyle(ChatFormatting.BLUE));
            if (bludgeoning != 0) lines.add(Component.translatable("component.tooltip.stoneycore.bludgeoningResistance", bludgeoning).withStyle(ChatFormatting.BLUE));
            if (piercing != 0) lines.add(Component.translatable("component.tooltip.stoneycore.piercingResistance", piercing).withStyle(ChatFormatting.BLUE));
        }

        if (ArmorDefinitionsLoader.containsItem(stack)) {
            double weight = ArmorDefinitionsLoader.getData(stack).weight();
            if (weight != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.weight", weight).withStyle(ChatFormatting.BLUE));
            }
        }

        if (AccessoriesDefinitionsLoader.containsItem(stack)) {
            double weight = AccessoriesDefinitionsLoader.getData(stack).weight();
            if (weight != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.weight", weight).withStyle(ChatFormatting.BLUE));
            }
        }

        boolean shiftPressed = GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        if (!shiftPressed) {
            boolean hasAdditionalInfo = hasAdditionalTooltipInfo(stack, tooltipFlag);
            if (hasAdditionalInfo) {
                lines.add(Component.translatable("component.tooltip.stoneycore.hold_shift_for_info"));
            }
            return;
        }

        addShiftTooltipInfo(stack, tooltipFlag, lines);
    }

    private boolean hasAdditionalTooltipInfo(ItemStack stack, TooltipFlag tooltipFlag) {
        if (ArmorDefinitionsLoader.containsItem(stack)) {
            Map<String, Double> deflectMap = ArmorDefinitionsLoader.getData(stack).deflectChance();
            if (!deflectMap.isEmpty()) {
                double avg = deflectMap.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                if (avg > 0) {
                    return true;
                }
            }
        }

        if (AccessoriesDefinitionsLoader.containsItem(stack)) {
            Map<String, Double> deflectMap = AccessoriesDefinitionsLoader.getData(stack).deflectChance();
            if (!deflectMap.isEmpty()) {
                double avg = deflectMap.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                if (avg > 0) {
                    return true;
                }
            }
        }

        if (stack.getItem() instanceof SCAccessoryItem scAccessoryItem && scAccessoryItem.getModels(stack).visorOpen().isPresent()) {
            return true;
        }

        return false;
    }

    private void addShiftTooltipInfo(ItemStack stack, TooltipFlag conComponent, List<Component> lines) {
        if (ArmorDefinitionsLoader.containsItem(stack)) {
            Map<String, Double> deflectMap = ArmorDefinitionsLoader.getData(stack).deflectChance();
            if (conComponent.isAdvanced()) {
                deflectMap.forEach((key, value) -> {
                    try {
                        ResourceLocation id = new ResourceLocation(key);
                        if (value != 0) {
                            if (BuiltInRegistries.ITEM.containsKey(id)) {
                                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChanceType",
                                        BuiltInRegistries.ITEM.get(id).getDescription(), (int) (value * 100)).withStyle(ChatFormatting.AQUA));
                            } else if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChanceType",
                                        BuiltInRegistries.ENTITY_TYPE.get(id).getDescription(), (int) (value * 100)).withStyle(ChatFormatting.AQUA));
                            }
                        }
                    } catch (Exception e) {
                        StoneyCore.LOGGER.warn("Invalid identifier in armor deflectChance map: {}", key);
                    }
                });
            } else {
                if (!deflectMap.isEmpty()) {
                    double avg = deflectMap.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                    if (avg > 0) {
                        lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", (int) (avg * 100))
                                .withStyle(ChatFormatting.AQUA));
                    }
                }
            }
        }

        if (AccessoriesDefinitionsLoader.containsItem(stack)) {
            Map<String, Double> deflectMap = AccessoriesDefinitionsLoader.getData(stack).deflectChance();
            if (conComponent.isAdvanced()) {
                deflectMap.forEach((key, value) -> {
                    try {
                        ResourceLocation id = new ResourceLocation(key);
                        if (value != 0) {
                            if (BuiltInRegistries.ITEM.containsKey(id)) {
                                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChanceType",
                                        BuiltInRegistries.ITEM.get(id).getDescription(), (int) (value * 100)).withStyle(ChatFormatting.AQUA));
                            } else if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChanceType",
                                        BuiltInRegistries.ENTITY_TYPE.get(id).getDescription(), (int) (value * 100)).withStyle(ChatFormatting.AQUA));
                            }
                        }
                    } catch (Exception e) {
                        StoneyCore.LOGGER.warn("Invalid identifier in accessories deflectChance map: {}", key);
                    }
                });
            } else {
                if (!deflectMap.isEmpty()) {
                    double avg = deflectMap.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                    if (avg > 0) {
                        lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", (int) (avg * 100))
                                .withStyle(ChatFormatting.AQUA));
                    }
                }
            }
        }

        if (stack.getItem() instanceof SCAccessoryItem scAccessoryItem && scAccessoryItem.getModels(stack).visorOpen().isPresent()) {
            if (NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)) lines.add(Component.translatable("component.tooltip.stoneycore.openVisorDeflectChance").withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("component.tooltip.stoneycore.openVisor").withStyle(ChatFormatting.WHITE));
        }
    }
}