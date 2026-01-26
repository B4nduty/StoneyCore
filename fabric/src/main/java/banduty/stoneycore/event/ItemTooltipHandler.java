package banduty.stoneycore.event;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemTooltipHandler implements ItemTooltipCallback {
    @Override
    public void getTooltip(ItemStack stack, TooltipFlag tooltipFlag, List<Component> lines) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Component attackDamage = Component.translatable("attribute.name.generic.attack_damage");
        if (WeaponDefinitionsStorage.containsItem(stack)) lines.removeIf(line -> line.contains(attackDamage));

        for (LandType landType : LandTypeRegistry.getAll()) {
            if (stack.getItem() == landType.coreItem() || (stack.getTag() != null && stack.getTag().contains(BuiltInRegistries.ITEM.getKey(landType.coreItem()).getPath()))) {
                lines.add(Component.translatable("component.tooltip.stoneycore.coreItem").withStyle(ChatFormatting.GOLD));
                break;
            }
        }

        if (stack.is(SCTags.HIDE_NAME_TAG.getTag())) lines.add(Component.translatable("component.tooltip.stoneycore.hideNameTag").withStyle(ChatFormatting.BLUE));
        if (stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) lines.add(Component.translatable("component.tooltip.stoneycore.freezing").withStyle(ChatFormatting.BLUE));

        if (WeaponDefinitionsStorage.isRanged(stack)) {
            double baseDamage = WeaponDefinitionsStorage.getData(stack).ranged().baseDamage();
            lines.add(Component.translatable("component.tooltip.stoneycore.baseDamage", baseDamage).withStyle(ChatFormatting.GREEN));
        }

        if (WeaponDefinitionsStorage.isMelee(stack)) {
            double bonusKnockback = WeaponDefinitionsStorage.getData(stack).melee().bonusKnockback();
            if (bonusKnockback != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.bonusKnockback", bonusKnockback).withStyle(ChatFormatting.AQUA));
            }
            double deflectChance = WeaponDefinitionsStorage.getData(stack).melee().deflectChance();
            if (deflectChance > 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", (int) (deflectChance * 100)).withStyle(ChatFormatting.AQUA));
            }
        }

        if (WeaponDefinitionsStorage.isAmmo(stack)) {
            double deflectChance = WeaponDefinitionsStorage.getData(stack).ammo().deflectChance();
            if (deflectChance != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", (int) (deflectChance * 100)).withStyle(ChatFormatting.AQUA));
            }
        }

        if (stack.getItem() instanceof ArmorItem armorItem && ArmorDefinitionsStorage.containsItem(armorItem)) {
            double slashing = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.SLASHING, armorItem) * 100;
            double bludgeoning = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.BLUDGEONING, armorItem) * 100;
            double piercing = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.PIERCING, armorItem) * 100;

            if (slashing != 0) lines.add(Component.translatable("component.tooltip.stoneycore.slashingResistance", slashing).withStyle(ChatFormatting.BLUE));
            if (bludgeoning != 0) lines.add(Component.translatable("component.tooltip.stoneycore.bludgeoningResistance", bludgeoning).withStyle(ChatFormatting.BLUE));
            if (piercing != 0) lines.add(Component.translatable("component.tooltip.stoneycore.piercingResistance", piercing).withStyle(ChatFormatting.BLUE));
        }

        if (ArmorDefinitionsStorage.containsItem(stack)) {
            double weight = ArmorDefinitionsStorage.getData(stack).weight();
            if (weight != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.weight", weight).withStyle(ChatFormatting.BLUE));
            }
        }

        if (AccessoriesDefinitionsStorage.containsItem(stack)) {
            double weight = AccessoriesDefinitionsStorage.getData(stack).weight();
            if (weight != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.weight", weight).withStyle(ChatFormatting.BLUE));
            }
        }

        if (stack.getItem() instanceof SCAccessoryItem scAccessoryItem && scAccessoryItem.getModels(stack).visorOpen().isPresent()) {
            lines.add(Component.translatable("component.tooltip.stoneycore.openVisor").withStyle(ChatFormatting.WHITE));
            if (!NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)) {
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorDeflectChance").withStyle(ChatFormatting.AQUA));
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorArmor").withStyle(ChatFormatting.AQUA));
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorToughness").withStyle(ChatFormatting.AQUA));
            }
        }
    }
}