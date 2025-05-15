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

public class ArmorUtil {
    public static void startArmorCheck(ServerPlayerEntity serverPlayerEntity) {
        if (!isWearingFullSCArmorSet(serverPlayerEntity)) {
            if (AccessoriesCapability.getOptionally(serverPlayerEntity).isPresent()) {
                for (SlotEntryReference equipped : AccessoriesCapability.get(serverPlayerEntity).getAllEquipped()) {
                    ItemStack itemStack = equipped.stack();
                    if (itemStack.getItem() instanceof SCAccessoryItem && !itemStack.isIn(SCTags.ALWAYS_WEARABLE.getTag())) {
                        serverPlayerEntity.giveItemStack(itemStack);
                        itemStack.setCount(0);
                        serverPlayerEntity.sendMessage(Text.translatable("text.warning.stoneycore.full_armor_needed").formatted(Formatting.DARK_RED), true);
                    }
                }
            }
        }
    }

    private static boolean isWearingFullSCArmorSet(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (isArmorSlot(slot)) {
                ItemStack armorPiece = entity.getEquippedStack(slot);
                if (!SCArmorDefinitionsLoader.containsItem(armorPiece.getItem())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }
}
