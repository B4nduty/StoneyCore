package banduty.stoneycore.event;

import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemTooltipHandler implements ItemTooltipCallback {
    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag tooltipType, List<Component> lines) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Component attackDamage = Component.translatable("attribute.name.generic.attack_damage");
        if (WeaponDefinitionsStorage.containsItem(stack)) lines.removeIf(line -> line.contains(attackDamage));

        for (LandType landType : LandTypeRegistry.getAll()) {
            ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
            for (ItemStack armorAttachment : SCUnderArmor.getArmorAttachments(itemStack)) {
                if (stack.is(landType.coreItem()) || (armorAttachment.getItem() == landType.coreItem())) {

                    lines.add(Component.translatable("component.tooltip.stoneycore.coreItem").withStyle(ChatFormatting.GOLD));
                    break;
                }
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
            double slashing = SCArmorUtil.getResistance(SCDamageType.SLASHING, armorItem) * 100;
            double bludgeoning = SCArmorUtil.getResistance(SCDamageType.BLUDGEONING, armorItem) * 100;
            double piercing = SCArmorUtil.getResistance(SCDamageType.PIERCING, armorItem) * 100;

            if (slashing != 0) lines.add(Component.translatable("component.tooltip.stoneycore.slashingResistance", String.format("%.1f", slashing)).withStyle(ChatFormatting.BLUE));
            if (bludgeoning != 0) lines.add(Component.translatable("component.tooltip.stoneycore.bludgeoningResistance", String.format("%.1f", bludgeoning)).withStyle(ChatFormatting.BLUE));
            if (piercing != 0) lines.add(Component.translatable("component.tooltip.stoneycore.piercingResistance", String.format("%.1f", piercing)).withStyle(ChatFormatting.BLUE));
        }

        if (ArmorDefinitionsStorage.containsItem(stack)) {
            double weight = ArmorDefinitionsStorage.getData(stack).weight();
            float attackSpeed = 0;
            int rechargeTime = 0;
            for (ItemStack attachment : SCUnderArmor.getArmorAttachments(stack)) {
                if (ArmorAttachmentDefinitionsStorage.containsItem(attachment)) {
                    weight += ArmorAttachmentDefinitionsStorage.getData(attachment).weight();
                    attackSpeed += ArmorAttachmentDefinitionsStorage.getData(attachment).attackSpeed();
                    rechargeTime += ArmorAttachmentDefinitionsStorage.getData(attachment).rechargeTime();
                }
            }
            if (weight != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.weight", weight).withStyle(ChatFormatting.BLUE));
            }
            if (attackSpeed != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.attackSpeed", attackSpeed).withStyle(ChatFormatting.BLUE));
            }
            if (rechargeTime != 0) {
                double recharge = (double) rechargeTime / 20;
                lines.add(Component.translatable("component.tooltip.stoneycore.rechargeTime", recharge).withStyle(ChatFormatting.BLUE));
            }
        }

        if (ArmorAttachmentDefinitionsStorage.containsItem(stack)) {
            double weight = ArmorAttachmentDefinitionsStorage.getData(stack).weight();
            if (weight != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.weight", weight).withStyle(ChatFormatting.BLUE));
            }
            float attackSpeed = ArmorAttachmentDefinitionsStorage.getData(stack).attackSpeed();
            if (attackSpeed != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.attackSpeed", attackSpeed).withStyle(ChatFormatting.BLUE));
            }
            int rechargeTime = ArmorAttachmentDefinitionsStorage.getData(stack).rechargeTime();
            if (rechargeTime != 0) {
                double recharge = (double) rechargeTime / 20;
                lines.add(Component.translatable("component.tooltip.stoneycore.rechargeTime", recharge).withStyle(ChatFormatting.BLUE));
            }
        }

        if (stack.getItem() instanceof ArmorAttachment armorAttachment && armorAttachment.hasOpenVisor(stack)) {
            lines.add(Component.translatable("component.tooltip.stoneycore.openVisor").withStyle(ChatFormatting.WHITE));
            if (!Boolean.TRUE.equals(stack.get(SCDataComponents.VISOR_OPEN.get()))) {
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorDeflectChance").withStyle(ChatFormatting.AQUA));
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorArmor").withStyle(ChatFormatting.AQUA));
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorToughness").withStyle(ChatFormatting.AQUA));
            }
        }
    }
}