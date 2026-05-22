package banduty.stoneycore.items.custom.armor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface SCAccessory {
    default boolean hasOpenVisor(ItemStack stack) {
        return false;
    }

    @NotNull ArmorItem.Type getArmorSlot();

    default boolean canEquip(ItemStack underArmorStack, Player player) {
        return true;
    }

    default @Range(from = 0, to = Integer.MAX_VALUE) int numberSlot() {
        return 0;
    }
}
