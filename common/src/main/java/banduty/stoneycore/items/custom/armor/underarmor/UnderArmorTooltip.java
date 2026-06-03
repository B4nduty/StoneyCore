package banduty.stoneycore.items.custom.armor.underarmor;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;

public record UnderArmorTooltip(UnderArmorContents contents, ArmorItem.Type armorType) implements TooltipComponent {
}