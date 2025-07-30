package banduty.stoneycore.util.servertick;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.EnumSet;
import java.util.Set;

public class ArmorUtil {

    private static final Set<EquipmentSlot> ARMOR_SLOTS = EnumSet.of(
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    );

    public static void startArmorCheck(ServerPlayerEntity player) {
        if (isWearingFullSCArmorSet(player)) return;

        AccessoriesCapability.getOptionally(player).ifPresent(accessories -> {
            for (SlotEntryReference equipped : accessories.getAllEquipped()) {
                ItemStack stack = equipped.stack();
                if (stack.getItem() instanceof SCAccessoryItem && !stack.isIn(SCTags.ALWAYS_WEARABLE.getTag())) {
                    player.giveItemStack(stack);
                    stack.setCount(0);
                    player.sendMessage(
                            Text.translatable("text.warning.stoneycore.full_armor_needed").formatted(Formatting.DARK_RED),
                            true
                    );
                }
            }
        });
    }

    private static boolean isWearingFullSCArmorSet(LivingEntity entity) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack equipped = entity.getEquippedStack(slot);
            if (!SCArmorDefinitionsLoader.containsItem(equipped.getItem())) {
                return false;
            }
        }
        return true;
    }
}