package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.event.KeyInputHandler;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class TooltipClientSide {
    public static void setTooltip(List<Text> tooltip, ItemStack itemStack) {
        if (WeaponDefinitionsLoader.isRanged(itemStack) && SCRangeWeaponUtil.getAmmoRequirement(itemStack) != null) {
            tooltip.add(Text.translatable("text.tooltip.stoneycore.need_to_hold",
                    KeyInputHandler.reload.getBoundKeyLocalizedText()));
        }
    }
}
