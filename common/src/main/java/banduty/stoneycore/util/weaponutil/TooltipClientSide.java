package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TooltipClientSide {
    public static void setTooltip(List<Component> tooltip, ItemStack itemStack) {
        if (WeaponDefinitionsStorage.isRanged(itemStack) && SCRangeWeaponUtil.getAmmoRequirement(itemStack) != SCRangeWeaponUtil.AmmoRequirement.EMPTY) {
            tooltip.add(Component.translatable("component.tooltip.stoneycore.need_to_hold", ClientPlatform.getKeyInputHelper().getTranslatedKeyMessage()));
        }
    }
}
