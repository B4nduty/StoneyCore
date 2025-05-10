package banduty.stoneycore.event;

import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCRangedWeaponDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemTooltipHandler implements ItemTooltipCallback {
    @Override
    public void getTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        Text attackDamage = Text.translatable("attribute.name.generic.attack_damage");
        if (SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem())) lines.removeIf(line -> line.contains(attackDamage));

        if (stack.getItem() instanceof SCTrinketsItem scTrinketsItem
                && scTrinketsItem.hungerDrainAddition() != 0.0d) {
            double hungerDrainAddition = scTrinketsItem.hungerDrainAddition();
            lines.add(Text.translatable("text.tooltip.stoneycore.hungerDrain", ((int) (hungerDrainAddition * 100))).formatted(Formatting.BLUE));
        }

        if (stack.isIn(SCTags.HIDE_NAME_TAG.getTag())) lines.add(Text.translatable("text.tooltip.stoneycore.hideNameTag").formatted(Formatting.BLUE));
        if (stack.isIn(ItemTags.FREEZE_IMMUNE_WEARABLES)) lines.add(Text.translatable("text.tooltip.stoneycore.freezing").formatted(Formatting.BLUE));

        if (SCRangedWeaponDefinitionsLoader.containsItem(stack.getItem())) {
            lines.add(Text.translatable("text.tooltip.stoneycore.baseDamage", (int) SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).baseDamage()).formatted(Formatting.GREEN));
        }

        if (stack.getItem() instanceof ArmorItem armorItem && SCArmorDefinitionsLoader.containsItem(armorItem)) {
            double slashingResistance = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.SLASHING, armorItem) * 100;
            double bludgeoningResistance = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.BLUDGEONING, armorItem) * 100;
            double piercingResistance = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.PIERCING, armorItem) * 100;
            if (slashingResistance != 0) lines.add(Text.translatable("text.tooltip.stoneycore.slashingResistance", (int) slashingResistance).formatted(Formatting.BLUE));
            if (bludgeoningResistance != 0) lines.add(Text.translatable("text.tooltip.stoneycore.bludgeoningResistance", (int) bludgeoningResistance).formatted(Formatting.BLUE));
            if (piercingResistance != 0) lines.add(Text.translatable("text.tooltip.stoneycore.piercingResistance", (int) piercingResistance).formatted(Formatting.BLUE));
        }
    }
}
