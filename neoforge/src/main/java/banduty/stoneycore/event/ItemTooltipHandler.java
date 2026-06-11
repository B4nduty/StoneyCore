package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ItemTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Component attackDamage = Component.translatable("attribute.name.generic.attack_damage");
        if (WeaponDefinitionsStorage.containsItem(stack)) {
            lines.removeIf(line -> line.contains(attackDamage));
        }

        for (LandType landType : LandTypeRegistry.getAll()) {
            ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
            for (ItemStack armorAttachments : SCUnderArmor.getArmorAttachments(itemStack)) {
                if (stack.is(landType.coreItem()) || (armorAttachments.getItem() == landType.coreItem())) {

                    lines.add(Component.translatable("component.tooltip.stoneycore.coreItem").withStyle(ChatFormatting.GOLD));
                    break;
                }
            }
        }

        if (stack.is(SCTags.HIDE_NAME_TAG.getTag())) {
            lines.add(Component.translatable("component.tooltip.stoneycore.hideNameTag").withStyle(ChatFormatting.BLUE));
        }
        if (stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
            lines.add(Component.translatable("component.tooltip.stoneycore.freezing").withStyle(ChatFormatting.BLUE));
        }

        if (WeaponDefinitionsStorage.isRanged(stack)) {
            double baseDamage = WeaponDefinitionsStorage.getData(stack).ranged().baseDamage();
            lines.add(Component.translatable("component.tooltip.stoneycore.baseDamage", baseDamage).withStyle(ChatFormatting.GREEN));
        }

        if (WeaponDefinitionsStorage.isMelee(stack)) {
            double bonusKnockback = WeaponDefinitionsStorage.getData(stack).melee().bonusKnockback();
            if (bonusKnockback != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.bonusKnockback", formatValue(bonusKnockback, true)).withStyle(ChatFormatting.AQUA));
            }
            double deflectChance = WeaponDefinitionsStorage.getData(stack).melee().deflectChance();
            if (deflectChance > 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", formatValue(deflectChance * 100, false)).withStyle(ChatFormatting.AQUA));
            }
        }

        if (WeaponDefinitionsStorage.isAmmo(stack)) {
            double deflectChance = WeaponDefinitionsStorage.getData(stack).ammo().deflectChance();
            if (deflectChance != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", formatValue(deflectChance * 100, false)).withStyle(ChatFormatting.AQUA));
            }
        }

        if (stack.getItem() instanceof ArmorItem armorItem && ArmorDefinitionsStorage.containsItem(armorItem)) {
            double slashing = SCArmorUtil.getResistance(SCDamageType.SLASHING, armorItem) * 100;
            double bludgeoning = SCArmorUtil.getResistance(SCDamageType.BLUDGEONING, armorItem) * 100;
            double piercing = SCArmorUtil.getResistance(SCDamageType.PIERCING, armorItem) * 100;

            if (slashing != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.slashingResistance", formatValue(slashing, false)).withStyle(ChatFormatting.BLUE));
            }
            if (bludgeoning != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.bludgeoningResistance", formatValue(bludgeoning, false)).withStyle(ChatFormatting.BLUE));
            }
            if (piercing != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.piercingResistance", formatValue(piercing, false)).withStyle(ChatFormatting.BLUE));
            }
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
                lines.add(Component.translatable("component.tooltip.stoneycore.weight", formatValue(weight, true)).withStyle(ChatFormatting.BLUE));
            }
            if (attackSpeed != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.attackSpeed", formatValue(attackSpeed, true)).withStyle(ChatFormatting.BLUE));
            }
            if (rechargeTime != 0) {
                double recharge = (double) rechargeTime / 20;
                lines.add(Component.translatable("component.tooltip.stoneycore.rechargeTime", formatValue(recharge, true)).withStyle(ChatFormatting.BLUE));
            }
        }

        if (ArmorAttachmentDefinitionsStorage.containsItem(stack)) {
            var data = ArmorAttachmentDefinitionsStorage.getData(stack);
            double weight = data.weight();
            if (weight != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.weight", formatValue(weight, true)).withStyle(ChatFormatting.BLUE));
            }
            float attackSpeed = data.attackSpeed();
            if (attackSpeed != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.attackSpeed", formatValue(attackSpeed, true)).withStyle(ChatFormatting.BLUE));
            }
            int rechargeTime = data.rechargeTime();
            if (rechargeTime != 0) {
                double recharge = (double) rechargeTime / 20;
                lines.add(Component.translatable("component.tooltip.stoneycore.rechargeTime", formatValue(recharge, true)).withStyle(ChatFormatting.BLUE));
            }
            double armor = data.armor();
            if (armor != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.armor", formatValue(armor, true)).withStyle(ChatFormatting.BLUE));
            }
            double toughness = data.toughness();
            if (toughness != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.toughness", formatValue(toughness, true)).withStyle(ChatFormatting.BLUE));
            }
            double hunger = data.hungerDrainMultiplier();
            if (hunger != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.hunger", formatValue(hunger, true)).withStyle(ChatFormatting.BLUE));
            }
            double deflectChance = data.deflectChance();
            if (deflectChance != 0) {
                lines.add(Component.translatable("component.tooltip.stoneycore.deflectChance", formatValue(deflectChance * 100, false)).withStyle(ChatFormatting.BLUE));
            }
        }

        if (stack.getItem() instanceof ArmorAttachment armorAttachment && armorAttachment.hasOpenVisor(stack)) {
            lines.add(Component.translatable("component.tooltip.stoneycore.openVisor").withStyle(ChatFormatting.WHITE));
            if (!Boolean.TRUE.equals(stack.get(SCDataComponents.VISOR_OPEN))) {
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorDeflectChance").withStyle(ChatFormatting.AQUA));
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorArmor").withStyle(ChatFormatting.AQUA));
                lines.add(Component.translatable("component.tooltip.stoneycore.openVisorToughness").withStyle(ChatFormatting.AQUA));
            }
        }
    }

    private static String formatValue(double value, boolean useDecimal) {
        String formatted = useDecimal ? String.format("%.1f", value) : String.format("%.0f", value);
        if (value > 0) {
            return "+" + formatted;
        }
        return formatted;
    }
}