package banduty.stoneycore.event;

import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.items.armor.SCUnderArmorItem;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil.getDefinitionData;

public class ItemTooltipHandler implements ItemTooltipCallback {
    @Override
    public void getTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        Text attackDamage = Text.translatable("attribute.name.generic.attack_damage");
        if (stack.isIn(SCTags.MELEE_COMBAT_MECHANICS.getTag())) lines.removeIf(line -> line.contains(attackDamage));

        if (stack.getItem() instanceof SCTrinketsItem scTrinketsItem
                && scTrinketsItem.hungerDrainAddition() != 0.0d) {
            double hungerDrainAddition = scTrinketsItem.hungerDrainAddition();
            lines.add(Text.translatable("text.tooltip.stoneycore.hungerDrain", ((int) (hungerDrainAddition * 100))).formatted(Formatting.BLUE));
        }

        if (stack.isIn(SCTags.HIDE_NAME_TAG.getTag())) lines.add(Text.translatable("text.tooltip.stoneycore.hideNameTag").formatted(Formatting.BLUE));
        if (stack.isIn(ItemTags.FREEZE_IMMUNE_WEARABLES)) lines.add(Text.translatable("text.tooltip.stoneycore.freezing").formatted(Formatting.BLUE));

        if (stack.isIn(SCTags.RANGED_WEAPON_COMBAT_MECHANICS.getTag())) {
            lines.add(Text.translatable("text.tooltip.stoneycore.baseDamage", (int) getDefinitionData(stack.getItem()).baseDamage()).formatted(Formatting.GREEN));
        }

        if (stack.getItem() instanceof SCUnderArmorItem scUnderArmorItem) {
            double slashingResistance = SCArmorUtil.getResistance(SCArmorUtil.ResistanceType.SLASHING, scUnderArmorItem) * 100;
            double bludgeoningResistance = SCArmorUtil.getResistance(SCArmorUtil.ResistanceType.BLUDGEONING, scUnderArmorItem) * 100;
            double piercingResistance = SCArmorUtil.getResistance(SCArmorUtil.ResistanceType.PIERCING, scUnderArmorItem) * 100;
            if (slashingResistance != 0) lines.add(Text.translatable("text.tooltip.stoneycore.slashingResistance", (int) slashingResistance).formatted(Formatting.BLUE));
            if (bludgeoningResistance != 0) lines.add(Text.translatable("text.tooltip.stoneycore.bludgeoningResistance", (int) bludgeoningResistance).formatted(Formatting.BLUE));
            if (piercingResistance != 0) lines.add(Text.translatable("text.tooltip.stoneycore.piercingResistance", (int) piercingResistance).formatted(Formatting.BLUE));
        }
    }
}
